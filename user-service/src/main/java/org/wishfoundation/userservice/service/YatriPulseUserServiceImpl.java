package org.wishfoundation.userservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.*;
import org.wishfoundation.userservice.entity.repository.*;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.enums.Gender;
import org.wishfoundation.userservice.enums.GovernmentIdType;
import org.wishfoundation.userservice.enums.SmtpTemplate;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.*;
import org.wishfoundation.userservice.request.notification.NotificationRequestModel;
import org.wishfoundation.userservice.response.*;
import org.wishfoundation.userservice.response.abha.ABHAUserResponse;
import org.wishfoundation.userservice.utils.AbhaApiUtils;
import org.wishfoundation.userservice.utils.Helper;
import org.wishfoundation.userservice.utils.NotificationUtils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.wishfoundation.userservice.utils.EnvironmentConfig.*;

@Service
@AllArgsConstructor
public class YatriPulseUserServiceImpl implements YatriPulseUserService {

    private final RedisTemplate redisTemplate;

    @Autowired
    private NotificationUtils notificationUtils;

    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepo;

    @Autowired
    private YatriDetailsRepository yatriDetailsRepo;

    @Autowired
    private MedicalsReportsRepository medicalsReportsRepo;

    @Autowired
    private DocumentsPathRepository documentsPathRepo;
    @Autowired
    private TourismUserInfoRepository tourismUserInfoRepository;

    @Autowired
    private ABHAUserDetailsRepository abhaUserDetailsRepo;

    @Autowired
    private VitalsRecordRepository vitalsRecordRepository;

    // FOR M2
    @Autowired
    private AbhaApiUtils abhaApiUtils;

    /**
     * Updates the YatriPulseUser with the provided YatriPulseUserRequest.
     *
     * @param yatriPulseUserRequest The request containing the updated YatriPulseUser information.
     * @param userName              The username of the YatriPulseUser to be updated.
     * @return A ResponseEntity with status OK if the update is successful.
     * @throws WishFoundationException If the user is not found or if any validation fails.
     */
    @Override
    public ResponseEntity<Void> updateYatriUser(YatriPulseUserRequest yatriPulseUserRequest, String userName) {

        if (!ObjectUtils.isEmpty(yatriPulseUserRequest)) {

            YatriPulseUsers userItem = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(userName, yatriPulseUserRequest.getPhoneNumber());

            if (!ObjectUtils.isEmpty(userItem)) {

                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getGovernmentIdType())
                        && ObjectUtils.isEmpty(yatriPulseUserRequest.getGovernmentId())) {
                    throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                            yatriPulseUserRequest.getGovernmentIdType() + " Id"
                                    + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);
                }

                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getGovernmentIdType()) && !ObjectUtils.isEmpty(yatriPulseUserRequest.getGovernmentId())) {
                    checkGovernmentIdValidation(yatriPulseUserRequest.getGovernmentId(), yatriPulseUserRequest.getGovernmentIdType());
                }

                Helper.updateFieldIfNotNull(userItem::setGovernmentIdType, yatriPulseUserRequest.getGovernmentIdType());

                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getGovernmentId())) {
                    if (yatriPulseUsersRepo.existsByGovernmentId(yatriPulseUserRequest.getGovernmentId()))
                        throw new WishFoundationException(ErrorCode.GOVERNMENT_ID_IS_ALREADY_LINKED.getCode(),
                                ErrorCode.GOVERNMENT_ID_IS_ALREADY_LINKED.getMessage(), HttpStatus.CONFLICT);

                    userItem.setGovernmentId(yatriPulseUserRequest.getGovernmentId());

                }

                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getYatriDetails())) {

                    YatriDetails yatriDetails = new YatriDetails();
                    if (userItem.getYatriDetailsId() != null)
                        yatriDetails = yatriDetailsRepo.findById(userItem.getYatriDetailsId()).orElseThrow();
                    else
                        yatriDetails.setYatriPulseUserId(UserContext.getUserId());

                    YatriDetailsRequest yatriDetailsRequest = yatriPulseUserRequest.getYatriDetails();

//                    Helper.updateFieldIfNotNull(yatriDetails::setFirstName, yatriDetailsRequest.getFirstName());
//                    Helper.updateFieldIfNotNull(yatriDetails::setLastName, yatriDetailsRequest.getLastName());
                    if (!ObjectUtils.isEmpty(yatriDetailsRequest.getFullName()) && ObjectUtils.isEmpty(yatriDetails.getFullName()))
                        yatriDetails.setFullName(yatriDetailsRequest.getFullName());

                    Helper.updateFieldIfNotNull(yatriDetails::setEmailId, yatriDetailsRequest.getEmailId());
                    Helper.updateFieldIfNotNull(yatriDetails::setGender, yatriDetailsRequest.getGender());
                    Helper.updateFieldIfNotNull(yatriDetails::setDateOfBirth, yatriDetailsRequest.getDateOfBirth());
                    if (!ObjectUtils.isEmpty(yatriDetailsRequest.getTourStartDate()) && !ObjectUtils.isEmpty(yatriDetailsRequest.getTourEndDate())) {
                        try {

                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date startDate = dateFormat.parse(yatriDetailsRequest.getTourStartDate());
                            Date endDate = dateFormat.parse(yatriDetailsRequest.getTourEndDate());
                            if (!(endDate.compareTo(startDate) >= 0)) {
                                throw new WishFoundationException(ErrorCode.END_DATE_MUST_BE_GREATER_THEN_START_DATE.getCode(),
                                        ErrorCode.END_DATE_MUST_BE_GREATER_THEN_START_DATE.getMessage(), HttpStatus.BAD_REQUEST);
                            }
                        } catch (Exception e) {
                            throw new WishFoundationException(ErrorCode.END_DATE_MUST_BE_GREATER_THEN_START_DATE.getCode(),
                                    ErrorCode.END_DATE_MUST_BE_GREATER_THEN_START_DATE.getMessage(), HttpStatus.BAD_REQUEST);
                        }
                    }
                    if (!ObjectUtils.isEmpty(yatriDetailsRequest.getPhoneNumber())) {
                        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getOtp()))
                            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                                    "OTP" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

                        OtpRequest otpRequest = new OtpRequest();

                        otpRequest.setPhoneNumber(yatriDetailsRequest.getPhoneNumber());
                        otpRequest.setUserName(userItem.getUserName());
                        otpRequest.setOtp(yatriPulseUserRequest.getOtp());
                        otpRequest.setTemplateKey(yatriPulseUserRequest.getTemplateKey());
                        verifyOTP(otpRequest);
                        yatriDetails.setPhoneNumber(yatriDetailsRequest.getPhoneNumber());
                    }
                    Helper.updateFieldIfNotNull(yatriDetails::setTourStartDate, yatriDetailsRequest.getTourStartDate());
                    Helper.updateFieldIfNotNull(yatriDetails::setTourEndDate, yatriDetailsRequest.getTourEndDate());
                    Helper.updateFieldIfNotNull(yatriDetails::setTourDuration, yatriDetailsRequest.getTourDuration());
                    Helper.updateFieldIfNotNull(yatriDetails::setAge, yatriDetailsRequest.getAge());
                    Helper.updateFieldIfNotNull(yatriDetails::setAddress, yatriDetailsRequest.getAddress());
                    Helper.updateFieldIfNotNull(yatriDetails::setPinCode, yatriDetailsRequest.getPinCode());
                    Helper.updateFieldIfNotNull(yatriDetails::setState, yatriDetailsRequest.getState());
                    Helper.updateFieldIfNotNull(yatriDetails::setDistrict, yatriDetailsRequest.getDistrict());
                    //Todo: Put validation on address,state,district,pinCode later
                    yatriDetails = yatriDetailsRepo.save(yatriDetails);

                    if (userItem.getYatriDetailsId() == null)
                        userItem.setYatriDetailsId(yatriDetails.getId());
                }

                // UPLOAD DOC & INIT ABHA M2
                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getDocumentsPath())) {
                    List<DocumentsPath> documentsPath = new ArrayList<>();
                    String careContextId = "visit-" + UUID.randomUUID();
                    yatriPulseUserRequest.getDocumentsPath().forEach(request -> {
                        DocumentsPath newDocument = new DocumentsPath();
                        Helper.updateFieldIfNotNull(newDocument::setFileName, request.getFileName());
                        Helper.updateFieldIfNotNull(newDocument::setFilePath, request.getFilePath());
                        Helper.updateFieldIfNotNull(newDocument::setMedicalDocumentType, yatriPulseUserRequest.getDocumentType());
                        Helper.updateFieldIfNotNull(newDocument::setHospitalLabName, yatriPulseUserRequest.getHospitalLabName());
                        Helper.updateFieldIfNotNull(newDocument::setVisitPurpose, yatriPulseUserRequest.getVisitPurpose());
                        newDocument.setYatriPulseUserId(UserContext.getUserId());
                        newDocument.setCareContextId(careContextId);
                        documentsPath.add(newDocument);

                    });

                    List<DocumentsPath> documentsPaths = documentsPathRepo.saveAll(documentsPath);
                    yatriPulseUserRequest.setDocumentsPathEntity(documentsPaths);
                    yatriPulseUserRequest.setCareContextId(careContextId);

                    // HITTING ABHA SERVICE CREATING CARE CONTEXT.
                    if (!ObjectUtils.isEmpty(userItem.getAbhaUserId())) {
                        // HIP INIT.
                        abhaApiUtils.uploadDocuments(yatriPulseUserRequest);
                    } else {
                        // USER INIT.
                        YatriDetails yd = yatriDetailsRepo.findById(userItem.getYatriDetailsId()).get();
                        YatriDetailsRequest yr = new YatriDetailsRequest();
                        yr.setPhoneNumber(userItem.getPhoneNumber());
                        yr.setGender(yd.getGender());
                        yr.setDateOfBirth(yd.getDateOfBirth());
                        yr.setFullName(yd.getFullName());
                        yatriPulseUserRequest.setYatriDetails(yr);
                        abhaApiUtils.notifyUser(yatriPulseUserRequest);
                    }
                }

                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getMedicalsReports())) {

                    MedicalsReports medicalsReports = new MedicalsReports();

                    if (userItem.getMedicalsReportsId() != null) {
                        Optional<MedicalsReports> medicalsReportsOpt = medicalsReportsRepo.findById(userItem.getMedicalsReportsId());
                        if (medicalsReportsOpt.isPresent())
                            medicalsReports = medicalsReportsOpt.get();
                    } else {
                        medicalsReports.setYatriPulseUserId(UserContext.getUserId());
                    }

                    MedicalsReportsRequest medicalsReportsRequest = yatriPulseUserRequest.getMedicalsReports();

                    Helper.updateFieldIfNotNull(medicalsReports::setHeartDisease,
                            medicalsReportsRequest.isHeartDisease());
                    Helper.updateFieldIfNotNull(medicalsReports::setHypertension,
                            medicalsReportsRequest.isHypertension());
                    Helper.updateFieldIfNotNull(medicalsReports::setRespiratoryDiseaseOrAsthma,
                            medicalsReportsRequest.isRespiratoryDiseaseOrAsthma());
                    Helper.updateFieldIfNotNull(medicalsReports::setDiabetesMellitus,
                            medicalsReportsRequest.isDiabetesMellitus());
                    Helper.updateFieldIfNotNull(medicalsReports::setEpilepsyOrAnyNeurologicalDisorder,
                            medicalsReportsRequest.isEpilepsyOrAnyNeurologicalDisorder());
                    Helper.updateFieldIfNotNull(medicalsReports::setKidneyOrUrinaryDisorder,
                            medicalsReportsRequest.isKidneyOrUrinaryDisorder());
                    Helper.updateFieldIfNotNull(medicalsReports::setCancer, medicalsReportsRequest.isCancer());
                    Helper.updateFieldIfNotNull(medicalsReports::setMigraineOrPersistentHeadache,
                            medicalsReportsRequest.isMigraineOrPersistentHeadache());
                    Helper.updateFieldIfNotNull(medicalsReports::setAnyAllergies,
                            medicalsReportsRequest.isAnyAllergies());
                    Helper.updateFieldIfNotNull(medicalsReports::setDisorderOfTheJointsOrMusclesArthritisGout,
                            medicalsReportsRequest.isDisorderOfTheJointsOrMusclesArthritisGout());
                    Helper.updateFieldIfNotNull(medicalsReports::setAnyMajorSurgery,
                            medicalsReportsRequest.isAnyMajorSurgery());
                    Helper.updateFieldIfNotNull(medicalsReports::setTuberculosis,
                            medicalsReportsRequest.isTuberculosis());
                    Helper.updateFieldIfNotNull(medicalsReports::setNoneOfTheAbove,
                            medicalsReportsRequest.isNoneOfTheAbove());


                    medicalsReports = medicalsReportsRepo.save(medicalsReports);

                    if (userItem.getMedicalsReportsId() == null)
                        userItem.setMedicalsReportsId(medicalsReports.getId());

                }
                yatriPulseUsersRepo.save(userItem);

            } else {
                throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                        ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);
            }
        }

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * This method deletes a yatri by their id.
     *
     * @param id The id of the yatri to be deleted.
     */
    @Override
    public void deleteYatri(String id) {
        yatriPulseUsersRepo.deleteById(Helper.parseUUIDFromString(id));
    }

    /**
     * This method retrieves the yatri details by their username and phone number.
     *
     * @param userName           The username of the yatri.
     * @param currentPhoneNumber The phone number of the yatri.
     * @return The yatri details.
     */
    public YatriPulseUserResponse getYatriPulseUser(String userName, String currentPhoneNumber) {
        YatriPulseUsers user = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(userName, currentPhoneNumber);

        if (ObjectUtils.isEmpty(user))
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

        YatriPulseUserResponse yatriPulseUserResponse = Helper.MAPPER.convertValue(user, YatriPulseUserResponse.class);

        if (user.getYatriDetailsId() != null) {
            YatriDetailsResponse yatriDetailsResponse = Helper.MAPPER.convertValue(yatriDetailsRepo.findById(user.getYatriDetailsId()).get(), YatriDetailsResponse.class);
            yatriPulseUserResponse.setYatriDetails(yatriDetailsResponse);
        }

        if (user.getMedicalsReportsId() != null) {
            MedicalsReportsResponse medicalsReportsResponse = Helper.MAPPER.convertValue(medicalsReportsRepo.findById(user.getMedicalsReportsId()).get(), MedicalsReportsResponse.class);
            yatriPulseUserResponse.setMedicalsReports(medicalsReportsResponse);
        }

        List<DocumentsPath> documentsPaths = documentsPathRepo.findByUserId(UserContext.getUserId());
        List<DocumentsPathResponse> documentsPathResponses = new ArrayList<>();
        if (!ObjectUtils.isEmpty(documentsPaths)) {
            documentsPaths.forEach(r ->
                    {
                        documentsPathResponses.add(Helper.MAPPER.convertValue(r, DocumentsPathResponse.class));
                    }
            );
            yatriPulseUserResponse.setDocumentsPath(documentsPathResponses);
        }
        //Todo :  Don't change this order ... First IDTP details we will check then ABHA
        if (!ObjectUtils.isEmpty(user.getTourismId())) {
            TourismUserDetails tourismUserDetails = Helper.MAPPER.convertValue(tourismUserInfoRepository.findById(user.getTourismId()).get(), TourismUserDetails.class);
            //Compare full name from YatriDetails full name
            if (yatriPulseUserResponse.getYatriDetails() != null) {
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setFullName, tourismUserDetails.getFullName());
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setGender, tourismUserDetails.getGender());
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setAddress, tourismUserDetails.getAddress());
            }
            yatriPulseUserResponse.setTourismUserInfo(tourismUserDetails);
        }
        //Todo :  Don't change this order ... Check for ABHA details to override
        if (!ObjectUtils.isEmpty(user.getAbhaUserId())) {
            //Compare full name and DOB and gender
            ABHAUserDetails details = abhaUserDetailsRepo.findById(user.getAbhaUserId()).get();
            ABHAUserResponse abhaUserDetails = Helper.MAPPER.convertValue(details, ABHAUserResponse.class);
            if (!ObjectUtils.isEmpty(details.getCreatedBy()) && details.getCreatedBy().getAuditOrganization().equalsIgnoreCase("YATRI_PULSE")) {
                abhaUserDetails.setAbhaVerified(true);
            }
            abhaUserDetails.setAbhaNumber(Helper.decrypt(details.getAbhaNumber()));
            abhaUserDetails.setPhrAddress(Helper.decryptList(details.getPhrAddress()));

            if (yatriPulseUserResponse.getYatriDetails() != null && abhaUserDetails.isAbhaVerified()) {
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setFullName, abhaUserDetails.getFullName());
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setDateOfBirth, abhaUserDetails.getDateOfBirth());
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setGender, Gender.valueOf(abhaUserDetails.getGender()));
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setAddress, abhaUserDetails.getAddress());
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setState, abhaUserDetails.getStateName());
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setDistrict, abhaUserDetails.getDistrictName());
                Helper.updateFieldIfNotNull(yatriPulseUserResponse.getYatriDetails()::setPinCode, abhaUserDetails.getPinCode());
            }
            yatriPulseUserResponse.setAbhaUserDetails(abhaUserDetails);
        }

        return yatriPulseUserResponse;
    }

    /**
     * This method sends an OTP to the specified phone number.
     *
     * @param otpRequest The request containing the phone number and template key.
     * @return The response containing the message and remaining attempts.
     */
    @Override
    public OtpResponse sendOTP(OtpRequest otpRequest) {
        String phoneNumber = otpRequest.getPhoneNumber();
        String rateLimitKey = RATE_LIMIT_PREFIX + otpRequest.getTemplateKey() + "_" + phoneNumber + "_"
                + otpRequest.getUserName();
        OtpResponse rateLimitResponse = isRateLimited(rateLimitKey);
        if (!rateLimitResponse.isRateLimitExceed()) {
            throw new WishFoundationException(ErrorCode.NO_ATTEMPTS_LEFT.getCode(),
                    ErrorCode.NO_ATTEMPTS_LEFT.getMessage(), HttpStatus.BAD_REQUEST);
        }
        // Generate OTP
        String userName = otpRequest.getUserName();
        String otpKey = OTP_PREFIX + otpRequest.getTemplateKey() + "_" + phoneNumber + "_" + userName;
        String otpCode = Helper.generateOTP(6);

        redisTemplate.opsForValue().set(otpKey, otpCode, Duration.ofMinutes(5));

        NotificationRequestModel requestModel = new NotificationRequestModel();

        String templateKey = otpRequest.getTemplateKey();
        if (StringUtils.hasLength(templateKey) && NotificationUtils.OTP_TEMPLATE_MAP.containsKey(templateKey)) {
            String subject = NotificationUtils.OTP_TEMPLATE_MAP.get(templateKey).split("#")[0];
            requestModel.setTemplateId(SmtpTemplate.TEMPLATE1.getTemplateId());
            String body = String.format(SmtpTemplate.TEMPLATE1.getTemplateValue(), otpRequest.getUserName(), otpCode);
            if (templateKey.equals("reset-password")) {
                body = String.format(SmtpTemplate.TEMPLATE3.getTemplateValue(), otpRequest.getUserName(), otpCode);
                requestModel.setTemplateId(SmtpTemplate.TEMPLATE3.getTemplateId());
            }
            requestModel.setSubject(subject);
            requestModel.setMessageBody(body);
            requestModel.setPhoneNumber(otpRequest.getPhoneNumber().startsWith("+91") ? otpRequest.getPhoneNumber()
                    : "+91" + otpRequest.getPhoneNumber());
            notificationUtils.sendSMSsmtp(requestModel);
        }
        return OtpResponse.builder()
                .message("OTP send To mobile number " + phoneNumber)
                .attemptLeft(RATE_LIMIT - rateLimitResponse.getAttemptLeft()).build();

    }

    /**
     * This method is used to verify the OTP sent to the user's mobile number.
     *
     * @param otpRequest The request containing the OTP, phone number, and template key.
     * @return An OtpResponse indicating whether the OTP was verified successfully or not.
     * @throws WishFoundationException If the OTP is invalid or not found in the Redis store.
     */
    @Override
    public OtpResponse verifyOTP(OtpRequest otpRequest) {
        if (ObjectUtils.isEmpty(otpRequest.getOtp()) || otpRequest.getOtp().length() != 6) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage(), HttpStatus.BAD_REQUEST);
        }

        String phoneNumber = otpRequest.getPhoneNumber();
        String userName = otpRequest.getUserName();
        String otpKey = OTP_PREFIX + otpRequest.getTemplateKey() + "_" + phoneNumber + "_" + userName;

        if (redisTemplate.hasKey(otpKey)) {
            String storedOTP = redisTemplate.opsForValue().get(otpKey).toString();
            if (storedOTP.equals(otpRequest.getOtp())) {
                // NOT VALID FOR STAGE 1
//                redisTemplate.delete(otpKey);
                return OtpResponse.builder().message("OTP verified successfully ").build();
            } else {
                throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method is used to resend the OTP to the user's mobile number.
     *
     * @param otpRequest The request containing the phone number, and template key.
     * @return An OtpResponse indicating whether the OTP was sent successfully or not.
     * @throws WishFoundationException If the rate limit for sending OTPs is exceeded.
     */
    public OtpResponse resendOTP(OtpRequest otpRequest) {
        String phoneNumber = otpRequest.getPhoneNumber();
        String userName = otpRequest.getUserName();
        String otpCode = "";
        String rateLimitKey = RATE_LIMIT_PREFIX + otpRequest.getTemplateKey() + "_" + phoneNumber + "_" + userName;
        OtpResponse rateLimitResponse = isRateLimited(rateLimitKey);
        if (!rateLimitResponse.isRateLimitExceed()) {
            throw new WishFoundationException(ErrorCode.NO_ATTEMPTS_LEFT.getCode(),
                    ErrorCode.NO_ATTEMPTS_LEFT.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
        }
        String otpKey = OTP_PREFIX + otpRequest.getTemplateKey() + "_" + phoneNumber + "_" + userName;

        if (redisTemplate.hasKey(otpKey)) {
            otpCode = redisTemplate.opsForValue().get(otpKey).toString();
        } else {
            otpCode = Helper.generateOTP(6);
        }

        redisTemplate.opsForValue().set(otpKey, otpCode, Duration.ofMinutes(5));

        NotificationRequestModel requestModel = new NotificationRequestModel();

        String templateKey = otpRequest.getTemplateKey();
        if (StringUtils.hasLength(templateKey) && NotificationUtils.OTP_TEMPLATE_MAP.containsKey(templateKey)) {
            String subject = NotificationUtils.OTP_TEMPLATE_MAP.get(templateKey).split("#")[0];
            requestModel.setTemplateId(SmtpTemplate.TEMPLATE1.getTemplateId());
            String body = String.format(SmtpTemplate.TEMPLATE1.getTemplateValue(), otpRequest.getUserName(), otpCode);
            if (templateKey.equals("reset-password")) {
                body = String.format(SmtpTemplate.TEMPLATE3.getTemplateValue(), otpRequest.getUserName(), otpCode);
                requestModel.setTemplateId(SmtpTemplate.TEMPLATE3.getTemplateId());
            }
            requestModel.setSubject(subject);
            requestModel.setMessageBody(body);
            requestModel.setPhoneNumber(otpRequest.getPhoneNumber().startsWith("+91") ? otpRequest.getPhoneNumber()
                    : "+91" + otpRequest.getPhoneNumber());
            notificationUtils.sendSMSsmtp(requestModel);
        }

        return OtpResponse.builder()
                .message("OTP send To mobile number " + phoneNumber)
                .attemptLeft(RATE_LIMIT - rateLimitResponse.getAttemptLeft()).build();
    }

    /**
     * This method checks if the rate limit for sending OTPs is exceeded.
     *
     * @param rateLimitKey The key to store the rate limit information in Redis.
     * @return An OtpResponse indicating whether the rate limit is exceeded and the remaining attempts.
     */
    private OtpResponse isRateLimited(String rateLimitKey) {
        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey, 0); // Check current value without incrementing

        if (currentCount == null) {
            redisTemplate.opsForValue().set(rateLimitKey, 1);
            currentCount = 1L;
        } else if (currentCount <= RATE_LIMIT) {
            redisTemplate.opsForValue().increment(rateLimitKey, 1);
            currentCount++;
        }

        redisTemplate.expire(rateLimitKey, RATE_LIMIT_PERIOD_SECONDS, TimeUnit.SECONDS);

        return OtpResponse.builder().rateLimitExceed(currentCount <= RATE_LIMIT).attemptLeft(currentCount).build();
    }

    /**
     * This method retrieves the list of supported government ID types.
     *
     * @return A DocumentTypeResponse containing the list of supported government ID types.
     */
    @Override
    public DocumentTypeResponse documentType() {
        DocumentTypeResponse documentTypeResponse = new DocumentTypeResponse();
        List<GovernmentIdType> documentTypeList = new ArrayList<>();
        Collections.addAll(documentTypeList, GovernmentIdType.values());
        documentTypeResponse.setDocumentType(documentTypeList);
        return documentTypeResponse;
    }

    /**
     * This method deletes the specified medical documents for the current user.
     *
     * @param documentsPathRequestList The list of DocumentsPathRequest objects containing the file names and file paths of the documents to delete.
     * @return A ResponseEntity indicating the success or failure of the deletion operation.
     */
    @Override
    public ResponseEntity<Void> deleteMedicalDocument(List<DocumentsPathRequest> documentsPathRequestList) {

        if (!ObjectUtils.isEmpty(documentsPathRequestList)) {
            documentsPathRequestList.forEach(r -> documentsPathRepo.deleteByFileNameAndUserId(r.getFileName(), r.getFilePath(), UserContext.getUserId()));
            return ResponseEntity.ok().build(); // Indicate success
        }
        return ResponseEntity.notFound().build(); // Indicate that user or request is not found
    }

    /**
     * This method checks the validity of the government ID based on the specified type.
     *
     * @param governmentId     The government ID to validate.
     * @param governmentIdType The type of government ID.
     * @throws WishFoundationException If the government ID is not valid for the specified type.
     */
    public void checkGovernmentIdValidation(String governmentId, GovernmentIdType governmentIdType) throws WishFoundationException {
        boolean isValid = false;
        switch (governmentIdType) {
            case PASSPORT:
                isValid = governmentId.matches("^[A-PR-WY-Z][1-9]\\d\\d{4}[1-9]$");
                break;
            case PAN_CARD:
                isValid = governmentId.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
                break;
            //Todo:Commenting aadhaar card validation
//        case AADHAR_CARD:
//            isValid = governmentId.matches("^[2-9]\\d{3}\\d{4}\\d{4}$");
//            break;
            case VOTER_ID_CARD:
                isValid = governmentId.matches("^[A-Z]{3}\\d{7}$") || governmentId.matches("^[A-Z]{3}[0-9]{7}$");
                break;
            case DRIVING_LICENSE:
                isValid = governmentId.matches("^(([A-Z]{2}[0-9]{2})|([A-Z]{2}-[0-9]{2}))((19|20)[0-9][0-9])[0-9]{7}$");
                break;
            default:
                break;
        }
        if (!isValid) {
            throwExceptionForDocumentType(governmentIdType);
        }
    }

    /**
     * This method throws an exception for the specified government ID type.
     *
     * @param governmentIdType The type of government ID.
     * @throws WishFoundationException If the government ID is not valid for the specified type.
     */
    private void throwExceptionForDocumentType(GovernmentIdType governmentIdType) throws WishFoundationException {
        switch (governmentIdType) {
            case PASSPORT:
                throw new WishFoundationException(ErrorCode.INVALID_PASSPORT.getCode(), ErrorCode.INVALID_PASSPORT.getMessage(), HttpStatus.BAD_REQUEST);
            case PAN_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_PAN_CARD.getCode(), ErrorCode.INVALID_PAN_CARD.getMessage(), HttpStatus.BAD_REQUEST);
//        case AADHAR_CARD:
//            throw new WishFoundationException(ErrorCode.INVALID_AADHAAR_NUMBER.getCode(), ErrorCode.INVALID_AADHAAR_NUMBER.getMessage(),HttpStatus.BAD_REQUEST);
            case VOTER_ID_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_VOTER_ID.getCode(), ErrorCode.INVALID_VOTER_ID.getMessage(), HttpStatus.BAD_REQUEST);
            case DRIVING_LICENSE:
                throw new WishFoundationException(ErrorCode.INVALID_DRIVING_LICENSE.getCode(), ErrorCode.INVALID_DRIVING_LICENSE.getMessage(), HttpStatus.BAD_REQUEST);
            default:
                throw new WishFoundationException(ErrorCode.INVALID_GOVERNMENT_ID.getCode(), ErrorCode.INVALID_GOVERNMENT_ID.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method retrieves the medical reports for the current user.
     *
     * @return The medical reports for the current user.
     */
    public MedicalsReportsResponse getMedicalReports() {
        MedicalsReports medicalsReports = medicalsReportsRepo.findMedicalReportsByUserId(UserContext.getUserId());
        if (ObjectUtils.isEmpty(medicalsReports)) {
            return new MedicalsReportsResponse();
        }
        return Helper.MAPPER.convertValue(medicalsReportsRepo.findMedicalReportsByUserId(UserContext.getUserId()), MedicalsReportsResponse.class);
    }

    /**
     * This method retrieves the user's detailed information based on the provided username and phone number.
     * It fetches the YatriPulseUser, YatriDetails, MedicalsReports, TourismUserDetails, and ABHAUserDetails for the user.
     * It then constructs a UserDetailsResponse object with the fetched information.
     * If the user is not found, it throws a WishFoundationException with the USER_IS_NOT_PRESENT error code.
     *
     * @param userName           The username of the user.
     * @param currentPhoneNumber The phone number of the user.
     * @return A UserDetailsResponse object containing the user's detailed information.
     * @throws WishFoundationException If the user is not found.
     */
    public UserDetailsResponse getYatriDetails(String userName, String currentPhoneNumber) {
        YatriPulseUsers user = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(userName, currentPhoneNumber);

        if (ObjectUtils.isEmpty(user))
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();

        YatriPulseUserResponse yatriPulseUserResponse = Helper.MAPPER.convertValue(user, YatriPulseUserResponse.class);
        Helper.updateFieldIfNotNull(userDetailsResponse::setUsername, yatriPulseUserResponse.getUserName());
        if (user.getYatriDetailsId() != null) {
            YatriDetailsResponse yatriDetailsResponse = Helper.MAPPER.convertValue(yatriDetailsRepo.findById(user.getYatriDetailsId()).get(), YatriDetailsResponse.class);
            yatriPulseUserResponse.setYatriDetails(yatriDetailsResponse);
            Helper.updateFieldIfNotNull(userDetailsResponse::setFullName, yatriDetailsResponse.getFullName());
            Helper.updateFieldIfNotNull(userDetailsResponse::setDateOfBirth, yatriDetailsResponse.getDateOfBirth());
            Helper.updateFieldIfNotNull(userDetailsResponse::setTourStartDate, yatriDetailsResponse.getTourStartDate());
            Helper.updateFieldIfNotNull(userDetailsResponse::setTourEndDate, yatriDetailsResponse.getTourEndDate());
            Helper.updateFieldIfNotNull(userDetailsResponse::setTourDuration, yatriDetailsResponse.getTourDuration());
            Helper.updateFieldIfNotNull(userDetailsResponse::setAddress, yatriDetailsResponse.getAddress());
            Helper.updateFieldIfNotNull(userDetailsResponse::setState, yatriDetailsResponse.getState());
            Helper.updateFieldIfNotNull(userDetailsResponse::setDistrict, yatriDetailsResponse.getDistrict());
            Helper.updateFieldIfNotNull(userDetailsResponse::setPinCode, yatriDetailsResponse.getPinCode());
            Helper.updateFieldIfNotNull(userDetailsResponse::setEmail, yatriDetailsResponse.getEmailId());
        }

        if (user.getMedicalsReportsId() != null) {
            MedicalsReportsResponse medicalsReportsResponse = Helper.MAPPER.convertValue(medicalsReportsRepo.findById(user.getMedicalsReportsId()).get(), MedicalsReportsResponse.class);
            userDetailsResponse.setMedicalsReports(medicalsReportsResponse);
        }

        //Todo :  Don't change this order ... First IDTP details we will check then ABHA
        if (!ObjectUtils.isEmpty(user.getTourismId())) {
            TourismUserDetails tourismUserDetails = Helper.MAPPER.convertValue(tourismUserInfoRepository.findById(user.getTourismId()).get(), TourismUserDetails.class);
            //Compare full name from YatriDetails full name
            Helper.updateFieldIfNotNull(userDetailsResponse::setFullName, tourismUserDetails.getFullName());
            Helper.updateFieldIfNotNull(userDetailsResponse::setGender, tourismUserDetails.getGender().toString());
            Helper.updateFieldIfNotNull(userDetailsResponse::setAddress, tourismUserDetails.getAddress());
        }
        //Todo :  Don't change this order ... Check for ABHA details to override
        if (!ObjectUtils.isEmpty(user.getAbhaUserId())) {
            //Compare full name and DOB and gender
            ABHAUserDetails abhaUserDetails = Helper.MAPPER.convertValue(abhaUserDetailsRepo.findById(user.getAbhaUserId()).get(), ABHAUserDetails.class);
            if (yatriPulseUserResponse.getYatriDetails() != null) {
                Helper.updateFieldIfNotNull(userDetailsResponse::setFullName, abhaUserDetails.getFullName());
                Helper.updateFieldIfNotNull(userDetailsResponse::setDateOfBirth, abhaUserDetails.getDateOfBirth());
                Helper.updateFieldIfNotNull(userDetailsResponse::setGender, abhaUserDetails.getGender());
                Helper.updateFieldIfNotNull(userDetailsResponse::setAddress, abhaUserDetails.getAddress());
                Helper.updateFieldIfNotNull(userDetailsResponse::setState, abhaUserDetails.getStateName());
                Helper.updateFieldIfNotNull(userDetailsResponse::setDistrict, abhaUserDetails.getDistrictName());
                Helper.updateFieldIfNotNull(userDetailsResponse::setPinCode, abhaUserDetails.getPinCode());
            }
        }
        return userDetailsResponse;
    }

    /**
     * This method retrieves the vitals record for the current user.
     * It fetches the VitalsRecordResponse from the vitalsRecordRepository for the user.
     * If the vitals record is not found, it throws a WishFoundationException with the VITALS_ARE_NOT_PRESENT error code.
     *
     * @return A VitalsRecordResponse object containing the vitals record for the current user.
     * @throws WishFoundationException If the vitals record is not found.
     */
    @Override
    public VitalsRecordResponse fetchVitalsRecord() {

        Optional<VitalsRecordResponse> vitalsRecordOpt = vitalsRecordRepository.fetchVitalsResponse(UserContext.getUserId());

        if (vitalsRecordOpt.isPresent()) {
            VitalsRecordResponse vitalsRecordResp = vitalsRecordOpt.get();
            vitalsRecordResp.setBloodPressureFullValue((vitalsRecordResp.getSystolicBp() != null ? vitalsRecordResp.getSystolicBp() : "") + "/" + (vitalsRecordResp.getDiastolicBp() != null ? vitalsRecordResp.getDiastolicBp() : ""));
            return vitalsRecordResp;
        } else
            throw new WishFoundationException(ErrorCode.VITALS_ARE_NOT_PRESENT.getCode(),
                    ErrorCode.VITALS_ARE_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

    }
}
