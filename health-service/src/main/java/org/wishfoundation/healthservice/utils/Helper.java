package org.wishfoundation.healthservice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.wishfoundation.healthservice.exception.WishFoundationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This interface contains utility methods used throughout the application.
 */
public interface Helper {

    /**
     * ObjectMapper instance for JSON serialization and deserialization.
     * It is configured to ignore null fields, fail on unknown properties,
     * and enable indentation for pretty printing.
     */
    ObjectMapper MAPPER = new Jackson2ObjectMapperBuilder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Updates a field with a non-null value using a setter function.
     *
     * @param setter The setter function to update the field.
     * @param value  The value to set the field to.
     * @param <T>    The type of the field.
     */
    static <T> void updateFieldIfNotNull(Consumer<T> setter, T value) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    /**
     * Returns the configuration bucket path for a given region.
     *
     * @param region The region for which to get the configuration bucket path.
     * @return The configuration bucket path.
     */
    static String getConfigBucket(String region) {
        return String.format(EnvironmentConfig.CONFIG_BUCKET_PATH, region);
    }

    /**
     * Encodes a byte array to Base64.
     *
     * @param value The byte array to encode.
     * @return The Base64 encoded string.
     */
    static String encodeBytesToBase64(byte[] value) {
        return new String(Base64.encode(value));
    }

    /**
     * Decodes a Base64 string to a byte array.
     *
     * @param value The Base64 encoded string to decode.
     * @return The decoded byte array.
     */
    static byte[] decodeBase64ToBytes(String value) {
        return Base64.decode(value);
    }

    /**
     * Creates a file at the specified absolute path with the given key and content.
     *
     * @param absolutePath The absolute path where the file should be created.
     * @param key          The key (name) of the file.
     * @param fileBase64   The content of the file in Base64 format.
     */
    static void fileCreate(String absolutePath, String key, String fileBase64) {
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
            byte[] fileBytes = fileBase64.getBytes();
            Files.write(filePath, fileBytes);
        } catch (IOException e) {
            throw new WishFoundationException(HttpStatus.BAD_REQUEST.name(), "IO exception occur", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Reads the content of a file at the specified absolute path with the given key.
     *
     * @param absolutePath The absolute path of the file.
     * @param key          The key (name) of the file.
     * @return The content of the file in Base64 format.
     */
    static String getFileContent(String absolutePath, String key) {
        String fileBase64 = "";
        Path filePath = Paths.get(absolutePath, key);
        try {
            if (!Files.exists(filePath)) {
                throw new WishFoundationException(HttpStatus.NOT_FOUND.name(), "File not found", HttpStatus.NOT_FOUND);
            }
            try(InputStream inputStream = Files.newInputStream(filePath)){
                byte[] fileBytes =  inputStream.readAllBytes();
                fileBase64 +=  new String(fileBytes);
            }
        } catch (IOException e) {
            throw new WishFoundationException(e.getMessage());
        }
        return fileBase64;
    }

    /**
     * Returns the current directory's absolute path.
     *
     * @return The current directory's absolute path.
     */
    //TODO :Change once path given by @Piyush
    static String currentDirectory(){
        return Paths.get("").toAbsolutePath().toString();
    }
}
