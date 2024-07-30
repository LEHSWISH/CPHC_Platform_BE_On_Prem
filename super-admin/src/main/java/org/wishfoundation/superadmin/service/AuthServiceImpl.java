package org.wishfoundation.superadmin.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import org.wishfoundation.superadmin.entity.UserAccounts;
import org.wishfoundation.superadmin.entity.UserRole;
import org.wishfoundation.superadmin.entity.repository.UserAccountsRepository;
import org.wishfoundation.superadmin.entity.repository.UserRoleRepository;
import org.wishfoundation.superadmin.enums.EmailMessage;
import org.wishfoundation.superadmin.enums.ErrorCode;
import org.wishfoundation.superadmin.exception.WishFoundationException;
import org.wishfoundation.superadmin.request.EmailRequestModel;
import org.wishfoundation.superadmin.request.LoginRequest;
import org.wishfoundation.superadmin.request.OtpRequest;
import org.wishfoundation.superadmin.request.RegisterUserRequest;
import org.wishfoundation.superadmin.response.LoginResponse;
import org.wishfoundation.superadmin.response.OtpResponse;
import org.wishfoundation.superadmin.security.JWTService;
import org.wishfoundation.superadmin.utils.EnvironmentConfig;
import org.wishfoundation.superadmin.utils.Helper;
import org.wishfoundation.superadmin.utils.NotificationUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    // RedisTemplate for storing and retrieving data from Redis
    private static final RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

    // Rate limit configuration
    public static final int RATE_LIMIT = 5;
    public static final long RATE_LIMIT_PERIOD_SECONDS = 3600;

    // Autowired dependencies
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private NotificationUtils notificationUtils;
    @Autowired
    private UserAccountsRepository userAccountsRepo;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRoleRepository userRoleRepo;

    /**
 * This method handles user login.
 *
 * @param loginRequest The request object containing emailId and password.
 * @param sessionId The session id of the user.
 * @return ResponseEntity<LoginResponse> containing the JWT token, user name, and phone number.
 * @throws WishFoundationException If sessionId is not provided, user is not found, or authentication fails.
 */
@Override
public ResponseEntity<LoginResponse> login(LoginRequest loginRequest, String sessionId) {
    LoginResponse model = new LoginResponse();

    // Check if sessionId is provided
    if (ObjectUtils.isEmpty(sessionId)) {
        throw new WishFoundationException(ErrorCode.UNABLE_TO_GET_SESSION_ID.getCode(),
                ErrorCode.UNABLE_TO_GET_SESSION_ID.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Authenticate user
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
            loginRequest.getEmailId(), loginRequest.getPassword());
    Authentication authentication = authenticationProvider.authenticate(usernamePasswordAuthenticationToken);

    // Fetch user by email
    Optional<UserAccounts> user = userAccountsRepo.findUserByEmail(loginRequest.getEmailId());

    // Check if user exists
    if (user.isEmpty())
        throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

    // Generate JWT token
    String phoneNumber = user.get().getPhoneNumber();
    String token = jwtService.generateToken(authentication, phoneNumber, sessionId);

    // Set response data
    model.setToken(token);
    model.setUserName(loginRequest.getEmailId());
    model.setPhoneNumber(phoneNumber);

    return new ResponseEntity<>(model, HttpStatus.OK);
}


    /**
 * This method handles user registration.
 *
 * @param registerUserRequest The request object containing user details like email, phone number, full name, and roles.
 * @return ResponseEntity<Void> with HTTP status code 201 (Created) if registration is successful.
 * @throws WishFoundationException If required fields are not provided, user already exists, or email sending fails.
 */
@Override
public ResponseEntity<Void> register(RegisterUserRequest registerUserRequest) {

    // Check if required fields are provided
    if (ObjectUtils.isEmpty(registerUserRequest.getEmail()))
        throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                "Email Id" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

    if (ObjectUtils.isEmpty(registerUserRequest.getPhoneNumber()))
        throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                "Phone number" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

    if (ObjectUtils.isEmpty(registerUserRequest.getFullName()))
        throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                "Full Name" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

    // Check if user already exists
    if (userAccountsRepo.existsByEmail(registerUserRequest.getEmail())) {
        throw new WishFoundationException(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(),
                ErrorCode.USERNAME_ALREADY_EXISTS.getMessage(), HttpStatus.CONFLICT);
    }

    // Create new user account
    UserAccounts userAccounts = new UserAccounts();
    userAccounts.setEmail(registerUserRequest.getEmail());
    userAccounts.setPhoneNumber(registerUserRequest.getPhoneNumber());
    userAccounts.setFullName(registerUserRequest.getFullName());

    // Generate and set password
    String password = Helper.generatePassword(10);
    // do not remove this required to keep front end And Backend Encryption in sync
    userAccounts.setPassword(passwordEncoder.encode(BCrypt.hashpw(password, EnvironmentConfig.SALT)));
//        userAccounts.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));
    userAccounts = userAccountsRepo.save(userAccounts);
    UUID userAccountId = userAccounts.getId();

    // Save user roles
    registerUserRequest.getRoles().forEach(roleName -> {
        UserRole userRole = new UserRole();
        userRole.setUserAccountId(userAccountId);
        userRole.setRoleName(roleName);
        userRoleRepo.save(userRole);
    });

    // Send email to user
    EmailRequestModel emailRequestModel = new EmailRequestModel();
    emailRequestModel.setFromMail("helpdesk@zoho.com");
    emailRequestModel.setSubject("Reset Password");
    emailRequestModel.setToMail(registerUserRequest.getEmail());
    String tempMessage = String.format(EmailMessage.REGISTER_MESSAGE_BODY.getTemplateValue(),registerUserRequest.getFullName() ,registerUserRequest.getRoles(),"url",registerUserRequest.getEmail(), password );
    emailRequestModel.setMessageBody(tempMessage);
    notificationUtils.sendEmail(emailRequestModel);

    return new ResponseEntity<Void>(HttpStatus.CREATED);
}

    /**
 * This method handles sending an OTP to the user's email.
 *
 * @param otpRequest The request object containing emailId, templateKey, and optional OTP.
 * @return ResponseEntity<OtpResponse> containing a success message and remaining attempts.
 * @throws WishFoundationException If no attempts left, invalid OTP, or email sending fails.
 */
@Override
public ResponseEntity<OtpResponse> sendOtp(OtpRequest otpRequest){
    // Fetch user by email
    String email= otpRequest.getEmailId();
    String fullName = userAccountsRepo.findUserByEmail(email).get().getFullName();

    // Rate limit key
    String rateLimitKey = "rateLimitKey"+"_"+otpRequest.getTemplateKey()+"_"+email;

    // Check rate limit
    OtpResponse rateLimitResponse = isRateLimited(rateLimitKey);
    if (!rateLimitResponse.isRateLimitExceed()) {
        throw new WishFoundationException(ErrorCode.NO_ATTEMPTS_LEFT.getCode(),
                ErrorCode.NO_ATTEMPTS_LEFT.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Generate and save OTP
    String otpKey = "otp:"+otpRequest.getTemplateKey()+"_"+email;
    String otpValue = Helper.generateOTP(EnvironmentConfig.OTP_LENGTH);

    redisTemplate.opsForValue().set(otpKey,otpValue, Duration.ofMinutes(5));

    // Send email to user
    EmailRequestModel emailRequestModel = new EmailRequestModel();
    String tempMessage = "";
    if(otpRequest.getTemplateKey().equals("reset-password")) {
        emailRequestModel.setFromMail("helpdesk@zoho.com");
        emailRequestModel.setSubject("Reset Password");
        emailRequestModel.setToMail(email);
        tempMessage= String.format(EmailMessage.OTP_MESSAGE_BODY.getTemplateValue(), fullName, otpValue);
    }
    emailRequestModel.setMessageBody(tempMessage);
    notificationUtils.sendEmail(emailRequestModel);

    return  ResponseEntity.status(HttpStatus.OK).body(OtpResponse.builder()
            .message("OTP sent To email " + email)
            .attemptLeft(RATE_LIMIT - rateLimitResponse.getAttemptLeft()).build());
}

    // Resend OTP method
/**
 * Resends an OTP to the user's email.
 *
 * @param otpRequest The request object containing emailId, templateKey, and optional OTP.
 * @return ResponseEntity<OtpResponse> containing a success message and remaining attempts.
 * @throws WishFoundationException If no attempts left, invalid OTP, or email sending fails.
 */
@Override
public ResponseEntity<OtpResponse> resendOTP(OtpRequest otpRequest){
    // Fetch user by email
    String email= otpRequest.getEmailId();
    String otpValue = "";
    String fullName = userAccountsRepo.findUserByEmail(email).get().getFullName();

    // Rate limit key
    String rateLimitKey = "rateLimitKey"+"_"+otpRequest.getTemplateKey()+"_"+email;

    // Check rate limit
    OtpResponse rateLimitResponse = isRateLimited(rateLimitKey);
    if (!rateLimitResponse.isRateLimitExceed()) {
        throw new WishFoundationException(ErrorCode.NO_ATTEMPTS_LEFT.getCode(),
                ErrorCode.NO_ATTEMPTS_LEFT.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Generate and save OTP
    String otpKey = "otp:"+otpRequest.getTemplateKey()+"_"+email;
    if (redisTemplate.hasKey(otpKey)) {
        otpValue = redisTemplate.opsForValue().get(otpKey).toString();
    } else {
        otpValue = Helper.generateOTP(EnvironmentConfig.OTP_LENGTH);
    }

    redisTemplate.opsForValue().set(otpKey,otpValue, Duration.ofMinutes(5));

    // Send email to user
    EmailRequestModel emailRequestModel = new EmailRequestModel();
    String tempMessage = "";
    if(otpRequest.getTemplateKey().equals("reset-password")) {
        emailRequestModel.setFromMail("helpdesk@zoho.com");
        emailRequestModel.setSubject("Reset Password");
        emailRequestModel.setToMail(email);
        tempMessage= String.format(EmailMessage.OTP_MESSAGE_BODY.getTemplateValue(), fullName, otpValue);
    }
    emailRequestModel.setMessageBody(tempMessage);
    notificationUtils.sendEmail(emailRequestModel);

    return  ResponseEntity.status(HttpStatus.OK).body(OtpResponse.builder()
            .message("OTP sent To email " + email)
            .attemptLeft(RATE_LIMIT - rateLimitResponse.getAttemptLeft()).build());
}

/**
 * Verifies the OTP sent to the user's email.
 *
 * @param otpRequest The request object containing emailId, templateKey, and OTP.
 * @return ResponseEntity<OtpResponse> containing a success message if OTP is verified.
 * @throws WishFoundationException If OTP is not provided, invalid, or does not exist in Redis.
 */
@Override
public ResponseEntity<OtpResponse> verifyOTP(OtpRequest otpRequest){

    // Check if OTP is provided
    if (ObjectUtils.isEmpty(otpRequest.getOtp()) || otpRequest.getOtp().length() != EnvironmentConfig.OTP_LENGTH) {
        throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Fetch user by email
    String email= otpRequest.getEmailId();
//        String userName = otpRequest.getUserName();
    String otpKey = "otp:"+otpRequest.getTemplateKey()+"_"+email;

    // Check if OTP exists in Redis
    if (redisTemplate.hasKey(otpKey)) {
        System.out.println("key exist ------------------------------------ ");
        String storedOTP = redisTemplate.opsForValue().get(otpKey).toString();
        // Check if OTP is valid
        if (storedOTP.equals(otpRequest.getOtp())) {
            return ResponseEntity.status(HttpStatus.OK).body(OtpResponse.builder().message("OTP verified successfully ").build());
        } else {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage(), HttpStatus.BAD_REQUEST);
        }
    } else {
        throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

/**
 * Checks the rate limit for sending OTPs.
 *
 * @param rateLimitKey The key to store the rate limit count in Redis.
 * @return OtpResponse containing the rate limit status and remaining attempts.
 */
private OtpResponse isRateLimited(String rateLimitKey) {
    Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey, 0); // Check current value without
    // incrementing

    if (currentCount == null) {
        redisTemplate.opsForValue().set(rateLimitKey, String.valueOf(1));
        currentCount = 1L;
    } else if (currentCount <= RATE_LIMIT) {
        redisTemplate.opsForValue().increment(rateLimitKey, 1);
        currentCount++;
    }

    redisTemplate.expire(rateLimitKey, RATE_LIMIT_PERIOD_SECONDS, TimeUnit.SECONDS);

    return OtpResponse.builder().rateLimitExceed(currentCount <= RATE_LIMIT).attemptLeft(currentCount).build();
}
}
