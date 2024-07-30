package org.wishfoundation.abhaservice.request.hip;

import lombok.*;

import java.util.Date;
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DhPublicKey {
    public String expiry;
    public String parameters;
    public String keyValue;
}
