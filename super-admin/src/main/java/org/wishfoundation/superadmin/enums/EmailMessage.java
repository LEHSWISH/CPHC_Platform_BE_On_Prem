package org.wishfoundation.superadmin.enums;

import lombok.Getter;

@Getter
public enum EmailMessage {

    OTP_MESSAGE_BODY("Hello %s,  <br><br>  Your One-Time Password to change password is <b>%s</b>.  <br><br>  For your security, please refrain from sharing this OTP with anyone.  <br><br>  Best regards,<br> Team Char Dham"),
    REGISTER_MESSAGE_BODY("Hello {Name} <br> Congratulations! You have been registered as a %s, %s and %s for the Char Dham Yatra. Please use the below credentials to log in to the portal. URL: {Attach web link} Email: %s Password: %s Best regards, Team Char Dham");

    private String templateValue;
    EmailMessage(String templateValue) {
        this.templateValue = templateValue;
    }
}
