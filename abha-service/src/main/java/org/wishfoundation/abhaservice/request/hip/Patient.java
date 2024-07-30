package org.wishfoundation.abhaservice.request.hip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.request.webhook.BaseHIPWebhookRequest;
import org.wishfoundation.abhaservice.request.webhook.Identifier;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    public String referenceNumber;
    public String display;
    public List<CareContextRequset> careContexts;

    // ON CONFIRM RESPONSE
    public String id;
    public String name;
    public String gender;
    public int yearOfBirth;
    public Address address;
    public List<Identifier> identifiers;
    public int dayOfBirth;
    public int monthOfBirth;

    // ON DISCOVERY
    public List<Identifier> verifiedIdentifiers;
    public List<Identifier> unverifiedIdentifiers;
    public List<String> matchedBy;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Address{
        public String line;
        public String district;
        public String state;
        public Object pincode;
    }
}
