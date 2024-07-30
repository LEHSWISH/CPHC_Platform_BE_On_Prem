package org.wishfoundation.abhaservice.keypairgen;


import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.abhaservice.utils.Helper;

import java.security.*;

import static org.wishfoundation.abhaservice.utils.Helper.*;

@RestController
public class KeyPairGenController {
	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/generate-key-pair")
	public KeyMaterial generate() throws Exception {
		KeyPair keyPair = generateKeyPair();
		String privateKey = getEncodedPrivateKeyAsBase64Str(
			keyPair.getPrivate()
		);
		String publicKey = getEncodedPublicKeyAsBase64Str(keyPair.getPublic());
		String x509PublicKey = getX509EncodedPublicKeyAsBase64Str(
			keyPair.getPublic()
		);
		String nonce = generateBase64Nonce();
		return new KeyMaterial(privateKey, publicKey, x509PublicKey, nonce);
	}

	private KeyPair generateKeyPair()
		throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			ALGORITHM,
			PROVIDER
		);
		X9ECParameters ecParams = CustomNamedCurves.getByName(CURVE);
		ECParameterSpec ecSpec = new ECParameterSpec(
			ecParams.getCurve(),
			ecParams.getG(),
			ecParams.getN(),
			ecParams.getH(),
			ecParams.getSeed()
		);

		keyPairGenerator.initialize(ecSpec, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	private String getEncodedPrivateKeyAsBase64Str(PrivateKey privateKey)
		throws Exception {
		ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;
		return Helper.encodeBytesToBase64(ecPrivateKey.getD().toByteArray());
	}

	private String getEncodedPublicKeyAsBase64Str(PublicKey publicKey)
		throws Exception {
		ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
		return Helper.encodeBytesToBase64(ecPublicKey.getQ().getEncoded(false));
	}

	private String getX509EncodedPublicKeyAsBase64Str(PublicKey publicKey)
		throws Exception {
		ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
		return Helper.encodeBytesToBase64(ecPublicKey.getEncoded());
	}

	private String generateBase64Nonce() {
		byte[] salt = new byte[32];
		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);
		return Helper.encodeBytesToBase64(salt);
	}
}
