package org.wishfoundation.chardhamcore.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BasicAuthConfiguration {
    private Map<String,BasicAuthProperties> organizations;

}
