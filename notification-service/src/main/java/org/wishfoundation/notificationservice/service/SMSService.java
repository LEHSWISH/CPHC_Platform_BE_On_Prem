package org.wishfoundation.notificationservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.notificationservice.models.NotificationRequestModel;
import org.wishfoundation.notificationservice.models.NotificationStatus;

/**
 * This interface defines the contract for sending SMS notifications.
 * It provides a method to send a notification to a specific number using SMTP.
 */
public interface SMSService {

    /**
     * Sends an SMS notification using SMTP.
     *
     * @param notificationRequestModel The model containing the necessary information for sending the notification.
     * @return A ResponseEntity containing the status of the notification.
     *         The status will indicate whether the notification was sent successfully or not.
     */
    ResponseEntity<NotificationStatus> smtpNotifyNumber(NotificationRequestModel notificationRequestModel);

}
