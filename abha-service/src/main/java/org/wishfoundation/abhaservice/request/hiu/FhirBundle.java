package org.wishfoundation.abhaservice.request.hiu;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FhirBundle {
    public String content;
}
