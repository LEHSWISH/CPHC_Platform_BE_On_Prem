package org.wishfoundation.userservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class IotRequest {
    private String deviceId;
    private String profileId;
    private Object events;
    private List<String> profileIds;
    private String userServiceToken;
}
