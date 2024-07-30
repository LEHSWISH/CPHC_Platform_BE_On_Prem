package org.wishfoundation.abhaservice.response.hiu;

import lombok.Data;
import org.wishfoundation.abhaservice.entity.Consent;

@Data
public class BaseHIUModel {
    private String id;
    private String message;
    private Consent consent;
}
