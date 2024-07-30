package org.wishfoundation.abhaservice.request.decryption;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DecryptionRequest {

	private String encryptedData;
	private String requesterNonce;
	private String senderNonce;
	private String requesterPrivateKey;
	private String senderPublicKey;

}