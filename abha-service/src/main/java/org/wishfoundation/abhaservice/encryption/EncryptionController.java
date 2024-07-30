package org.wishfoundation.abhaservice.encryption;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.abhaservice.request.encryption.EncryptionRequest;
import org.wishfoundation.abhaservice.response.encryption.EncryptionResponse;
import org.wishfoundation.abhaservice.utils.Helper;

import java.security.Security;
import java.util.Arrays;
/**
 * Controller for handling encryption requests.
 */
@RestController
public class EncryptionController {
    static {
        try {
            // Add Bouncy Castle provider to support AES-GCM encryption
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Endpoint for encrypting data.
     *
     * @param encryptionRequest The request containing necessary parameters for encryption.
     * @return The encrypted data in the response.
     * @throws Exception If any error occurs during encryption.
     */
    @PostMapping("/encrypt")
    public EncryptionResponse encrypt(@RequestBody EncryptionRequest encryptionRequest)
        throws Exception {
        // Calculate XOR of sender and requester nonces
        byte[] xorOfNonces = Helper.calculateXorOfBytes(
                Helper.decodeBase64ToBytes(encryptionRequest.getSenderNonce()),
                Helper.decodeBase64ToBytes(encryptionRequest.getRequesterNonce())
        );

        // Extract IV and salt from XOR of nonces
        byte[] iv = Arrays.copyOfRange(xorOfNonces, xorOfNonces.length - 12, xorOfNonces.length);
        byte[] salt = Arrays.copyOfRange(xorOfNonces, 0, 20);

        // Encrypt the data using the extracted IV, salt, sender private key, requester public key, and string to encrypt
        String encryptedData = encrypt(iv, salt, encryptionRequest.getSenderPrivateKey(), encryptionRequest.getRequesterPublicKey(), encryptionRequest.getStringToEncrypt());

        // Return the encrypted data in the response
        return new EncryptionResponse(encryptedData);
    }

    /**
     * Encrypts the given string using AES-GCM encryption.
     *
     * @param iv The initialization vector.
     * @param salt The salt for key derivation.
     * @param senderPrivateKey The sender's private key.
     * @param requesterPublicKey The requester's public key.
     * @param stringToEncrypt The string to encrypt.
     * @return The encrypted data.
     * @throws Exception If any error occurs during encryption.
     */
    private String encrypt(byte[] iv, byte[] salt, String senderPrivateKey, String requesterPublicKey, String stringToEncrypt)
        throws Exception {
        // Compute the shared secret using sender's private key and requester's public key
        String sharedSecret = Helper.computeSharedSecret(senderPrivateKey, requesterPublicKey);

        // Derive AES encryption key using SHA-256 HKDF with the salt and shared secret
        byte[] aesEncryptionKey = Helper.sha256Hkdf(salt, sharedSecret, 32);

        String encryptedData = "";
        try {
            // Convert the string to encrypt to bytes
            byte[] stringBytes = stringToEncrypt.getBytes();

            // Create a GCMBlockCipher with AES engine
            GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());

            // Set the encryption parameters
            AEADParameters parameters = new AEADParameters(new KeyParameter(aesEncryptionKey), 128, iv, null);

            // Initialize the cipher for encryption
            cipher.init(true, parameters);

            // Process the input bytes and get the encrypted bytes
            byte[] cipherBytes = new byte[cipher.getOutputSize(stringBytes.length)];
            int encryptedBytesLength = cipher.processBytes(stringBytes, 0, stringBytes.length, cipherBytes, 0);

            // Do the final encryption
            cipher.doFinal(cipherBytes, encryptedBytesLength);

            // Encode the encrypted bytes to Base64
            encryptedData = Helper.encodeBytesToBase64(cipherBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the encrypted data
        return encryptedData;
    }
}
