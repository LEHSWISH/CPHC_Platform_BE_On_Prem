package org.wishfoundation.abhaservice.request.hiu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class HIUPermission {
    public String accessMode;
    public DateRange dateRange;
    public String dataEraseAt;
    public HIUFrequency frequency;


}
