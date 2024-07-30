package org.wishfoundation.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.userservice.config.AsyncConfig;
import org.wishfoundation.userservice.entity.*;
import org.wishfoundation.userservice.entity.repository.*;
import org.wishfoundation.userservice.enums.CareType;
import org.wishfoundation.userservice.enums.CreationType;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.enums.SmtpTemplate;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.VitalsRecordRequest;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.request.notification.NotificationRequestModel;
import org.wishfoundation.userservice.response.*;
import org.wishfoundation.userservice.response.abha.ABHAProfile;
import org.wishfoundation.userservice.response.abha.ABHAUserResponse;
import org.wishfoundation.userservice.utils.Helper;
import org.wishfoundation.userservice.utils.NotificationUtils;
import org.wishfoundation.userservice.utils.EnvironmentConfig;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

/**
 * This class is responsible for handling YatriPulseUser related operations.
 * It includes methods for creating, updating, and retrieving YatriPulseUser data.
 *
 */

@Service
public class EvaidyaYatriPulseUserServiceImpl implements EvaidyaYatriPulseUserService {

    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepo;

    @Autowired
    private YatriDetailsRepository yatriDetailsRepo;

    @Autowired
    private MedicalsReportsRepository medicalsReportsRepo;

    @Autowired
    private TourismUserInfoRepository tourismUserInfoRepository;

    @Autowired
    private ABHAUserDetailsRepository abhaUserDetailsRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationUtils notificationUtils;

    @Autowired
    private AsyncConfig asyncConfig;

    @Autowired
    private EvaidyaUserDetailsRepository evaidyaUserDetailsRepo;

    @Autowired
    private VitalsRecordRepository vitalsRecordRepository;

    @Autowired
    private YatriPulseUserLoginSignUpServiceImpl yatriPulseUserLoginSignUpService;

    /**
 * This method is used to get the Yatri details based on the provided username.
 * It retrieves the Yatri details from the database and populates the UserDetailsResponse object.
 * It checks for the existence of Yatri details, TourismUserDetails, and ABHAUserDetails.
 * If the details are found, it compares the full name, DOB, and gender from the YatriDetails and
 * overrides the values in the UserDetailsResponse object.
 *
 * @param userName The username of the Yatri for which the details need to be retrieved.
 * @return UserDetailsResponse object containing the Yatri details.
 * @throws WishFoundationException If the user is not found in the database.
 */
    @Override
    public UserDetailsResponse getYatriDetails(String userName) {
        //Get the yatri pulse user details from the database based on the username
        Optional<YatriPulseUsers> userOpt = yatriPulseUsersRepo.findUserByUserName(userName);
        //check whether user exists with the username
        if (userOpt.isEmpty())
            //throw WISHFOUNDATION exception if the user does not exist
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

        // If the user exists get the user from the optional
        YatriPulseUsers user = userOpt.get();
        // Create a UserDetailsResponse object to store the Yatri details for response
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();

        // Convert the YatriPulseUsers object to YatriPulseUserResponse object
        YatriPulseUserResponse yatriPulseUserResponse = Helper.MAPPER.convertValue(user, YatriPulseUserResponse.class);
        // Set the username and phone number in the UserDetailsResponse object
        Helper.updateFieldIfNotNull(userDetailsResponse::setUsername, yatriPulseUserResponse.getUserName());
        userDetailsResponse.setPhoneNumber(user.getPhoneNumber());

        // Check if the Yatri pulse user has any YatriDetails
        if (user.getYatriDetailsId() != null) {
            // Get the YatriDetails from the database
            YatriDetailsResponse yatriDetailsResponse = Helper.MAPPER.convertValue(yatriDetailsRepo.findById(user.getYatriDetailsId()).get(), YatriDetailsResponse.class);
            // Set the YatriDetails in the YatriPulseUserResponse object
            yatriPulseUserResponse.setYatriDetails(yatriDetailsResponse);
            // Compare and override the full name, gender, DOB, tour start date, tour end date, tour duration, address, state, and district in the UserDetailsResponse object
            Helper.updateFieldIfNotNull(userDetailsResponse::setFullName, yatriDetailsResponse.getFullName());
            Helper.updateFieldIfNotNull(userDetailsResponse::setGender, yatriDetailsResponse.getGender() != null ? yatriDetailsResponse.getGender().toString() : "");
            Helper.updateFieldIfNotNull(userDetailsResponse::setDateOfBirth, yatriDetailsResponse.getDateOfBirth());
            Helper.updateFieldIfNotNull(userDetailsResponse::setTourStartDate, yatriDetailsResponse.getTourStartDate());
            Helper.updateFieldIfNotNull(userDetailsResponse::setTourEndDate, yatriDetailsResponse.getTourEndDate());
            Helper.updateFieldIfNotNull(userDetailsResponse::setTourDuration, yatriDetailsResponse.getTourDuration());
            Helper.updateFieldIfNotNull(userDetailsResponse::setTourDuration, yatriDetailsResponse.getTourDuration());
            Helper.updateFieldIfNotNull(userDetailsResponse::setAddress, yatriDetailsResponse.getAddress());
            Helper.updateFieldIfNotNull(userDetailsResponse::setState, yatriDetailsResponse.getState());
            Helper.updateFieldIfNotNull(userDetailsResponse::setDistrict, yatriDetailsResponse.getDistrict());
            Helper.updateFieldIfNotNull(userDetailsResponse::setPinCode, yatriDetailsResponse.getPinCode());
            Helper.updateFieldIfNotNull(userDetailsResponse::setEmail, yatriDetailsResponse.getEmailId());
        }

        //Todo :  Don't change this order ... First IDTP details we will check then ABHA
        // Check if the Yatri has any TourismUserDetails
        if (!ObjectUtils.isEmpty(user.getTourismId())) {
            // Get the TourismUserDetails from the database if tourism id is available
            TourismUserDetails tourismUserDetails = Helper.MAPPER.convertValue(tourismUserInfoRepository.findById(user.getTourismId()).get(), TourismUserDetails.class);
            //override the full name, gender as fetched from the tourismUserDetails
            Helper.updateFieldIfNotNull(userDetailsResponse::setFullName, tourismUserDetails.getFullName());
            Helper.updateFieldIfNotNull(userDetailsResponse::setGender, tourismUserDetails.getGender().toString());
        }
        //Todo :  Don't change this order ... Check for ABHA details to override
        // Check if the Yatri has any ABHAUserDetails
        if (!ObjectUtils.isEmpty(user.getAbhaUserId())) {
            //Compare full name and DOB and gender
            ABHAUserDetails details = Helper.MAPPER.convertValue(abhaUserDetailsRepo.findById(user.getAbhaUserId()).get(), ABHAUserDetails.class);
            // Convert the ABHAUserDetails to ABHAUserResponse object
            ABHAUserResponse abhaUserDetails = Helper.MAPPER.convertValue(details, ABHAUserResponse.class);
            // Decrypt the ABHA number and PHR address
            abhaUserDetails.setAbhaNumber(Helper.decrypt(details.getAbhaNumber()));
            abhaUserDetails.setPhrAddress(Helper.decryptList(details.getPhrAddress()));
            // Compare and override the full name, DOB, gender, ABHA number, address, state, and district in the UserDetailsResponse object from the abhaUserDetails
            if (yatriPulseUserResponse.getYatriDetails() != null) {
                yatriPulseUserResponse.getYatriDetails().setFullName(abhaUserDetails.getFullName());
                Helper.updateFieldIfNotNull(userDetailsResponse::setFullName, abhaUserDetails.getFullName());
                Helper.updateFieldIfNotNull(userDetailsResponse::setDateOfBirth, abhaUserDetails.getDateOfBirth());
                Helper.updateFieldIfNotNull(userDetailsResponse::setGender, abhaUserDetails.getGender());
                Helper.updateFieldIfNotNull(userDetailsResponse::setAbhaNumber, abhaUserDetails.getAbhaNumber());
                Helper.updateFieldIfNotNull(userDetailsResponse::setAddress, abhaUserDetails.getAddress());
                Helper.updateFieldIfNotNull(userDetailsResponse::setState, abhaUserDetails.getStateName());
                Helper.updateFieldIfNotNull(userDetailsResponse::setDistrict, abhaUserDetails.getDistrictName());
                Helper.updateFieldIfNotNull(userDetailsResponse::setPinCode, abhaUserDetails.getPinCode());
                Helper.updateFieldIfNotNull(userDetailsResponse::setPhrAddress, abhaUserDetails.getPhrAddress());
                Helper.updateFieldIfNotNull(userDetailsResponse::setEmail, abhaUserDetails.getEmailId());
            }
        }
        return userDetailsResponse;
    }

    /**
 * This method is used to register a new Evaidya user to Yatri Pulse by generating an alphanumeric username and password.
 * The generated credentials are then shared with the phone number provided in the request.
 * @param yatriPulseUserRequest The request object containing the user details.
 * @return A ResponseEntity with the created EvaidyaUserResponse object.
 * @throws WishFoundationException If any required fields are missing or invalid.
 */
    @Override
    public ResponseEntity<EvaidyaUserResponse> signUp(YatriPulseUserRequest yatriPulseUserRequest) {

        // Check if Evaidya user ID is provided else throw WishFoundationException
        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getEvaidyaUserId()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "Evaidya's User Id" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);


        // Check if phone number is provided else throw WishFoundationException
        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getPhoneNumber()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "Phone number" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        if (yatriPulseUserLoginSignUpService.phoneNumberLinked(yatriPulseUserRequest.getPhoneNumber(), false).getLinkedWith() >= 100)

        // Check if the phone number is already linked to the maximum number of users per number
        if (yatriPulseUserLoginSignUpService.phoneNumberLinked(yatriPulseUserRequest.getPhoneNumber(), false).getLinkedWith() >= 6)
            throw new WishFoundationException(ErrorCode.PHONE_NUMBER_ALREADY_LINKED.getCode(),
                    ErrorCode.PHONE_NUMBER_ALREADY_LINKED.getMessage(), HttpStatus.CONFLICT);

        // Check if the Evaidya user ID already exists
        if (evaidyaUserDetailsRepo.existsByEvaidyaUserId(yatriPulseUserRequest.getEvaidyaUserId())) {
            throw new WishFoundationException(ErrorCode.EVAIDYA_USER_ID_ALREADY_EXISTS.getCode(),
                    ErrorCode.EVAIDYA_USER_ID_ALREADY_EXISTS.getMessage(), HttpStatus.CONFLICT);
        }

        // Create a new YatriPulseUsers object
        YatriPulseUsers yatriPulseUserItem = new YatriPulseUsers();

        // Generate a unique user name
        String userName = generateUniqueUserName();
        yatriPulseUserItem.setUserName(userName);
        yatriPulseUserItem.setPhoneNumber(yatriPulseUserRequest.getPhoneNumber());

        // Generate a random password and encrypt it
        String password = Helper.generatePassword(10);
        // DO NOT CHANGE IT . this is to keep backend and frontend in sync .
        yatriPulseUserItem.setPassword(passwordEncoder.encode(BCrypt.hashpw(password, EnvironmentConfig.SALT)));

        // Set the license agreement details
        yatriPulseUserItem.setLicenseAgreement(true);
        yatriPulseUserItem.setLicenseAgreementTime(Instant.now());
        yatriPulseUserItem.setCreationType(CreationType.EVAIDYA);
        yatriPulseUserItem.setCareType(CareType.CARE_GIVER);

        // Save the YatriPulseUsers details into the database
        yatriPulseUserItem = yatriPulseUsersRepo.save(yatriPulseUserItem);

        // Create a new EvaidyaUserDetails to store evaidya user details in the database
        EvaidyaUserDetails evaidyaUserDetailsEntity = new EvaidyaUserDetails();

        // Set the Evaidya user ID and other details in the EvaidyaUserDetails object
        evaidyaUserDetailsEntity.setEvaidyaUserId(yatriPulseUserRequest.getEvaidyaUserId());
        Helper.updateFieldIfNotNull(evaidyaUserDetailsEntity::setName, yatriPulseUserRequest.getName());
        Helper.updateFieldIfNotNull(evaidyaUserDetailsEntity::setDob, yatriPulseUserRequest.getDob());
        Helper.updateFieldIfNotNull(evaidyaUserDetailsEntity::setStatus, yatriPulseUserRequest.getStatus());

        // Map the Yatri pulse user and Evaidya user with each other
        evaidyaUserDetailsEntity.setYatriPulseUserId(yatriPulseUserItem.getId());
        evaidyaUserDetailsEntity = evaidyaUserDetailsRepo.save(evaidyaUserDetailsEntity);
        yatriPulseUserItem.setEvaidyaUserDetailsEntityId(evaidyaUserDetailsEntity.getId());

        // If any of the following  name, dob, or status are provided, create a new YatriDetails object
        if (yatriPulseUserRequest.getName() != null || yatriPulseUserRequest.getDob() != null || yatriPulseUserRequest.getStatus() != null) {

            // Create and Map the Yatri details with the Yatri pulse user
            YatriDetails yatriDetails = new YatriDetails();
            Helper.updateFieldIfNotNull(yatriDetails::setFullName, yatriPulseUserRequest.getName());
            Helper.updateFieldIfNotNull(yatriDetails::setDateOfBirth, yatriPulseUserRequest.getDob());
            yatriDetails.setYatriPulseUserId(yatriPulseUserItem.getId());
            yatriDetails = yatriDetailsRepo.save(yatriDetails);

            yatriPulseUserItem.setYatriDetailsId(yatriDetails.getId());

        }
        yatriPulseUsersRepo.save(yatriPulseUserItem);

        // If ABHA profile is provided, create a new ABHAUserDetails object for storing the ABHA user details in databse
        if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getAbhaProfile())) {

            ABHAUserDetails abhaUserDetails = new ABHAUserDetails();
            ABHAProfile abhaProfileRequest = yatriPulseUserRequest.getAbhaProfile();

            // Set the ABHA number and other details in the ABHAUserDetails object
            if (abhaProfileRequest.getAbhaNumber() != null)
                abhaUserDetails.setAbhaNumber(Helper.encrypt(abhaProfileRequest.getAbhaNumber()));

            // Set other details in the ABHAUserDetails object
            Helper.updateFieldIfNotNull(abhaUserDetails::setFirstName, abhaProfileRequest.getFirstName());
            Helper.updateFieldIfNotNull(abhaUserDetails::setMiddleName, abhaProfileRequest.getMiddleName());
            Helper.updateFieldIfNotNull(abhaUserDetails::setLastName, abhaProfileRequest.getLastName());
            Helper.updateFieldIfNotNull(abhaUserDetails::setDateOfBirth, abhaProfileRequest.getDob());
            Helper.updateFieldIfNotNull(abhaUserDetails::setGender, abhaProfileRequest.getGender());

            Helper.updateFieldIfNotNull(abhaUserDetails::setPhoneNumber, abhaProfileRequest.getMobile());
            Helper.updateFieldIfNotNull(abhaUserDetails::setEmailId, abhaProfileRequest.getEmail());

            if (abhaProfileRequest.getPhrAddress() != null)
                abhaUserDetails.setPhrAddress(Helper.encryptList(abhaProfileRequest.getPhrAddress()));


            Helper.updateFieldIfNotNull(abhaUserDetails::setAddress, abhaProfileRequest.getAddress());
            Helper.updateFieldIfNotNull(abhaUserDetails::setDistrictName, abhaProfileRequest.getDistrictName());
            Helper.updateFieldIfNotNull(abhaUserDetails::setDistrictCode, abhaProfileRequest.getDistrictCode());
            Helper.updateFieldIfNotNull(abhaUserDetails::setStateName, abhaProfileRequest.getStateName());
            Helper.updateFieldIfNotNull(abhaUserDetails::setStateCode, abhaProfileRequest.getStateCode());
            Helper.updateFieldIfNotNull(abhaUserDetails::setPinCode, abhaProfileRequest.getPinCode());
            Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaType, abhaProfileRequest.getAbhaType());
            Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaStatus, abhaProfileRequest.getAbhaStatus());
            Helper.updateFieldIfNotNull(abhaUserDetails::setFullName, abhaProfileRequest.getName());
            // Set the YatriPulseUsers ID in the ABHAUserDetails object
            abhaUserDetails.setYatriPulseUserId(yatriPulseUserItem.getId());

            // Save the ABHAUserDetails object
            yatriPulseUserItem.setAbhaUserId(abhaUserDetailsRepo.save(abhaUserDetails).getId());
            yatriPulseUsersRepo.save(yatriPulseUserItem);

        }


        //Need to change template letter.
        Function<NotificationRequestModel, Void> function = r -> {
            // Create a new NotificationRequestModel object
            NotificationRequestModel requestModel = new NotificationRequestModel();
            // Set the template ID, subject, and body in the NotificationRequestModel object
            String subject = "Here is your User name and password";
            String body = String.format(SmtpTemplate.TEMPLATE5.getTemplateValue(), userName, password);
            requestModel.setTemplateId(SmtpTemplate.TEMPLATE5.getTemplateId());
            requestModel.setSubject(subject);
            requestModel.setMessageBody(body);

            requestModel.setPhoneNumber(yatriPulseUserRequest.getPhoneNumber().startsWith("+91") ? yatriPulseUserRequest.getPhoneNumber()
                    : "+91" + yatriPulseUserRequest.getPhoneNumber());
            // Send the SMS using the notificationUtils service
            notificationUtils.sendSMSsmtp(requestModel);
            return null;
        };
        asyncConfig.execute(function, null);
        // Create a new EvaidyaUserResponse object
        EvaidyaUserResponse evaidyaUserResponse = new EvaidyaUserResponse();
        // Set the username and password in the EvaidyaUserResponse object
        evaidyaUserResponse.setUserName(userName);
        evaidyaUserResponse.setPassword(password);
        // Return the EvaidyaUserResponse object as a ResponseEntity
        return new ResponseEntity<EvaidyaUserResponse>(evaidyaUserResponse, HttpStatus.CREATED);

    }

 /**
 * Updates the Yatri Pulse user's profile with the provided details and, if available, updates the latest vitals.
 *
 * @param yatriPulseUserRequest The request containing the user's details to be updated.
 * @return A ResponseEntity with status OK if the update is successful.
 * @throws WishFoundationException If the user is not found or if the request is invalid.
 */
    @Override
    public ResponseEntity<Void> updateUser(YatriPulseUserRequest yatriPulseUserRequest) {

        // Check if the request is not null
        if (!ObjectUtils.isEmpty(yatriPulseUserRequest)) {

            // Fetch the user from the database based on the provided username and phone number
            YatriPulseUsers yatriPulseUserItem = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(yatriPulseUserRequest.getUserName(), yatriPulseUserRequest.getPhoneNumber());

            // Check if the user is found in the database
            if (!ObjectUtils.isEmpty(yatriPulseUserItem)) {

                // If name, dob, or status are provided in the request, update the corresponding fields in the database
                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getName()) || !ObjectUtils.isEmpty(yatriPulseUserRequest.getDob()) || !ObjectUtils.isEmpty(yatriPulseUserRequest.getStatus())) {
                    EvaidyaUserDetails evaidyaUserDetailsEntity = new EvaidyaUserDetails();

                    // If the user's EvaidyaUserDetails entity is not already present in the database, create a new one
                    if (ObjectUtils.isEmpty(yatriPulseUserItem.getEvaidyaUserDetailsEntityId())) {
                        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getEvaidyaUserId()))
                            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                                    "Evaidya's User Id" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

                        evaidyaUserDetailsEntity.setEvaidyaUserId(yatriPulseUserRequest.getEvaidyaUserId());
                        evaidyaUserDetailsEntity.setYatriPulseUserId(yatriPulseUserItem.getId());
                    } else
                        // If the user's EvaidyaUserDetails entity is already present in the database, fetch it
                        evaidyaUserDetailsEntity = evaidyaUserDetailsRepo.findById(yatriPulseUserItem.getEvaidyaUserDetailsEntityId()).get();

                    YatriDetails yatriDetails = new YatriDetails();

                    // If the user's YatriDetails entity is not already present in the database, create a new one
                    if (!ObjectUtils.isEmpty(yatriPulseUserItem.getYatriDetailsId()))
                        yatriDetails = yatriDetailsRepo.findById(yatriPulseUserItem.getYatriDetailsId()).get();

                    boolean createYatriDetailsFlag = false;

                    // Update the name field in both EvaidyaUserDetails and YatriDetails entities
                    if (ObjectUtils.isEmpty(evaidyaUserDetailsEntity.getName())) {
                        Helper.updateFieldIfNotNull(evaidyaUserDetailsEntity::setName, yatriPulseUserRequest.getName());
                        Helper.updateFieldIfNotNull(yatriDetails::setFullName, yatriPulseUserRequest.getName());
                        createYatriDetailsFlag = true;
                    }

                    // Update the dob field in both EvaidyaUserDetails and YatriDetails entities
                    if (ObjectUtils.isEmpty(evaidyaUserDetailsEntity.getDob())) {
                        Helper.updateFieldIfNotNull(evaidyaUserDetailsEntity::setDob, yatriPulseUserRequest.getDob());
                        Helper.updateFieldIfNotNull(yatriDetails::setDateOfBirth, yatriPulseUserRequest.getDob());
                        createYatriDetailsFlag = true;
                    }

                    // If any of the YatriDetails fields were updated, save the updated YatriDetails entity
                    if (createYatriDetailsFlag)
                        yatriDetails.setYatriPulseUserId(yatriPulseUserItem.getId());

                    // Update the status field in the EvaidyaUserDetails entity
                    Helper.updateFieldIfNotNull(evaidyaUserDetailsEntity::setStatus, yatriPulseUserRequest.getStatus());

                    // If the EvaidyaUserDetails entity was newly created, save it
                    if (ObjectUtils.isEmpty(yatriPulseUserItem.getEvaidyaUserDetailsEntityId())) {
                        evaidyaUserDetailsEntity = evaidyaUserDetailsRepo.save(evaidyaUserDetailsEntity);
                        yatriPulseUserItem.setEvaidyaUserDetailsEntityId(evaidyaUserDetailsEntity.getId());
                    }

                    // Save the updated YatriDetails entity
                    yatriDetails = yatriDetailsRepo.save(yatriDetails);

                    // If the YatriDetails entity was newly created, save it
                    if (ObjectUtils.isEmpty(yatriPulseUserItem.getYatriDetailsId())) {
                        yatriPulseUserItem.setYatriDetailsId(yatriDetails.getId());
                    }

                    // Save the updated YatriPulseUsers entity
                    yatriPulseUsersRepo.save(yatriPulseUserItem);
                }

                //Create a ABHAUserDertails optional object
                Optional<ABHAUserDetails> abhaUserDetailsExiting = null;
                //Check if ABHA Profile is provided in the request
                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getAbhaProfile())) {
                    ABHAUserDetails abhaUserDetails = new ABHAUserDetails();
                    //Check if ABHA User Id is present in the user Abha Details from database
                    if (!ObjectUtils.isEmpty(yatriPulseUserItem.getAbhaUserId()))
                        abhaUserDetailsExiting = abhaUserDetailsRepo.findById(yatriPulseUserItem.getAbhaUserId());
                    if (ObjectUtils.isEmpty(abhaUserDetailsExiting)) {
                        abhaUserDetails.setYatriPulseUserId(yatriPulseUserItem.getId());


                        ABHAProfile abhaProfileRequest = yatriPulseUserRequest.getAbhaProfile();
                        //Get the abha number for the abha profile and encrypt it before saving it to the database
                        if (!ObjectUtils.isEmpty(abhaProfileRequest.getAbhaNumber()))
                            abhaUserDetails.setAbhaNumber(Helper.encrypt(abhaProfileRequest.getAbhaNumber()));
                        //Update the remaining information
                        Helper.updateFieldIfNotNull(abhaUserDetails::setFirstName, abhaProfileRequest.getFirstName());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setMiddleName, abhaProfileRequest.getMiddleName());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setLastName, abhaProfileRequest.getLastName());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setDateOfBirth, abhaProfileRequest.getDob());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setGender, abhaProfileRequest.getGender());

                        Helper.updateFieldIfNotNull(abhaUserDetails::setPhoneNumber, abhaProfileRequest.getMobile());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setEmailId, abhaProfileRequest.getEmail());
                        //Get the PHR Address for the abha profile and encrypt it before saving it to the database
                        if (ObjectUtils.isEmpty(abhaProfileRequest.getPhrAddress()))
                            abhaUserDetails.setPhrAddress(Helper.encryptList(abhaProfileRequest.getPhrAddress()));


                        Helper.updateFieldIfNotNull(abhaUserDetails::setAddress, abhaProfileRequest.getAddress());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setDistrictName, abhaProfileRequest.getDistrictName());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setDistrictCode, abhaProfileRequest.getDistrictCode());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setStateName, abhaProfileRequest.getStateName());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setStateCode, abhaProfileRequest.getStateCode());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setPinCode, abhaProfileRequest.getPinCode());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaType, abhaProfileRequest.getAbhaType());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaStatus, abhaProfileRequest.getAbhaStatus());
                        Helper.updateFieldIfNotNull(abhaUserDetails::setFullName, abhaProfileRequest.getName());

                        //save the ABHA user details to the database
                        abhaUserDetails = abhaUserDetailsRepo.save(abhaUserDetails);

                        //Map the Abha Details with the user if newly created
                        if (ObjectUtils.isEmpty(yatriPulseUserItem.getAbhaUserId())) {
                            yatriPulseUserItem.setAbhaUserId(abhaUserDetails.getId());
                            yatriPulseUsersRepo.save(yatriPulseUserItem);
                        }
                    }
                }

                //check if the vitals information is present for the yatri pulse user
                if (!ObjectUtils.isEmpty(yatriPulseUserRequest.getVitalsRecord())) {

                    VitalsRecordRequest vitalsRecordRequest = yatriPulseUserRequest.getVitalsRecord();
                    VitalsRecord vitalsRecord = new VitalsRecord();
                    //check if the consultation is provided in the request
                    if (ObjectUtils.isEmpty(vitalsRecordRequest.getConsultationId())) {
                        throw new WishFoundationException("Consultation id" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                                ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.NOT_FOUND);
                    }
                    //check if vitals entry exists with the same consultation Id for this yatri pulse yatri
                    Optional<VitalsRecord> vitalsRecordOpt = vitalsRecordRepository.findByConsultationId(vitalsRecordRequest.getConsultationId(), yatriPulseUserItem.getId());
                    if (vitalsRecordOpt.isPresent())
                        vitalsRecord = vitalsRecordOpt.get();
                    else {
                        vitalsRecord.setYatriPulseUserId(yatriPulseUserItem.getId());
                        vitalsRecord.setConsultationId(vitalsRecordRequest.getConsultationId());
                    }

                    //update the vitals for the user
                    Helper.updateFieldIfNotNull(vitalsRecord::setSystolicBp, vitalsRecordRequest.getSystolicBp());
                    Helper.updateFieldIfNotNull(vitalsRecord::setDiastolicBp, vitalsRecordRequest.getDiastolicBp());
                    Helper.updateFieldIfNotNull(vitalsRecord::setMeanBp, vitalsRecordRequest.getMeanBp());
                    Helper.updateFieldIfNotNull(vitalsRecord::setHeartRate, vitalsRecordRequest.getHeartRate());
                    Helper.updateFieldIfNotNull(vitalsRecord::setSpo2, vitalsRecordRequest.getSpo2());
                    Helper.updateFieldIfNotNull(vitalsRecord::setTemperature, vitalsRecordRequest.getTemperature());
                    Helper.updateFieldIfNotNull(vitalsRecord::setTemperatureUnits, vitalsRecordRequest.getTemperatureUnits());
                    Helper.updateFieldIfNotNull(vitalsRecord::setTemperatureSource, vitalsRecordRequest.getTemperatureSource());
                    Helper.updateFieldIfNotNull(vitalsRecord::setEcg, vitalsRecordRequest.getEcg());
                    Helper.updateFieldIfNotNull(vitalsRecord::setHeight, vitalsRecordRequest.getHeight());
                    Helper.updateFieldIfNotNull(vitalsRecord::setHeightUnits, vitalsRecordRequest.getHeightUnits());
                    Helper.updateFieldIfNotNull(vitalsRecord::setWeight, vitalsRecordRequest.getWeight());
                    Helper.updateFieldIfNotNull(vitalsRecord::setWeightUnits, vitalsRecordRequest.getWeightUnits());
                    Helper.updateFieldIfNotNull(vitalsRecord::setBloodSugar, vitalsRecordRequest.getBloodSugar());
                    Helper.updateFieldIfNotNull(vitalsRecord::setLocation, vitalsRecordRequest.getLocation());
                    Helper.updateFieldIfNotNull(vitalsRecord::setBmi, vitalsRecordRequest.getBmi());
                    Helper.updateFieldIfNotNull(vitalsRecord::setAge, vitalsRecordRequest.getAge());
                    vitalsRecordRepository.save(vitalsRecord);

                }


            } else {
                // Throw an exception if the user is not found in the database
                throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                        ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);
            }
        } else {
            // Throw an exception if the request is invalid
            throw new WishFoundationException(ErrorCode.INVALID_REQUEST.getCode(),
                    ErrorCode.INVALID_REQUEST.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Return a ResponseEntity with status OK if the update is successful
        return new ResponseEntity<Void>(HttpStatus.OK);

    }

    /**
 * Generates a unique username for a new user.
 *
 * @return A unique username.
 */
    private String generateUniqueUserName() {
        String userName = "";

    // Loop until a unique username is generated.
        for (int i = 0; i < 5; i++) {
            // Generate a random alphanumeric string of length 11.
            userName = Helper.generateRandomAlphaNumeric(11);

            // Check if the generated username already exists in the database.
            // If it does not exist, break the loop and return the generated username.
            if (!yatriPulseUsersRepo.existsByUserName(userName)) {
                break;
            }
        }

        // Return the generated unique username.
        return userName;
    }


}
