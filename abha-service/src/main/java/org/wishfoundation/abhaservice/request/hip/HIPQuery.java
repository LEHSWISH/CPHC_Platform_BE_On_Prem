package org.wishfoundation.abhaservice.request.hip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.enums.AuthMode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class HIPQuery {
    private String id;
    private String purpose;
    private AuthMode authMode;
    private HIPRequester requester;
}
