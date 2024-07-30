package org.wishfoundation.userservice.config;

import org.springframework.stereotype.Component;
import org.wishfoundation.userservice.enums.CareType;

import java.util.UUID;

/**
 * This class is responsible for maintaining the user context throughout the application.
 * It uses ThreadLocal variables to store user-specific information such as user ID, username, token, phone number, organization, and care type.
 * The class provides methods to set and get these values, as well as a clear method to remove all user context information.
 */
@Component
public class UserContext {

    private static ThreadLocal<String> currentUserName = new InheritableThreadLocal<>();
    private static ThreadLocal<String> currentToken = new InheritableThreadLocal<>();
    private static ThreadLocal<String> currentPhoneNumber = new InheritableThreadLocal<>();
    private static ThreadLocal<UUID> currentUserId = new InheritableThreadLocal<>();
    private static ThreadLocal<String> currentOrganization = new InheritableThreadLocal<>();
    private static ThreadLocal<CareType> currentCareType = new InheritableThreadLocal<>();

    /**
     * Sets the current care type.
     *
     * @param careType The care type to be set.
     */
    public static final void setCareType(CareType careType) {
        currentCareType.set(careType);
    }

    /**
     * Gets the current care type.
     *
     * @return The current care type.
     */
    public static final CareType getCareType() {
        return currentCareType.get();
    }

    /**
     * Sets the current user ID.
     *
     * @param userId The user ID to be set.
     */
    public static final void setUserId(UUID userId) {
        currentUserId.set(userId);
    }

    /**
     * Gets the current user ID.
     *
     * @return The current user ID.
     */
    public static final UUID getUserId() {
        return currentUserId.get();
    }

    /**
     * Gets the current user name.
     *
     * @return The current user name.
     */
    public static final String getCurrentUserName() {
        return currentUserName.get();
    }

    /**
     * Sets the current user name.
     *
     * @param userName The user name to be set.
     */
    public static final void setCurrentUserName(String userName) {
        currentUserName.set(userName);
    }

    /**
     * Gets the current token.
     *
     * @return The current token.
     */
    public static final String getCurrentToken() {
        return currentToken.get();
    }

    /**
     * Sets the current token.
     *
     * @param token The token to be set.
     */
    public static final void setCurrentToken(String token) {
        currentToken.set(token);
    }

    /**
     * Gets the current phone number.
     *
     * @return The current phone number.
     */
    public static final String getCurrentPhoneNumber() {
        return currentPhoneNumber.get();
    }

    /**
     * Sets the current phone number.
     *
     * @param phoneNumber The phone number to be set.
     */
    public static final void setCurrentPhoneNumber(String phoneNumber) {
        currentPhoneNumber.set(phoneNumber);
    }

    /**
     * Gets the current organization.
     *
     * @return The current organization.
     */
    public static final String getCurrentOrganization() {
        return currentOrganization.get();
    }

    /**
     * Sets the current organization.
     *
     * @param organization The organization to be set.
     */
    public static final void setCurrentOrganization(String organization) {
        currentOrganization.set(organization);
    }

    /**
     * Clears all user context information.
     */
    public static void clear() {
        currentUserName.remove();
        currentToken.remove();
        currentPhoneNumber.remove();
        currentUserId.remove();
        currentOrganization.remove();
        currentCareType.remove();
    }
}
