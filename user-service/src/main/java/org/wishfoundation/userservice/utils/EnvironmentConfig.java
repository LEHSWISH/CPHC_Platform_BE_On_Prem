package org.wishfoundation.userservice.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Data
public class EnvironmentConfig {


    public static final String S3_PROFILE_DETAILS = "profile-details/";
    public  static  final String dataSecretKey = "Wish20foundation";

    public static final String CONFIG_BUCKET_PATH  = System.getenv("CONFIG_BUCKET_PATH")!= null ? System.getenv("CONFIG_BUCKET_PATH"): "wishfoundation.config.dev.ap-south-1";

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${abhaAPILoginCreds.hostname}")
    public String abhaRegistrationHost;

    @Value("${abhaAPILoginCreds.accessKey}")
    public String clientAccessKey;

    @Value("${abhaAPILoginCreds.secretKey}")
    public String clientSecretKey;

    @Value("${abhaAPILoginCreds.grantType}")
    public String grantType;

    @Value("${abhaAPILoginCreds.gatewaySessionHostname}")
    public String gatewayHostname;

    @Value("${abhaAPILoginCreds.abhaV2Hostname}")
    public String abhaV2Hostname;



    public static final String SALT = "$2a$10$CwTycUXWue0Thq9StjUM0u";

    public static final List<String> IGNORED_API_FOR_AUTH = new ArrayList<>(Arrays.asList("/", "/api/v1/yatri/validate-user/*", "/api/v1/yatri/login", "/api/v1/yatri/sign-up", "/actuator/**",
            "/api/v1/yatri/send-otp", "api/v1/yatri/resend-otp", "api/v1/yatri/verify-otp", "api/v1/yatri/forget-username", "api/v1/yatri/reset-password", "/api/v1/yatri/validate/user-name-phone-number",
            "/api/v1/yatri/phone-number/linked/*","/api/v1/utility/pinCode/*","/api/v1/password-encryption","/api/v1/abha-detail/*","/api/v1/yatri/excel-sign-up","/api/v1/yatri/split-file","/api/v1/yatri/get-user/Bulk-User","/api/iomt/*"));


    public static final String ABHA_V2_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAstWB95C5pHLXiYW59qyO\n" +
            "4Xb+59KYVm9Hywbo77qETZVAyc6VIsxU+UWhd/k/YtjZibCznB+HaXWX9TVTFs9N\n" +
            "wgv7LRGq5uLczpZQDrU7dnGkl/urRA8p0Jv/f8T0MZdFWQgks91uFffeBmJOb58u\n" +
            "68ZRxSYGMPe4hb9XXKDVsgoSJaRNYviH7RgAI2QhTCwLEiMqIaUX3p1SAc178ZlN\n" +
            "8qHXSSGXvhDR1GKM+y2DIyJqlzfik7lD14mDY/I4lcbftib8cv7llkybtjX1Aayf\n" +
            "Zp4XpmIXKWv8nRM488/jOAF81Bi13paKgpjQUUuwq9tb5Qd/DChytYgBTBTJFe7i\n" +
            "rDFCmTIcqPr8+IMB7tXA3YXPp3z605Z6cGoYxezUm2Nz2o6oUmarDUntDhq/PnkN\n" +
            "ergmSeSvS8gD9DHBuJkJWZweG3xOPXiKQAUBr92mdFhJGm6fitO5jsBxgpmulxpG\n" +
            "0oKDy9lAOLWSqK92JMcbMNHn4wRikdI9HSiXrrI7fLhJYTbyU3I4v5ESdEsayHXu\n" +
            "iwO/1C8y56egzKSw44GAtEpbAkTNEEfK5H5R0QnVBIXOvfeF4tzGvmkfOO6nNXU3\n" +
            "o/WAdOyV3xSQ9dqLY5MEL4sJCGY1iJBIAQ452s8v0ynJG5Yq+8hNhsCVnklCzAls\n" +
            "IzQpnSVDUVEzv17grVAw078CAwEAAQ==";

    public static final String ABHA_PHR_V1_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Zq7YKcjmccSBnR9CDHd\n" +
            "6IX96V7D/a2XSMs+yCgejSe956mqjA/0Q9h+Xnx7ZZdwe2Tf2Jq/mWXa+gYdnta5\n" +
            "8otreXg/5oGnNV3Edlixz1Oc8tJg5bG4sIUCGZcbEQGSbm1iC+Fp1kS+YLVG4Su8\n" +
            "KoRxcCvRJI2QkfqAruX3JoFjggOkv0TgWCo9z6NV6PPmPN3UsXyH3OPDi3Ewnvd6\n" +
            "4ngCUKPSBiIDwhLj2yYSShcxH8aWbrz00SJodBJzqgjvCfZuljBXXIN4Ngi/nzqE\n" +
            "J7woKQ1kNgWoHFZy7YL74PihW//4OlniSRoITX+7ChILIv2ezSmAdIjpNJ9Dg9XK\n" +
            "cQIDAQAB";


    public static final String ABHA_V3_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAstWB95C5pHLXiYW59qyO\n" +
            "4Xb+59KYVm9Hywbo77qETZVAyc6VIsxU+UWhd/k/YtjZibCznB+HaXWX9TVTFs9N\n" +
            "wgv7LRGq5uLczpZQDrU7dnGkl/urRA8p0Jv/f8T0MZdFWQgks91uFffeBmJOb58u\n" +
            "68ZRxSYGMPe4hb9XXKDVsgoSJaRNYviH7RgAI2QhTCwLEiMqIaUX3p1SAc178ZlN\n" +
            "8qHXSSGXvhDR1GKM+y2DIyJqlzfik7lD14mDY/I4lcbftib8cv7llkybtjX1Aayf\n" +
            "Zp4XpmIXKWv8nRM488/jOAF81Bi13paKgpjQUUuwq9tb5Qd/DChytYgBTBTJFe7i\n" +
            "rDFCmTIcqPr8+IMB7tXA3YXPp3z605Z6cGoYxezUm2Nz2o6oUmarDUntDhq/PnkN\n" +
            "ergmSeSvS8gD9DHBuJkJWZweG3xOPXiKQAUBr92mdFhJGm6fitO5jsBxgpmulxpG\n" +
            "0oKDy9lAOLWSqK92JMcbMNHn4wRikdI9HSiXrrI7fLhJYTbyU3I4v5ESdEsayHXu\n" +
            "iwO/1C8y56egzKSw44GAtEpbAkTNEEfK5H5R0QnVBIXOvfeF4tzGvmkfOO6nNXU3\n" +
            "o/WAdOyV3xSQ9dqLY5MEL4sJCGY1iJBIAQ452s8v0ynJG5Yq+8hNhsCVnklCzAls\n" +
            "IzQpnSVDUVEzv17grVAw078CAwEAAQ==";

    public static final String PASSWORD_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbRBFxvIAFJfMS92005gK41cBdQsFUYxkZrj61oHDByHfHYfA5I0v/vRBNFthqAyHyL2f5amhYq//3/25SE+bt7uzPWsRWTjOWhJR4nqxaOTHPlA+bP+rg3OtecaTWI2Hr9tkfLagHrqJqESuypMtnxqqnK+YIj4kKIZ6imlh4cwIDAQAB";

    public static final String PASSWORD_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJtEEXG8gAUl8xL3bTTmArjVwF1CwVRjGRmuPrWgcMHId8dh8DkjS/+9EE0W2GoDIfIvZ/lqaFir//f/blIT5u3u7M9axFZOM5aElHierFo5Mc+UD5s/6uDc615xpNYjYev22R8tqAeuomoRK7Kky2fGqqcr5giPiQohnqKaWHhzAgMBAAECgYBVhV6eaReSvl5u+f4a/v5M1N/J/rUCSDtRG9rKq/6vnj3rgpSQxFJeIMNT0tuNOQB/p4D7rpKfFM4+yWD4sKGnXR2I62ZscE6/g/W6Eyiyy98aJVCAegtqXNr7eBYSNp6k1keQh2id0mSlqT6Jl7lzT00rhcSmusJGI+khFdekcQJBAMvljKP50ijKFXTbnL8uoLBTCDIed++MXvUIB8yf7ynViIKyBqNUSMELGM+Wj7MIgd1f8IYsCzy2TVtCS2V4V4cCQQDC8TRwTZ3QM+fREHTkylEKOblaH0dZwm2XzD7Hus4QQBIBP5lywpFc+L6HCc4RWuHguLc45Pn+m6RSLHpjojq1AkBDMrkiRbBkrw1ZQROs0pI06niWBAKlGU8mVGo1nzQ0RmLCKCgV5i5AKQcZS1a6u4AJVJgxsAxYCwD9paCxpXK7AkBJq5v0oPB14VfHA2AZonez1JK+gzmUq0x9ZFuJYYhETeJABIf5/ZuvtfVS1RrCJkVchpH/d1EerjUQNOzfwExNAkEAgbs++numQLeGBA1wd7MBl0vG3bVz+h6y9RnqFtHOXHOuB0UFUCSAhi57p/tdz22XK3VSH07v15Sz0YkJ7UkIAg==";


//    ----------------------TOURISM API ENV --------------------------

    //Todo: Change it to environment
    public static final String TOURISM_API_HOST = "https://registrationandtouristcare.uk.gov.in:9113/apiuser/traveller/getpilgrimdatabyuniquecode?Uniquecode=";

    public static final String TOURISM_API_TOKEN = "cLit56fSl8fOco8BqXNCye3wcOwcdY+N3S9Hgql1qjrW1tM2G+3AjV7LsG8AIX+UpHAxkw8DbfyN4BpNUkVjaQ==";

    public static final int TOURISM_RATE_LIMIT = 3;
    public static final long TOURISM_RATE_LIMIT_PERIOD_SECONDS = 3600;

//    ----------------------------------------


    public static final String OTP_PREFIX = "otp:";
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";
    public static final int RATE_LIMIT = 5;
    public static final long RATE_LIMIT_PERIOD_SECONDS = 3600;
    public static final int ABHA_RATE_LIMIT = 3;
    public static final long ABHA_RATE_LIMIT_PERIOD_SECONDS = 1800;


    // ---------- YATRI EMAIL TEMPLATE START ----------

    public static final String USER_NAME_MARCO = "<USER_NAME>";
    public static final String OTP_MARCO = "<OTP>";
    public static final String LINK_MARCO = "<LINK>";
    public static final String YATRI_ID_MARCO = "<YATRI_ID_MARCO>";
    public static final String YATRI_PULE_LINK = !ObjectUtils.isEmpty(System.getenv("YATRI_PULE_LINK"))?  System.getenv("YATRI_PULE_LINK"): "https://web-yatripulse.centilytics.com/login";


    //
    public static final String USER_SIGN_UP_OTP_SUBJECT = "Here is your Signup OTP";

    public static final String USER_SIGN_UP_OTP_TEMPLATE = "Hello " + USER_NAME_MARCO + "! <br>" +
            "Thank you for signing up. Your OTP is: " + OTP_MARCO + ". <br>" +
            "Enter it to verify your number and complete the sign-up <br>";


    //
    public static final String RESET_PASSWORD_SUBJECT = "Here is your OTP for resetting your password.";
    public static final String RESET_PASSWORD_TEMPLATE = "Hello " + USER_NAME_MARCO + ",<br>" +
            "Your OTP is: " + OTP_MARCO + " for resetting your password.<br>" +
            "Please enter this OTP on the reset password page to complete the process.";

    public static final String UPDATE_CONTACT_DETAILS_SUBJECT = "Here is your OTP for updating contact details.";
    public static final String UPDATE_CONTACT_DETAILS_TEMPLATE = "Hello Yatri, <br>" +
            "Your OTP is: " + OTP_MARCO + " for updating your contact number.<br>" +
            "Please enter this OTP to complete the process.";


    //
    public static final String RECOVER_USERNAME_SUBJECT = "Your Username Recovery Confirmation and Login Details";
    public static final String RECOVER_USERNAME_TEMPLATE = "Hello Yatri,<br>" +
            "Your username has been recovered successfully. Your username is " + USER_NAME_MARCO +".";


    //
    public static final String AT_SIGN_UP_SUBJECT = "Medical Certificate and YatriPulse Registration ID Required for Your Upcoming Travel with Yatri Pulse";
    public static final String AT_SIGN_UP_TEMPLATE = "Hello Customer,<br>" +
            "Congratulations on successfully registering with YatriPulse! Please remember to carry your medical certificate and YatriPulse registration ID for screening on your day of travel. Your Yatri Pulse unique id is " + YATRI_ID_MARCO + "<br>" +
            "Visit: " + LINK_MARCO + " to upload your Medical Certificate before Yatra<br>" +
            "Safe travels,<br>" +
            "Yatri Pulse";


    //
    public static final String LINK_PORTAL_ID_SUBJECT = "Complete Your YatriPulse Registration Now!";
    public static final String LINK_PORTAL_ID_TEMPLATE = "Hello Customer,<br>" +
            "You have successfully created your YatriPulse unique ID! However, it is mandatory to link tourism portal ID to the Yatri pulse account. Please proceed to complete your registration on the Yatri Pulse at your earliest convenience or Visit " + LINK_MARCO + " to register.<br>" +
            "Thank you,<br>" +
            "Yatri Pulse";

    //
    public static final String LOCATE_HRF_SUBJECT = "Request to get clearance from Nearby Registered Health Facilities!";
    public static final String LOCATE_HRF_TEMPLATE = "Hello Customer,<br>" +
            "Congratulations on completing your registration! You can visit here (link) to get the details about nearby Registered Health Facility (RHF) locations to facilitate your medical certification examination before your Yatra.<br>" +
            "Best regards,<br>" +
            "Yatri Pulse";


    //
    public static final String UPLOAD_MEDICAL_CERT_SUBJECT = "Confirmation: Medical Certificate Uploaded Successfully!";
    public static final String UPLOAD_MEDICAL_CERT_TEMPLATE = "Hello Pilgrims,<br>" +
            "Your medical certificate has been successfully uploaded to our portal application. Thank you for completing this important step.<br>" +
            "Best regards,<br>" +
            "Yatri Pulse";

    //
    public static final String FORGOT_MEDICAL_CERT_SUBJECT = "Upload Your Medical Certificate for Yatra Safety";
    public static final String FORGOT_MEDICAL_CERT_TEMPLATE = "Hello Pilgrims<br>" +
            "This is a gentle reminder to upload your medical certificate before starting your yatra. Your health and safety are our top priority. Please ensure the completion of this important step.<br>" +
            "You can directly upload the document by clicking here " + LINK_MARCO + "<br>" +
            "Best regards,<br>" +
            "Yatri Pulse";

    // ---------- YATRI EMAIL TEMPLATE END ----------


    //-----------PinCode API Host ----------------------

    public static final String PIN_CODE_API_HOST = "https://api.postalpincode.in/pincode/";

    //------------------------------------HOST FOR Other Microservices----------------------------------

    @Value("${baseUrl.notificationService}")
    private String notificationService;

    @Value("${baseUrl.healthService}")
    private String healthService;
    @Value("${baseUrl.abhaService}")
    private String abhaService;
    @Value("${baseUrl.userService}")
    private String userService;


}


