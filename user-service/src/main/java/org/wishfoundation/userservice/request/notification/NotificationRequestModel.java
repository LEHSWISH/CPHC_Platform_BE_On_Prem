package org.wishfoundation.userservice.request.notification;

import lombok.Data;

import java.util.Set;

@Data
public class NotificationRequestModel {
    private String subject;
    private String emailFrom;
    private Set<String> emailTo;
    private Set<String> copyTo;
    private Set<String> attachFiles;
    private String messageBody;
    private Set<String> bcc;
    private String phoneNumber;
    private String templateId;
}
