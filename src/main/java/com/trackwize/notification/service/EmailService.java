package com.trackwize.notification.service;

import com.trackwize.common.constant.ErrorConst;
import com.trackwize.common.constant.NotificationConst;
import com.trackwize.common.exception.TrackWizeException;
import com.trackwize.notification.model.dto.NotificationReqDTO;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${smtp.port}")
    private String smtpPort;

    @Value("${smtp.host}")
    private String smtpHost;

    @Value("${smtp.username}")
    private String smtpUsername;

    @Value("${smtp.password}")
    private String smtpPassword;

    public void sendEmail(NotificationReqDTO reqDTO) throws MessagingException {

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.ssl.trust", smtpHost);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(reqDTO.getRecipient()));
        message.setSubject(reqDTO.getSubject());

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(contentsToHtml(reqDTO), "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }

    public String contentsToHtml(NotificationReqDTO reqDTO) {
        if (reqDTO.getContents() == null || reqDTO.getContents().isEmpty()) {
            return "";
        }
        
        int template = reqDTO.getTemplate();
        return switch (template) {
            case NotificationConst.PASSWORD_RESET_TEMPLATE -> resetPasswordContent(reqDTO.getContents());
            case NotificationConst.ACCOUNT_IS_CREATED_TEMPLATE -> accountIsCreatedContent(reqDTO.getContents());
            case NotificationConst.ACCOUNT_IS_VERIFIED_TEMPLATE -> accountIsVerifiedContent(reqDTO.getContents());
            case NotificationConst.ACCOUNT_IS_ACTIVATE_TEMPLATE -> accountIsActivateContent(reqDTO.getContents());
            default -> {
                log.warn("Unexpected Template Type: [template]{}", template);
                throw new TrackWizeException(
                        ErrorConst.INVALID_INPUT_CODE,
                        ErrorConst.INVALID_INPUT_MSG
                );
            }
        };

    }

    private String accountIsActivateContent(Map<String, Object> contents) {
        String title = (String) contents.getOrDefault("title", "Notification");
        String message = (String) contents.getOrDefault("message", "Please review the details below.");

        return "<html><body>" +
                "<h3>" + title + "</h3>" +
                "<p>" + message + "</p>" +
                "</body></html>";
    }

    private String accountIsCreatedContent(Map<String, Object> contents) {
        String title = (String) contents.getOrDefault("title", "Notification");
        String message = (String) contents.getOrDefault("message", "Please review the details below.");
        String name = (String) contents.get("name");

        return "<html><body>" +
                "<h2> Hi! " + name + "</h2>" +
                "<h3>" + title + "</h3>" +
                "<p>" + message + "</p>" +
                "</body></html>";
    }

    private String accountIsVerifiedContent(Map<String, Object> contents) {
        String title = (String) contents.getOrDefault("title", "Notification");
        String message = (String) contents.getOrDefault("message", "Please review the details below.");
        String token = (String) contents.get("token");
        String name = (String) contents.get("name");
        String link = "http://localhost:8080/reg/verify-account?token=" + token;
        String expiry = String.valueOf(contents.getOrDefault("expiry", "30"));

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>")
                .append("<h2> Hi! ").append(name).append("</h2>")
                .append("<h3>").append(title).append("</h3>")
                .append("<p>").append(message).append("</p>");

        if (token != null && !token.isEmpty()) {
            sb.append("<p><a href=\"").append(link).append("\">Click here to verify</a></p>");
        }

        sb.append("<br><p>This link will expire in ").append(expiry).append(" minutes.</p>")
                .append("</body></html>");

        return sb.toString();
    }

    public String resetPasswordContent(Map<String, Object> contents) {
        String title = (String) contents.getOrDefault("title", "Notification");
        String message = (String) contents.getOrDefault("message", "Please review the details below.");
        String token = (String) contents.get("token");
        String link = "http://localhost:8080/auth/reset-password/process?token=" + token;
        String expiry = String.valueOf(contents.getOrDefault("expiry", "10"));

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>")
                .append("<h3>").append(title).append("</h3>")
                .append("<p>").append(message).append("</p>");

        if (token != null && !token.isEmpty()) {
            sb.append("<p><a href=\"").append(link).append("\">Reset Password</a></p>");
        }

        sb.append("<br><p>This link will expire in ").append(expiry).append(" minutes.</p>")
                .append("</body></html>");

        return sb.toString();
    }
}
