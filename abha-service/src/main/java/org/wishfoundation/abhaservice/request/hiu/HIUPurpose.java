package org.wishfoundation.abhaservice.request.hiu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class HIUPurpose {
    // DEFAULT VALUE.
    public String text = "string";
    public String code = "PATRQT";
    public String refUri;
}
