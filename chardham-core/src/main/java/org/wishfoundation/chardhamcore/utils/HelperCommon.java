package org.wishfoundation.chardhamcore.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class HelperCommon {

    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();
    public static final ObjectMapper MAPPER = new Jackson2ObjectMapperBuilder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS).build()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String encrypt(String strToEncrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(EnvironmentConfigCommon.dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(EnvironmentConfigCommon.dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return strToDecrypt;
    }

    public static List<String> encryptList(List<String> strings) {
        try {
            List<String> encryptedStrings = new ArrayList<>();
            SecretKey secretKey = new SecretKeySpec(EnvironmentConfigCommon.dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            for (String str : strings) {
                byte[] encryptedBytes = cipher.doFinal(str.getBytes());
                String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
                encryptedStrings.add(encryptedString);
            }
            return encryptedStrings;
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }


    public static List<String> decryptList(List<String> encryptedStrings) {
        try {
            List<String> decryptedStrings = new ArrayList<>();
            SecretKey secretKey = new SecretKeySpec(EnvironmentConfigCommon.dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            for (String encryptedString : encryptedStrings) {
                byte[] encryptedBytes = Base64.getDecoder().decode(encryptedString);
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                String decryptedString = new String(decryptedBytes);
                decryptedStrings.add(decryptedString);
            }
            return decryptedStrings;
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return encryptedStrings;
    }
}
