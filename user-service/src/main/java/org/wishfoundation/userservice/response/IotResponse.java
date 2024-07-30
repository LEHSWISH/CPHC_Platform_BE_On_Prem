package org.wishfoundation.userservice.response;

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
