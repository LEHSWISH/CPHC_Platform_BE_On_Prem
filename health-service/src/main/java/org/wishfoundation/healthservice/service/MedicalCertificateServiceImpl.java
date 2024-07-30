package org.wishfoundation.healthservice.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.chardhamcore.config.UserContext;
import org.wishfoundation.healthservice.exception.WishFoundationException;
import org.wishfoundation.healthservice.request.DocumentsPathRequest;
import org.wishfoundation.healthservice.request.MedicalsReportsRequest;
import org.wishfoundation.healthservice.request.YatriPulseUserRequest;
import org.wishfoundation.healthservice.response.DocumentsPathResponse;
import org.wishfoundation.healthservice.response.MedicalsReportsResponse;
import org.wishfoundation.healthservice.utils.EnvironmentConfig;
import org.wishfoundation.healthservice.utils.Helper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class MedicalCertificateServiceImpl implements MedicalCertificateService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalCertificateServiceImpl.class);

    private WebClient.Builder webClient;

    private final EnvironmentConfig env;

    private final YatriServiceUtils yatriServiceUtils;

    public static final List<String> extensions = Arrays.asList("png","jpeg","jpg","pdf");

    /**
     * Save medical template to YatriServiceUtils.
     *
     * @param medicalsReportsRequest The medical report request.
     */
    @Override
    public void saveMedicalTemplate(MedicalsReportsRequest medicalsReportsRequest) {
        YatriPulseUserRequest request = YatriPulseUserRequest.builder().userName(UserContext.getCurrentUserName()).phoneNumber(UserContext.getCurrentPhoneNumber()).medicalsReports(medicalsReportsRequest).build();
        yatriServiceUtils.updateYatriDetails(request);
    }

    /**
     * Get medical report from YatriServiceUtils.
     *
     * @return The medical report response.
     */
    @Override
    public MedicalsReportsResponse getMedicalReport() {
        return yatriServiceUtils.fetchMedicalRecords();
    }

    /**
 * Generate presigned URLs for uploading medical documents to S3.
 *
 * @param request The list of documents path requests. Each request contains the file name and base64 encoded file data.
 * @return The list of documents path responses. Each response contains the file path, presigned URL, and file name.
 * @throws WishFoundationException If the file extension is not supported or the file size exceeds the limit.
 */
    @Override
    public List<DocumentsPathResponse> generateUploadFilePresignedUrl(List<DocumentsPathRequest> request) {

        String fileKey = "health/medical-certificate/username/" + UserContext.getCurrentUserName() + "/";
        String bucketName = Helper.getConfigBucket(Region.AP_SOUTH_1.toString());
        List<DocumentsPathResponse> documentsPathResponseList = new ArrayList<>();
        List<DocumentsPathRequest> documentsPathRequestList = new ArrayList<>();

        // Iterate over each request
        request.stream().forEach(documentsPathRequest ->
                {
                    try (S3Presigner presigner = S3Presigner.builder().region(Region.AP_SOUTH_1)
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(env.getAccessKey(), env.getSecretKey())))
                            .build()) {

                        String key = fileKey + documentsPathRequest.getFileName();

                        // Validate file extension and size
                        if(key.contains(".")) {
                            String extension = key.substring(key.lastIndexOf(".") + 1, key.length());
                            System.out.println(extension);

                            if(!extensions.contains(extension.toLowerCase()) || documentsPathRequest.getFileBase64().getBytes().length > 15728640){
                                throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "Invaild file uploaded",HttpStatus.BAD_REQUEST);
                            }
                        }else {
                            throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "Invaild file uploaded",HttpStatus.BAD_REQUEST);
                        }

                        // Generate presigned URL for uploading the file
                        PutObjectRequest objectRequest = PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build();

                        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                                .putObjectRequest(objectRequest)
                                .build();
                        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
                        String myURL = presignedRequest.url().toString();
                        logger.info("Presigned URL to upload a file to: [{}]", myURL);
                        logger.info("HTTP method: [{}]", presignedRequest.httpRequest().method());
                        // Add the response to the list
                        DocumentsPathResponse response = DocumentsPathResponse.builder().filePath(key)
                                .presignedUrl(presignedRequest.url().toString())
                                .fileName(documentsPathRequest.getFileName()).build();

                        documentsPathResponseList.add(response);
                        documentsPathRequestList.add(documentsPathRequest.toBuilder().filePath(key).build());
                    }
                }
        );

        return documentsPathResponseList;
    }

        /**
     * Generate presigned URLs for downloading medical documents from S3.
     *
     * @param request The list of documents path requests. Each request contains the file name.
     * @return The list of documents path responses. Each response contains the file path, presigned URL, and file name.
     * @throws Exception If there is an error while generating presigned URL.
     */
    @Override
    public List<DocumentsPathResponse> generateGetFilePresignedUrl(List<DocumentsPathRequest> request) {
        String fileKey = "health/medical-certificate/username/" + UserContext.getCurrentUserName() + "/";
        String bucketName = Helper.getConfigBucket(Region.AP_SOUTH_1.toString());
        List<DocumentsPathResponse> documentsPathResponseList = new ArrayList<>();

        // Iterate over each request
        request.stream().forEach(documentsPathRequest ->
                {
                    String key = fileKey + documentsPathRequest.getFileName();

                    try (S3Presigner presigner = S3Presigner.builder().region(Region.AP_SOUTH_1)
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(env.getAccessKey(), env.getSecretKey())))
                            .build()) {

                        // Create GetObjectRequest
                        GetObjectRequest objectRequest = GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build();

                        // Generate presigned URL for downloading the file
                        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
                                .getObjectRequest(objectRequest)
                                .build();

                        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

                        // Log the presigned URL and HTTP method
                        logger.info("Presigned URL: [{}]", presignedRequest.url().toString());
                        logger.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

                        // Add the response to the list
                        DocumentsPathResponse response = DocumentsPathResponse.builder().filePath(key)
                                .presignedUrl(presignedRequest.url().toString())
                                .fileName(documentsPathRequest.getFileName()).build();

                        documentsPathResponseList.add(response);
                    } catch (Exception e) {
                        // Handle the exception
                        logger.error("Error while generating presigned URL for file: " + key, e);
                        throw e;
                    }
                }
        );

        return documentsPathResponseList;
    }

        /**
     * Delete medical documents from S3 and YatriServiceUtils.
     *
     * @param documentsPathRequestList The list of documents path requests. Each request contains the file path to be deleted.
     * @return The HTTP response entity with status OK. If the deletion is successful, the HTTP status code will be 200 (OK).
     * @throws Exception If there is an error while deleting the documents from S3 or updating the YatriServiceUtils.
     */
    @Override
    public ResponseEntity<Void> deleteMedicalDocument(List<DocumentsPathRequest> documentsPathRequestList) {

        try (S3Client s3Client = S3Client.builder().region(Region.AP_SOUTH_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(env.getAccessKey(), env.getSecretKey())))
                .build()) {

            // Extract file paths from the request
            List<String> filePaths = documentsPathRequestList.stream()
                    .map(DocumentsPathRequest::getFilePath)
                    .collect(Collectors.toList());

            // Prepare a list of ObjectIdentifier for S3 delete operation
            ArrayList<ObjectIdentifier> keys = new ArrayList<>();

            filePaths.forEach(filePath -> {
                ObjectIdentifier objectIdentifier = ObjectIdentifier.builder().key(filePath).build();
                keys.add(objectIdentifier);
            });

            // Create DeleteObjectsRequest for S3 delete operation
            DeleteObjectsRequest dor = DeleteObjectsRequest.builder()
                    .bucket(Helper.getConfigBucket(Region.AP_SOUTH_1.toString()))
                    .delete(Delete.builder()
                            .objects(keys).build())
                    .build();

            // Perform S3 delete operation
            s3Client.deleteObjects(dor);
        }

        // Update YatriServiceUtils after successful S3 deletion
        yatriServiceUtils.deleteMedicalDocument(documentsPathRequestList);

        // Return HTTP response entity with status OK
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

        /**
     * This method is used to upload medical certificates from VM to local directory.
     *
     * @param request The list of documents path requests. Each request contains the file name and base64 encoded file data.
     * @return The list of documents path responses. Each response contains the file path and file name.
     * @throws WishFoundationException If the file extension is not supported or the file size exceeds the limit.
     */
    public List<DocumentsPathResponse> uploadMedicalCertificateFromVM(List<DocumentsPathRequest> request) {
        // Define the directory path where the files will be stored
        String fileKey = "health-service"+ File.separator +"uploads" + File.separator + "health" + File.separator + "medical-certificate" + File.separator + "username" + File.separator + UserContext.getCurrentUserName() + File.separator;

        // Initialize an empty list to store the responses
        List<DocumentsPathResponse> documentsPathResponseList = new ArrayList<>();
        List<DocumentsPathRequest> documentsPathRequestList = new ArrayList<>();
        // Iterate over each request
        request.stream().forEach(documentsPathRequest ->
                {
                    try {
                        // Construct the file path
                        String key = fileKey + documentsPathRequest.getFileName();
                        // Validate file extension and size
                        if (key.contains(".")) {
                            String extension = key.substring(key.lastIndexOf(".") + 1, key.length());
                            System.out.println(extension);

                            if (!extensions.contains(extension.toLowerCase()) || documentsPathRequest.getFileBase64().getBytes().length > 15728640) {
                                throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "Invaild file uploaded", HttpStatus.BAD_REQUEST);
                            }
                        } else {
                            throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "Invaild file uploaded", HttpStatus.BAD_REQUEST);
                        }

                        // Get the absolute path of the current directory
                        String absolutePath = Helper.currentDirectory();

                        // Save the file to the local directory
                        Helper.fileCreate(absolutePath,key,documentsPathRequest.getFileBase64());
                        // Log the successful upload
                        logger.info("File is upload", key);
                        // Add the response to the list
                        DocumentsPathResponse response = DocumentsPathResponse.builder().filePath(key)
                                .fileName(documentsPathRequest.getFileName()).build();

                        documentsPathResponseList.add(response);
                        documentsPathRequestList.add(documentsPathRequest.toBuilder().filePath(key).build());
                    } catch (Exception e) {
                        // Log the exception and re-throw it as a WishFoundationException
                        e.printStackTrace();
                        throw new WishFoundationException(e.getMessage());
                    }
                }
        );

        // Return the list of responses
        return documentsPathResponseList;
    }
        /**
     * This method is used to get medical certificates from local directory.
     *
     * @param request The list of documents path requests. Each request contains the file name.
     * @return The list of documents path responses. Each response contains the file path, file name, and file content in base64 format.
     * @throws WishFoundationException If there is an error while reading the file content.
     */
    @Override
    public List<DocumentsPathResponse> getMedicalCertificateFromVM(List<DocumentsPathRequest> request) {
        // Define the directory path where the files are stored
        String fileKey = "health-service"+ File.separator +"uploads" + File.separator + "health" + File.separator + "medical-certificate" + File.separator + "username" + File.separator + UserContext.getCurrentUserName() + File.separator;

        // Initialize an empty list to store the responses
        List<DocumentsPathResponse> documentsPathResponseList = new ArrayList<>();

        // Iterate over each request
        request.stream().forEach(documentsPathRequest ->
                {
                    // Construct the file path
                    String key = fileKey + documentsPathRequest.getFileName();

                    try{
                        // Get the absolute path of the current directory
                        String absolutePath = Helper.currentDirectory();

                        // Read the file content in base64 format
                        String fileBase64 = Helper.getFileContent(absolutePath,key);

                        // Create a response object and add it to the list
                        DocumentsPathResponse response = DocumentsPathResponse.builder().filePath(key)
                                .fileBase64(fileBase64)
                                .fileName(documentsPathRequest.getFileName()).build();

                        documentsPathResponseList.add(response);
                    } catch (WishFoundationException e) {
                        // Re-throw the exception with a more descriptive message
                        throw new WishFoundationException(e.getMessage());
                    }
                }
        );
        // Return the list of responses
        return documentsPathResponseList;
    }
}
