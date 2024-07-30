package org.wishfoundation.notificationservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class EnvironmentConfig {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${stmp.username}")
    private String username;

    @Value("${stmp.password}")
    private String password;

    @Value("${stmp.senderId}")
    private String senderId;

    @Value("${stmp.entityId}")
    private String entityId;

    public static final String YATRI_PULSE_FROM_EMAIL = "noreply@yatripulse.in";


    public static final String USER_NAME_MARCO = "<USER_NAME>";
    public static final String OTP_MARCO = "<OTP>";
    public static final String LINK_MARCO = "<LINK>";
    public static final String YATRI_ID_MARCO = "<YATRI_ID_MARCO>";

    public static final String REQUEST_ID = "<REQUEST_ID>";
    public static final String NAME = "<NAME>";
    public static final String USERNAME = "<USERNAME>";
    public static final String  CONTACT_NUMBER = "<CONTACT_NUMBER>";
    public static final String  BODY_MESSAGE = "<BODY_MESSAGE>";


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


    //
    public static final String RECOVER_USERNAME_SUBJECT = "Your Username Recovery Confirmation and Login Details";
    public static final String RECOVER_USERNAME_TEMPLATE = "Hello Yatri,<br>" +
            "Your username has been recovered successfully. Your username is " + USER_NAME_MARCO + ". To access your account, please use the following link to log in: " + LINK_MARCO + ".<br>" +
            "If you have any further questions or need assistance, feel free to reach out to our support team.<br>" +
            "Best regards,<br>" +
            "Team Yatri Pulse";


    //
    public static final String AT_SIGN_UP_SUBJECT = "Medical Certificate and YatriPulse Registration ID Required for Your Upcoming Travel with Yatri Pulse";
    public static final String AT_SIGN_UP_TEMPLATE = "Hello Customer,<br>" +
            "Congratulations on successfully registering with YatriPulse! Please remember to carry your medical certificate and YatriPulse registration ID for screening on your day of travel. Your Yatri Pulse unique id is " + YATRI_ID_MARCO + "<br>" +
            "Visit: (link) to upload your Medical Certificate before Yatra<br>" +
            "Safe travels,<br>" +
            "Yatri Pulse";


    //
    public static final String LINK_PORTAL_ID_SUBJECT = "Complete Your YatriPulse Registration Now!";
    public static final String LINK_PORTAL_ID_TEMPLATE = "Hello Customer,<br>" +
            "You have successfully created your YatriPulse unique ID! However, it is mandatory to link tourism portal ID to the Yatri pulse account. Please proceed to complete your registration on the Yatri Pulse at your earliest convenience or Visit (link) to register.<br>" +
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

    public static final String SMTP_MESSAGE_BODY_HEADER_RESPONSE = "Hello team," + "<br><br>" +
            "We have received a request from a Yatri." + "<br><br>" +
            "Name : " + NAME + "<br>";
    public static final String SMTP_MESSAGE_USERNAME = "Username : " + USERNAME + "<br>" ;
    public static final String SMTP_MESSAGE_BODY_MIDDLE_RESPONSE = "Contact Number : " + CONTACT_NUMBER + "<br>";
    public static final String SMTP_IMAGE_ATTACHMENT = "Attachment : Image attached" + "<br>" ;
    public static final String SMTP_MESSAGE_BODY_FOOTER_RESPONSE = "<br>Message : " + BODY_MESSAGE  + "<br><br>" +
            "We request you to please assist the Yatri." + "<br><br>" +
            "Thanks.";
}
