package org.wishfoundation.healthservice.enums;

import lombok.Getter;

@Getter
public enum FacilityStatus {

    APPROVED("Approved"),
    SUBMITTED("Submitted"),

    REJECTED("Rejected"),

    RESUBMITTED("Resubmitted"),
    QUERY_RAISED("QueryRaised"),

    QUERY_UPDATED("QueryUpdated");

    private final String status;

    FacilityStatus(String status){
        this.status = status;
    }
}
