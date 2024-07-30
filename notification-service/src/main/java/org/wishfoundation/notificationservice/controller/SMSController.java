package org.wishfoundation.notificationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import org.wishfoundation.chardhamcore.utils.EnvironmentConfigCommon;
import org.wishfoundation.notificationservice.config.EnvironmentConfig;
import org.wishfoundation.notificationservice.exception.WishFoundationException;
import org.wishfoundation.notificationservice.models.NotificationRequestModel;
import org.wishfoundation.notificationservice.models.NotificationStatus;

import org.wishfoundation.notificationservice.service.SMSService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller for handling SMS related operations.
 */
@RequestMapping("/api/v1/sms")
@RestController
public class SMSController {

    /**
     * Autowired instance of EnvironmentConfig for accessing AWS credentials.
     */
    @Autowired
    EnvironmentConfig env;

//    @Autowired
//    EnvironmentConfigCommon envCommon;

    /**
     * Autowired instance of SMSService for handling SMS related operations.
     */
    @Autowired
    SMSService smsService;

    /**
 * Endpoint for sending SMS using AWS SNS service.
 *
 * @param request The request object containing the phone number and message body.
 * @return ResponseEntity with a NotificationStatus object indicating the success or failure of the operation.
 * @throws WishFoundationException If there is an issue with the AWS SNS client.
 */
    @PostMapping ("/send-sms")
    public ResponseEntity<NotificationStatus> sendSMS(@RequestBody NotificationRequestModel request) {

        // Initialize a new NotificationStatus object to hold the response status
        NotificationStatus notificationStatus = new NotificationStatus();

        try {
            // TODO create bean & handling of aws creds.
            // Create a new SNS client using the AWS SDK, providing the access key and secret key from the environment configuration
            SnsClient snsClient = SnsClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(env.getAccessKey(), env.getSecretKey())))
                    .region(Region.AP_SOUTH_1).build();

            PublishRequest publishRequest = PublishRequest.builder().message(request.getMessageBody().replaceAll("<br>","\n")).phoneNumber(request.getPhoneNumber()).build();

            // Publish the SMS message using the SNS client
            PublishResponse result = snsClient.publish(publishRequest);

            // Prepare the response message with the message ID and HTTP status code
            String response = result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode();

            // Set the response message in the NotificationStatus object
            notificationStatus.setMessage(response);

            // Close the SNS client
            snsClient.close();

            // Return a ResponseEntity with the NotificationStatus object
            return ResponseEntity.ok(notificationStatus);
        }
        catch (SnsException snx){
            // If there is an SNS exception, print the stack trace and throw a WishFoundationException
            snx.printStackTrace();
            throw new WishFoundationException(snx.getMessage()+" issue with sns client");
        }
        catch (Exception e){
            // If there is any other exception, print the stack trace and throw a WishFoundationException
            e.printStackTrace();
            throw new WishFoundationException(e.getMessage()+" issue with sns client");

        }
    }



//    @PostMapping ("/notify-number")
//    public  ResponseEntity<NotificationStatus> notifyNumber(@RequestBody NotificationRequestModel request) {
//        NotificationStatus notificationStatus = new NotificationStatus();
//        try {
//                String phoneNumber = request.getPhoneNumber();
//                String accountId = envCommon.isProduction() ? "891377192048" : "891377123666";
//                SqsClient sqs = SqsClient.builder()
//                        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(env.getAccessKey(), env.getSecretKey())))
//                        .region(Region.AP_SOUTH_1)
//                        .build();
//                String sqsUrl = String.format("https://sqs.ap-south-1.amazonaws.com/%s/yatri-notification-queue", accountId);
//                SendMessageRequest messageRequest = SendMessageRequest.builder().queueUrl(sqsUrl).messageBody(phoneNumber).build();
//            SendMessageResponse sendMessageResponse = sqs.sendMessage(messageRequest);
//            notificationStatus.setMessage(" id: "+sendMessageResponse.messageId());
//            sqs.close();
//            }
//        catch (Exception e){
//            e.printStackTrace();
//            throw new WishFoundationException(e.getMessage()+" issue with sns client");
//        }
//        return ResponseEntity.ok(notificationStatus);
//    }


    /**
 * Endpoint for sending SMS using SMTP protocol.
 *
 * @param notificationRequestModel The request object containing the phone number and message body.
 * @return ResponseEntity with a NotificationStatus object indicating the success or failure of the operation.
 * @throws WishFoundationException If there is an issue with the SMTP client.
 */
    @PostMapping(path = "smtp/notify-number")
    public ResponseEntity<NotificationStatus> smtpNotifyNumber(@RequestBody NotificationRequestModel notificationRequestModel){
        return smsService.smtpNotifyNumber(notificationRequestModel);
    }



    //    public static void main(String[] args) throws IOException, InterruptedException {
//        // Replace the following values with your actual credentials
//        String userId = "testdemo";
//        String password = "cbhe1755CB";
//        String senderId = "TECHNP";
//        String phoneNumber = "9794719794";
//        String message = "Hello..............";
//        String entityId = "1201159409941345107";
//        String templateId = "1707167940051443628";
//
//
//
//
////        String response = sendSingleSms(userId, password, senderId, phoneNumber, message, entityId, templateId);
////        System.out.println(response);
//
//
//
//    }



}
