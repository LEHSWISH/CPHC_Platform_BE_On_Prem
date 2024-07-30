package org.wishfoundation.notificationservice.exception;

public class WishFoundationException extends RuntimeException{

    @java.io.Serial
    static final long serialVersionUID = -7034897190745788562L;

    private String code;
    private String message;

    public WishFoundationException(String message){
        super(message);
    }

    public WishFoundationException(String code ,String message){
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
