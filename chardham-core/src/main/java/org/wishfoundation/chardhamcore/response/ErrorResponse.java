package org.wishfoundation.chardhamcore.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Getter
@Setter
public class ErrorResponse implements Serializable {

    private final long serialVersionUID = 1645819808925306680L;

    private int status;
    private String code;
    private String message;

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.code = status.getReasonPhrase();
        this.message = message;
    }

    public ErrorResponse(HttpStatus status, String message, String code) {
        this.status = status.value();
        this.code = code;
        this.message = message;
    }

}
