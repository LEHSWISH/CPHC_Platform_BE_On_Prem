package org.wishfoundation.abhaservice.request.hiu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DateRange {
    public String from;
    @JsonProperty("to")
    public String myto;
}
