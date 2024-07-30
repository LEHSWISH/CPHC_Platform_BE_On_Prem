package org.wishfoundation.abhaservice.request;

import lombok.Data;
import org.wishfoundation.abhaservice.enums.RequesterType;

import java.sql.Types;

@Data
public class ABHAFlowChainRequest {

    public static final String PREV_KEY = "PREV_KEY";
    public static final String NEXT_KEY = "NEXT_KEY";
    public static final String DB_KEY = "DB_KEY";

    public static final String HIP_KEY_PREFIX = "HIP_";
    public static final String HIU_KEY_PREFIX = "HIU_";
    public static final String ON_INIT_KEY = "on-init";
    public static final String CARE_CONTEXT_KEY = "care-context";
    public static final String YATRI_UUID = "yatri-uuid";
    public static final String CONSENT_ARTEFACT_ID = "consent-artefact-id";
    public static final String DATE_TO = "date_to";
    public static final String DATE_FROM = "date_from";
    public static final String DATE_EXP = "date_exp";
    public static final String HIP_TYPES = "hip_types";
    public static final String HIP_DISPLAY = "hip_display";
    public static final String MOBILE = "mobile";
    public static final String OTP = "otp";
    public static final String ABHA_ID = "abha-id";


    private String body;
    private String prevKey;
    private String currentKey;
    private String nextKey;
//    private RequesterType currentKeyType;
    private String hashKey;
    private String dbKey;

}
