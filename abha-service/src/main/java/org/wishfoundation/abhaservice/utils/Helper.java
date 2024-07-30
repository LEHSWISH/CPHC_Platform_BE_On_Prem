package org.wishfoundation.abhaservice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.wishfoundation.abhaservice.config.EnvironmentConfig;
import org.wishfoundation.abhaservice.exception.WishFoundationException;
import org.wishfoundation.abhaservice.keypairgen.KeyMaterial;
import org.wishfoundation.abhaservice.request.ABHAFlowChainRequest;
import org.wishfoundation.chardhamcore.enums.Gender;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.wishfoundation.abhaservice.request.ABHAFlowChainRequest.*;

@Data
@Service
@RequiredArgsConstructor
public class Helper {

    private final WebClient.Builder webClient;
    private static final SecureRandom random = new SecureRandom();

    public static final String GATEWAY_SESSION_TOKEN_HOST = "https://dev.abdm.gov.in";

    public static final String ABDM_HOST = "https://dev.abdm.gov.in";

    public static final String ABDM_X_CM_ID = "sbx";

    public static final String ABHA_REGISTRATION_API_HOST = "https://abhasbx.abdm.gov.in/abha";

    public static final String HIP_ID = "IN2710001507";
    public static final String HIP_NAME = "Ankit Singh Hospital New Delhi";

    public static final String HIU_ID = "IN2710001592";

    public static final String HIU_S3_KEY = "hiu-data/";

    public static final String DATA_PUSH_URL = StringUtils.hasLength(System.getenv("DATA_PUSH_URL")) ? System.getenv("DATA_PUSH_URL") : "http://yatripulse-dev.centilytics.com/abha-service/api/v1/abha-hiu/data-push";

    public static final String NONCE = "6i/ZyMogZ/Yo3t4H95hn9orH76z876vAkp7F4zMd+h8=";
    public static final String cryptoAlg = "ECDH";
    public static final String curve = "Curve25519";
    public static final String parameters = "Curve25519/32byte random key";
    public static final String keyValue = "MIIBMTCB6gYHKoZIzj0CATCB3gIBATArBgcqhkjOPQEBAiB/////////////////////////////////////////7TBEBCAqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqYSRShRAQge0Je0Je0Je0Je0Je0Je0Je0Je0Je0Je0JgtenHcQyGQEQQQqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq0kWiCuGaG4oIa04B7dLHdI0UySPU1+bXxhsinpxaJ+ztPZAiAQAAAAAAAAAAAAAAAAAAAAFN753qL3nNZYEmMaXPXT7QIBCANCAAQoUC7SFWl+NJqjey/5F33BSZyAcgkrUR66ULP+WN8UHHmyuxUxirL1g7Pn3URm9Zn4JSno8iOSBkoKk9Zwks8a";

    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_DIGITS = "0123456789";
    private static final String CHAR_SPECIAL = "!@#$%^&*()-_=+";
    private static final String OTP_CHARS = "0123456789";
    private static final String ALL_CHARS = CHAR_UPPER + CHAR_LOWER + CHAR_DIGITS + CHAR_SPECIAL;

    private static final String ALPHANUMERIC = CHAR_UPPER + CHAR_DIGITS;

    private final RedisTemplate redisTemplate;

    public static final String ALGORITHM = "ECDH";
    public static final String CURVE = "curve25519";
    public static final String PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

    public static final String PUID_PREFIX = "PUID-";

    private final EnvironmentConfig environmentConfig;

    public static final ObjectMapper MAPPER = new Jackson2ObjectMapperBuilder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS).build()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public  static  final String dataSecretKey = "Wish20foundation";

    public String generateGatewayToken() {
        WebClient client = webClient.baseUrl(GATEWAY_SESSION_TOKEN_HOST).build();
        Map<String, String> m = new HashMap<>();
        m.put("clientId", "SBX_004686");
        m.put("clientSecret", "c6c6bd82-dfdc-4a3b-b8f2-7eae71b4a055");
        m.put("grantType", "client_credentials");

        URI uri = UriComponentsBuilder.fromUriString(GATEWAY_SESSION_TOKEN_HOST)
                .path("/gateway/v0.5/sessions")
                .build(true)
                .toUri();
        Map<String, Object> myData = client.post().uri(uri).bodyValue(m).retrieve().bodyToMono(Map.class).onErrorMap(ex -> new RuntimeException("Failed with an error", ex))
                .block();

        String token = String.valueOf(myData.get("accessToken"));
        System.out.println("TOKEN : " + token);
        return token;
    }

    public static String getIsoTimeStamp() {
        DateTimeFormatter isoTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);
        String currentIsoTimestamp = currentDateTime.format(isoTimestampFormatter);
        return currentIsoTimestamp;
    }

    public static String getSimpleTimeStamp() {
        DateTimeFormatter simpleTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String currentSimpleTimestamp = currentDateTime.format(simpleTimestampFormatter);
        return currentSimpleTimestamp;
    }

    public static <T> void updateFieldIfNotNull(Consumer<T> setter, T value) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    public static String setGender(String gender) {
        if (ObjectUtils.isEmpty(gender)) {
            return "";
        }
        switch (gender) {
            case "F":
                return Gender.Female.toString();
            case "M":
                return Gender.Male.toString();
            default:
                return Gender.Other.toString();
        }
    }

    public static String generateRandomAlphaNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = new SecureRandom().nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(randomIndex));
        }
        Random random = new Random();
        return sb.toString() + random.nextInt(10);
    }

    public String abhaFlowChain(ABHAFlowChainRequest request) {
//        String key = HIP_KEY_PREFIX +  request.getCurrentKey();
//        redisTemplate.opsForHash().put(key, ON_INIT_KEY, request.get);
//        redisTemplate.opsForHash().put(key, "request-id", requestId);
//        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
        return "";
    }

    public void setAbhaData(ABHAFlowChainRequest request) {
        redisTemplate.opsForHash().put(request.getCurrentKey(), PREV_KEY, request.getPrevKey());
        redisTemplate.opsForHash().put(request.getCurrentKey(), NEXT_KEY, request.getNextKey());
        redisTemplate.opsForHash().put(request.getCurrentKey(), DB_KEY, request.getDbKey());
        if (StringUtils.hasLength(request.getHashKey()))
            redisTemplate.opsForHash().put(request.getCurrentKey(), request.getHashKey(), request.getBody());
        redisTemplate.expire(request.getCurrentKey(), 30, TimeUnit.MINUTES);
    }

    public String getAbhaData(ABHAFlowChainRequest request) {
        return String.valueOf(redisTemplate.opsForHash().get(request.getCurrentKey(), ON_INIT_KEY));
    }

    public String getRootRedisKey(String key) {
        if (StringUtils.hasLength(key)) {
            Object obj = redisTemplate.opsForHash().get(key, PREV_KEY);
            if (ObjectUtils.isEmpty(obj))
                return key;
            String prevKey = obj.toString();
            return getRootRedisKey(prevKey);
        }

        return key;

    }

    public static String getUniqueIdentifier() {
        return "urn:uuid:" + UUID.randomUUID();
    }

    public static String encodeBytesToBase64(byte[] value) {
        return new String(Base64.encode(value));
    }

    public static byte[] decodeBase64ToBytes(String value) {
        return Base64.decode(value);
    }

    public static byte[] calculateXorOfBytes(
            byte[] byteArrayA,
            byte[] byteArrayB
    ) {
        byte[] xorOfBytes = new byte[byteArrayA.length];
        for (int i = 0; i < byteArrayA.length; i++) {
            xorOfBytes[i] =
                    (byte) (byteArrayA[i] ^ byteArrayB[i % byteArrayB.length]);
        }
        return xorOfBytes;
    }

    // A SHA-256 HKDF for generating an AES encryption key
    public static byte[] sha256Hkdf(
            byte[] salt,
            String initialKeyMaterial,
            Integer keyLengthInBytes
    ) {
        HKDFBytesGenerator hkdfBytesGenerator = new HKDFBytesGenerator(
                new SHA256Digest()
        );
        HKDFParameters hkdfParameters = new HKDFParameters(
                decodeBase64ToBytes(initialKeyMaterial),
                salt,
                null
        );
        hkdfBytesGenerator.init(hkdfParameters);
        byte[] encryptionKey = new byte[keyLengthInBytes];
        hkdfBytesGenerator.generateBytes(encryptionKey, 0, keyLengthInBytes);
        return encryptionKey;
    }

    private static PrivateKey generateECPrivateKeyFromBase64Str(
            String base64PrivateKey
    )
            throws Exception {
        byte[] privateKeyBytes = decodeBase64ToBytes(base64PrivateKey);

        X9ECParameters ecParams = CustomNamedCurves.getByName(CURVE);
        ECParameterSpec ecParamSpec = new ECParameterSpec(
                ecParams.getCurve(),
                ecParams.getG(),
                ecParams.getN(),
                ecParams.getH(),
                ecParams.getSeed()
        );
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(
                new BigInteger(privateKeyBytes),
                ecParamSpec
        );
        return KeyFactory
                .getInstance(ALGORITHM, PROVIDER)
                .generatePrivate(privateKeySpec);
    }

    private static PublicKey generateECPublicKeyFromBase64Str(
            String base64PublicKey
    )
            throws Exception {
        byte[] publicKeyBytes = decodeBase64ToBytes(base64PublicKey);

        X9ECParameters ecParams = CustomNamedCurves.getByName(CURVE);
        ECParameterSpec ecParamSpec = new ECParameterSpec(
                ecParams.getCurve(),
                ecParams.getG(),
                ecParams.getN(),
                ecParams.getH(),
                ecParams.getSeed()
        );

        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(
                ecParamSpec.getCurve().decodePoint(publicKeyBytes),
                ecParamSpec
        );

        return KeyFactory
                .getInstance(ALGORITHM, PROVIDER)
                .generatePublic(publicKeySpec);
    }

    private static PublicKey generateX509PublicKeyFromBase64Str(
            String base64PublicKey
    )
            throws Exception {
        byte[] publicKeyBytes = decodeBase64ToBytes(base64PublicKey);

        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKeyBytes
        );
        return KeyFactory
                .getInstance(ALGORITHM, PROVIDER)
                .generatePublic(x509EncodedKeySpec);
    }

    public static String computeSharedSecret(
            String base64PrivateKey,
            String base64PublicKey
    )
            throws Exception {
        PrivateKey privateKey = generateECPrivateKeyFromBase64Str(
                base64PrivateKey
        );
        // X509 encoded base64 public key string has 412 characters
        PublicKey publicKey = base64PublicKey.length() == 88
                ? generateECPublicKeyFromBase64Str(base64PublicKey)
                : generateX509PublicKeyFromBase64Str(base64PublicKey);

        KeyAgreement keyAgreement = KeyAgreement.getInstance(
                ALGORITHM,
                PROVIDER
        );
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        byte[] sharedSecretBytes = keyAgreement.generateSecret();
        return encodeBytesToBase64(sharedSecretBytes);
    }

    public static String getConfigBucket(String region) {
        return String.format(EnvironmentConfig.CONFIG_BUCKET_PATH, region);
    }

    public static String getTempPath(){
        return System.getProperty("java.io.tmpdir");
    }

    public static KeyMaterial getKeyPairMaterial(){
       return KeyMaterial.builder()
                .privateKey("A6u0kgPL88iPC5qXTtwca+slsV8DNel80prV3sOZOeM=")
                .publicKey("BChQLtIVaX40mqN7L/kXfcFJnIByCStRHrpQs/5Y3xQcebK7FTGKsvWDs+fdRGb1mfglKejyI5IGSgqT1nCSzxo=")
                .x509PublicKey("MIIBMTCB6gYHKoZIzj0CATCB3gIBATArBgcqhkjOPQEBAiB/////////////////////////////////////////7TBEBCAqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqYSRShRAQge0Je0Je0Je0Je0Je0Je0Je0Je0Je0Je0JgtenHcQyGQEQQQqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq0kWiCuGaG4oIa04B7dLHdI0UySPU1+bXxhsinpxaJ+ztPZAiAQAAAAAAAAAAAAAAAAAAAAFN753qL3nNZYEmMaXPXT7QIBCANCAAQoUC7SFWl+NJqjey/5F33BSZyAcgkrUR66ULP+WN8UHHmyuxUxirL1g7Pn3URm9Zn4JSno8iOSBkoKk9Zwks8a")
                .nonce("6i/ZyMogZ/Yo3t4H95hn9orH76z876vAkp7F4zMd+h8=").build();
    }

    public  static KeyMaterial getKeyPairMaterialHIU(){
        return KeyMaterial.builder()
                .privateKey("DACH6Mq33RLdznshkRkB+AnEY29gGksvrgxE75ZFiBI=")
                .publicKey("BF4w6xbjRu7gdZV1YjdsrUkkyHAdBX8kC7AB/l8YvTBYXKPVctI3fxzb6vIzcKilDq2u4+NUEpdf+3WGkhUW1Eo=")
                .x509PublicKey("MIIBMTCB6gYHKoZIzj0CATCB3gIBATArBgcqhkjOPQEBAiB/////////////////////////////////////////7TBEBCAqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqYSRShRAQge0Je0Je0Je0Je0Je0Je0Je0Je0Je0Je0JgtenHcQyGQEQQQqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq0kWiCuGaG4oIa04B7dLHdI0UySPU1+bXxhsinpxaJ+ztPZAiAQAAAAAAAAAAAAAAAAAAAAFN753qL3nNZYEmMaXPXT7QIBCANCAAReMOsW40bu4HWVdWI3bK1JJMhwHQV/JAuwAf5fGL0wWFyj1XLSN38c2+ryM3CopQ6truPjVBKXX/t1hpIVFtRK")
                .nonce("nYNbfS83+QDH2q3zAFhZv0kqqv7wmUOHN9FSGqfJvzI=").build();

    }

    public static String currentDirectory(){
        return Paths.get("").toAbsolutePath().toString();
    }

    public static boolean compareLessThanOrEqualTo(String dateTimeString1, String dateTimeString2) {
        LocalDateTime dateTime1 = parseDateTimeString(dateTimeString1);
        LocalDateTime dateTime2 = parseDateTimeString(dateTimeString2);

        return !dateTime1.isAfter(dateTime2);
    }

    public static boolean compareGreaterThanOrEqualTo(String dateTimeString1, String dateTimeString2) {
        LocalDateTime dateTime1 = parseDateTimeString(dateTimeString1);
        LocalDateTime dateTime2 = parseDateTimeString(dateTimeString2);

        return !dateTime1.isBefore(dateTime2);
    }

    public static LocalDateTime parseDateTimeString(String dateTimeString) {
        OffsetDateTime odt = OffsetDateTime.parse(dateTimeString);
        return odt.toLocalDateTime();
    }

    public static String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(OTP_CHARS.length());
            otp.append(OTP_CHARS.charAt(index));
        }
        return otp.toString();
    }


    public static String encrypt(String strToEncrypt) {
        if(StringUtils.hasLength(strToEncrypt))
        try {
            SecretKeySpec secretKey = new SecretKeySpec(dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes());
            return java.util.Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return strToEncrypt;
    }

    public static String decrypt(String strToDecrypt) {
        if(StringUtils.hasLength(strToDecrypt))
        try {
            SecretKeySpec secretKey = new SecretKeySpec(dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(java.util.Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return strToDecrypt;
    }

   public static void fileCreate(String absolutePath, String key) {
        Path filePath = Paths.get(absolutePath, key);
        try {
            if (!Files.exists(filePath.getParent())) {
                try {
                    Files.createDirectories(filePath.getParent());
                } catch (IOException e) {
                    throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "IO exception occur", HttpStatus.BAD_REQUEST);
                }
            }
            if (!Files.exists(filePath)) {
                try {
                    Files.createFile(filePath);
                } catch (IOException e) {
                    throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "IO exception occur", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "IO exception occur", HttpStatus.BAD_REQUEST);
        }
    }
}
