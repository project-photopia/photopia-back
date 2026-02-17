package com.photopia.photopia_back.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "test@photopia.com");
    }

    @Test
    void shouldSendEmailToRecipients() throws Exception {
        // Given
        String recipient = "user@example.com";
        List<String> recipients = List.of(recipient);

        // Use a real MimeMessage to verify content set by helper
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendNewMediaNotification(recipients, "My Capsule", "Louis");

        // Then
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertEquals("New media in My Capsule 📸", sentMessage.getSubject());
        assertEquals(recipient, sentMessage.getAllRecipients()[0].toString());
        assertEquals("test@photopia.com", sentMessage.getFrom()[0].toString());

        // Verify HTML content structure
        String content = getTextFromMessage(sentMessage);

        assertTrue(content.contains("Louis"));
        assertTrue(content.contains("My Capsule"));
        assertTrue(content.contains("View Memory"));
    }

    private String getTextFromMessage(jakarta.mail.Part p) throws Exception {
        Object content = p.getContent();

        if (content instanceof jakarta.mail.Multipart) {
            jakarta.mail.Multipart mp = (jakarta.mail.Multipart) content;

            // If it's alternative, prioritize HTML, then plain text
            // We can't rely on p.isMimeType() here if headers are stale, so assume it's
            // like mixed/alternative
            // Let's iterate all parts and prioritize HTML
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                jakarta.mail.Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getTextFromMessage(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getTextFromMessage(bp);
                    if (s != null)
                        return s;
                } else if (bp.isMimeType("multipart/*") || bp.getContent() instanceof jakarta.mail.Multipart) {
                    // Recursive
                    String s = getTextFromMessage(bp);
                    if (s != null)
                        return s;
                } else {
                    // Other types?
                    String s = getTextFromMessage(bp);
                    if (s != null && text == null)
                        text = s;
                }
            }
            return text;
        } else if (content instanceof String) {
            return (String) content;
        }

        return null;
    }

    @Test
    void shouldNotSendEmailWhenRecipientsListIsEmpty() {
        // Given
        List<String> recipients = Collections.emptyList();

        // When
        emailService.sendNewMediaNotification(recipients, "My Capsule", "Louis");

        // Then
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
}
