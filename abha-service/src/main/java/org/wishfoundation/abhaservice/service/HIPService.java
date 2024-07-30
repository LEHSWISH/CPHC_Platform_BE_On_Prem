package org.wishfoundation.abhaservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.abhaservice.aws.s3.S3Wrapper;
import org.wishfoundation.abhaservice.entity.*;
import org.wishfoundation.abhaservice.enums.AuthMode;
import org.wishfoundation.abhaservice.enums.ConsentStatus;
import org.wishfoundation.abhaservice.enums.FieldType;
import org.wishfoundation.abhaservice.enums.RequesterType;
import org.wishfoundation.abhaservice.request.*;
import org.wishfoundation.abhaservice.request.encryption.EncryptionRequest;
import org.wishfoundation.abhaservice.request.hip.*;
import org.wishfoundation.abhaservice.request.hiu.DhPublicKey;
import org.wishfoundation.abhaservice.request.hiu.KeyMaterial;
import org.wishfoundation.abhaservice.request.hiu.*;
import org.wishfoundation.abhaservice.request.webhook.*;
import org.wishfoundation.abhaservice.response.abha.ABHAUserDetails;
import org.wishfoundation.abhaservice.response.hip.BaseHIPModel;
import org.wishfoundation.abhaservice.response.hip.BaseHIPWebhookResponse;
import org.wishfoundation.abhaservice.response.yatri.YatriPulseUserResponse;
import org.wishfoundation.abhaservice.utils.AbhaApiUtils;
import org.wishfoundation.abhaservice.utils.Helper;
import org.wishfoundation.abhaservice.utils.NotificationUtils;
import org.wishfoundation.abhaservice.utils.UserServiceUtils;
import org.wishfoundation.chardhamcore.config.UserContext;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.wishfoundation.abhaservice.request.ABHAFlowChainRequest.*;
import static org.wishfoundation.abhaservice.request.NotificationRequestModel.*;
import static org.wishfoundation.abhaservice.utils.Helper.*;

@Service
@Data
public class HIPService {

    public static final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private Helper helper;
    @Autowired
    private UserServiceUtils userServiceUtils;
    @Autowired
    private AbhaApiUtils abhaUtils;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CareContextRepository careContextRepo;
    @Autowired
    private ConsentDocumentsRepository consentDocumentsRepo;
    @Autowired
    private NotificationUtils notificationUtils;

    private final FHIRService fhirService;

    private final FhirDocumentRepository fhirDocumentsRepository;

    private final ConsentRepository consentRepository;

    private final S3Wrapper s3Wrapper;

    /**
     * This method initializes the ABHA-PHR linkage process.
     * It sends a POST request to the ABHA-PHR gateway with the necessary details.
     * The method also saves the ABHA details and the care context details in the database.
     *
     * @param request The request object containing the necessary details for the linkage process.
     * @return A ResponseEntity with a status code of 200 if the linkage process is successful.
     * @throws Exception If any error occurs during the linkage process.
     */
    public ResponseEntity<BaseHIPModel> init(BaseHIPRequest request) {
        WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
        String requestId = UUID.randomUUID().toString();

        ABHAUserDetails abhaDetails = userServiceUtils.getAbhaDetails();

        List<String> phrAddress = abhaDetails.getPhrAddress();

        String abhaString = "";

        try {
            abhaString = MAPPER.writeValueAsString(abhaDetails);

            System.out.println(abhaString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        abhaUtils.checkValidation(phrAddress.get(0), FieldType.ABHA_ADDRESS);

        BaseHIPRequest hipRequest = new BaseHIPRequest();
        hipRequest.setRequestId(requestId);
        hipRequest.setTimestamp(Helper.getIsoTimeStamp());

        HIPQuery query = new HIPQuery();
        query.setId(phrAddress.get(0));
        query.setPurpose("KYC_AND_LINK");
        query.setAuthMode(AuthMode.DEMOGRAPHICS);

        HIPRequester hipRequester = new HIPRequester();
        hipRequester.setId(HIP_ID);
        hipRequester.setType(RequesterType.HIP);
        query.setRequester(hipRequester);

        hipRequest.setQuery(query);

        try {
            System.out.println(MAPPER.writeValueAsString(hipRequest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        Mono<ResponseEntity<String>> response = client.post()
                .uri("/gateway/v0.5/users/auth/init")
                .header("Authorization", "Bearer " + helper.generateGatewayToken())
                .header("X-CM-ID", ABDM_X_CM_ID)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(hipRequest).retrieve().toEntity(String.class);

        ResponseEntity<String> responseEntity = response.block();


        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String key = HIP_KEY_PREFIX + requestId;
//            redisTemplate.opsForHash().put(key, ON_INIT_KEY, abhaString);
            ABHAFlowChainRequest dtaReq = new ABHAFlowChainRequest();
            dtaReq.setCurrentKey(key);
            dtaReq.setBody(abhaString);
            dtaReq.setHashKey(ON_INIT_KEY);
            helper.setAbhaData(dtaReq);


            List<String> paths = new ArrayList<>();
            List<String> pathsId = new ArrayList<>();

            for (DocumentsPath docP : request.getDocumentsPathEntity()) {
                paths.add(docP.getFilePath());
                pathsId.add(docP.getId().toString());
            }

            CareContext careContext = new CareContext();
            careContext.setId(request.getCareContextId());

            careContext.setPatientId((PUID_PREFIX + UUID.randomUUID()));
            careContext.setDocumentPath(paths);
            careContext.setDocumentPathId(pathsId);
            careContext.setHiType(request.getDocumentType());
            careContext.setDocumentsDescription(request.getVisitPurpose());
            careContext.setAbhaId(abhaDetails.getAbhaNumber());
            careContextRepo.save(careContext);
            dtaReq.setCurrentKey(key);
            dtaReq.setBody(request.getCareContextId());
            dtaReq.setHashKey(CARE_CONTEXT_KEY);
            helper.setAbhaData(dtaReq);
            helper.getRedisTemplate().opsForHash().entries(key).entrySet().forEach(System.out::println);
            // NEED
            List<String> fhirPathList = fhirService.createFHIRBundleVM(request.getDocumentsPathEntity(), abhaDetails);
            if(!ObjectUtils.isEmpty(fhirPathList)){
                careContextRepo.save(careContext);
                try{
                    List<FhirDocuments> fhirDocumentsList = new ArrayList<>();
                    fhirPathList.forEach(
                            path -> {
                                FhirDocuments fhirDocuments = new FhirDocuments();
                                Helper.updateFieldIfNotNull(fhirDocuments::setCareContextId, request.getCareContextId());
                                Helper.updateFieldIfNotNull(fhirDocuments::setPath, path);
                                fhirDocumentsList.add(fhirDocuments);
                            }
                    );
                    fhirDocumentsRepository.saveAll(fhirDocumentsList);
                    System.out.println("FHIR Bundle Added -------------------- ");
                    fhirDocumentsList.forEach(i->System.out.println(i.getPath()));
                }catch (Exception e) {
                    careContextRepo.deleteById(request.getCareContextId());
                }
            }

        }


        HttpHeaders headers = responseEntity.getHeaders();
        String body = responseEntity.getBody();
        try {
            System.out.println(" headers " + MAPPER.writeValueAsString(headers));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println("body " + body);

      return  ResponseEntity.ok(null);
    }

    /**
     * This method is used to confirm the authentication request from the user.
     *
     * @param request The confirmation request containing the user's credentials.
     * @return A ResponseEntity with a status code indicating the success or failure of the request.
     * @throws JsonProcessingException If there is an error processing the JSON data.
     */
    public ResponseEntity<BaseHIPModel> confim(HIPConfirmRequest request) {

        WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
        String requestId = UUID.randomUUID().toString();
        ABHAFlowChainRequest dtaReq = new ABHAFlowChainRequest();


        Credential creds = new Credential();
        DemographicCredential demoCreds = new DemographicCredential();

        String key = HIP_KEY_PREFIX + request.getRequestId();
        ABHAUserDetails abusr = null;
        try {
            // String content = String.valueOf(redisTemplate.opsForHash().get(key, ON_INIT_KEY));
            dtaReq.setCurrentKey(key);
            dtaReq.setPrevKey(key);
            dtaReq.setHashKey(ON_INIT_KEY);
            System.out.println("redis key " + key);
            String content = helper.getAbhaData(dtaReq);
            abusr = MAPPER.readValue(content, ABHAUserDetails.class);
            System.out.println("abusr :" + MAPPER.writeValueAsString(abusr));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        request.setRequestId(requestId);
        request.setTimestamp(Helper.getIsoTimeStamp());

        demoCreds.setName(abusr.getFullName());
        demoCreds.setGender(String.valueOf(abusr.getGender().charAt(0)));

        String[] parts = abusr.getDateOfBirth().split("/");
        String day = parts[0];
        String month = parts[1];
        String year = parts[2];
        String formattedDate = year + "-" + month + "-" + day;
        demoCreds.setDateOfBirth(formattedDate);

        creds.setDemographic(demoCreds);

        request.setCredential(creds);

        try {
            System.out.println("confim care context :" + MAPPER.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Mono<ResponseEntity<String>> response = client.post()
                .uri("/gateway/v0.5/users/auth/confirm")
                .header("Authorization", "Bearer " + helper.generateGatewayToken())
                .header("X-CM-ID", ABDM_X_CM_ID)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request).retrieve().toEntity(String.class);

        ResponseEntity<String> responseEntity = response.block();


        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            key = HIP_KEY_PREFIX + request.getRequestId();
            dtaReq.setCurrentKey(key);
            helper.setAbhaData(dtaReq);
        }


        return ResponseEntity.ok(null);
    }

    /**
     * This method is responsible for adding a new care context to the ABHA system.
     * It sends a POST request to the ABHA gateway with the necessary details.
     *
     * @param request The request object containing the details of the care context to be added.
     * @return A ResponseEntity with a status code indicating the success or failure of the operation.
     * @throws RuntimeException If there is an error in processing the request.
     */
    public ResponseEntity<BaseHIPModel> addCareContext(BaseHIPRequest request) {

        try {
            System.out.println("Add care Context Request ------ "+ MAPPER.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
        String requestId = UUID.randomUUID().toString();

        String prevKey = request.getRequestId();


        CareContext careContext = careContextRepo.findById(request.getCareContextId()).get();


        request.setRequestId(requestId);
        request.setTimestamp(Helper.getIsoTimeStamp());

        Patient pt = new Patient();
        CareContextRequset crCtx = new CareContextRequset();
        crCtx.setDisplay(careContext.getDocumentsDescription());
        crCtx.setReferenceNumber(request.getCareContextId());
        pt.setCareContexts(Arrays.asList(crCtx));

        pt.setDisplay(request.getPatient().getName());
        pt.setReferenceNumber(careContext.getPatientId());

        request.getLink().setPatient(pt);
        try {
            System.out.println("add ctx :" + MAPPER.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Mono<ResponseEntity<String>> response = client.post()
                .uri("/gateway/v0.5/links/link/add-contexts")
                .header("Authorization", "Bearer " + helper.generateGatewayToken())
                .header("X-CM-ID", ABDM_X_CM_ID)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request).retrieve().toEntity(String.class);

        ResponseEntity<String> responseEntity = response.block();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {

            ABHAFlowChainRequest dtaReq = new ABHAFlowChainRequest();
            String key = HIP_KEY_PREFIX + request.getRequestId();
            dtaReq.setCurrentKey(key);
            dtaReq.setPrevKey(HIP_KEY_PREFIX + prevKey);
            helper.setAbhaData(dtaReq);
            helper.getRedisTemplate().opsForHash().entries(key).entrySet().forEach(System.out::println);


            //TODO FHIR BUBLE PERSIST HERE
        }
       return ResponseEntity.ok(null);
    }

    // callback
    /**
     * This method handles the notifications received from the Health Information User (HIU).
     * It processes the notifications and updates the consent documents accordingly.
     *
     * @param request The webhook request containing the notification details.
     * @return A BaseHIPWebhookResponse object, which can be null in this case.
     */
    public BaseHIPWebhookResponse notifyHip(BaseHIPWebhookRequest request) {


        HIUNotification notification = request.getNotification();

        if (notification.getStatus().equals(ConsentStatus.REVOKED)) {
            ConsentDocuments consDoc = consentDocumentsRepo.findById(UUID.fromString(notification.getConsentId())).get();
            consDoc.setStatus(ConsentStatus.REVOKED);
            consentDocumentsRepo.save(consDoc);
            return null;
        }

        //onnotifyHIP
        onNotifyHIP(request);


        ConsentDocuments consDoc = new ConsentDocuments();

        ConsentRequest consentDetail = notification.getConsentDetail();

        consDoc.setId(UUID.fromString(notification.getConsentId()));
        consDoc.setAbhaAddress(consentDetail.getPatient().getId());
        consDoc.setHipId(consentDetail.getHip().getId());
        consDoc.setHipName(consentDetail.getHip().getName());
        consDoc.setHiTypes(consentDetail.getHiTypes());
        consDoc.setAccessMode(consentDetail.getPermission().getAccessMode());
        DateRange dateRange = consentDetail.getPermission().getDateRange();
        consDoc.setDateTo(dateRange.getMyto());
        consDoc.setDateFrom(dateRange.getFrom());
        String dataEraseAt = consentDetail.getPermission().getDataEraseAt();
        consDoc.setDataEraseAt(dataEraseAt);

        List<String> careContexts = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();

        for (CareContextRequset contx : consentDetail.getCareContexts()) {
            careContexts.add(contx.getCareContextReference());
            patientIds.add(contx.getPatientReference());
        }

        consDoc.setCareContextIds(careContexts);
        consDoc.setPatientIds(patientIds);
        consDoc.setStatus(ConsentStatus.GRANTED);
        consentDocumentsRepo.save(consDoc);
        return null;
    }

    private ResponseEntity<BaseHIPWebhookResponse> onNotifyHIP(BaseHIPWebhookRequest request){
        try{
            HIPOnNotifyRequest hipOnNotifyRequest = new HIPOnNotifyRequest();
            hipOnNotifyRequest.setRequestId(UUID.randomUUID().toString());
            hipOnNotifyRequest.setTimestamp(Helper.getIsoTimeStamp());

            HIPAcknowledgementRequest hipAcknowledgementRequest = new HIPAcknowledgementRequest();
            hipAcknowledgementRequest.setStatus(ConsentStatus.OK);
            hipAcknowledgementRequest.setConsentId(request.getNotification().getConsentId());

            hipOnNotifyRequest.setAcknowledgement(hipAcknowledgementRequest);

            Resp resp = new Resp();
            resp.setRequestId(request.getRequestId());
            hipOnNotifyRequest.setResp(resp);

            System.out.println("hip on notify req : "+ MAPPER.writeValueAsString(hipOnNotifyRequest));

            WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();

            Mono<ResponseEntity<String>> response = client.post().uri("/gateway/v0.5/consents/hip/on-notify")
                    .header("Authorization", "Bearer " + helper.generateGatewayToken())
                    .header("X-CM-ID", ABDM_X_CM_ID)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .bodyValue(hipOnNotifyRequest)
                    .retrieve()
                    .toEntity(String.class);
            ResponseEntity<String> res = response.block();
            //TODO : handle in some time
            if(!res.getStatusCode().is2xxSuccessful()){
                // throw exception
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    /**
     * This method handles the notifications received from the Health Information User (HIU).
     * It processes the notifications and updates the consent documents accordingly.
     *
     * @param request The webhook request containing the notification details.
     * @return A BaseHIPWebhookResponse object, which can be null in this case.
     */
    public ResponseEntity<BaseHIPWebhookResponse> requestHip(BaseHIPWebhookRequest request) {

        HiRequest hiRequest = request.getHiRequest();
        ConsentDocuments consDoc = consentDocumentsRepo.findById(UUID.fromString(hiRequest.getConsent().getId())).get();

        consDoc.setTransactionId(UUID.fromString(request.getTransactionId()));
        consDoc.setTransactionRequestId(UUID.fromString(request.getRequestId()));
        consDoc.setDataPushUrl(hiRequest.getDataPushUrl()); //

        KeyMaterial km = hiRequest.getKeyMaterial();
        consDoc.setNonce(km.getNonce());
        consDoc.setCurve(km.getCurve());
        consDoc.setCryptoAlg(km.getCryptoAlg());

        DhPublicKey dpk = km.getDhPublicKey();
        consDoc.setParameters(dpk.getParameters());
        consDoc.setKeyValue(dpk.getKeyValue());
        consentDocumentsRepo.save(consDoc);

        try {
            org.wishfoundation.abhaservice.keypairgen.KeyMaterial senderKeyPair = getKeyPairMaterial();

            EncryptionRequest encryptionRequest = EncryptionRequest.builder().requesterNonce(km.getNonce()).senderNonce(senderKeyPair.getNonce())
                    .requesterPublicKey(dpk.getKeyValue()).senderPrivateKey(senderKeyPair.getPrivateKey()).build();

            List<Entry> entries = new ArrayList<>();
//            for (String careContextId : consDoc.getCareContextIds()) {
//                List<FhirDocuments> fhirDocumentsList = fhirDocumentsRepository.findByCareContextIdAndCreatedAtBetweenStartAndEnd(
//                        Instant.parse(consDoc.getDateFrom()),
//                        Instant.parse(consDoc.getDateTo()), careContextId);
//                for (FhirDocuments fhirDocuments : fhirDocumentsList) {
//                    byte[] bytes = s3Wrapper.getObjectAsBytes(fhirDocuments.getPath());
//                    String encryptedData = fhirService.encryptFhirBundle(encryptionRequest.toBuilder().stringToEncrypt(new String(bytes)).build()).getEncryptedData();
//                    entries.add(Entry.builder().media("application/fhir+json").careContextReference(careContextId).content(
//                            encryptedData
//                    ).checksum(UUID.randomUUID().toString()).build());
//                }
//            }

//            for (String careContextId : consDoc.getCareContextIds()) {
                List<FhirDocuments> fhirDocumentsList =  careContextRepo.findByCareContextIdAndHiType(consDoc.getCareContextIds(),consDoc.getHiTypes());
                System.out.println(" REAL FHIR DOUCMENT LIST ******************* "+fhirDocumentsList.size());
//                List<FhirDocuments> fhirDocumentsList = fhirDocumentsRepository.findByCareContextId(careContextId);

                List<FhirDocuments> filteredFhirDocumentsList = fhirDocumentsList.stream()
                        .filter(i -> {
                            String createdOn = i.getCreatedOn().toString().replace(" ","T");
                            return compareGreaterThanOrEqualTo(createdOn,consDoc.getDateFrom())
                                    && compareLessThanOrEqualTo(createdOn, consDoc.getDateTo());
                        })
                        .collect(Collectors.toList());

                System.out.println("list of fhir bundle present in S3: "+"  date from   "+Instant.parse(consDoc.getDateFrom())+" dtae to ------------  "+ Instant.parse(consDoc.getDateTo())+" "+" fhir list --------- " +filteredFhirDocumentsList.size());
            for (FhirDocuments fhirDocuments : fhirDocumentsList) {
                String absolutePath = Helper.currentDirectory();
                byte[] bytes = Files.readAllBytes(Paths.get(absolutePath,fhirDocuments.getPath()));
                String encryptedData = fhirService.encryptFhirBundle(encryptionRequest.toBuilder().stringToEncrypt(new String(bytes)).build()).getEncryptedData();
                    entries.add(Entry.builder().media("application/fhir+json").careContextReference(fhirDocuments.getCareContextId()).content(
                        encryptedData
                ).checksum(UUID.randomUUID().toString()).build());
            }
//            }

            DataPushRequest dataPushRequest = DataPushRequest.builder().keyMaterial(org.wishfoundation.abhaservice.request.hip.KeyMaterial.builder()
                            .curve(km.getCurve())
                            .cryptoAlg(km.getCryptoAlg())
                            .nonce(senderKeyPair.getNonce())
                    .dhPublicKey(org.wishfoundation.abhaservice.request.hip.DhPublicKey.builder()
                            .keyValue(senderKeyPair.getPublicKey())
                            .expiry(dpk.getExpiry())
                            .parameters(dpk.getParameters()).build()).build())
                    .transactionId(request.getTransactionId())
                    .entries(entries)
                    .pageNumber(1).pageCount(0).build();

                     WebClient client = helper.getWebClient().baseUrl(hiRequest.getDataPushUrl()).build();
                     client.post()
                    .header("Authorization", "Bearer " + helper.generateGatewayToken())
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .bodyValue(dataPushRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    // TODO on assumption data is dumped into HIP portal without linking.
    /**
     * This method handles the discover request from HIP.
     * It retrieves the patient's details from the request,
     * matches the patient with the existing ABHA and PHR records,
     * and prepares the necessary data for linking.
     *
     * @param request The HIP webhook request containing the patient's details.
     * @return A BaseHIPModel object representing the response to the discover request.
     * @throws Exception If any error occurs during the processing of the request.
     *  */
    public BaseHIPWebhookResponse discover(BaseHIPWebhookRequest request) {

        try {
            // IF ABHA LINKED BUT NOT SYNCED WITH PHR.
            String abhaNumber = "";
            String phoneNumber = "";
            Patient patient = request.getPatient();
            for (Identifier idf : patient.getVerifiedIdentifiers()) {
                if (idf.getType().equalsIgnoreCase("NDHM_HEALTH_NUMBER")) {
                    abhaNumber = idf.getValue();
                }
                if (idf.getType().equalsIgnoreCase("MOBILE")) {
                    phoneNumber = idf.getValue();
                }
            }
            ABHAUserDetails abhaDetails = new ABHAUserDetails();
            abhaDetails.setAbhaNumber(abhaNumber);
            abhaDetails = userServiceUtils.getAbhaDetails(abhaDetails);

            List<CareContextRequset> ccrList = new ArrayList<>();

            if (!ObjectUtils.isEmpty(abhaDetails)) {
                System.out.println("getAbhaNumber() : " + abhaDetails.getAbhaNumber());
                List<CareContext> careContexts = careContextRepo.findByAbhaId(abhaDetails.getAbhaNumber());
                for (CareContext cc : careContexts) {
                    CareContextRequset ccr = new CareContextRequset();
                    ccr.setDisplay(cc.getDocumentsDescription());
                    ccr.setReferenceNumber(cc.getId());
                    ccrList.add(ccr);
                }
            }

            // WHEN ABHA NOT LINKED.
            BaseDiscoveryRequest discoveryReq = new BaseDiscoveryRequest();
            discoveryReq.setName(patient.getName());
            discoveryReq.setPhoneNumber(phoneNumber);
            discoveryReq.setYearOfBirth(patient.getYearOfBirth());
            discoveryReq.setGender(patient.getGender());
            YatriPulseUserResponse yatri = userServiceUtils.getYatriDetails(discoveryReq);

            if (!ObjectUtils.isEmpty(yatri)) {
                System.out.println("yatri.getUserName() : " + yatri.getUserName());
                List<CareContext> byUserAndNullAbhaId = careContextRepo.findByUserAndNullAbhaId(yatri.getUserName());
                for (CareContext cc : byUserAndNullAbhaId) {
                    CareContextRequset ccr = new CareContextRequset();
                    ccr.setDisplay(cc.getDocumentsDescription());
                    ccr.setReferenceNumber(cc.getId());
                    ccrList.add(ccr);
                }
            }


            BaseHIPRequest hipRequest = new BaseHIPRequest();
            hipRequest.setTransactionId(request.getTransactionId());
            Patient p = new Patient();
            String puid = PUID_PREFIX + abhaNumber;
            p.setReferenceNumber(puid);
            String fullName = yatri.getYatriDetails().getFullName();
            p.setDisplay(fullName);
            p.setCareContexts(ccrList);
            p.setMatchedBy(Arrays.asList("MOBILE"));
            hipRequest.setPatient(p);

            Resp resp = new Resp();
            resp.setRequestId(request.getRequestId());
            hipRequest.setResp(resp);

            // TODO nned optimization.

            // ON DISCOVER
            onDiscover(hipRequest);

            // PUTTING ALL CARE CONTEXT and other details IN ABHA FLOW CHAIN.
            ABHAFlowChainRequest redisReq = new ABHAFlowChainRequest();
            redisReq.setCurrentKey(puid);
            redisReq.setHashKey(CARE_CONTEXT_KEY);
            redisReq.setBody(MAPPER.writeValueAsString(ccrList));
            helper.setAbhaData(redisReq);

            redisReq.setCurrentKey(puid);
            redisReq.setHashKey(HIP_DISPLAY);
            redisReq.setBody(fullName);
            helper.setAbhaData(redisReq);

            redisReq.setCurrentKey(puid);
            redisReq.setHashKey(MOBILE);
            redisReq.setBody(phoneNumber);
            helper.setAbhaData(redisReq);

            redisReq.setCurrentKey(puid);
            redisReq.setHashKey(ABHA_ID);
            redisReq.setBody(abhaNumber);
            helper.setAbhaData(redisReq);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method handles the discover request from HIP.
     * It retrieves the patient's details from the request,
     * matches the patient with the existing ABHA and PHR records,
     * and prepares the necessary data for linking.
     *
     * @param request The HIP webhook request containing the patient's details.
     * @return A BaseHIPModel object representing the response to the discover request.
     * @throws Exception If any error occurs during the processing of the request.
     */
    public ResponseEntity<BaseHIPModel> onDiscover(BaseHIPRequest request) {
        try {

            request.setRequestId(UUID.randomUUID().toString());
            request.setTimestamp(Helper.getIsoTimeStamp());

            System.out.println(" onDiscover : " + MAPPER.writeValueAsString(request));

            WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
            Mono<ResponseEntity<String>> response = client.post()
                    .uri("/gateway/v0.5/care-contexts/on-discover")
                    .header("Authorization", "Bearer " + helper.generateGatewayToken())
                    .header("X-CM-ID", ABDM_X_CM_ID)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .bodyValue(request).retrieve().toEntity(String.class);

            ResponseEntity<String> responseEntity = response.block();

            if (responseEntity.getStatusCode().is2xxSuccessful()) {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * This method handles the link init request from HIP.
     * It retrieves the patient's details from the request,
     * filters the approved care contexts, and prepares the necessary data for linking.
     *
     * @param request The HIP webhook request containing the patient's details.
     * @return A BaseHIPWebhookResponse object representing the response to the link init request.
     * @throws Exception If any error occurs during the processing of the request.
     */
    public BaseHIPWebhookResponse linkInit(BaseHIPWebhookRequest request) {
        try {
            BaseHIPRequest hipRequest = new BaseHIPRequest();
            hipRequest.setTransactionId(request.getTransactionId());

            Resp resp = new Resp();
            resp.setRequestId(request.getRequestId());
            hipRequest.setResp(resp);

            Patient patient = request.getPatient();
            String puid = patient.getReferenceNumber();
            hipRequest.setRequestId(puid);
            //  save approved care context in memory only (Filter care context) with PUID.

            List<CareContextRequset> careContexts = patient.getCareContexts();
            List<String> ccrIds = new ArrayList<>();
            for (CareContextRequset ccr : careContexts) {
                ccrIds.add(ccr.getReferenceNumber());
            }

            List<CareContextRequset> onDisCareCtx = MAPPER.readValue((String) redisTemplate.opsForHash().get(puid, CARE_CONTEXT_KEY), new TypeReference<>() {
            });

            List<CareContextRequset> finalCareCtxs = new ArrayList<>();
            for (CareContextRequset ccr : onDisCareCtx) {
                if (ccrIds.contains(ccr.getReferenceNumber()))
                    finalCareCtxs.add(ccr);
            }

            ABHAFlowChainRequest redisReq = new ABHAFlowChainRequest();
            redisReq.setCurrentKey(puid);
            redisReq.setHashKey(CARE_CONTEXT_KEY);
            redisReq.setBody(MAPPER.writeValueAsString(finalCareCtxs));
            helper.setAbhaData(redisReq);

            linkOnInit(hipRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method handles the link init request from HIP.
     * It retrieves the patient's details from the request,
     * filters the approved care contexts, and prepares the necessary data for linking.
     *
     * @param request The HIP webhook request containing the patient's details.
     * @return A ResponseEntity with a BaseHIPModel object representing the response to the link init request.
     * @throws Exception If any error occurs during the processing of the request.
     */
    public ResponseEntity<BaseHIPModel> linkOnInit(BaseHIPRequest request) {

        try {
            // get PUID storing OTP with this ref.
            String puid = request.getRequestId();

            request.setRequestId(UUID.randomUUID().toString());
            request.setTimestamp(Helper.getIsoTimeStamp());

            LocalDateTime futureDateTime = LocalDateTime.now().plusMinutes(30);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String formattedFutureTime = futureDateTime.format(formatter);

            Link linkReq = new Link();
            linkReq.setAuthenticationType(AuthMode.DIRECT.name());
            linkReq.setReferenceNumber(puid);

            Meta meta = new Meta();
            meta.setCommunicationMedium("MOBILE");
            meta.setCommunicationHint("string");
            meta.setCommunicationExpiry(formattedFutureTime);
            linkReq.setMeta(meta);

            request.setLink(linkReq);


            System.out.println(" linkOnInit : " + Helper.MAPPER.writeValueAsString(request));

            WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
            Mono<ResponseEntity<String>> response = client.post()
                    .uri("/gateway/v0.5/links/link/on-init")
                    .header("Authorization", "Bearer " + helper.generateGatewayToken())
                    .header("X-CM-ID", ABDM_X_CM_ID)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .bodyValue(request).retrieve().toEntity(String.class);

            ResponseEntity<String> responseEntity = response.block();

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                //  code for OTP gentration to yatri phone number
                String otp = generateOTP(6);
                Object mobile = redisTemplate.opsForHash().get(puid, MOBILE);
                if(!ObjectUtils.isEmpty(mobile)){
                    String m = mobile.toString();
                    Object display =   redisTemplate.opsForHash().get(puid, HIP_DISPLAY);

                    System.out.println("mobile : "+m);

                    NotificationRequestModel requestModel = new NotificationRequestModel();
                    // TODO NEED CHANGES IN TEMPLATE.
                    String subject = DEEP_LINK_SUBJECT;
                    String body = DEEP_LINK_TEMPLATE;
                    requestModel.setSubject(subject);
                    requestModel.setMessageBody(body.replaceAll(USER_NAME_MARCO, ObjectUtils.isEmpty(display) ? "yatri" : display.toString() ).replaceAll(OTP_MARCO, otp));
                    requestModel.setPhoneNumber(m.startsWith("+91") ? m : "+91" + m);
                    notificationUtils.sendSMS(requestModel);
                }

                // save the OTP in redis with puid.
                ABHAFlowChainRequest redisReq = new ABHAFlowChainRequest();
                redisReq.setCurrentKey(puid);
                redisReq.setHashKey(OTP);
                redisReq.setBody(otp);
                helper.setAbhaData(redisReq);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method handles the confirmation request from HIP.
     * It validates the OTP provided by the user with the stored OTP in Redis.
     * If the OTP is valid, it prepares the necessary data for linking the patient with the ABHA system.
     *
     * @param request The HIP webhook request containing the confirmation details.
     * @return A BaseHIPWebhookResponse object representing the response to the confirmation request.
     * @throws Exception If any error occurs during the processing of the request.
     */
    public BaseHIPWebhookResponse confirm(BaseHIPWebhookRequest request) {
        try {

            HIPConfirmation confirmation = request.getConfirmation();
            // OTP IS VAILD OR NOT WITH REF PUID FROM REDIS.
            Object otp = redisTemplate.opsForHash().get(confirmation.getLinkRefNumber(), OTP);
            String userOtp = confirmation.getToken();

            System.out.println("userOtp : "+userOtp);
            System.out.println("otp : "+otp);

            if(!ObjectUtils.isEmpty(otp) && otp.toString().equals(userOtp)){
                BaseHIPRequest hipRequest = new BaseHIPRequest();
                hipRequest.setRequestId(confirmation.getLinkRefNumber());

                Resp resp = new Resp();
                resp.setRequestId(request.getRequestId());
                hipRequest.setResp(resp);

                onConfirm(hipRequest);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method handles the confirmation request from HIP.
     * It validates the OTP provided by the user with the stored OTP in Redis.
     * If the OTP is valid, it prepares the necessary data for linking the patient with the ABHA system.
     *
     * @param request The HIP webhook request containing the confirmation details.
     * @return A BaseHIPWebhookResponse object representing the response to the confirmation request.
     * @throws Exception If any error occurs during the processing of the request.
     */
    public ResponseEntity<BaseHIPModel> onConfirm(BaseHIPRequest request) {

        try {

            String puid = request.getRequestId();
            List<CareContextRequset> careContexts = MAPPER.readValue((String) redisTemplate.opsForHash().get(puid, CARE_CONTEXT_KEY), new TypeReference<>() {
            });
            String display = (String) redisTemplate.opsForHash().get(puid, HIP_DISPLAY);

            request.setRequestId(UUID.randomUUID().toString());
            request.setTimestamp(Helper.getIsoTimeStamp());

            Patient patient = new Patient();
            patient.setReferenceNumber(puid);
            patient.setDisplay(display);
            patient.setCareContexts(careContexts);
            request.setPatient(patient);

            request.setError(null);

            System.out.println(" onConfirm : " + Helper.MAPPER.writeValueAsString(request));

            WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
            Mono<ResponseEntity<String>> response = client.post()
                    .uri("/gateway/v0.5/links/link/on-confirm")
                    .header("Authorization", "Bearer " + helper.generateGatewayToken())
                    .header("X-CM-ID", ABDM_X_CM_ID)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .bodyValue(request).retrieve().toEntity(String.class);

            ResponseEntity<String> responseEntity = response.block();

            if (responseEntity.getStatusCode().is2xxSuccessful()) {

                List<String> ccrIds = new ArrayList<>();
                for (CareContextRequset ccr : careContexts) {
                    ccrIds.add(ccr.getReferenceNumber());
                }

                List<CareContext> unLinkedCareCtxs = careContextRepo.findByIdsAndNullAbhaId(ccrIds);

                String abhaId = (String) redisTemplate.opsForHash().get(puid, ABHA_ID);

                if(!ObjectUtils.isEmpty(unLinkedCareCtxs)) {
                    String userName = unLinkedCareCtxs.get(0).getCreatedBy().getYatriUserName();
                    UserContext.setCurrentUserName(userName);
                    for (CareContext cCtx : unLinkedCareCtxs) {
                        cCtx.setAbhaId(abhaId);
                        cCtx.setPatientId(puid);
                    }
                    careContextRepo.saveAll(unLinkedCareCtxs);
                    UserContext.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    /**
     * This method handles the notifications received from the Health Information User (HIU).
     * It processes the notifications and updates the consent documents accordingly.
     *
     * @param request The webhook request containing the notification details.
     * @return A ResponseEntity with a status code indicating the success or failure of the operation.
     * @throws Exception If any error occurs during the processing of the request.
     */
    public ResponseEntity<BaseHIPModel> notifyUser(BaseHIPRequest request) {

        try {
            request.setRequestId(UUID.randomUUID().toString());
            request.setTimestamp(Helper.getIsoTimeStamp());

            HIPNotification hin = new HIPNotification();
            String phoneNumber = request.getYatriDetails().getPhoneNumber();
            phoneNumber = phoneNumber.startsWith("+91-") ? phoneNumber : "+91-"+phoneNumber;
            hin.setPhoneNo(phoneNumber);

            HIPRequester hip = new HIPRequester();
            hip.setName(HIP_NAME);
            hip.setId(HIP_ID);
            hin.setHip(hip);

            request.setNotification(hin);

//            System.out.println("notifyUser : "+ MAPPER.writeValueAsString(request));
//            WebClient client = helper.getWebClient().baseUrl(ABDM_HOST).build();
//            Mono<ResponseEntity<String>> response = client.post()
//                    .uri("/gateway/v0.5/patients/sms/notify2")
//                    .header("Authorization", "Bearer " + helper.generateGatewayToken())
//                    .header("X-CM-ID", ABDM_X_CM_ID)
//                    .header("Content-Type", "application/json")
//                    .header("Accept", "*/*")
//                    .bodyValue(request).retrieve().toEntity(String.class);
//
//            ResponseEntity<String> responseEntity = response.block();
//

//            if (responseEntity.getStatusCode().is2xxSuccessful()) {

                List<String> paths = new ArrayList<>();
                List<String> pathsId = new ArrayList<>();

                for (DocumentsPath docP : request.getDocumentsPathEntity()) {
                    paths.add(docP.getFilePath());
                    pathsId.add(docP.getId().toString());
                }

                CareContext careContext = new CareContext();
                careContext.setId(request.getCareContextId());

                careContext.setPatientId((PUID_PREFIX + UUID.randomUUID()));
                careContext.setDocumentPath(paths);
                careContext.setDocumentPathId(pathsId);
                careContext.setHiType(request.getDocumentType());
                careContext.setDocumentsDescription(request.getVisitPurpose());
                careContext.setAbhaId(null);
                careContextRepo.save(careContext);

                // DO NOT PERSIST THESE DETAILS IN DB.
                ABHAUserDetails abhaDetails = new ABHAUserDetails();
                abhaDetails.setFullName(request.getYatriDetails().getFullName());
                abhaDetails.setDateOfBirth(request.getYatriDetails().getDateOfBirth());
                abhaDetails.setGender(request.getYatriDetails().getGender());
                abhaDetails.setPhoneNumber(phoneNumber);
                //TODO : for S3
//                List<String> fhirPathList = fhirService.createFHIRBundle(request.getDocumentsPathEntity(), abhaDetails);
                List<String> fhirPathList = fhirService.createFHIRBundleVM(request.getDocumentsPathEntity(), abhaDetails);
                List<FhirDocuments> fhirDocumentsList = new ArrayList<>();
                fhirPathList.forEach(
                        path -> {
                            FhirDocuments fhirDocuments = new FhirDocuments();
                            Helper.updateFieldIfNotNull(fhirDocuments::setCareContextId, request.getCareContextId());
                            Helper.updateFieldIfNotNull(fhirDocuments::setPath, path);
                            fhirDocumentsList.add(fhirDocuments);
                        }
                );
                fhirDocumentsRepository.saveAll(fhirDocumentsList);
                System.out.println("FHIR Bundle Added -------------------- ");
                fhirDocumentsList.forEach(i->System.out.println(i.getPath()));
//            }


//            HttpHeaders headers = responseEntity.getHeaders();
//            String body = responseEntity.getBody();
//            try {
//                System.out.println(" headers " + MAPPER.writeValueAsString(headers));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//            System.out.println("body " + body);

            return  ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // FOR PORTAL
    public ResponseEntity<List<CareContext>> getCareContext(BaseHIPRequest request) {
        return ResponseEntity.ok(careContextRepo.findByUserAndNullAbhaId(UserContext.getCurrentUserName()));
    }
}
