package org.wishfoundation.healthservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentsPathResponse {
	private String fileName;
	private String filePath;
	private String presignedUrl;
	private String fileBase64;
}
