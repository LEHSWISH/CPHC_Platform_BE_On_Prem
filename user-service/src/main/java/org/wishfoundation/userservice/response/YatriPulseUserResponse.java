package org.wishfoundation.userservice.response;

import org.wishfoundation.userservice.enums.GovernmentIdType;

import lombok.Data;
import org.wishfoundation.userservice.response.abha.ABHAUserResponse;

import java.util.List;
import java.util.UUID;

@Data
public class YatriPulseUserResponse {
	private String userName;
	private String phoneNumber;
	private UUID abhaUserId;
	private GovernmentIdType governmentIdType;
	private String governmentId;
	private List<DocumentsPathResponse> documentsPath;
	private YatriDetailsResponse yatriDetails;
	private MedicalsReportsResponse medicalsReports;
	private TourismUserDetails tourismUserInfo;
	private ABHAUserResponse abhaUserDetails;
}
