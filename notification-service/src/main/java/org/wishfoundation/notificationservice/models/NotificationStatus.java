package org.wishfoundation.notificationservice.models;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class NotificationStatus {
    private HttpStatus status;
    private String message;
}
