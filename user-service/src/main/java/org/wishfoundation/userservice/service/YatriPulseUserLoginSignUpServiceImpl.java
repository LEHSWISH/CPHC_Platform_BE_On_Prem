package org.wishfoundation.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.wishfoundation.userservice.config.AsyncConfig;
import org.wishfoundation.userservice.entity.YatriDetails;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.TourismUserInfoRepository;
import org.wishfoundation.userservice.entity.repository.YatriDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.*;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.ExcelUserCreationRequest;
import org.wishfoundation.userservice.request.OtpRequest;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.request.notification.NotificationRequestModel;
import org.wishfoundation.userservice.response.LoginResponse;
import org.wishfoundation.userservice.response.PhoneNumberLinkedResponse;
import org.wishfoundation.userservice.response.UserAuthDetailsResp;
import org.wishfoundation.userservice.security.JWTService;
import org.wishfoundation.userservice.utils.EnvironmentConfig;
import org.wishfoundation.userservice.utils.Helper;
import org.wishfoundation.userservice.utils.NotificationUtils;
import org.wishfoundation.userservice.utils.YatriUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;

import static org.wishfoundation.userservice.utils.EnvironmentConfig.*;

/**
 * This class implements the YatriPulseUserLoginSignUpService interface, which provides methods for user login, signup, and other related functionalities.
 */
@Service
public class YatriPulseUserLoginSignUpServiceImpl implements YatriPulseUserLoginSignUpService {

    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTService jwtService;

    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepo;

    @Autowired
    private YatriDetailsRepository yatriDetailsRepo;

    @Autowired
    private TourismUserInfoRepository tourismUserInfoRepository;

    @Autowired
    private YatriPulseUserServiceImpl yatriPulseUserServiceImpl;

    @Autowired
    private NotificationUtils notificationUtils;

    @Autowired
    private YatriUtils yatriUtils;

    @Autowired
    private RedisTemplate redis;

    @Autowired
    private AsyncConfig asyncConfig;

    /**
     * This method handles the login process for YatriPulseUsers.
     * It validates the user's credentials, generates a JWT token, and returns the token along with the user's details.
     *
     * @param yatriPulseUserRequest The request object containing the user's credentials and session ID.
     * @return ResponseEntity containing the login response with the JWT token and user details.
     * @throws WishFoundationException If any validation or authentication fails, a WishFoundationException is thrown.
     */
    @Override
    public ResponseEntity<LoginResponse> login(YatriPulseUserRequest yatriPulseUserRequest) {
        LoginResponse model = new LoginResponse();
        try {
            // Validate session ID
            if (ObjectUtils.isEmpty(yatriPulseUserRequest.getSessionId()))
                throw new WishFoundationException(ErrorCode.UNABLE_TO_GET_SESSION_ID.getCode(),
                        ErrorCode.UNABLE_TO_GET_SESSION_ID.getMessage(), HttpStatus.NOT_FOUND);

            // Decrypt the password
            String password = Helper.decryptPassword(yatriPulseUserRequest.getPassword());

            // Split the decrypted password into time and hashed password
            String[] PassAndTime = password.split("%");
            OffsetDateTime offsetDateTime = Instant.now().atOffset(ZoneOffset.UTC);
            OffsetDateTime dateTime = OffsetDateTime.parse(PassAndTime[0]);

            // Check if the password is valid within the time limit
            long diff = offsetDateTime.toEpochSecond() - dateTime.toEpochSecond();
            if (diff < 20) {
                // Check if the password is already in use within the time limit
                if (redis.hasKey(password))
                    throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "Invaild Password!!", HttpStatus.BAD_REQUEST);
                try {
                    // Store the password in Redis for future reference
                    redis.opsForValue().set(password, 1);
                    redis.expire(password, Duration.ofSeconds(20));
                } catch (Exception e) {
                }
            } else {
                // Throw an exception if the password is not valid within the time limit
                throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "Invaild Password!", HttpStatus.BAD_REQUEST);
            }

            // Use the decrypted hashed password for authentication
            password = PassAndTime[1];
            System.out.println("HASHED password : " + password);

            // Create a UsernamePasswordAuthenticationToken for authentication
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    yatriPulseUserRequest.getUserName(), password);
            Authentication authentication = authenticationProvider.authenticate(usernamePasswordAuthenticationToken);

            // Retrieve the user's details from the database
            Optional<UserAuthDetailsResp> user = yatriPulseUsersRepo.findUserAuthDetailsByUserName(yatriPulseUserRequest.getUserName());

            // Throw an exception if the user is not found in the database
            if (user.isEmpty())
                throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                        ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

            // Extract the user's phone number
            String phoneNumber = user.get().getPhoneNumber();

            // Generate a JWT token for the user
            String token = jwtService.generateToken(authentication, phoneNumber, yatriPulseUserRequest.getSessionId());

            // Set the token, user name, and phone number in the response object
            model.setToken(token);
            model.setUserName(yatriPulseUserRequest.getUserName());
            model.setPhoneNumber(phoneNumber);

            // Return the response object with the JWT token and user details
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (Exception e) {
            // Handle any exceptions that occur during the login process
            e.printStackTrace();
            model.setMessage(e.getMessage());
            return new ResponseEntity<>(model, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Registers a new YatriPulseUser.
     *
     * @param yatriPulseUserRequest The request object containing the user's details.
     * @return ResponseEntity with HTTP status code 201 (Created) if the user is successfully registered.
     * @throws WishFoundationException If the user name or phone number is already taken.
     */
    @Override
    public ResponseEntity<Void> registerYatri(YatriPulseUserRequest yatriPulseUserRequest) {

        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getUserName()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "User name" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getPhoneNumber()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "Phone number" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        if (!ObjectUtils.isEmpty(yatriPulseUserRequest.isLicenseAgreement())
                && !yatriPulseUserRequest.isLicenseAgreement())
            throw new WishFoundationException(ErrorCode.ACCEPT_POLICES.getCode(),
                    ErrorCode.ACCEPT_POLICES.getMessage(), HttpStatus.BAD_REQUEST);

        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getOtp()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "OTP" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        if (this.phoneNumberLinked(yatriPulseUserRequest.getPhoneNumber(), false).getLinkedWith() >= 100)
            throw new WishFoundationException(ErrorCode.PHONE_NUMBER_ALREADY_LINKED.getCode(),
                    ErrorCode.PHONE_NUMBER_ALREADY_LINKED.getMessage(), HttpStatus.CONFLICT);

        OtpRequest otpRequest = new OtpRequest();

        otpRequest.setPhoneNumber(yatriPulseUserRequest.getPhoneNumber());
        otpRequest.setUserName(yatriPulseUserRequest.getUserName());
        otpRequest.setOtp(yatriPulseUserRequest.getOtp());
        otpRequest.setTemplateKey(yatriPulseUserRequest.getTemplateKey());

        yatriPulseUserServiceImpl.verifyOTP(otpRequest);

        if (yatriPulseUsersRepo.existsByUserNameAndPhoneNumber(yatriPulseUserRequest.getUserName(), yatriPulseUserRequest.getPhoneNumber())) {
            throw new WishFoundationException(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(),
                    ErrorCode.USERNAME_ALREADY_EXISTS.getMessage(), HttpStatus.CONFLICT);
        }

        YatriPulseUsers yatriPulseUserItem = new YatriPulseUsers();

        yatriPulseUserItem.setUserName(yatriPulseUserRequest.getUserName());
        yatriPulseUserItem.setPhoneNumber(yatriPulseUserRequest.getPhoneNumber());

        String password = Helper.decryptPassword(yatriPulseUserRequest.getPassword());
        String[] PassAndTime = password.split("%");
        yatriPulseUserItem.setPassword(passwordEncoder.encode(PassAndTime[1]));

        yatriPulseUserItem.setLicenseAgreement(yatriPulseUserRequest.isLicenseAgreement());
        yatriPulseUserItem.setLicenseAgreementTime(Instant.now());
        yatriPulseUserItem.setCareType(CareType.CARE_GIVER);
        yatriPulseUserItem.setCreationType(CreationType.YATRI_PULSE);
        yatriPulseUsersRepo.save(yatriPulseUserItem);

        // EMAIL AFTER SIGN UP COMPLETED
        Function<NotificationRequestModel, Void> function = r -> {
            NotificationRequestModel requestModel = new NotificationRequestModel();
            String subject = AT_SIGN_UP_SUBJECT;
            String body = SmtpTemplate.TEMPLATE2.getTemplateValue();
            requestModel.setTemplateId(SmtpTemplate.TEMPLATE2.getTemplateId());
            requestModel.setSubject(subject);
            requestModel.setMessageBody(String.format(body, otpRequest.getUserName(), otpRequest.getUserName()));

            // TODO IN STAGE 2
//            requestModel.setEmailTo(new HashSet<>(Arrays.asList(yatriPulseUserRequest.getYatriDetails().getEmailId())));
            requestModel.setPhoneNumber(otpRequest.getPhoneNumber().startsWith("+91") ? otpRequest.getPhoneNumber()
                    : "+91" + otpRequest.getPhoneNumber());
            notificationUtils.sendSMSsmtp(requestModel);
            return null;
        };

        // EMAIL IF TP ID NOT AVAILABLE
        Function<NotificationRequestModel, Void> functionLinkIdEmail = r -> {
            NotificationRequestModel requestModel = new NotificationRequestModel();
            String subject = LINK_PORTAL_ID_SUBJECT;
            String body = LINK_PORTAL_ID_TEMPLATE;
            requestModel.setSubject(subject);
            requestModel.setMessageBody(body.replaceAll(EnvironmentConfig.USER_NAME_MARCO, otpRequest.getUserName())
                    .replaceAll(LINK_MARCO, YATRI_PULE_LINK).replaceAll(YATRI_ID_MARCO, otpRequest.getUserName()));
            // TODO IN STAGE 2
//            requestModel.setEmailTo(new HashSet<>(Arrays.asList(yatriPulseUserRequest.getYatriDetails().getEmailId())));
            notificationUtils.sendEmail(requestModel);
            return null;
        };
        // TODO uncomment when email is needed.
//		asyncConfig.execute(functionLinkIdEmail, null);
        asyncConfig.execute(function, null);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }

    /**
     * This method registers multiple YatriPulseUsers from an Excel file.
     * It validates the data, checks for existing users, and handles exceptions.
     * It also sends SMS notifications to the registered users.
     *
     * @param yatriPulseUserRequestList List of ExcelUserCreationRequest objects containing user data.
     * @return ResponseEntity containing a HashMap of exceptions and their respective messages.
     */
    public ResponseEntity<HashMap> excelRegisterYatri(ArrayList<ExcelUserCreationRequest> yatriPulseUserRequestList) {
        HashMap<HashMap, String> exceptionMap = new HashMap<>();

        // Iterate over the list of ExcelUserCreationRequest objects
        for (ExcelUserCreationRequest yatriPulseUser : yatriPulseUserRequestList) {

            // Extract phone number and IDTP from the ExcelUserCreationRequest object
            String number = yatriPulseUser.getMobileNo();
            String IDTP = yatriPulseUser.getUniqueCode();

            // Validate and handle exceptions for IDTP
            if (!StringUtils.hasLength(IDTP) || IDTP.equals("null")) {
                HashMap<String, String> identifiermap = new HashMap<>();
                identifiermap.put("Phone Number ", number);
                exceptionMap.put(identifiermap, "Toursim Portal Id" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.formatMessage(String.valueOf(0)));
                continue;
            }
            if (!StringUtils.hasLength(number) || number.equals("null")) {
                HashMap<String, String> identifiermap = new HashMap<>();
                identifiermap.put("Phone Number ", number);
                exceptionMap.put(identifiermap, "Phone Number " + ErrorCode.VALUE_SHOULD_BE_PROVIDED.formatMessage(String.valueOf(0)));
                continue;
            }

            // Check if the IDTP already exists in the database
            if (tourismUserInfoRepository.existsByIdtpId(IDTP)) {
                HashMap<String, String> identifiermap = new HashMap<>();
                identifiermap.put("Tourism Portal Id", IDTP);
                exceptionMap.put(identifiermap, ErrorCode.IDTP_IS_ALREADY_LINKED.formatMessage(String.valueOf(0)));
                continue;
            }

            // Check if the phone number is already linked with 100 or more users
            if (this.phoneNumberLinked(number, false).getLinkedWith() >= 100) {
                HashMap<String, String> identifiermap = new HashMap<>();
                identifiermap.put("Phone Number ", number);
                exceptionMap.put(identifiermap, ErrorCode.PHONE_NUMBER_ALREADY_LINKED.getMessage());
                continue;
            }

            // Create a new YatriPulseUsers object
            YatriPulseUsers yatriPulseUserItem = new YatriPulseUsers();
            String userName = generateUniqueUserName();
            yatriPulseUserItem.setUserName(userName);
            String password = Helper.generatePassword(10);

            // Encrypt the password using BCrypt and store it in the database
            // do not remove this required to keep front end And Backend Encryption in sync
            yatriPulseUserItem.setPassword(passwordEncoder.encode(BCrypt.hashpw(password, SALT)));

            yatriPulseUserItem.setPhoneNumber(number);
            yatriPulseUserItem.setLicenseAgreement(true);
            yatriPulseUserItem.setLicenseAgreementTime(Instant.now());
            yatriPulseUserItem.setCareType(CareType.CARE_GIVER);
            yatriPulseUserItem.setCreationType(CreationType.TOURISM_EXCEL);

            // Save the YatriPulseUsers object in the database
            YatriPulseUsers userItem = yatriPulseUsersRepo.save(yatriPulseUserItem);

            // Create a new YatriDetails object
            YatriDetails yatriDetails = new YatriDetails();
            String fullName = yatriPulseUser.getPassengerName();
            yatriDetails.setFullName(fullName);
            yatriDetails.setYatriPulseUserId(userItem.getId());
            Helper.updateFieldIfNotNull(yatriDetails::setEmailId, yatriPulseUser.getEmail());
            // Set other YatriDetails fields using the data from the ExcelUserCreationRequest object
            if (StringUtils.hasLength(yatriPulseUser.getGender())) {
                Helper.updateFieldIfNotNull(yatriDetails::setGender, Gender.valueOf(yatriPulseUser.getGender()));
            }

            Date startDate = new Date();
            Date endDate = new Date();
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");

            if (!ObjectUtils.isEmpty(yatriPulseUser.getTourStartDate()) && !ObjectUtils.isEmpty(yatriPulseUser.getTourEndDate())) {
                try {

                    startDate = inputDateFormat.parse(yatriPulseUser.getTourStartDate());
                    endDate = inputDateFormat.parse(yatriPulseUser.getTourEndDate());

                    if (!(endDate.compareTo(startDate) >= 0)) {

                        HashMap<String, String> identifiermap = new HashMap<>();
                        identifiermap.put("IDTP ID", IDTP);
                        exceptionMap.put(identifiermap, ErrorCode.END_DATE_MUST_BE_GREATER_THEN_START_DATE.getMessage());

                        continue;
                    }
                } catch (Exception e) {

                    HashMap<String, String> identifiermap = new HashMap<>();
                    identifiermap.put("IDTP ID", IDTP);
                    exceptionMap.put(identifiermap, ErrorCode.END_DATE_MUST_BE_GREATER_THEN_START_DATE.getMessage());
                    continue;
                }
            }

            Helper.updateFieldIfNotNull(yatriDetails::setTourStartDate, outputDateFormat.format(startDate));
            Helper.updateFieldIfNotNull(yatriDetails::setTourEndDate, outputDateFormat.format(endDate));
            Helper.updateFieldIfNotNull(yatriDetails::setTourDuration, calculateDuration(startDate, endDate));
            Helper.updateFieldIfNotNull(yatriDetails::setPhoneNumber, number);
            Helper.updateFieldIfNotNull(yatriDetails::setAddress, yatriPulseUser.getAddress());
            Helper.updateFieldIfNotNull(yatriDetails::setState, yatriPulseUser.getStateName());
            Helper.updateFieldIfNotNull(yatriDetails::setDistrict, yatriPulseUser.getDistrictName());
            yatriDetailsRepo.save(yatriDetails);
            if (userItem.getYatriDetailsId() == null)
                userItem.setYatriDetailsId(yatriDetails.getId());
            yatriPulseUsersRepo.save(userItem);

            // Send SMS notification to the registered user
            Function<NotificationRequestModel, Void> function = r -> {
                NotificationRequestModel requestModel = new NotificationRequestModel();
                String body = SmtpTemplate.TEMPLATE4.getTemplateValue();
                requestModel.setTemplateId(SmtpTemplate.TEMPLATE4.getTemplateId());
                requestModel.setMessageBody(String.format(body, userName, password));

                requestModel.setPhoneNumber(String.valueOf(yatriPulseUser.getMobileNo()).startsWith("+91") ? String.valueOf(yatriPulseUser.getMobileNo())
                        : "+91" + String.valueOf(yatriPulseUser.getMobileNo()));
                notificationUtils.sendSMSsmtp(requestModel);
                return null;
            };

            // Execute the function asynchronously
            asyncConfig.execute(function, null);

            // Link the IDTP with the registered user
            try {
                String token = jwtService.generateMasterToken(userName, number, UUID.randomUUID().toString());
                yatriUtils.linkTourismThroughExcel(IDTP, token);
            } catch (WishFoundationException e) {
                HashMap<String, String> identifiermap = new HashMap<>();
                identifiermap.put("IDTP ID ", IDTP);
                exceptionMap.put(identifiermap, new WishFoundationException(e.getMessage()).getMessage());
            } catch (Exception e) {
                HashMap<String, String> identifiermap = new HashMap<>();
                identifiermap.put("IDTP ID ", IDTP);
                exceptionMap.put(identifiermap, new WishFoundationException(e.getMessage()).getMessage());
            }
        }
        return new ResponseEntity<>(exceptionMap, HttpStatus.OK);
    }

    /**
     * Validates if a given username already exists in the database.
     * If the username exists, it throws a WishFoundationException with a conflict status code.
     *
     * @param id The username to be validated.
     * @return A ResponseEntity with a status code of OK if the username does not exist.
     * @throws WishFoundationException If the username already exists.
     */
    @Override
    public ResponseEntity<Void> validateUser(String id) {
        if (yatriPulseUsersRepo.existsByUserName(id)) {
            throw new WishFoundationException(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(),
                    ErrorCode.USERNAME_ALREADY_EXISTS.getMessage(), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * Checks the number of users linked with a given phone number.
     * If the fetchUserFlag is true, it also fetches the usernames of the linked users.
     *
     * @param phoneNumber   The phone number to be checked.
     * @param fetchUserFlag A flag indicating whether to fetch the usernames of the linked users.
     * @return A PhoneNumberLinkedResponse object containing the count of linked users and optionally their usernames.
     * @throws WishFoundationException If the phone number is not provided.
     */
    @Override
    public PhoneNumberLinkedResponse phoneNumberLinked(String phoneNumber, boolean fetchUserFlag) {
        if (ObjectUtils.isEmpty(phoneNumber)) {
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "Phone Number" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);
        }

        PhoneNumberLinkedResponse phoneNumberLinkedResponse = new PhoneNumberLinkedResponse();
        List<String> users = yatriPulseUsersRepo.findUsersByPhoneNumber(phoneNumber);
        phoneNumberLinkedResponse.setLinkedWith(users.size());
        if (fetchUserFlag && !ObjectUtils.isEmpty(users)) {
            phoneNumberLinkedResponse.setUsers(new ArrayList<>());
            users.forEach(a -> phoneNumberLinkedResponse.getUsers().add(Helper.encryptDataCBCNoPadding(a)));
        }
        phoneNumberLinkedResponse.setMobileNumber(phoneNumber);

        return phoneNumberLinkedResponse;
    }

    /**
     * Checks if a given username exists in the database.
     * If the username does not exist, it throws a WishFoundationException with a not found status code.
     *
     * @param userName The username to be checked.
     * @return A ResponseEntity with a status code of OK if the username exists.
     * @throws WishFoundationException If the username does not exist.
     */
    @Override
    public ResponseEntity<Void> checkUserExistence(String userName) {
        if (!yatriPulseUsersRepo.existsByUserName(userName)) {
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * Generates a unique username by randomly generating an alphanumeric string.
     * It checks if the generated username already exists in the database.
     * If the username already exists, it generates a new one until a unique username is found.
     *
     * @return A unique username.
     */
    private String generateUniqueUserName() {
        String userName = "";

        for (int i = 0; i < 5; i++) {
            int min = 5;
            int max = 20;
            int length = random.nextInt((max - min) + 1) + min;
            userName = Helper.generateRandomAlphaNumeric(length);
            if (!yatriPulseUsersRepo.existsByUserName(userName)) {
                break;
            }
        }
        return userName;
    }

    /**
     * Calculates the duration between two dates in days.
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return The duration in days.
     */
    private int calculateDuration(Date startDate, Date endDate) {
        long durationInMillis = endDate.getTime() - startDate.getTime();

        long seconds = durationInMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return (int) (hours / 24);
    }
}

