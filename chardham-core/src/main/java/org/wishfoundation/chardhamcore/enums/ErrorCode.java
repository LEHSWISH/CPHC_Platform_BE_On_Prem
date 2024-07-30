package org.wishfoundation.chardhamcore.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

	USERNAME_ALREADY_EXISTS("USR001", "An account with this username already exists."),
	PASSWORD_MISMATCH("USR002", "Password does not meet the required conditions."),
	INVALID_PHONE_NUMBER("USR003", "Please enter a valid phone number."),
	PHONE_NUMBER_ALREADY_LINKED("USR004", "You cannot link the same phone number with more than 100 users."),
	POLICIES_NOT_AGREED("USR005", "Please agree to policies to proceed."),
	ENTER_OTP("USR006", "Please enter OTP to continue."),
	INVALID_OTP("USR007", "Invalid OTP. Try again by clicking on resend."),
	NO_ATTEMPTS_LEFT("USR008", "No attempts left. Please try again in 1 hour."),
	ABHA_NUMBER_ALREADY_EXISTS("USR009", "ABHA Number already exists."),
	INVALID_AADHAAR_NUMBER("USR010", "Invalid Aadhaar number. Please try again."),
	INVALID_VOTER_ID("USR011", "Invalid Voter ID number. Please try again."),
	INVALID_PASSPORT("USR012", "Invalid Passport. Please try again."),
	INVALID_PAN_CARD("USR013", "Invalid PAN card number. Please try again."),
	INVALID_DRIVING_LICENSE("USR014", "Invalid Driving License number. Please try again."),
	GOVERNMENT_ID_ALREADY_EXISTS("USR015", "An account with this 'Government ID type' already exists."),
	INCORRECT_IDTP("USR016", "Incorrect IDTP. You have %s more attempts left."),
	PHONE_NUMBER_NOT_LINKED("USR017", "This phone number is not linked with any username."),
	ABHA_NUMBER_NOT_LINKED("USR018", "This ABHA number is not linked with any username."),
	GOVERNMENT_ID_NOT_LINKED("USR019", "This Government ID is not linked with any username."),
	INVALID_REQUEST("USR020", "Invalid Request."),
	VALUE_SHOULD_BE_PROVIDED("USR021", " should be provided."),
	USERNAME_MISMATCH("USR022", "Username does not meet the required conditions."),
	USER_IS_NOT_PRESENT("USR023", "User is not present."),
	ABHA_USER_DETAILS_IS_NOT_PRESENT("USR024", "ABHA user details is not present."),
	ACCEPT_POLICES("USR025", "Please agree to policies to proceed."),
	PHONE_NUMBER_IS_NOT_MATCHED("USR026", "The entered phone number doesn't match with the registered username."),
	INVALID_GOVERNMENT_ID("USR027", "Invalid Government Id"),
	END_DATE_MUST_BE_GREATER_THEN_START_DATE("USR028", "Invalid entry. Ensure that your Yatra end date is not before the start date."),
	INVALID_TRANSACTION_ID("USR028", "Invalid transaction Id. Please provide a valid transaction id."),
	INVALID_HEALTH_ID("HIS-422" , "Unable to process the current request due to incorrect data entered."),
	INVALID_PIN_CODE("USR029","Invalid PinCode"),
	UNCONSENT_REQUEST("403" , "Please accept terms and conditions to continue."),
	OTP_RATE_LIMIT("429" , "OTP generation limit exceeded, please try again after 30 min."),
	USER_IS_NOT_FOUND_WITH_GOV_TYPE_AND_ID("USR030", "Provided government ID is not linked with the given phone number."),
	PROVIDE_ANY_ONE_EITHER_GOV_ID_OR_ABHA_NUMBER("USR031", "Please provide either a government ID or an Abha number."),
	IDTP_IS_ALREADY_LINKED("USR32","IDTP Id is already linked."),
	GOVERNMENT_ID_IS_ALREADY_LINKED("USR33","Government Id is already linked."),
	EVAIDYA_USER_ID_ALREADY_EXISTS("USR34", "Evaidya user id already exists."),
	VITALS_ARE_NOT_PRESENT("USR035", "Vitals are not present.");

	private final String code, message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String formatMessage(String dynamicValue) {
		return String.format(message, dynamicValue);
	}
}
