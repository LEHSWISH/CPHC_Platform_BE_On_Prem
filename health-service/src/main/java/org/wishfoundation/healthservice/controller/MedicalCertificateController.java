package org.wishfoundation.healthservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.healthservice.request.DocumentsPathRequest;
import org.wishfoundation.healthservice.request.MedicalsReportsRequest;
import org.wishfoundation.healthservice.response.DocumentsPathResponse;
import org.wishfoundation.healthservice.response.MedicalsReportsResponse;
import org.wishfoundation.healthservice.service.MedicalCertificateServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for handling medical certificate related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/medical")
public class MedicalCertificateController {

    /**
     * Service for medical certificate operations.
     */
    private final MedicalCertificateServiceImpl medicalCertificateService;

    /**
     * Saves a medical report.
     *
     * @param medicalsReportsRequest The medical report data to be saved.
     */
    @PostMapping("/save-medical-report")
    public void saveMedicalTemplate(@RequestBody MedicalsReportsRequest medicalsReportsRequest){
            medicalCertificateService.saveMedicalTemplate(medicalsReportsRequest);
    }

    /**
     * Fetches a medical report.
     *
     * @return The medical report data.
     */
    @GetMapping("/fetch-medical-report")
    public MedicalsReportsResponse getMedicalReport(){
        return medicalCertificateService.getMedicalReport();
    }

    /**
     * Generates presigned URLs for uploading medical documents.
     *
     * @param documentsPathRequestList The list of documents for which URLs need to be generated.
     * @return The list of generated URLs.
     */
    @PostMapping("/generate-upload-file-presigned-url")
    public List<DocumentsPathResponse> generateUploadFilePresignedUrl(@RequestBody  List<DocumentsPathRequest> documentsPathRequestList){
        return medicalCertificateService.generateUploadFilePresignedUrl(documentsPathRequestList);
    }

    /**
     * Generates presigned URLs for downloading medical documents.
     *
     * @param documentsPathRequestList The list of documents for which URLs need to be generated.
     * @return The list of generated URLs.
     */
    @PostMapping("/generate-get-file-presigned-url")
    public List<DocumentsPathResponse> generateGetFilePresignedUrl(@RequestBody  List<DocumentsPathRequest> documentsPathRequestList){
        return medicalCertificateService.generateGetFilePresignedUrl(documentsPathRequestList);
    }

    /**
     * Deletes medical documents.
     *
     * @param documentsPathRequestList The list of documents to be deleted.
     * @return HTTP status 200 (OK) if the documents are deleted successfully.
     */
    @PostMapping("/delete-medical-document")
    public ResponseEntity<Void> deleteMedicalDocument(@RequestBody  List<DocumentsPathRequest> documentsPathRequestList){
        return medicalCertificateService.deleteMedicalDocument(documentsPathRequestList);
    }

    /**
     * Uploads medical certificates from a virtual machine.
     *
     * @param documentsPathRequestList The list of documents to be uploaded.
     * @return The list of uploaded documents with their paths.
     */
    @PostMapping("/upload-medical-certificate-from-vm")
    public List<DocumentsPathResponse> uploadMedicalCertificateFromVM(@RequestBody List<DocumentsPathRequest> documentsPathRequestList){
        return medicalCertificateService.uploadMedicalCertificateFromVM(documentsPathRequestList);
    }

    /**
     * Retrieves medical certificates from a virtual machine.
     *
     * @param documentsPathRequestList The list of documents to be retrieved.
     * @return The list of retrieved documents with their paths.
     */
    @PostMapping("/get-medical-certificate-from-vm")
    public List<DocumentsPathResponse> getMedicalCertificateFromVM(@RequestBody List<DocumentsPathRequest> documentsPathRequestList){
        return medicalCertificateService.getMedicalCertificateFromVM(documentsPathRequestList);
    }
    //TODO : get file for FHIRService
    /**
     * Retrieves a medical certificate for a FHIR service.
     *
     * @param documentsPathRequest The document for which the certificate needs to be retrieved.
     * @return The medical certificate as a base64 encoded string.
     */
    @PostMapping("/get-medical-certificate-for-fhir")
    public ResponseEntity<String> getMedicalCertificate(@RequestBody DocumentsPathRequest documentsPathRequest){
        List<DocumentsPathRequest> documentsPathRequestList = new ArrayList<>();
        documentsPathRequestList.add(documentsPathRequest);
        DocumentsPathResponse documentsPathResponseList = medicalCertificateService.getMedicalCertificateFromVM(documentsPathRequestList).get(0);
        return ResponseEntity.ok(documentsPathResponseList.getFileBase64());
    }
}
