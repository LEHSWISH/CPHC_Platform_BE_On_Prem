package org.wishfoundation.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.ABHAUserDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.ForgetUserName;
import org.wishfoundation.userservice.request.OtpRequest;
import org.wishfoundation.userservice.request.ValidatePhoneNumberUserName;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.request.notification.NotificationRequestModel;
import org.wishfoundation.userservice.response.PasswordResetResponse;
import org.wishfoundation.userservice.utils.Helper;
import org.wishfoundation.userservice.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.wishfoundation.userservice.utils.EnvironmentConfig.*;

/**
 * This class implements the ForgetUserPasswordService interface.
 * It provides methods for validating user name and phone number, resetting password, and retrieving username.
 */
@Service
public class ForgetUserPasswordServiceImpl implements ForgetUserPasswordService {

    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepo;

    @Autowired
    private YatriPulseUserServiceImpl yatriPulseUserServiceImpl;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private NotificationUtils notificationUtils;
    @Autowired
    private ABHAUserDetailsRepository abhaUserDetailsRepo;

    /**
     * Validates the user name and phone number.
     *
     * @param validatePhoneNumberUserName The request object containing user name and phone number.
     * @return ResponseEntity with HTTP status OK if validation is successful.
     * @throws WishFoundationException if user name and phone number do not match or user is not found.
     */
    @Override
    public ResponseEntity<Void> validateUserNamePhoneNumber(ValidatePhoneNumberUserName validatePhoneNumberUserName) {

        Optional<YatriPulseUsers> userOpt = yatriPulseUsersRepo.findUserByUserName(validatePhoneNumberUserName.getUserName());
        if (userOpt.isPresent()) {
            YatriPulseUsers user = userOpt.get();
            if (!user.getPhoneNumber().equals(validatePhoneNumberUserName.getPhoneNumber()))
                throw new WishFoundationException(ErrorCode.PHONE_NUMBER_IS_NOT_MATCHED.getCode(),
                        ErrorCode.PHONE_NUMBER_IS_NOT_MATCHED.getMessage(), HttpStatus.BAD_REQUEST);
        } else {
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * Resets the password for the given user.
     *
     * @param yatriPulseUserRequest The request object containing user name, phone number, OTP, and new password.
     * @return PasswordResetResponse with success message if password reset is successful.
     * @throws WishFoundationException if user is not found, OTP is invalid, or new password is not provided.
     */
    @Override
    public PasswordResetResponse resetPassword(YatriPulseUserRequest yatriPulseUserRequest) {

        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getUserName()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "User name" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getPhoneNumber()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "Phone number" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);


        YatriPulseUsers yatriPulseUsers = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(yatriPulseUserRequest.getUserName(), yatriPulseUserRequest.getPhoneNumber());

        if (ObjectUtils.isEmpty(yatriPulseUsers)) {
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.BAD_REQUEST);

        }

        if (ObjectUtils.isEmpty(yatriPulseUserRequest.getOtp()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "OTP" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        OtpRequest otpRequest = new OtpRequest();

        otpRequest.setPhoneNumber(yatriPulseUserRequest.getPhoneNumber());
        otpRequest.setUserName(yatriPulseUserRequest.getUserName());

        otpRequest.setOtp(yatriPulseUserRequest.getOtp());
        otpRequest.setTemplateKey(yatriPulseUserRequest.getTemplateKey());

        yatriPulseUserServiceImpl.verifyOTP(otpRequest);

        String password = Helper.decryptPassword(yatriPulseUserRequest.getPassword());
        String[] PassAndTime = password.split("%");
        yatriPulseUsers.setPassword(passwordEncoder.encode(PassAndTime[1]));
        yatriPulseUsersRepo.save(yatriPulseUsers);

        return PasswordResetResponse.builder().message("You have successfully reset your password.").build();
    }

    /**
     * Retrieves the username for the given phone number and optional ABHA number or government ID type and ID.
     *
     * @param forgetUserName The request object containing phone number, ABHA number, government ID type, and ID.
     * @return ResponseEntity with HTTP status OK if username retrieval is successful.
     * @throws WishFoundationException if phone number is not linked to any user, ABHA number is not provided,
     *                                 or government ID type and ID are not provided.
     */
    @Override
    public ResponseEntity<Void> forgetUsername(ForgetUserName forgetUserName) {

        boolean abhaNumberFlag = ObjectUtils.isEmpty(forgetUserName.getAbhaNumber());
        boolean governmentIdTypeFlag = ObjectUtils.isEmpty(forgetUserName.getGovernmentIdType());

        if (ObjectUtils.isEmpty(forgetUserName.getPhoneNumber()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "Phone Number" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);


        if (abhaNumberFlag && governmentIdTypeFlag)
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "Abha Number or Government Id Type" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        if (!abhaNumberFlag && !governmentIdTypeFlag)
            throw new WishFoundationException(ErrorCode.PROVIDE_ANY_ONE_EITHER_GOV_ID_OR_ABHA_NUMBER.getCode(),
                    ErrorCode.PROVIDE_ANY_ONE_EITHER_GOV_ID_OR_ABHA_NUMBER.getMessage(), HttpStatus.BAD_REQUEST);

        ArrayList<UUID> abhaUserIds = yatriPulseUsersRepo.findAllUserIdByPhoneNumber(forgetUserName.getPhoneNumber());
        if (abhaUserIds == null || abhaUserIds.isEmpty())
            throw new WishFoundationException(ErrorCode.PHONE_NUMBER_NOT_LINKED.getCode(),
                    ErrorCode.PHONE_NUMBER_NOT_LINKED.getMessage(), HttpStatus.NOT_FOUND);

        String userName = "";
        if (!abhaNumberFlag) {

            ArrayList<ABHAUserDetails> abhaUserDetails = abhaUserDetailsRepo.findByUserIds(abhaUserIds);
            UUID abhaUserId = null;

            for (ABHAUserDetails r : abhaUserDetails) {
                if (r.getAbhaNumber().equals(forgetUserName.getAbhaNumber())) {
                    abhaUserId = r.getId();
                    break;
                }
            }

            userName = yatriPulseUsersRepo.findUserByAbhaUserId(abhaUserId);
            if (ObjectUtils.isEmpty(userName))
                throw new WishFoundationException(ErrorCode.ABHA_NUMBER_NOT_LINKED.getCode(),
                        ErrorCode.ABHA_NUMBER_NOT_LINKED.getMessage(), HttpStatus.NOT_FOUND);

        } else {
            if (!governmentIdTypeFlag &&
                    ObjectUtils.isEmpty(forgetUserName.getGovernmentId())) {
                throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                        forgetUserName.getGovernmentIdType() + " Id"
                                + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);
            }
            userName = yatriPulseUsersRepo.findUserByPhoneNumberAndGovIdTypeAndGovId(forgetUserName.getPhoneNumber(), forgetUserName.getGovernmentIdType(), forgetUserName.getGovernmentId());
            if (userName == null || userName.isEmpty())
                throw new WishFoundationException(ErrorCode.USER_IS_NOT_FOUND_WITH_GOV_TYPE_AND_ID.getCode(),
                        ErrorCode.USER_IS_NOT_FOUND_WITH_GOV_TYPE_AND_ID.getMessage(), HttpStatus.NOT_FOUND);
        }

        NotificationRequestModel requestModel = new NotificationRequestModel();
        String body = RECOVER_USERNAME_TEMPLATE;
        requestModel.setMessageBody(body
                .replaceAll(USER_NAME_MARCO, userName)
                .replaceAll(LINK_MARCO, YATRI_PULE_LINK));
        requestModel.setPhoneNumber(forgetUserName.getPhoneNumber().startsWith("+91") ? forgetUserName.getPhoneNumber() : "+91" + forgetUserName.getPhoneNumber());
        String s = notificationUtils.sendSMS(requestModel);
        System.out.println(userName + " : " + s);

        return new ResponseEntity<Void>(HttpStatus.OK);
    }
}
