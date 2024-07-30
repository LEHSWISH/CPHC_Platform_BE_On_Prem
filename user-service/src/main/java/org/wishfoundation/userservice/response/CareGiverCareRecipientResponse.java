package org.wishfoundation.userservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.wishfoundation.userservice.enums.RequestStatus;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CareGiverCareRecipientResponse {
    private UUID id;
    private String userName;
    private String phoneNumber;
    private String fullName;
    private RequestStatus requestStatus;
    private List<CareGiverCareRecipientResponse> careGiverRequests;
    private List<UserBasicDetailsResp> careGiverRecipient;
    private List<UserBasicDetailsResp> careGiver;
    public CareGiverCareRecipientResponse(UUID id, String userName, String phoneNumber) {
        this.id = id;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }

    public CareGiverCareRecipientResponse(String userName, String fullName) {
        this.userName = userName;
        this.fullName = fullName;
    }
}
