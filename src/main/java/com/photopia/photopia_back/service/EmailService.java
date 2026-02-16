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

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendNewMediaNotification(List<String> recipients, String capsuleName, String uploaderName) {
        if (recipients.isEmpty()) {
            return;
        }

        String subject = "New media in " + capsuleName + " 📸";
        String content = buildHtmlContent(capsuleName, uploaderName);

        // Send individually or as BCC to respect privacy
        // For simplicity and better deliverability, we'll loop here (or use BCC if list
        // is huge)
        for (String recipient : recipients) {
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
        return """
                <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                    <h2 style="color: #6200ea;">Photopia 📸</h2>
                    <p>Hello,</p>
                    <p><strong>%s</strong> just added a new photo to the capsule <strong>%s</strong>!</p>
                    <p>Open the app to recover the memory.</p>
                    <br/>
                    <p style="font-size: 0.8em; color: #777;">See you soon on Photopia.</p>
                </div>
                """
                .formatted(uploaderName != null ? uploaderName : "Someone", capsuleName);
    }
}
