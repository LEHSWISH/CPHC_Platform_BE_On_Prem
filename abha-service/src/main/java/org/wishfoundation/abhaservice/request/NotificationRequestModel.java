package org.wishfoundation.abhaservice.request;

import lombok.Data;

import java.util.Set;

@Data
public class NotificationRequestModel {

    // TODO PRODCUT CALL
    public static final String DEEP_LINK_SUBJECT = "Here is your OTP for (PRODUCT_TEAM)";

    public static final String USER_NAME_MARCO = "<USER_NAME>";
    public static final String OTP_MARCO = "<OTP>";

    // TODO PRODCUT CALL
    public static final String DEEP_LINK_TEMPLATE = "Hello " + USER_NAME_MARCO + ",<br>" +
            "Your OTP is: " + OTP_MARCO + " .<br>" +
            "get template from product team";

    private String subject;
    private String emailFrom;
    private Set<String> emailTo;
    private Set<String> copyTo;
    private Set<String> attachFiles;
    private String messageBody;
    private Set<String> bcc;
    private String phoneNumber;
    private String currentToken;
    private String templateKey;
}
