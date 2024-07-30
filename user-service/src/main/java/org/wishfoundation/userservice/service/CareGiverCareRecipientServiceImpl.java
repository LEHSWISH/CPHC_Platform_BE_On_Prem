package org.wishfoundation.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.CareGiverCareRecipient;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.CareGiverCareRecipientRepository;
import org.wishfoundation.userservice.entity.repository.EvaidyaUserDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.CareType;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.enums.HealthStatus;
import org.wishfoundation.userservice.enums.RequestStatus;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.response.*;
import org.wishfoundation.userservice.security.JWTService;
import org.wishfoundation.userservice.utils.Helper;

import java.time.Instant;
import java.util.*;

@Service
public class CareGiverCareRecipientServiceImpl implements CareGiverCareRecipientService {

    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepos;

    @Autowired
    private CareGiverCareRecipientRepository careGiverCareRecipientRepo;

    @Autowired
    private YatriDetailsRepository yatriDetailsRepo;

    @Autowired
    private EvaidyaUserDetailsRepository evaidyaUserDetailsRepo;

    @Autowired
    private JWTService jwtService;

    /**
     * Sends a request from the current user to the specified user.
     *
     * @param userName The username of the user to send the request to.
     * @return A ResponseEntity with HTTP status code 201 (Created) if the request is sent successfully.
     * @throws WishFoundationException If the request cannot be sent due to various reasons.
     */
    @Override
    public ResponseEntity<Void> sendRequest(String userName) {
        Optional<YatriPulseUsers> yatriPulseUsersOpt = yatriPulseUsersRepos.findUserByUserName(userName);
        if (yatriPulseUsersOpt.isPresent()) {
            if (userName.equals(UserContext.getCurrentUserName()))
                throw new WishFoundationException(ErrorCode.SELF_REQUEST.getCode(),
                        ErrorCode.SELF_REQUEST.getMessage(), HttpStatus.CONFLICT);

            if (UserContext.getCareType().equals(CareType.CARE_RECIPIENT)) {
                throw new WishFoundationException(ErrorCode.CAREGIVER_ALREADY_EXISTS.getCode(),
                        ErrorCode.CAREGIVER_ALREADY_EXISTS.getMessage(), HttpStatus.CONFLICT);
            }

            YatriPulseUsers yatriPulseUsers = yatriPulseUsersOpt.get();
            if (!ObjectUtils.isEmpty(yatriPulseUsers.getEvaidyaUserDetailsEntityId())) {
                HealthStatus status = evaidyaUserDetailsRepo.findStatusById(yatriPulseUsers.getEvaidyaUserDetailsEntityId());


                if (status.equals(HealthStatus.HIGH_RISK) || status.equals(HealthStatus.HIGH_RISK_NOT_FIT_TO_TRAVEL)) {
                    throw new WishFoundationException(ErrorCode.HIGH_RISK_YATRI.getCode(),
                            ErrorCode.HIGH_RISK_YATRI.getMessage(), HttpStatus.BAD_REQUEST);
                }

            } else {
                //TODO: if status is not present
            }

            if (careGiverCareRecipientRepo.countByParentUserId(yatriPulseUsers.getId()) >= 20) {
                throw new WishFoundationException(ErrorCode.YOUR_CAREGIVER_HAVE_TOO_MANY_REQUESTS.getCode(),
                        ErrorCode.YOUR_CAREGIVER_HAVE_TOO_MANY_REQUESTS.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
            }

            if (UserContext.getCareType().equals(CareType.CARE_GIVER) && careGiverCareRecipientRepo.existsByUserIdAndRequestStatus(UserContext.getUserId(), RequestStatus.ACCEPTED)) {
                throw new WishFoundationException(ErrorCode.CARE_RECIPIENT_ALREADY_EXISTS.getCode(),
                        ErrorCode.CARE_RECIPIENT_ALREADY_EXISTS.getMessage(), HttpStatus.BAD_REQUEST);
            }

            RequestStatus requestStatus = careGiverCareRecipientRepo.findRequestStatusByParentAndChildUserId(yatriPulseUsers.getId(), UserContext.getUserId());

            if (!ObjectUtils.isEmpty(requestStatus))
                if (requestStatus.equals(RequestStatus.PENDING))
                    throw new WishFoundationException(ErrorCode.REQUEST_IS_ALREADY_SENT.getCode(),
                            ErrorCode.REQUEST_IS_ALREADY_SENT.getMessage(), HttpStatus.CONFLICT);

            CareGiverCareRecipient careGiverCareTaker = new CareGiverCareRecipient();
            careGiverCareTaker.setParentYatriPulseUserId(yatriPulseUsers.getId());
            careGiverCareTaker.setChildYatriPulseUserId(UserContext.getUserId());
            careGiverCareTaker.setRequestStatus(RequestStatus.PENDING);
            careGiverCareTaker.setAgreementTime(Instant.now());
            careGiverCareRecipientRepo.save(careGiverCareTaker);

        } else {
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Views the caregiver requests sent by the specified user.
     *
     * @param userId The UUID of the user whose caregiver requests to view.
     * @return A ResponseEntity containing a list of caregiver requests sent by the user.
     * @throws WishFoundationException If there are no caregiver requests to view.
     */
    @Override
    public ResponseEntity<CareGiverCareRecipientResponse> viewCareGiverRequests(UUID userId) {
        List<UUID> userIds = careGiverCareRecipientRepo.findChildUserIdByParentUserId(userId, RequestStatus.PENDING);
        CareGiverCareRecipientResponse careGiverRequestsResponse = new CareGiverCareRecipientResponse();
        careGiverRequestsResponse.setCareGiverRequests(new ArrayList<>());

        if (!ObjectUtils.isEmpty(userIds)) {
            List<CareGiverCareRecipientResponse> userNamePhoneNumberList = yatriPulseUsersRepos.findUserNameByIds(userIds);
            HashMap<UUID, CareGiverCareRecipientResponse> userAuthDetailsRespMap = new HashMap<>();

            userNamePhoneNumberList.forEach(r -> {
                userAuthDetailsRespMap.put(r.getId(), r);
            });
            List<ChildUserIdStatusResponse> childUserIdStatusRespList = careGiverCareRecipientRepo.findChildIdAndRequestStatusByParentUserId(userId, RequestStatus.PENDING);
            childUserIdStatusRespList.forEach(r -> {

                CareGiverCareRecipientResponse careGiverCareTakerResponse = new CareGiverCareRecipientResponse();
                careGiverCareTakerResponse.setRequestStatus(r.getRequestStatus());
                careGiverCareTakerResponse.setUserName(userAuthDetailsRespMap.get(r.getChildYatriPulseUserId()).getUserName() != null ? userAuthDetailsRespMap.get(r.getChildYatriPulseUserId()).getUserName() : "N/A");
                careGiverCareTakerResponse.setPhoneNumber(userAuthDetailsRespMap.get(r.getChildYatriPulseUserId()).getPhoneNumber());
                careGiverRequestsResponse.getCareGiverRequests().add(careGiverCareTakerResponse);

            });

        }
        return new ResponseEntity<>(careGiverRequestsResponse, HttpStatus.OK);
    }

    /**
     * Views the caregiver recipient of the specified user.
     *
     * @param userId The UUID of the user whose caregiver recipient to view.
     * @return A ResponseEntity containing the caregiver recipient of the user.
     * @throws WishFoundationException If the user does not have a caregiver recipient.
     */
    @Override
    public ResponseEntity<CareGiverCareRecipientResponse> viewCareGiverRecipient(UUID userId) {
        CareGiverCareRecipientResponse careGiverCareTakerResp = new CareGiverCareRecipientResponse();
        if (UserContext.getCareType().equals(CareType.CARE_GIVER)) {
            List<UUID> userIds = careGiverCareRecipientRepo.findChildUserIdByParentUserId(userId, RequestStatus.ACCEPTED);
            List<UserBasicDetailsResp> userBasicDetailsResp = yatriPulseUsersRepos.findBasicDetailsByIds(userIds);
            if (!ObjectUtils.isEmpty(userBasicDetailsResp))
                careGiverCareTakerResp.setCareGiverRecipient(userBasicDetailsResp);

        } else {
            UUID parentUserId = careGiverCareRecipientRepo.findParentUserIdByChildAndRequestStatus(userId, RequestStatus.ACCEPTED);
            List<UserBasicDetailsResp> userBasicDetailsResp = yatriPulseUsersRepos.findBasicDetailsByIds(Collections.singletonList(parentUserId));
            if (!ObjectUtils.isEmpty(userBasicDetailsResp))
                careGiverCareTakerResp.setCareGiver(userBasicDetailsResp);
        }
        return new ResponseEntity<>(careGiverCareTakerResp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> respondRequest(RequestStatus requestStatus, String userName) {
        Optional<YatriPulseUsers> yatriPulseUsersOpt = yatriPulseUsersRepos.findUserByUserName(userName);
        if (yatriPulseUsersOpt.isPresent()) {
            if (UserContext.getCareType().equals(CareType.CARE_RECIPIENT)) {
                throw new WishFoundationException(ErrorCode.YOU_CAN_NOT_ACCEPT_REQUEST.getCode(),
                        ErrorCode.YOU_CAN_NOT_ACCEPT_REQUEST.getMessage(), HttpStatus.BAD_REQUEST);
            }
            UUID requestedUserId = yatriPulseUsersOpt.get().getId();
            if (careGiverCareRecipientRepo.existsByParentAndChildUserId(UserContext.getUserId(), requestedUserId)) {
                if (requestStatus.equals(RequestStatus.ACCEPT)) {
                    if (careGiverCareRecipientRepo.countByParentAndChildUserIdAndRequestStatus(UserContext.getUserId(), requestedUserId, RequestStatus.ACCEPTED) >= 2)
                        throw new WishFoundationException(ErrorCode.ACCEPT_REQUEST_LIMIT.getCode(),
                                ErrorCode.ACCEPT_REQUEST_LIMIT.getMessage(), HttpStatus.CONFLICT);

                    careGiverCareRecipientRepo.updateRequestStatus(RequestStatus.ACCEPTED, Instant.now(), UserContext.getUserId(), requestedUserId);
                    yatriPulseUsersRepos.updateCareType(CareType.CARE_RECIPIENT, requestedUserId);
                    careGiverCareRecipientRepo.deleteByChildUserIdAndRequestStatus(requestedUserId, RequestStatus.PENDING);
                    careGiverCareRecipientRepo.deleteByChildUserIdAndRequestStatus(UserContext.getUserId(), RequestStatus.PENDING);

                } else if (requestStatus.equals(RequestStatus.REJECT))
                    careGiverCareRecipientRepo.deleteByParentAndChildUserId(UserContext.getUserId(), requestedUserId);

            } else {
                throw new WishFoundationException(ErrorCode.YOU_DONT_HAVE_ANY_REQUESTS.getCode(),
                        ErrorCode.YOU_DONT_HAVE_ANY_REQUESTS.getMessage(), HttpStatus.NOT_FOUND);
            }

        } else {
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * This method removes the caregiver recipient of the current user.
     *
     * @param userId The UUID of the user whose caregiver recipient to remove.
     * @return A ResponseEntity with a NO_CONTENT status.
     * @throws WishFoundationException If the requested user is not a caregiver recipient of the current user.
     */
    @Override
    public ResponseEntity<Void> removeRecipient(String userName) {
        Optional<YatriPulseUsers> yatriPulseUsersOpt = yatriPulseUsersRepos.findUserByUserName(userName);
        if (yatriPulseUsersOpt.isPresent()) {
            YatriPulseUsers yatriPulseUsers = yatriPulseUsersOpt.get();
            UUID requestedUserId = yatriPulseUsers.getId();

            HealthStatus status = evaidyaUserDetailsRepo.findStatusById(yatriPulseUsers.getEvaidyaUserDetailsEntityId());

            if (!ObjectUtils.isEmpty(status) && (status.equals(HealthStatus.HIGH_RISK) || status.equals(HealthStatus.HIGH_RISK_NOT_FIT_TO_TRAVEL)))
                throw new WishFoundationException(ErrorCode.RECIPIENT_HIGH_RISK.getCode(),
                        ErrorCode.RECIPIENT_HIGH_RISK.getMessage(), HttpStatus.NOT_ACCEPTABLE);

            careGiverCareRecipientRepo.deleteByParentAndChildUserId(UserContext.getUserId(), requestedUserId);
            yatriPulseUsersRepos.updateCareType(CareType.CARE_GIVER, requestedUserId);

        } else
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * This method generates a master token for the requested user.
     *
     * @param userName  The username of the user.
     * @param sessionId The session ID.
     * @return A ResponseEntity with a LoginResponse containing the generated token.
     * @throws WishFoundationException If the requested user is not a caregiver recipient of the current user.
     */
    @Override
    public ResponseEntity<LoginResponse> getToken(String userName, String sessionId) {
        Optional<YatriPulseUsers> usersOptional = yatriPulseUsersRepos.findUserByUserName(userName);
        LoginResponse loginResponse = new LoginResponse();
        if (usersOptional.isPresent()) {
            YatriPulseUsers yatriPulseUsers = usersOptional.get();
            if (careGiverCareRecipientRepo.existsByParentAndChildUserIdAndRequestStatus(UserContext.getUserId(), yatriPulseUsers.getId(), RequestStatus.ACCEPTED)) {
                String token = jwtService.generateMasterToken(userName, yatriPulseUsers.getPhoneNumber(), sessionId);

                loginResponse.setToken(token);
                loginResponse.setMessage("Master token generated.");
                loginResponse.setPhoneNumber(yatriPulseUsers.getPhoneNumber());
                loginResponse.setUserName(userName);

                return new ResponseEntity<>(loginResponse, HttpStatus.OK);
            } else {
                throw new WishFoundationException(ErrorCode.USER_NOT_VALID.getCode(),
                        ErrorCode.USER_NOT_VALID.getMessage(), HttpStatus.BAD_REQUEST);
            }

        } else
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

    }

    /**
     * This method removes the caregiver recipient of the specified user.
     *
     * @param userId The UUID of the user whose caregiver recipient to remove.
     * @return A ResponseEntity with a NO_CONTENT status.
     * @throws WishFoundationException If the requested user is not a caregiver recipient of the current user.
     */
    @Override
    public ResponseEntity<Void> removeCareGiver(UUID userId) {
        careGiverCareRecipientRepo.deleteByChildUserIdAndRequestStatus(userId, RequestStatus.ACCEPTED);
        yatriPulseUsersRepos.updateCareType(CareType.CARE_GIVER, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * This method retrieves the usernames and full names of users linked to the specified phone number.
     * The usernames are encrypted using a CBC No Padding encryption algorithm.
     *
     * @param phoneNumber The phone number to search for linked users.
     * @return A list of CareGiverCareRecipientResponse objects containing the encrypted usernames and full names of linked users.
     * @throws WishFoundationException If no users are linked to the specified phone number.
     */
    @Override
    public List<CareGiverCareRecipientResponse> phoneNumberLinkedUsers(String phoneNumber) {
        List<CareGiverCareRecipientResponse> userNameAndFullNameResp = yatriPulseUsersRepos.findUserNameAndFullNameByPhoneNumber(phoneNumber);
        if (ObjectUtils.isEmpty(userNameAndFullNameResp))
            throw new WishFoundationException(ErrorCode.PHONE_NUMBER_NOT_LINKED.getCode(),
                    ErrorCode.PHONE_NUMBER_NOT_LINKED.getMessage(), HttpStatus.NOT_FOUND);
        userNameAndFullNameResp.forEach(a -> {
            a.setUserName(Helper.encryptDataCBCNoPadding(a.getUserName()));
        });

        return userNameAndFullNameResp;
    }

}
