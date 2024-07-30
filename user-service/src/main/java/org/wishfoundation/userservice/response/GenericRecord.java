package org.wishfoundation.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GenericRecord {

    private Instant createdOn;
    private Instant lastUpdatedOn;
}
