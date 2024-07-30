package org.wishfoundation.userservice.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.userservice.enums.CipherType;
import org.wishfoundation.userservice.request.EncryptKeyRequest;
import org.wishfoundation.userservice.utils.EnvironmentConfig;
import org.wishfoundation.userservice.utils.Helper;

@RestController
@RequestMapping("/api/v1/password-encryption")
public class PasswordEncryptionController {

    @PostMapping
    public ResponseEntity<String> passwordEncryption(@RequestBody EncryptKeyRequest encryptKeyRequest){
         String password = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(encryptKeyRequest.getKeyToEncrypt())
                .publicKey(EnvironmentConfig.PASSWORD_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_PKCS1Padding.getCipherType()).build());
        return ResponseEntity.ok(password);
    }


}
