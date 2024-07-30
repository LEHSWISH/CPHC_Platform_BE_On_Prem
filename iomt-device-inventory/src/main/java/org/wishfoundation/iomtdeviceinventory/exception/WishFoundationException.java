package org.wishfoundation.iomtdeviceinventory.exception;

import org.springframework.http.HttpStatus;

public class WishFoundationException extends RuntimeException{

    @java.io.Serial
    static final long serialVersionUID = -7034897190745788562L;

    private String code;
    private String message;

    private HttpStatus statusCode;

    public WishFoundationException(String message){
        super(message);
    }

    public WishFoundationException(String code ,String message){
        super(message);
        this.code = code;
    }

    public WishFoundationException(String code ,String message,HttpStatus statusCode){
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}

