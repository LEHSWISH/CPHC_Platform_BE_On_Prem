package org.wishfoundation.abhaservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentsPathRequest {
    private String fileName;
    private String filePath;
    private String fileBase64;
}
