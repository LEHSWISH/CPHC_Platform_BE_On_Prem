package org.wishfoundation.abhaservice.request.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.enums.AuthMode;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Meta{
    public String hint;
    public String expiry;

    // M2 ON-INIT (DEEP LINK)
    public String communicationMedium;
    public String communicationHint;
    public String communicationExpiry;
}