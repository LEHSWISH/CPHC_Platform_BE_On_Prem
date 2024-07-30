package org.wishfoundation.iomtdeviceinventory.exception;

import java.util.Map;

public class WebClientCustomException extends RuntimeException {

    private final int statusCode;
    private final String statusText;
    private final Map<String, String> headers;
    private final String responseBody;

    public WebClientCustomException(int statusCode, String statusText, Map<String, String> headers, String responseBody) {
        super("Failed with an error - Status: " + statusCode + ", Text: " + statusText);

        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
