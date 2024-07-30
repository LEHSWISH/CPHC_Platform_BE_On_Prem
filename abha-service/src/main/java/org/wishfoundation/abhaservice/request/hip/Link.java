package org.wishfoundation.abhaservice.request.hip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.request.webhook.Meta;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class Link {
    public String accessToken;
    public Patient patient;

    // M2 ON-INIT (DEEP LINK)
    public String referenceNumber;
    public String authenticationType;
    public Meta meta;
}
