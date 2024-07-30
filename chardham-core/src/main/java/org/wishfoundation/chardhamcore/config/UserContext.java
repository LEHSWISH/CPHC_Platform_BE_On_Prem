package org.wishfoundation.chardhamcore.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserContext {

	private static ThreadLocal<String> currentUserName = new InheritableThreadLocal<>();

	private static ThreadLocal<String> currentToken = new InheritableThreadLocal<>();

	private static ThreadLocal<String> currentPhoneNumber = new InheritableThreadLocal<>();

	private static ThreadLocal<UUID> currentUserId = new InheritableThreadLocal<>();

	private static ThreadLocal<String> currentOrganization = new InheritableThreadLocal<>();

	public static final void setUserId(UUID userId) {
		currentUserId.set(userId);
	}

	public static final UUID getUserId() {
		return currentUserId.get();
	}

	public static final String getCurrentUserName() {
		return currentUserName.get();
	}

	public static final void setCurrentUserName(String userName) {
		currentUserName.set(userName);
	}


	public static final String getCurrentToken() {
		return currentToken.get();
	}

	public static final void setCurrentToken(String token) {
		currentToken.set(token);
	}

	public static final String getCurrentPhoneNumber() {
		return currentPhoneNumber.get();
	}

	public static final void setCurrentPhoneNumber(String phoneNumber) {
		currentPhoneNumber.set(phoneNumber);
	}

	public static final String getCurrentOrganization() {
		return currentOrganization.get();
	}

	public static final void setCurrentOrganization(String organization) {
		currentOrganization.set(organization);
	}

	public static void clear() {
		currentUserName.remove();
		currentToken.remove();
		currentPhoneNumber.remove();
		currentUserId.remove();
		currentOrganization.remove();
	}

}
