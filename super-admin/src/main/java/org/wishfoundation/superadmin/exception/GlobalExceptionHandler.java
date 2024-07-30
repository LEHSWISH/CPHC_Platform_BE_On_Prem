package org.wishfoundation.superadmin.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.wishfoundation.superadmin.enums.ErrorCode;
import org.wishfoundation.superadmin.response.ErrorResponse;
import org.wishfoundation.superadmin.utils.Helper;


import java.util.*;
import java.util.stream.Collectors;
/**
 * @author Sandeep kumar
 * This class is responsible for handling global exceptions in the application.
 * It uses a ResourceBundle to retrieve error messages based on field validation errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * The locale used for retrieving error messages.
     */
    private final Locale locale = new Locale("en", "US");

    /**
     * The ResourceBundle used for retrieving error messages.
     */
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("ValidationMessages", locale);

    /**
     * This method handles MethodArgumentNotValidException, which is thrown when a method argument fails validation.
     * It maps the field errors to a list of error maps and returns them in the response.
     *
     * @param ex The MethodArgumentNotValidException that occurred.
     * @return A ResponseEntity containing the list of error maps and a BAD_REQUEST status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(this::getErrorMap).collect(Collectors.toList());
        return new ResponseEntity<>(errors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * This method maps a FieldError to an error map containing the error code and message.
     *
     * @param fieldError The FieldError to be mapped.
     * @return A Map containing the error code and message.
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
 * This method handles WishFoundationException, which is thrown when a custom exception is thrown in the application.
 * It sets the HTTP status code and error response based on the exception's properties.
 *
 * @param ex The WishFoundationException that occurred.
 * @param request The WebRequest object representing the current request.
 * @return A ResponseEntity containing the error response and the appropriate HTTP status code.
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
 * This method handles DataIntegrityViolationException, which is thrown when a database operation violates a constraint.
 * It sets the HTTP status code and error response based on the exception's message.
 *
 * @param ex The DataIntegrityViolationException that occurred.
 * @param request The WebRequest object representing the current request.
 * @return A ResponseEntity containing the error response and the appropriate HTTP status code.
 */
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ErrorResponse> handleWishFoundationException(DataIntegrityViolationException ex, WebRequest request) {
    HttpStatus status;
    String msg = "";
    if (ex.getMessage().contains("value too long for type")) {
        msg = "Value is too long.";
    } else if (ex.getMessage().contains("duplicate key value violates unique constraint")) {
        msg = "Entry already exists.";
    } else {
        msg = "An error occurred while performing database operations.";
    }
    status = getStatus(ex);

    ErrorResponse errorResponse = new ErrorResponse(status, msg);
    return new ResponseEntity<>(errorResponse, status);
}

/**
 * This method handles WebClientCustomException, which is thrown when a web client request fails.
 * It sets the HTTP status code and error response based on the exception's properties.
 *
 * @param ex The WebClientCustomException that occurred.
 * @param request The WebRequest object representing the current request.
 * @return A ResponseEntity containing the error response and the appropriate HTTP status code.
 * @throws JsonProcessingException If there is an error parsing the exception's response body.
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
 * This method handles any uncaught exceptions.
 * It sets the HTTP status code and error response based on the exception's properties.
 *
 * @param ex The Exception that occurred.
 * @param request The WebRequest object representing the current request.
 * @return A ResponseEntity containing the error response and the appropriate HTTP status code.
 */
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
    HttpStatus status = getStatus(ex);
    ErrorResponse errorResponse = new ErrorResponse(status, ex.getMessage());
    return new ResponseEntity<>(errorResponse, status);
}

/**
 * This method retrieves the HTTP status code for a given exception.
 * It checks if the exception has a ResponseStatus annotation and returns its value if present.
 * Otherwise, it returns the default INTERNAL_SERVER_ERROR status code.
 *
 * @param ex The Exception for which to retrieve the HTTP status code.
 * @return The HTTP status code for the given exception.
 */
private HttpStatus getStatus(Exception ex) {
    ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
    if (responseStatus != null) {
        return responseStatus.value();
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
}
}

