package org.wishfoundation.userservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EncryptKeyRequest {
    private String keyToEncrypt;
    private String publicKey;
    private String cipherType;


    // PASSWORD DECRYPTION
    private String privateKey;

}
