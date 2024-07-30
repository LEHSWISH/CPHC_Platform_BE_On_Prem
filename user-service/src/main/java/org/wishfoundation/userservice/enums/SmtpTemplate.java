package org.wishfoundation.userservice.enums;

import lombok.Getter;

@Getter
public enum SmtpTemplate {
    TEMPLATE1("1307171422056639481","UKDGHL- Namaste %s Thank you for signing up on eSwasthya Dham! Your OTP is %s. Enter it to verify your phone number and complete the sign-up process"),
    TEMPLATE2("1307171422076891305","UKDGHL: Namaste! %s Thanks for registering with eSwasthya Dham. Please remember to carry your medical certificate and eSwasthya Dham registration ID for screening. Your eSwasthya Dham unique ID is %s; kindly upload your medical certificate before Yatra."),
    TEMPLATE3("1307171422081553046","UKDGHL: Namaste! %s, Your OTP is %s for resetting your password. Please enter this OTP on the reset password page to reset your password and complete the process."),
    TEMPLATE4("1307171627551675957","UKDGHL-Namaste! Welcome to the Health Portal, eSwasthya Dham. Here is your user ID %s and password %s. Please create your ABHA (Ayushman Bharat Health Account) using the link www.eswasthyadham.uk.gov.in before starting your yatra. Your health is important to us."),
    TEMPLATE5("1307171506506744244","UKDGHL- Namaste! Your eSwasthya Dham user ID created successfully! User Name: %s Password: %s Login now: https://eswasthyadham.uk.gov.in.");


    private String templateId;
    private String templateValue;
    SmtpTemplate(String templateId, String templateValue) {
        this.templateId = templateId;
        this.templateValue = templateValue;
    }

}
