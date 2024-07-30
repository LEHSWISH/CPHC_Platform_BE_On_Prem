package org.wishfoundation.userservice.enums;

import lombok.Getter;

@Getter
public enum CipherType {
    RSA ("RSA"),
    RSA_ECB_PKCS1Padding("RSA/ECB/PKCS1Padding"),
    RSA_ECB_OAEPWithSHA_1AndMGF1Padding("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");

    private final String cipherType;

    CipherType(String cipherType){
        this.cipherType = cipherType;
    }


}
