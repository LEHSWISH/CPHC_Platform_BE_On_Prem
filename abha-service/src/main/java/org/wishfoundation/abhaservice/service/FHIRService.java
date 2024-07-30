package org.wishfoundation.abhaservice.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wishfoundation.abhaservice.aws.s3.S3Wrapper;
import org.wishfoundation.abhaservice.decryption.DecryptionController;
import org.wishfoundation.abhaservice.encryption.EncryptionController;
import org.wishfoundation.abhaservice.entity.CareContext;
import org.wishfoundation.abhaservice.keypairgen.KeyMaterial;
import org.wishfoundation.abhaservice.keypairgen.KeyPairGenController;
import org.wishfoundation.abhaservice.request.DocumentsPath;
import org.wishfoundation.abhaservice.request.DocumentsPathRequest;
import org.wishfoundation.abhaservice.request.decryption.DecryptionRequest;
import org.wishfoundation.abhaservice.request.encryption.EncryptionRequest;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;
import org.wishfoundation.abhaservice.response.abha.ABHAUserDetails;
import org.wishfoundation.abhaservice.response.decryption.DecryptionResponse;
import org.wishfoundation.abhaservice.response.encryption.EncryptionResponse;
import org.wishfoundation.abhaservice.utils.Helper;
import org.wishfoundation.abhaservice.utils.UserServiceUtils;
import org.wishfoundation.chardhamcore.config.UserContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.wishfoundation.abhaservice.fhir.DiagnosticRecord.populateDiagnosticReportLabBundle;
import static org.wishfoundation.abhaservice.fhir.DischargeSummaryRecord.populateDischargeSummaryBundle;
import static org.wishfoundation.abhaservice.fhir.HealthDocumentRecord.populateHealthDocumentRecordBundle;
import static org.wishfoundation.abhaservice.fhir.ImmunizationRecord.populateImmunizationRecordBundle;
import static org.wishfoundation.abhaservice.fhir.OpConsultRecord.populateOPConsultNoteBundle;
import static org.wishfoundation.abhaservice.fhir.PrescriptionRecord.populatePrescriptionBundle;
import static org.wishfoundation.abhaservice.fhir.WellnessRecord.populateWellnessRecordBundle;

/**
 * This class is responsible for handling FHIR (Fast Healthcare Interoperability Resources) related operations.
 * It includes methods for creating FHIR bundles, saving them in DB, encrypting and decrypting them.
 */
@Service
@RequiredArgsConstructor
public class FHIRService {

    /**
     * Static instance of FhirContext for R4 version.
     */
    static FhirContext ctx = FhirContext.forR4();

    /**
     * S3Wrapper instance for interacting with AWS S3.
     */
    private final S3Wrapper s3Wrapper;

    /**
     * KeyPairGenController instance for generating key pairs.
     */
    private final KeyPairGenController keyPairGenController;

    /**
     * EncryptionController instance for encrypting FHIR bundles.
     */
    private final EncryptionController encryptionController;

    /**
     * DecryptionController instance for decrypting FHIR bundles.
     */
    private final DecryptionController decryptionController;

    /**
     * UserServiceUtils instance for utility methods related to user service.
     */
    private final UserServiceUtils userServiceUtils;

    /**
     * This method creates FHIR bundles from the given list of DocumentsPath and saves them in S3.
     *
     * @param documentsPathEntity List of DocumentsPath containing file paths and other details.
     * @param abhaUserDetails ABHAUserDetails containing user information.
     * @return List of paths where the FHIR bundles are saved in S3.
     */
    public List<String> createFHIRBundle(List<DocumentsPath> documentsPathEntity, ABHAUserDetails abhaUserDetails){
        List<String> fhirPathList = new ArrayList<>();
        for(DocumentsPath documentsPath : documentsPathEntity) {
            File file = null;
            List<CareContext> careContexts = new ArrayList<>();
            try {
                byte[] fileBytes = s3Wrapper.getObjectAsBytes(documentsPath.getFilePath());
                String base64 = Helper.encodeBytesToBase64(fileBytes);
                FHIRAdditionalDetails fhirAdditionalDetails = FHIRAdditionalDetails.builder().fileName(documentsPath.getFileName()).base64Content(base64)
                        .contentType(getContentType(documentsPath.getFileName())).patientName(abhaUserDetails.getFullName())
                        .organizationName(documentsPath.getHospitalLabName()).base64Content(base64)
                        .heathInformationType(documentsPath.getMedicalDocumentType()).phoneNumber(abhaUserDetails.getPhoneNumber())
                        .gender(abhaUserDetails.getGender()).dateOfBirth(abhaUserDetails.getDateOfBirth()).build();

                Bundle bundle = getFHIRBundle(fhirAdditionalDetails);
                file = getPrettyJson(bundle);
                String fhirPath = "health/fhir-bundle/username/" + UserContext.getCurrentUserName() + "/"+fhirAdditionalDetails.getHeathInformationType()+"/"+fhirAdditionalDetails.getFileName()+".json";
                s3Wrapper.putObject(fhirPath, file);
                fhirPathList.add(fhirPath);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                file.delete();
            }
        }
        return fhirPathList;
    }

    /**
     * This method creates FHIR bundles from the given list of DocumentsPath and saves them in a local directory.
     *
     * @param documentsPathEntity List of DocumentsPath containing file paths and other details.
     * @param abhaUserDetails ABHAUserDetails containing user information.
     * @return List of paths where the FHIR bundles are saved in the local directory.
     */
    public List<String> createFHIRBundleVM(List<DocumentsPath> documentsPathEntity, ABHAUserDetails abhaUserDetails){
        List<String> fhirPathList = new ArrayList<>();
        for(DocumentsPath documentsPath : documentsPathEntity) {
            List<CareContext> careContexts = new ArrayList<>();
            try {
                DocumentsPathRequest request = new DocumentsPathRequest();
                request.setFilePath(documentsPath.getFilePath());
                request.setFileName(documentsPath.getFileName());
                String base64 = userServiceUtils.getMedicalCertificateBase64(request);
                String absolutePath = Helper.currentDirectory();
                FHIRAdditionalDetails fhirAdditionalDetails = FHIRAdditionalDetails.builder().fileName(documentsPath.getFileName()).base64Content(base64)
                        .contentType(getContentType(documentsPath.getFileName())).patientName(abhaUserDetails.getFullName())
                        .organizationName(documentsPath.getHospitalLabName()).base64Content(base64)
                        .heathInformationType(documentsPath.getMedicalDocumentType()).phoneNumber(abhaUserDetails.getPhoneNumber())
                        .gender(abhaUserDetails.getGender()).dateOfBirth(abhaUserDetails.getDateOfBirth()).build();

                Bundle bundle = getFHIRBundle(fhirAdditionalDetails);
                String fhirPath = "abha-service" + File.separator + "uploads" + File.separator + "health" + File.separator + "fhir-bundle" + File.separator + "username" + File.separator + UserContext.getCurrentUserName() + File.separator + fhirAdditionalDetails.getHeathInformationType() + File.separator + fhirAdditionalDetails.getFileName() +".json";
                bundleFileCreate(bundle,absolutePath,fhirPath);
                fhirPathList.add(fhirPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fhirPathList;
    }

    /**
     * This method generates a FHIR bundle based on the given FHIRAdditionalDetails.
     *
     * @param fhirAdditionalDetails FHIRAdditionalDetails containing file details and other information.
     * @return Bundle representing the FHIR bundle.
     */
    public Bundle getFHIRBundle(FHIRAdditionalDetails fhirAdditionalDetails){
        switch(fhirAdditionalDetails.getHeathInformationType()){
            case Prescription -> {
                return populatePrescriptionBundle(fhirAdditionalDetails);
            }
            case OPConsultation -> {
                return populateOPConsultNoteBundle(fhirAdditionalDetails);
            }
            case WellnessRecord -> {
                return populateWellnessRecordBundle(fhirAdditionalDetails);
            }
            case DischargeSummary -> {
                return populateDischargeSummaryBundle(fhirAdditionalDetails);
            }
            case DiagnosticReport -> {
                return  populateDiagnosticReportLabBundle(fhirAdditionalDetails);
            }
            case ImmunizationRecord -> {
                return  populateImmunizationRecordBundle(fhirAdditionalDetails);
            }
            case HealthDocumentRecord -> {
                return populateHealthDocumentRecordBundle(fhirAdditionalDetails);
            }
        }
        return null;
    }

    /**
     * This method saves the given FHIR bundle in the database.
     */
    public void saveFHIRBundleInDB(){

    }

    /**
     * This method converts the given Bundle to a pretty-printed JSON file and returns the File object.
     *
     * @param bundle Bundle to be converted to JSON.
     * @return File object representing the pretty-printed JSON file.
     * @throws IOException If an I/O error occurs.
     */
    public File getPrettyJson(Bundle bundle) throws IOException {
        IParser parser;
        String filePath = Helper.getTempPath() + File.separator+ UUID.randomUUID()+".json";
        parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);
        String json = parser.encodeResourceToString(bundle);
        File file = new File(filePath);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(json);
        writer.flush();
        writer.close();
        return file;
    }

    /**
     * This method creates a JSON file from the given Bundle and saves it in the specified directory.
     *
     * @param bundle Bundle to be converted to JSON.
     * @param absolutePath Absolute path of the directory where the JSON file should be saved.
     * @param fhirPath Path where the JSON file should be saved.
     * @return File object representing the JSON file.
     * @throws IOException If an I/O error occurs.
     */
    public File bundleFileCreate(Bundle bundle,String absolutePath,String fhirPath) throws IOException {
            Helper.fileCreate(absolutePath, fhirPath);
            IParser parser;
            String filePath = absolutePath + File.separator + fhirPath;
            parser = ctx.newJsonParser();
            parser.setPrettyPrint(true);
            String json = parser.encodeResourceToString(bundle);
            File file = new File(filePath);
//            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.flush();
            writer.close();
            return file;
    }
    public String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "pdf":
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * This method generates a key pair for encryption and decryption.
     *
     * @return KeyMaterial representing the generated key pair.
     * @throws Exception If an error occurs during key pair generation.
     */
    public KeyMaterial generateKeyPair() throws Exception {
        return keyPairGenController.generate();
    }

    /**
     * This method encrypts the given FHIR bundle using the provided encryption request.
     *
     * @param encryptionRequest EncryptionRequest containing the necessary information for encryption.
     * @return EncryptionResponse containing the encrypted FHIR bundle and other relevant information.
     * @throws Exception If an error occurs during encryption.
     */
    public EncryptionResponse encryptFhirBundle(EncryptionRequest encryptionRequest) throws Exception {
        return encryptionController.encrypt(encryptionRequest);
    }

    /**
     * This method decrypts the given encrypted FHIR bundle using the provided decryption request.
     *
     * @param decryptionRequest DecryptionRequest containing the necessary information for decryption.
     * @return DecryptionResponse containing the decrypted FHIR bundle and other relevant information.
     * @throws Exception If an error occurs during decryption.
     */
    public DecryptionResponse decryptFhirBundle(DecryptionRequest decryptionRequest) throws Exception {
        return decryptionController.decrypt(decryptionRequest);
    }
}
