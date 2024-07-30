package org.wishfoundation.abhaservice.request.hiu;

import lombok.Data;

@Data
public class KeyMaterial {
    public String cryptoAlg;
    public String curve;
    public DhPublicKey dhPublicKey;
    public String nonce;
}
