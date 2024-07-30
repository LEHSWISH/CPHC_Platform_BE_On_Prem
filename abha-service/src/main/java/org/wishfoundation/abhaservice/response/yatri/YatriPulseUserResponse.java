package org.wishfoundation.abhaservice.response.yatri;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class YatriPulseUserResponse {
	private String userName;
	private String phoneNumber;
	private UUID abhaUserId;
	private String governmentId;
	private YatriDetailsResponse yatriDetails;
}
