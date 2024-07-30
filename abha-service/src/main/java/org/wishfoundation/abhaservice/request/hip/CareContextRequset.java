package org.wishfoundation.abhaservice.request.hip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class CareContextRequset {
    public String referenceNumber;
    public String display;

    // FOR M3 ON FETCH & M2 NOTIFY
    public String patientReference;
    public String careContextReference;
}
