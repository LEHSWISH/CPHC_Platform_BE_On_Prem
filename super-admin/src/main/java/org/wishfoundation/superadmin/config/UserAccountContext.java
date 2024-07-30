package org.wishfoundation.superadmin.config;

import org.springframework.stereotype.Component;


import java.util.UUID;

/**
 * This class is used to maintain the context of the currently logged-in user.
 * It uses ThreadLocal to store the user's information in a thread-safe manner.
 * This allows for easy access to the user's information across different parts of the application.
 */
@Component
public class UserAccountContext {

    /**
     * ThreadLocal to store the current user's email id.
     */
    private static ThreadLocal<String> currentEmailId = new InheritableThreadLocal<>();

    /**
     * ThreadLocal to store the current user's authentication token.
     */
    private static ThreadLocal<String> currentToken = new InheritableThreadLocal<>();

    /**
     * ThreadLocal to store the current user's phone number.
     */
    private static ThreadLocal<String> currentPhoneNumber = new InheritableThreadLocal<>();

    /**
     * ThreadLocal to store the current user's id.
     */
    private static ThreadLocal<UUID> currentUserId = new InheritableThreadLocal<>();

    /**
     * Sets the current user's id.
     *
     * @param userId The id of the current user.
     */
    public static final void setUserId(UUID userId) {
        currentUserId.set(userId);
    }

    /**
     * Gets the current user's id.
     *
     * @return The id of the current user.
     */
    public static final UUID getUserId() {
        return currentUserId.get();
    }

    /**
     * Gets the current user's email id.
     *
     * @return The email id of the current user.
     */
    public static final String getCurrentEmailId() {
        return currentEmailId.get();
    }

    /**
     * Sets the current user's email id.
     *
     * @param emailId The email id of the current user.
     */
    public static final void setCurrentEmailId(String emailId) {
        currentEmailId.set(emailId);
    }

    /**
     * Gets the current user's authentication token.
     *
     * @return The authentication token of the current user.
     */
    public static final String getCurrentToken() {
        return currentToken.get();
    }

    /**
     * Sets the current user's authentication token.
     *
     * @param token The authentication token of the current user.
     */
    public static final void setCurrentToken(String token) {
        currentToken.set(token);
    }

    /**
     * Gets the current user's phone number.
     *
     * @return The phone number of the current user.
     */
    public static final String getCurrentPhoneNumber() {
        return currentPhoneNumber.get();
    }

    /**
     * Sets the current user's phone number.
     *
     * @param phoneNumber The phone number of the current user.
     */
    public static final void setCurrentPhoneNumber(String phoneNumber) {
        currentPhoneNumber.set(phoneNumber);
    }

    /**
     * Clears the user context by removing all stored information.
     */
    public static void clear() {
        currentEmailId.remove();
        currentToken.remove();
        currentPhoneNumber.remove();
        currentUserId.remove();
    }

}
