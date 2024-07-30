package org.wishfoundation.userservice.exception;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExceptionDefinition implements Serializable {

    private static final long serialVersionUID = 1645819808925306680L;
    private String message;
    private Class<?> exceptionClass;
}
