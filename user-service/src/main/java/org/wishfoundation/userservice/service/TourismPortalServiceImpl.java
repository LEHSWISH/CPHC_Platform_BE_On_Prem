package org.wishfoundation.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.TourismUserInfo;
import org.wishfoundation.userservice.entity.repository.TourismUserInfoRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.enums.Gender;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.response.OtpResponse;
import org.wishfoundation.userservice.response.TourismUserApiResponse;
import org.wishfoundation.userservice.response.TourismUserDetails;
import org.wishfoundation.userservice.utils.Helper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.wishfoundation.userservice.utils.EnvironmentConfig.*;

/**
 * Service class for handling operations related to Tourism Portal.
 */
@Service
@RequiredArgsConstructor
public class TourismPortalServiceImpl implements TourismPortalService {

    private final RedisTemplate redisTemplate;
    private final TourismUserInfoRepository tourismUserInfoRepository;
    private final WebClient.Builder webClient;
    private final YatriPulseUsersRepository yatriPulseUsersRepository;

    /**
     * Fetches user details from Tourism Portal based on IDTP ID.
     *
     * @param id IDTP ID
     * @param consent Consent status
     * @return TourismUserDetails object containing user details
     * @throws WishFoundationException if any error occurs
     */

    @Override
    public TourismUserDetails getUserByIDTP(String id, boolean consent) {
        // Check if consent is given
        if(!consent){
            throw new WishFoundationException(ErrorCode.UNCONSENT_REQUEST.getCode(),ErrorCode.UNCONSENT_REQUEST.getMessage(),HttpStatus.FORBIDDEN);
        }

        // Check if IDTP ID is provided
        if (ObjectUtils.isEmpty(id)) {
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(), "Tourism Id" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(),HttpStatus.BAD_REQUEST);
        }

        // Check if IDTP ID is already linked to this user or any other user
        Optional<TourismUserInfo> tourismUserInfoOpt  = tourismUserInfoRepository.findByIdtpIdAndUserId(id,UserContext.getUserId());
        if (tourismUserInfoOpt.isPresent()) {
            return mapDBTourismInfoToYatriPulseResponse(tourismUserInfoOpt.get(), 0);
        } else if(tourismUserInfoRepository.existsByIdtpId(id)) {
            throw new WishFoundationException(ErrorCode.IDTP_IS_ALREADY_LINKED.getCode(),
                    ErrorCode.IDTP_IS_ALREADY_LINKED.formatMessage(String.valueOf(0)), HttpStatus.CONFLICT);
        }

        // Check rate limit for the user
        String rateLimitKey = RATE_LIMIT_PREFIX + UserContext.getCurrentUserName() + "_Tourism";
        OtpResponse rateLimitResponse = isRateLimited(rateLimitKey);
        long attemptLeft = TOURISM_RATE_LIMIT - rateLimitResponse.getAttemptLeft();

        // If rate limit is exceeded, throw exception
        if (!rateLimitResponse.isRateLimitExceed()) {
            throw new WishFoundationException(ErrorCode.INCORRECT_IDTP.getCode(),
                    ErrorCode.INCORRECT_IDTP.formatMessage(String.valueOf(0)), HttpStatus.TOO_MANY_REQUESTS);
        }

        // Make API call to Tourism Portal
            WebClient client = webClient.build();
            String response = client.get()
                    .uri(TOURISM_API_HOST+id)
                    .header("token", TOURISM_API_TOKEN)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        try {
            // Parse API response
            JsonNode jsonNode = Helper.MAPPER.readTree(response);
            if (jsonNode.has("data") && jsonNode.get("data").isArray() && jsonNode.get("data").size() > 0) {
                JsonNode dataArray = jsonNode.get("data").get(0);
                TourismUserApiResponse pilgrimInfo = Helper.MAPPER.readValue(dataArray.toString(), TourismUserApiResponse.class);
                TourismUserDetails result = mapTourismInfoToYatriPulseResponse(pilgrimInfo, attemptLeft);
                saveIDTPUser(pilgrimInfo, id);
                return result;
            } else {
                throw new WishFoundationException(ErrorCode.INCORRECT_IDTP.getCode(), ErrorCode.INCORRECT_IDTP.formatMessage(String.valueOf(attemptLeft)), HttpStatus.NOT_FOUND);
            }
        }catch (JsonProcessingException e){
            throw  new WishFoundationException(e.getLocalizedMessage());
        }
    }

    /**
 * Saves the user information fetched from the Tourism Portal to the database.
 *
 * @param pilgrimInfo The TourismUserApiResponse object containing the fetched user information.
 * @param idTp The IDTP ID of the user.
 */
    public void saveIDTPUser(TourismUserApiResponse pilgrimInfo, String idTp) {
        // Create a new TourismUserInfo object
        TourismUserInfo tourismUserInfo = new TourismUserInfo();

        // Update the fields of the TourismUserInfo object with the fetched user information
        Helper.updateFieldIfNotNull(tourismUserInfo::setIdtpId, idTp);
        Helper.updateFieldIfNotNull(tourismUserInfo::setFullName, pilgrimInfo.getPassengerName());
        Helper.updateFieldIfNotNull(tourismUserInfo::setPhoneNumber, pilgrimInfo.getMobileNo());
        Helper.updateFieldIfNotNull(tourismUserInfo::setGender, Gender.valueOf(pilgrimInfo.getGender()));
        Helper.updateFieldIfNotNull(tourismUserInfo::setAddress, pilgrimInfo.getAddress());
        Helper.updateFieldIfNotNull(tourismUserInfo::setAge, pilgrimInfo.getAge());
        Helper.updateFieldIfNotNull(tourismUserInfo::setDisease, pilgrimInfo.getDisease());
        Helper.updateFieldIfNotNull(tourismUserInfo::setOtherDisease, pilgrimInfo.getOtherDisease());
        Helper.updateFieldIfNotNull(tourismUserInfo::setYatraId, pilgrimInfo.getYatraId());
        Helper.updateFieldIfNotNull(tourismUserInfo::setPassengerId, pilgrimInfo.getPassengerId());

        // Set the YatriPulseUserId of the TourismUserInfo object
        tourismUserInfo.setYatriPulseUserId(UserContext.getUserId());

        // Save the TourismUserInfo object to the database
        TourismUserInfo result = tourismUserInfoRepository.save(tourismUserInfo);

        // Update the tourismId of the YatriPulseUsers table with the ID of the saved TourismUserInfo object
        yatriPulseUsersRepository.updateTourismIdByUserName(result.getId(), UserContext.getCurrentUserName());

    }

    /**
 * Maps TourismUserApiResponse object to TourismUserDetails object.
 *
 * @param tourismUserResponse TourismUserApiResponse object containing user details
 * @param attemptLeft Number of attempts left
 * @return TourismUserDetails object containing user details
 *
 * @todo Commenting this for now as we are not getting these details in tourism api response
 */
    private TourismUserDetails mapTourismInfoToYatriPulseResponse(TourismUserApiResponse tourismUserResponse, long attemptLeft) {
//      Todo : Commenting this for now as we are not getting these details in tourism api response

//        String[] tourDate = tourismUserResponse.getTourDuration().split(" - ");
//        LocalDate startTourDate = Helper.parseDateIntoLocalDate(tourDate[0]);
//        LocalDate endTourDate = Helper.parseDateIntoLocalDate(tourDate[1]);
//        int tourDuration = startTourDate.until(endTourDate).getDays();
//

//        TourismUser.builder().fullName(tourismUserResponse.getPassengerName())
//                .emailId(tourismUserResponse.getEmailId())
//                .phoneNumber(tourismUserResponse.getMobileNo())
//                .age(tourismUserResponse.getAge()).gender(Gender.valueOf(tourismUserResponse.getGender()))
//                .tourStartDate(tourDate[0]).tourEndDate(tourDate[1])
//                .tourDuration(tourDuration)
//                .attemptLeft(String.valueOf(attemptLeft)).build();


        return TourismUserDetails.builder()
                .idtpId(tourismUserResponse.getUniqueCode()).fullName(tourismUserResponse.getPassengerName())
                .phoneNumber(tourismUserResponse.getMobileNo())
                .age(tourismUserResponse.getAge()).gender(Gender.valueOf(tourismUserResponse.getGender()))
                .address(tourismUserResponse.getAddress())
                .attemptLeft(String.valueOf(attemptLeft)).build();
    }

    /**
 * Maps TourismUserInfo object to TourismUserDetails object.
 *
 * @param tourismUserResponse TourismUserInfo object containing user details
 * @param attemptLeft Number of attempts left
 * @return TourismUserDetails object containing user details
 *
 */
    private TourismUserDetails mapDBTourismInfoToYatriPulseResponse(TourismUserInfo tourismUserResponse, long attemptLeft) {
        // Using builder pattern to create TourismUserDetails object
        return TourismUserDetails.builder()
                .idtpId(tourismUserResponse.getIdtpId()) // Set IDTP ID
                .fullName(tourismUserResponse.getFullName()) // Set full name
                .phoneNumber(tourismUserResponse.getPhoneNumber()) // Set phone number
                .age(tourismUserResponse.getAge()) // Set age
                .gender(tourismUserResponse.getGender()) // Set gender
                .address(tourismUserResponse.getAddress()) // Set address
                .attemptLeft(String.valueOf(attemptLeft)) // Set attempt left
                .build(); // Build and return TourismUserDetails object
    }

    /**
 * Checks if rate limit has been exceeded for a specific user.
 *
 * @param rateLimitKey Rate limit key. This key is used to identify the user for rate limiting.
 * @return OtpResponse object containing rate limit status and attempt left.
 *
 * The method uses Redis to store and manage rate limits. It increments the count for the given rateLimitKey,
 * checks if the limit has been exceeded, and returns an OtpResponse object with the rate limit status and attempt left.
 * If the rateLimitKey does not exist in Redis, it sets the count to 1 and returns rateLimitExceed as true.
 * If the rate limit is exceeded, it returns rateLimitExceed as false and the remaining attempts.
 * The rate limit is reset after the specified period (TOURISM_RATE_LIMIT_PERIOD_SECONDS) using Redis' expire command.
 */
    private OtpResponse isRateLimited(String rateLimitKey) {
        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey, 0); // Check current value without incrementing

        if (currentCount == null) {
            redisTemplate.opsForValue().set(rateLimitKey, 1);
            currentCount = 1L;
        } else if (currentCount <= TOURISM_RATE_LIMIT) {
            redisTemplate.opsForValue().increment(rateLimitKey, 1);
            currentCount++;
        }
        redisTemplate.expire(rateLimitKey, TOURISM_RATE_LIMIT_PERIOD_SECONDS, TimeUnit.SECONDS);
        return OtpResponse.builder().rateLimitExceed(currentCount <= TOURISM_RATE_LIMIT).attemptLeft(currentCount).build();
    }


}
