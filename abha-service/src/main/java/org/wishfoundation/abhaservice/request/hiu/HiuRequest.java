package org.wishfoundation.abhaservice.request.hiu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.wishfoundation.abhaservice.utils.Helper.HIU_ID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class HiuRequest {
    public String id=HIU_ID;
}
