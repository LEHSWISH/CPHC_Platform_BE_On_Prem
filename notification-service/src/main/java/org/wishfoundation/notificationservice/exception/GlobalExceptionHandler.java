package org.wishfoundation.notificationservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.wishfoundation.chardhamcore.enums.ErrorCode;
import org.wishfoundation.chardhamcore.response.ErrorResponse;
import org.wishfoundation.chardhamcore.utils.HelperCommon;


import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Locale locale = new Locale("en", "US");
    private final  ResourceBundle resourceBundle = ResourceBundle.getBundle("ValidationMessages", locale);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(this::getErrorMap).collect(Collectors.toList());
        return new ResponseEntity<>(errors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    private Map<String, String> getErrorMap(FieldError fieldError) {
        String message = resourceBundle.getString(fieldError.getDefaultMessage());
        String errorMessage = ErrorCode.valueOf(message).getMessage();
        String errorCode = ErrorCode.valueOf(message).getCode();
        return  Map.of(
                "code", errorCode,
                "message", errorMessage
        );
    }

    @ExceptionHandler(WishFoundationException.class)
    public ResponseEntity<ErrorResponse> handleWishFoundationException(WishFoundationException ex, WebRequest request) {
        HttpStatus status = getStatus(ex);
        ErrorResponse errorResponse = new ErrorResponse(status, ex.getMessage(), ex.getCode());
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(WebClientCustomException.class)
    public ResponseEntity<Map<String,Object>> handleWebClientResponseException(WebClientCustomException ex, WebRequest request) throws JsonProcessingException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatusCode());
        body.put("error", ex.getStatusText());
//        body.put("headers", ex.getHeaders()); For headers
        body.put("message", "Failed with an error");
        body.put("errorDetails", HelperCommon.MAPPER.readValue(ex.getResponseBody(), new TypeReference<>() {
        }));

        return new ResponseEntity<>(body, HttpStatus.valueOf(ex.getStatusCode()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        HttpStatus status = getStatus(ex);
        ErrorResponse errorResponse = new ErrorResponse(status, ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }

    private HttpStatus getStatus(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

