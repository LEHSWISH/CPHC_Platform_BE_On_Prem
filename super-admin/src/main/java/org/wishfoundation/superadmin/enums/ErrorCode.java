package org.wishfoundation.superadmin.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    USERNAME_ALREADY_EXISTS("SA001", "An account with this username already exists."),
    PASSWORD_MISMATCH("SA002", "Password does not meet the required conditions."),
    INVALID_PHONE_NUMBER("SA003", "Please enter a valid phone number."),
    PHONE_NUMBER_ALREADY_LINKED("SA004", "You cannot link the same phone number with more than 6 users."),
    ENTER_OTP("SA005", "Please enter OTP to continue."),
    INVALID_OTP("SA006", "Invalid OTP. Try again by clicking on resend."),
    NO_ATTEMPTS_LEFT("SA007", "No attempts left. Please try again in 1 hour."),
    INVALID_REQUEST("SA008", "Invalid Request."),
    VALUE_SHOULD_BE_PROVIDED("SA009", " should be provided."),
    USERNAME_MISMATCH("SA010", "Username does not meet the required conditions."),
    USER_IS_NOT_PRESENT("SA011", "User is not present."),
    PHONE_NUMBER_IS_NOT_MATCHED("SA012", "The entered phone number doesn't match with the registered username."),
    USER_NOT_VALID("SA013", "You are not authorized."),
    UNABLE_TO_GET_SESSION_ID("USR47", "Unable to get session id.");
    private final String code, message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String formatMessage(String dynamicValue) {
        return String.format(message, dynamicValue);
    }
}
