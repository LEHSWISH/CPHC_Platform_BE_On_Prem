package org.wishfoundation.superadmin.request;

import lombok.Data;

import java.util.Set;

@Data
public class EmailRequestModel {

    private String fromMail;
    private String subject;
    private String toMail;
    private String messageBody;
}
