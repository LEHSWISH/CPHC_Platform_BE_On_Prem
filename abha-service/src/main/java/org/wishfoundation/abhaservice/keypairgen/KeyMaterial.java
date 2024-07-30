package org.wishfoundation.abhaservice.keypairgen;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class KeyMaterial {

	private String privateKey;
	private String publicKey;
	private String x509PublicKey;
	private String nonce;
}
