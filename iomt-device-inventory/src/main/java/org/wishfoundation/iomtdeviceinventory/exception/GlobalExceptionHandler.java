package org.wishfoundation.iomtdeviceinventory.exception;

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
import org.wishfoundation.iomtdeviceinventory.response.ErrorResponse;
import org.wishfoundation.iomtdeviceinventory.enums.ErrorCode;
import org.wishfoundation.iomtdeviceinventory.utils.Helper;


import java.util.*;
import java.util.stream.Collectors;

/**
 * A global exception handler for the application.
 * It handles exceptions thrown by the controllers and provides appropriate responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * The locale for the validation messages.
     */
    private final Locale locale = new Locale("en", "US");

    /**
     * The resource bundle for the validation messages.
     */
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("ValidationMessages", locale);


    /**
     * Handles MethodArgumentNotValidException, which is thrown when a method argument fails validation.
     *
     * @param ex The MethodArgumentNotValidException to handle.
     * @return A ResponseEntity containing a list of error messages and a 400 Bad Request status code.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(this::getErrorMap).collect(Collectors.toList());
        return new ResponseEntity<>(errors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Maps a FieldError to a map containing the error code and message.
     *
     * @param fieldError The FieldError to map.
     * @return A map containing the error code and message.
     */
    private Map<String, String> getErrorMap(FieldError fieldError) {
        String message = resourceBundle.getString(fieldError.getDefaultMessage());
        String errorMessage = ErrorCode.valueOf(message).getMessage();
        String errorCode = ErrorCode.valueOf(message).getCode();
        return Map.of(
                "code", errorCode,
                "message", errorMessage
        );
    }

    /**
     * Handles WishFoundationException, which is a custom exception thrown by the application.
     *
     * @param ex The WishFoundationException to handle.
     * @param request The WebRequest for the current request.
     * @return A ResponseEntity containing an ErrorResponse and the appropriate status code.
     */
    @ExceptionHandler(WishFoundationException.class)
    public ResponseEntity<ErrorResponse> handleWishFoundationException(WishFoundationException ex, WebRequest request) {
        HttpStatus status;
        if (ex.getStatusCode() != null) {
            status = ex.getStatusCode();
        } else {
            status = getStatus(ex);
        }
        ErrorResponse errorResponse = new ErrorResponse(status, ex.getMessage(), ex.getCode());
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles WebClientCustomException, which is a custom exception thrown when a web client request fails.
     *
     * @param ex The WebClientCustomException to handle.
     * @param request The WebRequest for the current request.
     * @return A ResponseEntity containing a map of error details and the appropriate status code.
     * @throws JsonProcessingException If there is an error parsing the response body.
     */
    @ExceptionHandler(WebClientCustomException.class)
    public ResponseEntity<Map<String, Object>> handleWebClientResponseException(WebClientCustomException ex, WebRequest request) throws JsonProcessingException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatusCode());
        body.put("error", ex.getStatusText());
        body.put("message", "Failed with an error");
        if (!ex.getResponseBody().startsWith("{") || !ex.getResponseBody().startsWith("[")) {
            body.put("errorDetails", ex.getResponseBody());
        } else {
            body.put("errorDetails", Helper.MAPPER.readValue(ex.getResponseBody(), new TypeReference<>() {
            }));
        }
        return new ResponseEntity<>(body, HttpStatus.valueOf(ex.getStatusCode()));
    }

    /**
     * Handles any other exceptions that are not explicitly handled.
     *
     * @param ex The Exception to handle.
     * @param request The WebRequest for the current request.
     * @return A ResponseEntity containing an ErrorResponse and the appropriate status code.
     */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
//        HttpStatus status = getStatus(ex);
//        ErrorResponse errorResponse = new ErrorResponse(status, ex.getMessage());
//        return new ResponseEntity<>(errorResponse, status);
//    }

    /**
     * Determines the appropriate status code for an exception.
     *
     * @param ex The Exception to get the status code for.
     * @return The appropriate status code.
     */
    private HttpStatus getStatus(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}



