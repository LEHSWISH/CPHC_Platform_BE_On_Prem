package org.wishfoundation.abhaservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.abhaservice.aws.s3.S3Wrapper;
import org.wishfoundation.abhaservice.config.EnvironmentConfig;
import org.wishfoundation.abhaservice.entity.*;
import org.wishfoundation.abhaservice.enums.ConsentStatus;
import org.wishfoundation.abhaservice.enums.FieldType;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;
import org.wishfoundation.abhaservice.request.ABHAFlowChainRequest;
import org.wishfoundation.abhaservice.request.decryption.DecryptionRequest;
import org.wishfoundation.abhaservice.request.hip.CareContextRequset;
import org.wishfoundation.abhaservice.request.hip.DataPushRequest;
import org.wishfoundation.abhaservice.request.hip.Entry;
import org.wishfoundation.abhaservice.request.hip.Patient;
import org.wishfoundation.abhaservice.request.hiu.*;
import org.wishfoundation.abhaservice.request.webhook.BaseHIPWebhookRequest;
import org.wishfoundation.abhaservice.request.webhook.HIUNotification;
import org.wishfoundation.abhaservice.request.webhook.Identifier;
import org.wishfoundation.abhaservice.response.abha.ABHAUserDetails;
import org.wishfoundation.abhaservice.response.decryption.DecryptionResponse;
import org.wishfoundation.abhaservice.response.hiu.BaseHIUModel;
import org.wishfoundation.abhaservice.utils.AbhaApiUtils;
import org.wishfoundation.abhaservice.utils.Helper;
import org.wishfoundation.abhaservice.utils.UserServiceUtils;
import org.wishfoundation.chardhamcore.config.UserContext;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.wishfoundation.abhaservice.request.ABHAFlowChainRequest.*;
import static org.wishfoundation.abhaservice.utils.Helper.*;

/**
 * This class is responsible for handling all the interactions with the Health Information User (HIU) system.
 * It includes methods for initializing consent requests, fetching consent details, requesting documents,
 * and handling callbacks from the HIU system.
 */
@Data
@Service
public class HIUService {
    @Autowired
    private Helper helper;

    @Autowired
    private UserServiceUtils userServiceUtils;

    @Autowired
    private AbhaApiUtils abhaUtils;

    @Autowired
    private ConsentRepository consentRepo;

    @Autowired
    private ConsentArtefactsRepository consentArtefactsRepo;

    @Autowired
    private EnvironmentConfig env;

    private final ConsentDocumentsRepository consentDocumentsRepository;

    private final S3Wrapper s3Wrapper;

    private final FHIRService fhirService;

    /**
     * This method initializes a consent request with the HIU system.
     *
     * @param request The request object containing the consent details.
     * @return A ResponseEntity indicating the success or failure of the request.
     */
    public ResponseEntity<BaseHIUModel> consentInit(BaseHIURequest request) {
        WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
        String requestId = UUID.randomUUID().toString();

        ABHAUserDetails abhaDetails = userServiceUtils.getAbhaDetails();


        List<String> phrAddress = abhaDetails.getPhrAddress();
        abhaUtils.checkValidation(phrAddress.get(0), FieldType.ABHA_ADDRESS);

        request.setRequestId(requestId);
        request.setTimestamp(Helper.getIsoTimeStamp());

        ConsentRequest conReq = new ConsentRequest();
        conReq.setPurpose(new HIUPurpose());
        conReq.setHiu(new HiuRequest());

        // setting patient
        Patient patient = new Patient();
        patient.setId(phrAddress.get(0));
        conReq.setPatient(patient);

        // setting requester
        HIURequester requester = new HIURequester();
        // TODO REMOVE UUID AT STAGE.
        requester.setName("wishfoundationindia_" + UUID.randomUUID());
        Identifier identifier = new Identifier();
        identifier.setType("REGNO");
        identifier.setValue("wishfoundationindia");
        identifier.setSystem("https://wishfoundationindia.org/");
        requester.setIdentifier(identifier);
        conReq.setRequester(requester);

        // setting hitype
        conReq.setHiTypes(request.getHiTypes());

        // setting permission
        HIUPermission permission = new HIUPermission();
        permission.setAccessMode("VIEW");
        permission.setDateRange(request.getDateRange());
        String dataEraseAt = request.getDataEraseAt();
        System.out.println("dataEraseAt : " + dataEraseAt);
        permission.setDataEraseAt(dataEraseAt);
        permission.setFrequency(new HIUFrequency());
        conReq.setPermission(permission);

        request.setConsent(conReq);

        try {
            System.out.println("request : " + MAPPER.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Mono<ResponseEntity<String>> response = client.post()
                .uri("/gateway/v0.5/consent-requests/init")
                .header("Authorization", "Bearer " + helper.generateGatewayToken())
                .header("X-CM-ID", ABDM_X_CM_ID)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request).retrieve().toEntity(String.class);

        ResponseEntity<String> responseEntity = response.block();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String key = HIU_KEY_PREFIX + requestId;
            ABHAFlowChainRequest dtaReq = new ABHAFlowChainRequest();

            dtaReq.setCurrentKey(key);
            String currentUserName = UserContext.getCurrentUserName();
            System.out.println("currentUserName : " + currentUserName);
            dtaReq.setBody(currentUserName);
            dtaReq.setHashKey(YATRI_UUID);
            helper.setAbhaData(dtaReq);

            dtaReq.setCurrentKey(key);
            dtaReq.setBody(request.getDateRange().getMyto());
            dtaReq.setHashKey(DATE_TO);
            helper.setAbhaData(dtaReq);

            dtaReq.setCurrentKey(key);
            dtaReq.setBody(request.getDateRange().getFrom());
            dtaReq.setHashKey(DATE_FROM);
            helper.setAbhaData(dtaReq);

            dtaReq.setCurrentKey(key);
            dtaReq.setBody(dataEraseAt);
            dtaReq.setHashKey(DATE_EXP);
            helper.setAbhaData(dtaReq);


            try {
                dtaReq.setCurrentKey(key);
                dtaReq.setBody(MAPPER.writeValueAsString(request.getHiTypes()));
                dtaReq.setHashKey(HIP_TYPES);
                helper.setAbhaData(dtaReq);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * This method fetches consent details from the HIU system.
     *
     * @param request The request object containing the consent ID.
     * @return A ResponseEntity containing the fetched consent details.
     */
    public ResponseEntity<BaseHIUModel> fetchConsent(BaseHIURequest request) {
        WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
        String requestId = UUID.randomUUID().toString();

        request.setRequestId(requestId);
        request.setTimestamp(Helper.getIsoTimeStamp());

        Mono<ResponseEntity<String>> response = client.post()
                .uri("/gateway/v0.5/consents/fetch")
                .header("Authorization", "Bearer " + helper.generateGatewayToken())
                .header("X-CM-ID", ABDM_X_CM_ID)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request).retrieve().toEntity(String.class);

        ResponseEntity<String> responseEntity = response.block();
        return ResponseEntity.ok(null);
    }

    /**
     * This method requests documents from the HIU system.
     *
     * @param request The request object containing the consent ID and other relevant details.
     * @return A ResponseEntity indicating the success or failure of the request.
     */
    public ResponseEntity<BaseHIUModel> requestDocument(BaseHIURequest request) {
        WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
        String requestId = request.getOnRequestKey().toString();

        request.setRequestId(requestId);
        request.setTimestamp(Helper.getIsoTimeStamp());

        HiRequest hiRequest = new HiRequest();
        ConsentRequest consent = new ConsentRequest();
        consent.setId(request.getConsentId());
        hiRequest.setConsent(consent);

        hiRequest.setDateRange(request.getDateRange());
        hiRequest.setDataPushUrl(DATA_PUSH_URL);

        try {
            org.wishfoundation.abhaservice.keypairgen.KeyMaterial requesterKeyPair = getKeyPairMaterialHIU();

            KeyMaterial keyMaterial = new KeyMaterial();
            keyMaterial.setNonce(requesterKeyPair.getNonce());
            keyMaterial.setCryptoAlg(cryptoAlg);
            keyMaterial.setCurve(curve);

            DhPublicKey dhPublicKey = new DhPublicKey();
            dhPublicKey.setKeyValue(requesterKeyPair.getPublicKey());
            dhPublicKey.setExpiry(request.getDataEraseAt());
            dhPublicKey.setParameters(parameters);
            keyMaterial.setDhPublicKey(dhPublicKey);

            hiRequest.setKeyMaterial(keyMaterial);
            request.setHiRequest(hiRequest);


            System.out.println("request : " + MAPPER.writeValueAsString(request));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Mono<ResponseEntity<String>> response = client.post()
                .uri("/gateway/v0.5/health-information/cm/request")
                .header("Authorization", "Bearer " + helper.generateGatewayToken())
                .header("X-CM-ID", ABDM_X_CM_ID)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request).retrieve().toEntity(String.class);

        ResponseEntity<String> responseEntity = response.block();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {

        }

        return ResponseEntity.ok(null);
    }

    // ...

    /**
     * This method handles the callback from the HIU system when a consent request is initiated.
     *
     * @param request The request object containing the consent details.
     * @return A ResponseEntity indicating the success or failure of the callback handling.
     */
    public ResponseEntity<BaseHIUModel> consentOnInit(BaseHIPWebhookRequest request) {
        String key = HIU_KEY_PREFIX + request.getResp().getRequestId();
        String userName = helper.getRedisTemplate().opsForHash().get(key, YATRI_UUID).toString();
        String dateTo = helper.getRedisTemplate().opsForHash().get(key, DATE_TO).toString();
        String dateFrom = helper.getRedisTemplate().opsForHash().get(key, DATE_FROM).toString();
        String dateExp = helper.getRedisTemplate().opsForHash().get(key, DATE_EXP).toString();
        UserContext.setCurrentUserName(userName);

        Consent consent = new Consent();
        consent.setStatus(ConsentStatus.INITIATED);
        consent.setId(UUID.fromString(request.getConsentRequest().getId()));
        consent.setHiTypes(MedicalDocumentType.getAll());
        consent.setDateTo(dateTo);
        consent.setDateFrom(dateFrom);
        consent.setDataEraseAt(dateExp);


        consentRepo.save(consent);
        UserContext.clear();
        return ResponseEntity.ok(null);
    }
    public ResponseEntity<BaseHIUModel> hiuNotify(BaseHIPWebhookRequest request) {
        try {
           System.out.println("hiuNotify--------------------------------"+MAPPER.writeValueAsString(request));
        }catch (Exception e) {
            e.printStackTrace();
        }

        HIUNotification notification = request.getNotification();

        // TODO REMOVE CONSENT ARTEFACTS FROM DATABASE AT TIME OF REVOKE AS STATUS.
        if(ConsentStatus.valueOf(notification.getStatus()).equals(ConsentStatus.REVOKED)){
            consentArtefactsRepo.deleteAllConsentArtefactsByConsentId(UUID.fromString(notification.getConsentRequestId()));
            Consent consent = consentRepo.findById(UUID.fromString(notification.getConsentRequestId())).get();
            String userName = consent.getCreatedBy().getYatriUserName();
            UserContext.setCurrentUserName(userName);
            consent.setStatus(ConsentStatus.REVOKED);
            consentRepo.save(consent);
            return ResponseEntity.ok(null);
        }
        Consent consent = consentRepo.findById(UUID.fromString(notification.getConsentRequestId())).get();
        String userName = consent.getCreatedBy().getYatriUserName();
        UserContext.setCurrentUserName(userName);
        consent.setStatus(ConsentStatus.valueOf(notification.getStatus()));
        consentRepo.save(consent);

        List<ConsentArtefact> consentArtefacts = new ArrayList<>();
        if (!ObjectUtils.isEmpty(notification.getConsentArtefacts())) {
            for (ConsentArtefact cosAft : notification.getConsentArtefacts()) {
                cosAft.setConsentId(consent.getId());
                consentArtefacts.add(cosAft);
            }
            consentArtefactsRepo.saveAll(consentArtefacts);

            for (ConsentArtefact cosAft : notification.getConsentArtefacts()) {
                BaseHIURequest baseHIURequest = new BaseHIURequest();
                baseHIURequest.setConsentId(cosAft.getId().toString());
                fetchConsent(baseHIURequest);
            }
        }


        UserContext.clear();
        return ResponseEntity.ok(null);
    }

    // ...

    /**
     * This method handles the callback from the HIU system when a document is fetched.
     *
     * @param request The request object containing the transaction ID.
     * @return A ResponseEntity indicating the success or failure of the callback handling.
     */
    public ResponseEntity<BaseHIUModel> onFetch(BaseHIPWebhookRequest request) {
        ConsentRequest consentDetail = request.getConsent().getConsentDetail();
        Optional<ConsentArtefact> consOp = consentArtefactsRepo.findById(UUID.fromString(consentDetail.getConsentId()));

        ConsentArtefact consentArtefact = consOp.get();

        String userName = consentArtefact.getCreatedBy().getYatriUserName();
        UserContext.setCurrentUserName(userName);

        consentArtefact.setAbhaAddress(consentDetail.getPatient().getId());
        consentArtefact.setHipId(consentDetail.getHip().getId());
        consentArtefact.setHipName(consentDetail.getHip().getName());
        consentArtefact.setHiTypes(consentDetail.getHiTypes());
        consentArtefact.setAccessMode(consentDetail.getPermission().getAccessMode());
        DateRange dateRange = consentDetail.getPermission().getDateRange();
        consentArtefact.setDateTo(dateRange.getMyto());
        consentArtefact.setDateFrom(dateRange.getFrom());
        String dataEraseAt = consentDetail.getPermission().getDataEraseAt();
        consentArtefact.setDataEraseAt(dataEraseAt);

        List<String> careContexts = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();

        for (CareContextRequset contx : consentDetail.getCareContexts()) {
            careContexts.add(contx.getCareContextReference());
            patientIds.add(contx.getPatientReference());
        }

        consentArtefact.setCareContextIds(careContexts);
        consentArtefact.setPatientIds(patientIds);

        UUID onRequestKey = UUID.randomUUID();
        consentArtefact.setTransactionRequestId(onRequestKey);
        consentArtefactsRepo.save(consentArtefact);
        UserContext.clear();

        // creating doc request to hip.
        BaseHIURequest baseHIURequest = new BaseHIURequest();
        baseHIURequest.setDateRange(dateRange);
        baseHIURequest.setDataEraseAt(dataEraseAt);
        baseHIURequest.setConsentId(consentArtefact.getId().toString());
        baseHIURequest.setUserName(userName);
        baseHIURequest.setOnRequestKey(onRequestKey);

        requestDocument(baseHIURequest);

        return ResponseEntity.ok(null);
    }

    // ...

    /**
     * This method handles the callback from the HIU system when a document is requested.
     *
     * @param request The request object containing the transaction ID.
     * @return A ResponseEntity indicating the success or failure of the callback handling.
     */
    public ResponseEntity<BaseHIUModel> onRequest(BaseHIPWebhookRequest request) {
        ConsentArtefact consentArtefact = consentArtefactsRepo.findByTransactionRequestId(UUID.fromString(request.getResp().getRequestId()));
        String userName = consentArtefact.getCreatedBy().getYatriUserName();
        UserContext.setCurrentUserName(userName);
        consentArtefact.setTransactionId(UUID.fromString(request.getHiRequest().getTransactionId()));
        consentArtefactsRepo.save(consentArtefact);
        UserContext.clear();
        return ResponseEntity.ok(null);
    }

    // ...

    /**
     * This method fetches consent details from the database.
     *
     * @param request The request object containing the user's name.
     * @return A ResponseEntity containing a list of fetched consent details.
     */
    public ResponseEntity<List<Consent>> fetchConsentDB(BaseHIURequest request) {
        List<Consent> consents = consentRepo.findByCreatedBy(UserContext.getCurrentUserName());
        return ResponseEntity.ok(consents);
    }

    // ...

    /**
     * This method fetches consent artefacts from the database.
     *
     * @param request The request object containing the consent IDs.
     * @return A ResponseEntity containing a list of fetched consent artefacts.
     */
    public ResponseEntity<List<ConsentArtefact>> fetchConsentArtefacts(BaseHIURequest request) {
        List<ConsentArtefact> byConsentId = consentArtefactsRepo.findByConsentId(request.getConsentIds());
        return ResponseEntity.ok(byConsentId);
    }

    // ...

    /**
     * This method handles the callback from the HIU system when data is pushed to a data push URL.
     *
     * @param request The request object containing the transaction ID.
     * @return A ResponseEntity indicating the success or failure of the callback handling.
     */
    public ResponseEntity<List<ConsentArtefact>> dataPushDurl(BaseHIPWebhookRequest request) {
        String fileName = request.getTransactionId() + ".json";
        String key = "abha-service" + File.separator + "uploads" + File.separator + HIU_S3_KEY;
        String reportPath = Helper.currentDirectory();
        String finalReportPath = reportPath + File.separator + key + fileName;
        File finalReport = null;
        try {
            System.out.println("finalReportPath : "+finalReportPath);
            finalReport = new File(finalReportPath);
            Files.createDirectories(Paths.get(reportPath));
            Files.createFile(finalReport.toPath());
            FileOutputStream fos = new FileOutputStream(finalReportPath);
            fos.write(MAPPER.writeValueAsString(request).getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ...

    /**
     * This method retrieves FHIR bundles from the HIU system.
     *
     * @param hipId The ID of the Health Information Provider (HIP).
     * @return A list of FHIR bundles.
     */
    public List<FhirBundle> getFHIRBundle(String hipId) {
        Set<String> fhirBundles = new HashSet<>();
        ABHAUserDetails abhaDetails = userServiceUtils.getAbhaDetails();
        System.out.println("abhaDetails ------------- "+abhaDetails.getPhrAddress());
        List<UUID> transactionIdList = consentDocumentsRepository.findTransactionIdByHipIdAndAbhaAddress(hipId, Helper.encrypt(abhaDetails.getPhrAddress().get(0)));
        try {
            for (UUID transactionId : transactionIdList) {
                if (ObjectUtils.isEmpty(transactionId)) {
                    continue;
                }
                //here also we are going to change
                String key = "abha-service" + File.separator + "uploads" + File.separator + HIU_S3_KEY;
                String reportPath = Helper.currentDirectory();
                String finalReportPath = reportPath + File.separator + key + transactionId + ".json";
                //TODO : if code will error out due to out of memory just reduce buffer size
                int bufferSize =  1024 * 1024;
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); InputStream fileData = Files.newInputStream(Paths.get(finalReportPath))) {
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead;
                    while ((bytesRead = fileData.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    byte[] bytes = outputStream.toByteArray();
                    if (!ObjectUtils.isEmpty(bytes)) {
                        DataPushRequest request = MAPPER.readValue(bytes, DataPushRequest.class);
                        String publicKey = request.getKeyMaterial().getDhPublicKey().getKeyValue();
                        String nonce = request.getKeyMaterial().getNonce();
                        org.wishfoundation.abhaservice.keypairgen.KeyMaterial requesterKeyPair = getKeyPairMaterialHIU();
                        DecryptionRequest decryptionRequest = DecryptionRequest.builder().requesterPrivateKey(requesterKeyPair.getPrivateKey())
                                .requesterNonce(requesterKeyPair.getNonce())
                                .senderPublicKey(publicKey).senderNonce(nonce).build();
                        for (Entry entry : request.getEntries()) {
                            DecryptionResponse decryptionResponse = fhirService.decryptFhirBundle(decryptionRequest.toBuilder().encryptedData(entry.getContent()).build());
                            fhirBundles.add(decryptionResponse.getDecryptedData());
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fhirBundles.stream().map(value -> FhirBundle.builder().content(value).build()).collect(Collectors.toList());
    }
}
