package org.wishfoundation.notificationservice.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.notificationservice.models.NotificationRequestModel;
import org.wishfoundation.notificationservice.models.NotificationStatus;
import org.wishfoundation.notificationservice.service.EmailServiceImpl;

@RestController
@RequestMapping("/api/v1/email")
public class SESMailController {

    @Autowired
    EmailServiceImpl emailService;

    @PostMapping(path = "/send-email")
    public NotificationStatus sendEmailSES(@RequestBody NotificationRequestModel emailRequest) {
        return emailService.sendEmailSES(emailRequest);
    }

    // MAIL OVER SMTP SERVER
    @PostMapping(path = "/send-email-smtp")
    public NotificationStatus sendSimpleMail(@RequestBody NotificationRequestModel emailRequest) {
        return emailService.smtpEmailServer(emailRequest);
    }

    @PostMapping("/send-email-super-admin")
    public NotificationStatus sendSuperAdminMail(@RequestBody NotificationRequestModel emailRequest){
        return emailService.sendSuperAdminMail(emailRequest);
    }



}
