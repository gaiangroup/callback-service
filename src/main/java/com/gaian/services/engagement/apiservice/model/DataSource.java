package com.gaian.services.engagement.apiservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataSource {
	
	private String uri;
    private String fileName;
    private String renameAs;
    private String destinationPath;
    private String mimeType;

}
