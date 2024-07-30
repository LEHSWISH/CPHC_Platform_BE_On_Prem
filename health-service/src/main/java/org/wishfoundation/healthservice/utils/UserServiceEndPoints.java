package org.wishfoundation.healthservice.utils;

import lombok.Getter;

@Getter
public class UserServiceEndPoints {
    //YatriPulseUserController
    public static final String FETCH_MEDICAL_REPORTS = "/api/v1/yatri/fetch/medical-reports";

    public  static  final String  UPDATE_USER_DETAILS = "/api/v1/yatri/update";
    public  static  final String  DELETE_MEDICAL_DOCUMENTS = "/api/v1/yatri/delete-medical-document";


}

