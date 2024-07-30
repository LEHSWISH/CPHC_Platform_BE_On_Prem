package org.wishfoundation.abhaservice.request.encryption;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EncryptionRequest {

	private String stringToEncrypt;
	private String senderNonce;
	private String requesterNonce;
	private String senderPrivateKey;
	private String requesterPublicKey;

}
