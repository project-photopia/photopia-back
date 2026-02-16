package com.photopia.photopia_back.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void shouldSendEmailToRecipients() {
        // Given
        String recipient = "user@example.com";
        List<String> recipients = List.of(recipient);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendNewMediaNotification(recipients, "My Capsule", "Louis");

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
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
