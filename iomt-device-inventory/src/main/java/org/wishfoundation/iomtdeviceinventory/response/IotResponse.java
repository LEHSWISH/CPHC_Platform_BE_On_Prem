package org.wishfoundation.iomtdeviceinventory.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IotResponse {
    private int code;
    private String message;

}
