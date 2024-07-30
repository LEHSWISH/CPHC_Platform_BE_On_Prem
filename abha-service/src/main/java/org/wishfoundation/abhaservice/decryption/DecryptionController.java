package org.wishfoundation.abhaservice.decryption;


import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.abhaservice.request.decryption.DecryptionRequest;
import org.wishfoundation.abhaservice.response.decryption.DecryptionResponse;
import org.wishfoundation.abhaservice.utils.Helper;

import java.security.Security;
import java.util.Arrays;

/**
 * Controller for handling decryption requests.
 */
@RestController
public class DecryptionController {
    static {
        try {
            // Add Bouncy Castle provider to support AES-GCM encryption
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Decrypts the encrypted data received in the request.
     *
     * @param decryptionRequest The request containing necessary parameters for decryption.
     * @return The decrypted data in the response.
     * @throws Exception If any error occurs during decryption.
     */
    @PostMapping("/decrypt")
    public DecryptionResponse decrypt(@RequestBody DecryptionRequest decryptionRequest)
        throws Exception {
        // Calculate XOR of sender and requester nonces
        byte[] xorOfNonces = Helper.calculateXorOfBytes(
                Helper.decodeBase64ToBytes(decryptionRequest.getSenderNonce()),
                Helper.decodeBase64ToBytes(decryptionRequest.getRequesterNonce())
        );

        // Extract IV and salt from the XOR of nonces
        byte[] iv = Arrays.copyOfRange(xorOfNonces, xorOfNonces.length - 12, xorOfNonces.length);
        byte[] salt = Arrays.copyOfRange(xorOfNonces, 0, 20);

        // Decrypt the data using the extracted IV, salt, and other parameters
        String decryptedData = decrypt(iv, salt, decryptionRequest.getRequesterPrivateKey(),
                decryptionRequest.getSenderPublicKey(), decryptionRequest.getEncryptedData());

        // Return the decrypted data in the response
        return new DecryptionResponse(decryptedData);
    }

    /**
     * Decrypts the encrypted data using AES-GCM encryption.
     *
     * @param iv The initialization vector for encryption.
     * @param salt The salt for key derivation.
     * @param requesterPrivateKey The private key of the requester.
     * @param senderPublicKey The public key of the sender.
     * @param encryptedDataAsBase64Str The encrypted data in base64 format.
     * @return The decrypted data as a string.
     * @throws Exception If any error occurs during decryption.
     */
    private String decrypt(byte[] iv, byte[] salt, String requesterPrivateKey, String senderPublicKey,
            String encryptedDataAsBase64Str) throws Exception {
        // Compute the shared secret using the requester's private key and sender's public key
        String sharedSecret = Helper.computeSharedSecret(requesterPrivateKey, senderPublicKey);

        // Derive the AES encryption key using SHA-256 HKDF
        byte[] aesEncryptionKey = Helper.sha256Hkdf(salt, sharedSecret, 32);

        String decryptedData = "";
        try {
            // Decode the encrypted data from base64 format
            byte[] encryptedBytes = Helper.decodeBase64ToBytes(encryptedDataAsBase64Str);

            // Initialize the GCM block cipher with AES engine
            GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());

            // Set the parameters for the cipher (AES encryption key, IV, and salt)
            AEADParameters parameters = new AEADParameters(new KeyParameter(aesEncryptionKey), 128, iv, null);

            // Initialize the cipher for decryption
            cipher.init(false, parameters);

            // Process the encrypted bytes and obtain the decrypted bytes
            byte[] cipherBytes = new byte[cipher.getOutputSize(encryptedBytes.length)];
            int encryptedBytesLength = cipher.processBytes(encryptedBytes, 0, encryptedBytes.length, cipherBytes, 0);
            cipher.doFinal(cipherBytes, encryptedBytesLength);

            // Convert the decrypted bytes to a string
            decryptedData = new String(cipherBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the decrypted data
        return decryptedData;
    }
}
