package org.wishfoundation.abhaservice.request.hiu;

import lombok.Data;

@Data
public class DhPublicKey {
    public String expiry;
    public String parameters;
    public String keyValue;
}
