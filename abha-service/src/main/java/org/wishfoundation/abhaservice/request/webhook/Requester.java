package org.wishfoundation.abhaservice.request.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class Requester {
    public String type;
    public String id;
}
