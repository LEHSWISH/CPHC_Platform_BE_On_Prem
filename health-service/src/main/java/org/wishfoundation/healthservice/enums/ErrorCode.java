package org.wishfoundation.healthservice.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

	INVALID_STATE_CODE("HS001", "Please provide a valid State Code"),
	INVALID_STATE_NAME("HS002", "Please provide a valid State Name"),
	INVALID_DISTRICT_NAME("HS003", "Please provide a valid District Name"),
	INVALID_DISTRICT_CODE("HS004", "Please provide a valid District Code"),
	VALUE_SHOULD_BE_PROVIDED("HS005", " should be provided."),
	INVALID_FACILITY_ID("HS006", "Please provide a valid Facility ID"),
	INVALID_FACILITY_NAME("HS007", "Please provide a valid Facility Name"),
	INVALID_FACILITY_STATUS("HS008", "Please provide a valid Facility Status"),
	INVALID_FACILITY_TYPE("HS009", "Please provide a valid Facility Type"),
	INVALID_FACILITY_OWNERSHIP("HS010", "Please provide a valid Facility Ownership"),
	INVALID_FACILITY_ADDRESS("HS011", "Please provide a valid Facility Address"),
	INVALID_STATE_AND_DISTRICT("HS0012", "Please provide a valid State or District");

	private final String code, message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
