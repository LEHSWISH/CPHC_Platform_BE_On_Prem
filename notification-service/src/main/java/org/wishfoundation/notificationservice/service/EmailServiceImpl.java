package org.wishfoundation.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.chardhamcore.config.UserContext;
import org.wishfoundation.notificationservice.config.EnvironmentConfig;
import org.wishfoundation.notificationservice.exception.WishFoundationException;
import org.wishfoundation.notificationservice.models.NotificationRequestModel;
import org.wishfoundation.notificationservice.models.NotificationStatus;
import org.wishfoundation.notificationservice.utils.Helper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

//import javax.mail.Message;
//import javax.mail.Session;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeBodyPart;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class EmailServiceImpl {

    @Autowired
    EnvironmentConfig env;

    @Autowired
    private JavaMailSender javaMailSender;
    public NotificationStatus sendEmailSES(NotificationRequestModel emailRequest){

        NotificationStatus emailStatus = new NotificationStatus();
        emailStatus.setStatus(HttpStatus.OK);

        emailRequest.setEmailFrom(EnvironmentConfig.YATRI_PULSE_FROM_EMAIL);

        HashSet<String> bccMails = new LinkedHashSet<>();
        bccMails.addAll(emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty() ? emailRequest.getBcc()
                : new HashSet<>());
        emailRequest.setBcc(bccMails);
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        try {
            System.out.println("EMAIL : "+new ObjectMapper().writeValueAsString(emailRequest));

            message.setSubject(emailRequest.getSubject(), StandardCharsets.UTF_8.name());

            message.setFrom(new InternetAddress(emailRequest.getEmailFrom()));

            for (String t : emailRequest.getEmailTo()) {
                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(t));
            }

            if (emailRequest.getCopyTo() != null) {
                for (String t : emailRequest.getCopyTo()) {
                    message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(t));
                }
            }

            if (emailRequest.getBcc() != null) {
                for (String t : emailRequest.getBcc()) {
                    message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(t));
                }
            }

            MimeBodyPart wrap = new MimeBodyPart();

            MimeMultipart cover = new MimeMultipart("alternative");
            MimeBodyPart html = new MimeBodyPart();
            cover.addBodyPart(html);

            wrap.setContent(cover);

            MimeMultipart content = new MimeMultipart("related");
            message.setContent(content);
            content.addBodyPart(wrap);

            if (emailRequest.getAttachFiles() != null) {
                for (String attachmentFileName : emailRequest.getAttachFiles()) {
                    String id = UUID.randomUUID().toString();

                    MimeBodyPart attachment = new MimeBodyPart();

                    DataSource fds =  new FileDataSource(attachmentFileName);
                    attachment.setDataHandler(new DataHandler(fds));
                    attachment.setHeader("Content-ID", "<" + id + ">");
                    attachment.setFileName(fds.getName());
                    content.addBodyPart(attachment);

                }
            }

            Document doc = Jsoup.parse(emailRequest.getMessageBody(), StandardCharsets.UTF_8.name());
            Document.OutputSettings setting = doc.outputSettings();
            setting.charset(StandardCharsets.US_ASCII);
            html.setContent(doc.html(), "text/html");

        } catch (Exception e2) {
            throw new WishFoundationException("BAD REQUEST : "+e2.getMessage());
        }

        try {
            SesClient client = SesClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(env.getAccessKey(), env.getSecretKey())))
                    .region(Region.AP_SOUTH_1).build();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);

            RawMessage rawMessage =  RawMessage.builder().data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray()))).build();

            SendRawEmailRequest rawEmailRequest =  SendRawEmailRequest.builder().rawMessage(rawMessage).build();
            client.sendRawEmail(rawEmailRequest);
            client.close();
        } catch (Exception ex) {
            throw new WishFoundationException("BAD REQUEST FOR SES: "+ex.getMessage());

        }
        return emailStatus;
    }

    public NotificationStatus smtpEmailServer(NotificationRequestModel emailRequest){
        NotificationStatus emailStatus = new NotificationStatus();
        List<String> filePaths = new ArrayList<>();
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom("noreply@wishfoundationindia.org");
            String[] toEmails = emailRequest.getEmailTo().toArray(String[]::new);
            mimeMessageHelper.setTo(toEmails);
            if (!ObjectUtils.isEmpty(emailRequest.getBcc())) {
                String[] bccEmails = emailRequest.getBcc().toArray(String[]::new);
                System.out.println("bccEmails: " + Arrays.toString(bccEmails));
                mimeMessageHelper.setBcc(bccEmails);
            }
            String finalMessgeBody = EnvironmentConfig.SMTP_MESSAGE_BODY_HEADER_RESPONSE ;
            if(!ObjectUtils.isEmpty(UserContext.getCurrentUserName()) && !UserContext.getCurrentUserName().equals("wish")){
                finalMessgeBody += EnvironmentConfig.SMTP_MESSAGE_USERNAME;
            }
            finalMessgeBody += EnvironmentConfig.SMTP_MESSAGE_BODY_MIDDLE_RESPONSE;
            if(!ObjectUtils.isEmpty(emailRequest.getAttachFiles()))
                finalMessgeBody += EnvironmentConfig.SMTP_IMAGE_ATTACHMENT;

            finalMessgeBody += EnvironmentConfig.SMTP_MESSAGE_BODY_FOOTER_RESPONSE;
            String messgeBody = finalMessgeBody
                    .replace(EnvironmentConfig.CONTACT_NUMBER, emailRequest.getPhoneNumber())
                    .replace(EnvironmentConfig.BODY_MESSAGE, emailRequest.getMessageBody())
                    .replace(EnvironmentConfig.NAME, emailRequest.getName());
            //TODO : username handling
            if(!ObjectUtils.isEmpty(UserContext.getCurrentUserName()) && !UserContext.getCurrentUserName().equals("wish")){
                messgeBody = messgeBody.replace(EnvironmentConfig.USERNAME, UserContext.getCurrentUserName());
            }
            mimeMessageHelper.setSubject(emailRequest.getSubject());
            mimeMessageHelper.setText(messgeBody, true);
            if (!ObjectUtils.isEmpty(emailRequest.getAttachFiles())) {
                emailRequest.getAttachFiles().forEach(base64String -> {
                    filePaths.add(Helper.createFileTempDirectory(emailRequest.getFileName(), base64String));
                });
                mimeMessageHelper.addAttachment(emailRequest.getFileName(), new File(filePaths.get(0)));
            }
            javaMailSender.send(mimeMessage);
            emailStatus.setStatus(HttpStatus.OK);
            return emailStatus;
        } catch (Exception e) {
            throw new WishFoundationException("BAD REQUEST FOR SMTP Server: "+e.getMessage());
        }finally {
            try{
                if (!ObjectUtils.isEmpty(emailRequest.getAttachFiles())) {
                    Files.delete(Paths.get(filePaths.get(0)));
                }
            }catch (IOException ignored){

            }
        }

    }

    public NotificationStatus sendSuperAdminMail(NotificationRequestModel emailRequest){

        NotificationStatus emailStatus = new NotificationStatus();
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom("demo@centilytics.org");
            String[] toEmails = emailRequest.getEmailTo().toArray(String[]::new);
            mimeMessageHelper.setTo(toEmails[0]);

            String messgeBody = emailRequest.getMessageBody();
            //TODO : username handling
            mimeMessageHelper.setSubject(emailRequest.getSubject());
            mimeMessageHelper.setText(messgeBody, true);
            javaMailSender.send(mimeMessage);
            emailStatus.setStatus(HttpStatus.OK);
            return emailStatus;
        }catch (Exception e){
            throw new WishFoundationException("BAD REQUEST FOR SMTP Server: "+e.getMessage());

        }
    }
}
