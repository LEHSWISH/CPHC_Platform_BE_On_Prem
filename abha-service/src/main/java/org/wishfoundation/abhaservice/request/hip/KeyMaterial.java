package org.wishfoundation.abhaservice.request.hip;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class KeyMaterial {
    public String cryptoAlg;
    public String curve;
    public DhPublicKey dhPublicKey;
    public String nonce;
}
