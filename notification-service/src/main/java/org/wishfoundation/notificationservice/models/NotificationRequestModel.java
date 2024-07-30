package org.wishfoundation.notificationservice.models;

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
    private String currentToken;
    private String templateKey;
    private String templateId;
    private String fileName;
    private String name;
    private String requestId;
}
