package com.gaian.services.engagement.callbackservice.service;

import com.gaian.services.engagement.model.InMemoryFile;
import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.db.EngagementInstance;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.FileSystemResource;

public interface PackageManager {
	
	/**
     * Creates a package for callback request
     *
     * @param packageName Name of the CALLBACK package
     * @param files In memory files to be added in the package
     * @param urls URLs of the files to be downloaded and added in the package
     * @param transactionId ID of the transaction
     * @return Storage path of the package
     */
    String buildPackage(
        String packageName, Set<InMemoryFile> files, Map<String, String> urls, String transactionId);

    /**
     * Creates packageInfo.json file for the callback request
     *
     * @param engagement Engagement model
     * @param instance Engagement log
     * @return In memory file containing the CALLBACK packageInfo
     */
    InMemoryFile buildPackageInfo(EngagementDBModel engagement, EngagementInstance instance);

    /**
     * Get the package that was delivered in the past
     *
     * @param transactionId ID of the transaction which involves the package delivery
     * @return callback package
     */
    FileSystemResource getPackage(String transactionId);

}
