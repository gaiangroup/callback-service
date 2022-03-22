package com.gaian.services.engagement.callbackservice.service;

import static com.gaian.services.engagement.callback.error.CallbackErrorTemplates.ERROR_BUILDING_PACKAGE;
import static com.gaian.services.engagement.callback.error.CallbackErrorTemplates.ERROR_DIRECTORY_CREATION;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Paths.get;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.util.FileCopyUtils.copyToByteArray;

//import io.micrometer.core.annotation.Timed;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.gaian.services.engagement.callbackservice.error.exception.PackageBuildException;
import com.gaian.services.engagement.content.processor.ContentProcessor;
import com.gaian.services.engagement.model.InMemoryFile;
import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.db.EngagementInstance;
import com.gaian.services.engagement.model.request.channel.MEF;
import com.gaian.services.engagement.model.request.channel.MEFType;
import com.gaian.services.engagement.model.request.content.ContentType;
import com.gaian.services.engagement.model.request.content.TextModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PackageManagerImpl implements PackageManager {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Qualifier("content_processors")
	private Map<ContentType, ContentProcessor> contentProcessors;

	@Value("${sharedFiles.location}")
	private String sharedFilesLocation;

	/**
	 * Creates a package for MEF request
	 *
	 * @param packageName   Name of the MEF package
	 * @param files         In memory files to be added in the package
	 * @param urls          URLs of the files to be downloaded and added in the
	 *                      package
	 * @param transactionId ID of the transaction
	 * @return Storage path of the package
	 */
	@Trace
	// @Timed
	@Override
	public String buildPackage(String packageName, Set<InMemoryFile> files, Map<String, String> urls,
			String transactionId) {

		Path sharedFilesDirectory = get(sharedFilesLocation);
		Path packageDirectory = sharedFilesDirectory.resolve(transactionId);

		String absoluteFilePath = sharedFilesDirectory.resolve(get(transactionId, packageName)).toString();

		try {
			// Creating a directory named as the transaction ID
			createDirectory(packageDirectory);
		} catch (IOException ioException) {
			throw new PackageBuildException(format(ERROR_DIRECTORY_CREATION, transactionId, sharedFilesDirectory),
					ioException);
		}

		log.info("Creating package {} for transaction {}", absoluteFilePath, transactionId);

		try (FileOutputStream fileStream = new FileOutputStream(absoluteFilePath);
				ZipOutputStream compressedFile = new ZipOutputStream(fileStream)) {

			addUrlsToPackage(packageName, urls, compressedFile);
			addFilesToPackage(packageName, files, compressedFile);

		} catch (Exception exception) {
			throw new PackageBuildException(format(ERROR_BUILDING_PACKAGE, packageName, transactionId), exception);
		}

		return absoluteFilePath;
	}

	/**
	 * Creates packageInfo.json file for the MEF request
	 *
	 * @param engagement Engagement model
	 * @param instance   Engagement log
	 * @return In memory file containing the MEF packageInfo
	 */
	@Trace
	// @Timed
	@Override
	public InMemoryFile buildPackageInfo(EngagementDBModel engagement, EngagementInstance instance) {

		MEF mef = (MEF) engagement.getChannel();
		MEFType mefType = mef.getMefType();

		List<Object> resolvedParameters = new ArrayList<>();

		if (isNotEmpty(mef.getParameters())) {
			for (TextModel parameter : mef.getParameters()) {

				Object resolvedParameter = contentProcessors
						.get(parameter.getContentType()).getContentResponse(engagement.getTenantId(), parameter,
								instance.getEvent(), instance.getSourceData(), instance.getDestData(), null, engagement)
						.getResponse();

				resolvedParameters.add(resolvedParameter);
			}
		}

		log.info("Resolved parameters for mef {} under transaction {} are {}", engagement.getId(),
				instance.getTransactionId(), resolvedParameters);

		JSONObject packageInfo = new JSONObject();
		packageInfo.put("action", mefType.getAction());
		packageInfo.put("parameters", resolvedParameters);
		packageInfo.put("engagementId", engagement.getId());
		packageInfo.put("cmd", mef.getCommand());
		packageInfo.put("jsonRpcType", mefType.getRpcType());
		packageInfo.put("execLocation",
				ofNullable(mef.getExecutionDirectory()).orElse(mefType.getDefaultExecutionDirectory()));

		if (isNotEmpty(mef.getRpcFilename())) {
			packageInfo.put("jsonRpcFile", mef.getRpcFilename());
		}

		log.info("Generated package info {} for MEF {}", packageInfo, engagement.getId());

		return new InMemoryFile("packageInfo.json", packageInfo.toString().getBytes());
	}

	/**
	 * Get the package that was delivered in the past
	 *
	 * @param transactionId ID of the transaction which involves the package
	 *                      delivery
	 * @return MEF package
	 */
	public FileSystemResource getPackage(String transactionId) {

		log.info("Looking for package delivered under transaction {}", transactionId);

		File directory = get(sharedFilesLocation, transactionId).toFile();
		if (directory.exists()) {
			File[] packages = directory.listFiles();
			if (packages.length > 0) {
				return new FileSystemResource(packages[0]);
			}
		}

		log.error("No package found under transaction {}", transactionId);
		return null;
	}

	/**
	 * Adds solid files to the package
	 *
	 * @param packageName    Name of the package
	 * @param files          Files to be added
	 * @param compressedFile Output stream of the package
	 * @throws IOException
	 */
	private void addFilesToPackage(String packageName, Set<InMemoryFile> files, ZipOutputStream compressedFile)
			throws IOException {

		for (InMemoryFile file : files) {

			String filename = file.getFilename();

			log.info("Adding file {} to package {}", filename, packageName);

			ZipEntry zipEntry = new ZipEntry(filename);
			compressedFile.putNextEntry(zipEntry);

			InputStream inputStream = file.getInputStream();
			byte[] bytes = new byte[1024];
			int length;
			while ((length = inputStream.read(bytes)) >= 0) {
				compressedFile.write(bytes, 0, length);
			}
		}
	}

	/**
	 * Downloads the file from the URL and adds them to the package
	 *
	 * @param packageName    Name of the package
	 * @param urls           URLs of the file
	 * @param compressedFile Output stream of the package
	 */
	private void addUrlsToPackage(String packageName, Map<String, String> urls, ZipOutputStream compressedFile) {

		for (Entry<String, String> entry : urls.entrySet()) {

			String name = entry.getValue();
			String url = entry.getKey();

			log.info("Trying to download file '{}' from '{}' ", name, url);

			// Downloading files from the url and inserting it in the package
			restTemplate.execute(url, GET, null, downloadedFile -> {

				byte[] downloadedFileContent = copyToByteArray(downloadedFile.getBody());

				log.info("Adding data of file '{}' of {} bytes from '{}' to package {}", name,
						downloadedFileContent.length, url, packageName);

				ZipEntry zipEntry = new ZipEntry(name);
				compressedFile.putNextEntry(zipEntry);
				compressedFile.write(downloadedFileContent);
				compressedFile.closeEntry();

				return null;
			});
		}
	}

}
