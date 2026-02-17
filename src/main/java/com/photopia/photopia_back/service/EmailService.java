package com.photopia.photopia_back.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String senderEmail;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public void sendNewMediaNotification(List<String> recipients, String capsuleName, String uploaderName) {
        if (recipients.isEmpty()) {
            return;
        }

        String subject = "New media in " + capsuleName + " 📸";
        String content = buildHtmlContent(capsuleName, uploaderName);

        for (String recipient : recipients) {
            if (recipient == null || recipient.isBlank() || !recipient.matches(EMAIL_REGEX)) {
                log.warn("Skipping invalid email address: {}", recipient);
                continue;
            }
            try {
                sendHtmlEmail(recipient, subject, content);
            } catch (Exception e) {
                log.error("Failed to send email to {}", recipient, e);
            }
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        javaMailSender.send(message);
        log.info("Sent email to {}", to);
    }

    private String buildHtmlContent(String capsuleName, String uploaderName) {
        String safeUploaderName = uploaderName != null ? uploaderName : "Someone";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>New Memory on Photopia</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            margin: 0;
                            padding: 0;
                            background-color: #ffffff;
                            color: #1a1a1a;
                        }
                        .container {
                            max-width: 480px;
                            margin: 0 auto;
                            padding: 40px 20px;
                        }
                        .header {
                            margin-bottom: 32px;
                        }
                        .logo {
                            font-size: 24px;
                            font-weight: 700;
                            color: #6200ea;
                            text-decoration: none;
                            letter-spacing: -0.5px;
                        }
                        .content {
                            margin-bottom: 32px;
                        }
                        .h1 {
                            font-size: 24px;
                            font-weight: 600;
                            line-height: 1.3;
                            margin: 0 0 16px;
                            color: #1a1a1a;
                        }
                        .text {
                            font-size: 16px;
                            line-height: 1.6;
                            color: #4a4a4a;
                            margin: 0 0 24px;
                        }
                        .button {
                            display: inline-block;
                            background-color: #6200ea;
                            color: #ffffff;
                            font-size: 16px;
                            font-weight: 600;
                            text-decoration: none;
                            padding: 12px 24px;
                            border-radius: 6px;
                            text-align: center;
                        }
                        .footer {
                            margin-top: 48px;
                            padding-top: 24px;
                            border-top: 1px solid #eaeaea;
                            font-size: 12px;
                            color: #888888;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <span class="logo">Photopia</span>
                        </div>
                        <div class="content">
                            <h1 class="h1">New memory unlocked! 📸</h1>
                            <p class="text">
                                <strong>%s</strong> just added a new photo to the <strong>%s</strong> capsule.
                                It's waiting for you to discover.
                            </p>
                            <a href="photopia://" class="button">View Memory</a>
                        </div>
                        <div class="footer">
                            <p>
                                Sent with 💜 by Photopia<br>
                                If you didn't expect this, you can safely ignore this email.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(safeUploaderName, capsuleName);
    }
}
