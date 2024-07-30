package org.wishfoundation.userservice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.wishfoundation.userservice.enums.Gender;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.EncryptKeyRequest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Helper {

    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_DIGITS = "0123456789";
    private static final String CHAR_SPECIAL = "!@#$%^&*()-_=+";
    private static final String ALL_CHARS = CHAR_UPPER + CHAR_LOWER + CHAR_DIGITS + CHAR_SPECIAL;

    private static final String ALPHANUMERIC = CHAR_UPPER + CHAR_DIGITS;
    private static final int EXCEL_TO_JSON_LIMIT = 100000;

    public static final ObjectMapper MAPPER = new Jackson2ObjectMapperBuilder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS).build()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String getTempPath() {
        return System.getProperty("java.io.tmpdir");
    }

    public static LocalDate parseDateIntoLocalDate(String date) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);

        return localDate;

    }

    /**
     * This method is used to encrypt a given key using a public RSA key.
     *
     * @param encryptKeyRequest An object containing the public RSA key and the key to encrypt.
     * @return The encrypted key as a Base64-encoded string.
     * @throws WishFoundationException If an error occurs during encryption.
     */
    public static String getEncryptedValue(EncryptKeyRequest encryptKeyRequest) {
        try {
            // Extract the public RSA key from the request
            String publicKey = encryptKeyRequest.getPublicKey();
            publicKey = publicKey.replaceAll("\\n", "");
            // Decode the public RSA key from Base64
            byte[] keyContentAsBytes = Base64.getDecoder().decode(publicKey);

            // Create a KeyFactory object to generate the public key
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyContentAsBytes);
            PublicKey key = keyFactory.generatePublic(publicKeySpec);

            // Extract the key to encrypt from the request
            String secretMessage = encryptKeyRequest.getKeyToEncrypt();

            // Create a Cipher object to encrypt the key
            Cipher encryptCipher = Cipher.getInstance(encryptKeyRequest.getCipherType());

            // Initialize the Cipher object for encryption
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);

            // Convert the key to encrypt to bytes
            byte[] secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8);

            // Encrypt the key
            byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

            // Encode the encrypted key as a Base64-encoded string
            return Base64.getEncoder().encodeToString(encryptedMessageBytes);
        } catch (Exception e) {
            // Throw a custom exception if an error occurs during encryption
            throw new WishFoundationException(e.getMessage());
        }
    }

    public static String getdencryptedValue(EncryptKeyRequest encryptKeyRequest) {
        try {
            PrivateKey privateKey = null;

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encryptKeyRequest.getPrivateKey().getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);

            byte[] secretMessageBytes = Base64.getDecoder().decode(encryptKeyRequest.getKeyToEncrypt().getBytes(StandardCharsets.UTF_8));

            Cipher dencryptCipher = Cipher.getInstance(encryptKeyRequest.getCipherType());
            dencryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(dencryptCipher.doFinal(secretMessageBytes));

        } catch (Exception e) {
            e.printStackTrace();
            throw new WishFoundationException(e.getMessage());
        }
    }

    public static String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(OTP_CHARS.length());
            otp.append(OTP_CHARS.charAt(index));
        }
        return otp.toString();
    }

    public static String getIsoTimeStamp() {
        DateTimeFormatter isoTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime currentDateTime = LocalDateTime.now();
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

    /**
     * Encrypts a given string using the AES algorithm with a specific secret key.
     *
     * @param strToEncrypt The string to encrypt.
     * @return The encrypted string, encoded in Base64.
     * @throws Exception If an error occurs during encryption.
     */
    public static String encrypt(String strToEncrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(EnvironmentConfig.dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    /**
     * Decrypts a given encrypted string using the AES algorithm with a specific secret key.
     *
     * @param strToDecrypt The encrypted string to decrypt.
     * @return The decrypted string.
     * @throws Exception If an error occurs during decryption.
     */
    public static String decrypt(String strToDecrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(EnvironmentConfig.dataSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return strToDecrypt;
    }

    /**
     * Encrypts a list of strings using the AES algorithm with a specific secret key.
     *
     * @param strings The list of strings to encrypt.
     * @return The list of encrypted strings, encoded in Base64.
     * @throws Exception If an error occurs during encryption.
     */
    public static List<String> encryptList(List<String> strings) {
        try {
            List<String> encryptedStrings = new ArrayList<>();
            SecretKey secretKey = new SecretKeySpec(EnvironmentConfig.dataSecretKey.getBytes(), "AES");
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

    /**
     * Decrypts a list of encrypted strings using the AES algorithm with a specific secret key.
     *
     * @param encryptedStrings The list of encrypted strings to decrypt.
     * @return The list of decrypted strings.
     * @throws Exception If an error occurs during decryption.
     */
    public static List<String> decryptList(List<String> encryptedStrings) {
        try {
            List<String> decryptedStrings = new ArrayList<>();
            SecretKey secretKey = new SecretKeySpec(EnvironmentConfig.dataSecretKey.getBytes(), "AES");
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


    public static UUID parseUUIDFromString(String uuidString) {
        return UUID.fromString(uuidString);
    }

    /**
     * Generates a random password of the specified length.
     * The password will contain at least one character from each character set: uppercase letters, lowercase letters, digits, and special characters.
     * The remaining characters will be randomly selected from all character sets.
     * The password will be shuffled to make it more random.
     *
     * @param length The desired length of the password.
     * @return The generated password as a string.
     */
    public static String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure the password contains at least one character from each character set
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_DIGITS.charAt(random.nextInt(CHAR_DIGITS.length())));
        password.append(CHAR_SPECIAL.charAt(random.nextInt(CHAR_SPECIAL.length())));

        // Fill the rest of the password with random characters from all character sets
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the characters in the password to make it more random
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(length);
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(randomIndex));
            password.setCharAt(randomIndex, temp);
        }

        return password.toString();
    }

    /**
     * Generates a random alphanumeric string of the specified length.
     * The string will contain a mix of uppercase letters, lowercase letters, and digits.
     * The last character of the string will be a random digit.
     *
     * @param length The desired length of the alphanumeric string.
     * @return The generated alphanumeric string as a string.
     */
    public static String generateRandomAlphaNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(randomIndex));
        }
        Random random = new Random();
        return sb.toString() + random.nextInt(10);
    }

    /**
     * Returns the configuration bucket path for the specified region.
     * The configuration bucket path is formatted using the region and a predefined constant.
     *
     * @param region The region for which the configuration bucket path is required.
     * @return The configuration bucket path as a string.
     */
    public static String getConfigBucket(String region) {
        return String.format(EnvironmentConfig.CONFIG_BUCKET_PATH, region);
    }

    /**
     * Encrypts a given string using the AES/CBC/NoPadding algorithm with a specific key and initialization vector (IV).
     * key and iv values should be  same as present in frontend
     * Used for username list encryption
     *
     * @param data The string to encrypt.
     * @return The encrypted string, encoded in Base64.
     * @throws Exception If an error occurs during encryption.
     */
    public static String encryptDataCBCNoPadding(String data) {
        try {
            String key = "Wishfoundation24";
            String iv = "Wishfoundation95";

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(padString(data).getBytes());

            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(encrypted);
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }

        return source;
    }


    /**
     * Decrypts a string of encrypted data using the AES/CBC/NoPadding algorithm.
     * key and iv values should be  same as present in frontend
     * Used for Aadhaar encryption
     *
     * @param data The encrypted data to decrypt.
     * @return The decrypted data as a string.
     * @throws Exception If an error occurs during decryption.
     */
    public static String decryptData(String data) {

        try {
            // Define the encryption key and initialization vector (IV)
            String key = "Wishfoundation24";
            String iv = "Wishfoundation95";

            // Decode the encrypted data from Base64
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] encrypted1 = decoder.decode(data);

            // Create a cipher object with the specified algorithm and mode
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            // Create a secret key from the encryption key
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");

            // Create an initialization vector from the IV
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            // Initialize the cipher object for decryption
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            // Decrypt the encrypted data
            byte[] original = cipher.doFinal(encrypted1);

            // Convert the decrypted bytes to a string and return it
            String originalString = new String(original);

            return originalString.trim();
        } catch (Exception e) {
            // Log or handle the exception
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String currentDirectory() {
        return Paths.get("").toAbsolutePath().toString();
    }

    static String encodeBytesToBase64(byte[] value) {
        return new String(org.bouncycastle.util.encoders.Base64.encode(value));
    }

    static byte[] decodeBase64ToBytes(String value) {
        return org.bouncycastle.util.encoders.Base64.decode(value);
    }

    public static void fileCreate(String absolutePath, String key, byte[] bytes) {
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
            Files.write(filePath, bytes);
        } catch (IOException e) {
            throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "IO exception occur", HttpStatus.BAD_REQUEST);
        }
    }

    public static String getFileContent(String absolutePath, String key) {
        String fileBase64 = "";
        Path filePath = Paths.get(absolutePath, key);
        try {
            if (!Files.exists(filePath)) {
                throw new WishFoundationException(HttpStatus.NOT_FOUND.name(), "File not found", HttpStatus.NOT_FOUND);
            }
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                byte[] fileBytes = inputStream.readAllBytes();
                fileBase64 += encodeBytesToBase64(fileBytes);
            }
        } catch (IOException e) {
            throw new WishFoundationException(e.getMessage());
        }
        return fileBase64;
    }

    //
    //

    /**
     * Decrypts a password using AES/CBC/NoPadding algorithm.
     * key and iv values should be  same as present in frontend
     * Used for password decryption
     *
     * @param data The encrypted password to decrypt.
     * @return The decrypted password as a string.
     * @throws Exception If an error occurs during decryption.
     */
    public static String decryptPassword(String data) {

        try {
            // Define the encryption key and initialization vector (IV)
            String key = "v#N/R1V]5z1Nb%|7";
            String iv = "aN[6|3s-O29x_n:c";

            // Decode the encrypted data from Base64
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] encrypted1 = decoder.decode(data);

            // Create a cipher object with the specified algorithm and mode
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            // Create a secret key from the encryption key
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");

            // Create an initialization vector from the IV
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            // Initialize the cipher object for decryption
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            // Decrypt the encrypted data
            byte[] original = cipher.doFinal(encrypted1);

            // Convert the decrypted bytes to a string and return it
            String originalString = new String(original);

            return originalString.trim();
        } catch (Exception e) {
            // Log or handle the exception
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static boolean validateFileType(MultipartFile requestFile) {
        return requestFile.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    /**
     * Converts an Excel file to a list of JSON objects, with each JSON object representing a row in the Excel file.
     * The method reads the Excel file in chunks to handle large files efficiently.
     * It uses Apache POI library to read the Excel file and Jackson library to convert the JSON objects.
     *
     * @param excelRequest The input stream of the Excel file.
     * @return A ResponseEntity containing a StreamingResponseBody that streams the zipped JSON objects to the client.
     * @throws WishFoundationException If an IO exception occurs during file processing.
     */
    public static ResponseEntity<StreamingResponseBody> convertExceltoList(InputStream excelRequest) {
        List<List<Map<String, String>>> finalFileList = new ArrayList<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(excelRequest);
            XSSFSheet sheet = workbook.getSheet("data");
            // Calculate the total number of rows in the sheet (excluding the header row)
            int size = sheet.getPhysicalNumberOfRows() - 1;
            System.out.println("The Size of Excel Uploaded contains , " + size + " entries . ");
            int iterations = (size / EXCEL_TO_JSON_LIMIT);
            int firstPtr = 1;
            int lastPtr = EXCEL_TO_JSON_LIMIT;
            Row headerRow = sheet.getRow(0);
            // Process each chunk of the Excel file
            for (int chunk = 0; chunk <= iterations; chunk++) {
                List<Map<String, String>> jsonData = new ArrayList<>();

                // Adjust the last pointer if the current chunk is the last chunk
                if (firstPtr + 100000 > size) {
                    lastPtr = sheet.getLastRowNum();
                }

                // Process each row in the current chunk
                for (int i = firstPtr; i <= lastPtr; i++) {
                    Row row = sheet.getRow(i);
                    Map<String, String> jsonRow = new HashMap<>();
                    // Process each cell in the current row
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        String header = headerRow.getCell(j).getStringCellValue();
                        String value = cellToString(cell);
                        jsonRow.put(header, value);
                    }
                    jsonData.add(jsonRow);
                }
                finalFileList.add(jsonData);
                // Update the pointers for the next chunk
                firstPtr = lastPtr;
                lastPtr = firstPtr + EXCEL_TO_JSON_LIMIT;
            }

            // Create a streaming response body to stream the zipped JSON objects to the client
            StreamingResponseBody stream = out -> {
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(out)) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

                    for (int i = 0; i < finalFileList.size(); i++) {
                        ByteArrayOutputStream jsonStream = new ByteArrayOutputStream();
                        // Write the JSON data for the current chunk to the ByteArrayOutputStream
                        writer.writeValue(jsonStream, finalFileList.get(i));
                        // Add JSON file to the zip
                        ZipEntry zipEntry = new ZipEntry("Bulk-User_" + (i + 1) + ".json");
                        zipOutputStream.putNextEntry(zipEntry);
                        zipOutputStream.write(jsonStream.toByteArray());
                        zipOutputStream.closeEntry();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            // Create a new HttpHeaders object to set the content type and content disposition for the response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Bulk-user_" + Instant.now() + "_.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stream);
        } catch (Exception e) {
            throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "IO exception occur", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Converts a cell value to a string based on its cell type.
     *
     * @param cell The cell to convert.
     * @return The converted string value.
     */
    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Check if the cell is formatted as a date
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Convert the numeric value to a long and return as a string
                    return Long.toString((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
