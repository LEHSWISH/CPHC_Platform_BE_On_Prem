package org.wishfoundation.iomtdeviceinventory.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_VALUE("IDI001", " value should be provided"),
    INVALID_TOKEN("IDI002", "Unable to verify token"),
    LOGIN_FAILED("IDI003", "Unable to login");

    private final String code, message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
