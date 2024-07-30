package org.wishfoundation.userservice.enums;

import lombok.Getter;

@Getter
public enum AbhaScope {

    ABHA_ENROL("abha-enrol"),
    MOBILE_VERIFY("mobile-verify"),

    ABHA_LOGIN("abha-login"),

    AADHAAR_VERIFY("aadhaar-verify");

    private final String scope;

    AbhaScope(String scope) {
        this.scope = scope;
    }
}
