package org.wishfoundation.userservice.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicAuthProperties {
    private String username;
    private String password;
}
