package org.wishfoundation.abhaservice.request.hip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.enums.RequesterType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class HIPRequester {
    private RequesterType type;
    private String id;
    private String name;
}
