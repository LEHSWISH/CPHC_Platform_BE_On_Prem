package org.wishfoundation.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.wishfoundation.notificationservice.config.EnvironmentConfig;
import org.wishfoundation.notificationservice.exception.WishFoundationException;
import org.wishfoundation.notificationservice.models.NotificationRequestModel;
import org.wishfoundation.notificationservice.models.NotificationStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This class is responsible for sending SMS notifications using the HTTP GET method.
 * It implements the SMSService interface and utilizes the EnvironmentConfig for accessing necessary credentials.
 */
@Service
public class SMSServiceImpl implements SMSService {

    /**
     * Autowired instance of EnvironmentConfig for accessing necessary credentials.
     */
    @Autowired
    EnvironmentConfig environmentConfig;

    /**
     * This method is used to send a single SMS notification to a specified phone number.
     *
     * @param notificationRequestModel The model containing the necessary details for sending the SMS.
     * @return ResponseEntity<NotificationStatus> containing the status of the SMS notification.
     */
    @Override
    public ResponseEntity<NotificationStatus> smtpNotifyNumber(NotificationRequestModel notificationRequestModel) {
        NotificationStatus notificationStatus = sendSingleSms(notificationRequestModel.getPhoneNumber(), notificationRequestModel.getMessageBody(), notificationRequestModel.getTemplateId());
        return ResponseEntity.ok(notificationStatus);
    }

    /**
     * This method is used to send a single SMS notification using the provided details.
     *
     * @param phoneNumber The phone number to which the SMS needs to be sent.
     * @param message The message body of the SMS.
     * @param templateId The template ID for the SMS.
     * @return NotificationStatus containing the status of the SMS notification.
     * @throws WishFoundationException If an error occurs during the HTTP request.
     */
    public NotificationStatus sendSingleSms(String phoneNumber, String message, String templateId) {
        // Initialize a new NotificationStatus object
        NotificationStatus notificationStatus = new NotificationStatus();

        try {
            // Construct the base URL for the SMS API
            String baseUrl = "http://itda.hmimedia.in/pushsms.php?";

            // Encode the message using UTF-8 encoding
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

            // Construct the complete API URL using the provided details and environment configuration
            String apiUrl = String.format("%susername=%s&api_password=%s&sender=%s&to=%s&message=%s&e_id=%s&t_id=%s&unicode=1&priority=11",
                    baseUrl, environmentConfig.getUsername(), environmentConfig.getPassword(), environmentConfig.getSenderId(), phoneNumber, encodedMessage, environmentConfig.getEntityId(), templateId);

            // Create a new URL object from the API URL
            URL url = new URL(apiUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Print the API URL for debugging purposes
            System.out.println(apiUrl);

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Get the response code from the server
            int responseCode = connection.getResponseCode();

            // Check if the response code is HTTP_OK (200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response from the server
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Extract the response string
                String string = response.toString();

                // Print the response string for debugging purposes
                System.out.println("string : " + string);

                // Set the message and status in the NotificationStatus object
                notificationStatus.setMessage(string);
                notificationStatus.setStatus(HttpStatus.valueOf(responseCode));

                // Return the NotificationStatus object
                return notificationStatus;
            } else {
                // Print an error message if the response code is not HTTP_OK
                System.out.println("HTTP GET request failed with response code " + responseCode);
            }
        } catch (Exception e) {
            // Throw a WishFoundationException if an error occurs during the HTTP request
            throw new WishFoundationException(e.getMessage());
        }

        // Return the NotificationStatus object
        return notificationStatus;
    }

}
