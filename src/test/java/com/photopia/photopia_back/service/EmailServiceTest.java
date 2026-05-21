package com.photopia.photopia_back.service;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class EmailServiceTest {

    private EmailService emailService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        emailService = new EmailService("test-api-key", "https://api.resend.com", "onboarding@resend.dev", builder);
    }

    @Test
    void shouldSendEmailToRecipients() throws Exception {
        // Given
        String recipient = "user@example.com";
        List<String> recipients = List.of(recipient);

        mockServer.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-api-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(Matchers.containsString("user@example.com")))
                .andExpect(content().string(Matchers.containsString("New media in My Capsule")))
                .andRespond(withSuccess());

        // When
        emailService.sendNewMediaNotification(recipients, "My Capsule", "Louis");

        // Then
        mockServer.verify();
    }

    @Test
    void shouldNotSendEmailWhenRecipientsListIsEmpty() {
        // Given
        List<String> recipients = Collections.emptyList();

        // When
        emailService.sendNewMediaNotification(recipients, "My Capsule", "Louis");

        // Then
        mockServer.verify();
    }
}
