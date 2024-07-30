package org.wishfoundation.healthservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.healthservice.request.DocumentsPathRequest;
import org.wishfoundation.healthservice.request.MedicalsReportsRequest;
import org.wishfoundation.healthservice.response.DocumentsPathResponse;
import org.wishfoundation.healthservice.response.MedicalsReportsResponse;

import java.util.List;

/**
 * This interface defines the contract for the Medical Certificate Service.
 * It provides methods for managing medical certificates and related documents.
 */
public interface MedicalCertificateService {

    /**
     * Saves a medical template.
     *
     * @param medicalsReportsRequest The request containing the medical template data.
     */
    void saveMedicalTemplate(MedicalsReportsRequest medicalsReportsRequest);

    /**
     * Retrieves the medical report.
     *
     * @return The medical report response.
     */
    MedicalsReportsResponse getMedicalReport();

    /**
     * Generates presigned URLs for uploading medical documents.
     *
     * @param documentsPathRequestList The list of documents for which URLs need to be generated.
     * @return The list of generated URLs.
     */
    List<DocumentsPathResponse> generateUploadFilePresignedUrl(List<DocumentsPathRequest> documentsPathRequestList);

    /**
     * Generates presigned URLs for downloading medical documents.
     *
     * @param request The list of documents for which URLs need to be generated.
     * @return The list of generated URLs.
     */
    List<DocumentsPathResponse> generateGetFilePresignedUrl(List<DocumentsPathRequest> request);

    /**
     * Deletes medical documents.
     *
     * @param documentsPathRequestList The list of documents to be deleted.
     * @return A ResponseEntity indicating success or failure.
     */
    ResponseEntity<Void> deleteMedicalDocument(List<DocumentsPathRequest> documentsPathRequestList);

    /**
     * Retrieves medical certificates from a virtual machine.
     *
     * @param request The list of medical certificates to be retrieved.
     * @return The list of retrieved medical certificates.
     */
    List<DocumentsPathResponse> getMedicalCertificateFromVM(List<DocumentsPathRequest> request);

    /**
     * Uploads medical certificates to a virtual machine.
     *
     * @param request The list of medical certificates to be uploaded.
     * @return The list of uploaded medical certificates.
     */
    List<DocumentsPathResponse> uploadMedicalCertificateFromVM(List<DocumentsPathRequest> request);
}
