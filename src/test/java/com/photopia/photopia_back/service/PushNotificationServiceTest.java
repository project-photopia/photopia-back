package com.photopia.photopia_back.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.photopia.photopia_back.model.CapsuleMember;
import com.photopia.photopia_back.repository.CapsuleMemberRepository;
import com.photopia.photopia_back.repository.DeviceTokenRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;
    @Mock
    private CapsuleMemberRepository capsuleMemberRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PushNotificationService pushNotificationService;

    @Test
    void shouldTriggerEmailService() {
        // Given
        UUID capsuleId = UUID.randomUUID();
        UUID excludeUserId = UUID.randomUUID();
        String uploaderName = "Louis";
        String capsuleName = "Holidays";

        List<String> emails = List.of("user@example.com");

        // Spy on the service to bypass Firebase check
        PushNotificationService spyService = spy(pushNotificationService);
        doReturn(true).when(spyService).isFirebaseInitialized();

        // Mock Repositories
        CapsuleMember member = new CapsuleMember();
        member.setUserId(UUID.randomUUID());

        when(capsuleMemberRepository.findByCapsuleId(capsuleId)).thenReturn(List.of(member));
        when(deviceTokenRepository.findByUserIdAndNotificationsEnabledTrue(any())).thenReturn(Collections.emptyList());
        when(capsuleMemberRepository.findMemberEmails(capsuleId, excludeUserId)).thenReturn(emails);

        // When
        spyService.notifyCapsuleMembersNewMedia(capsuleId, excludeUserId, uploaderName, capsuleName);

        // Then
        verify(emailService).sendNewMediaNotification(emails, capsuleName, uploaderName);
    }
}
