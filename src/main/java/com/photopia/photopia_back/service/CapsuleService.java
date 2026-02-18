package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.capsule.CapsuleCreateRequest;
import com.photopia.photopia_back.dto.capsule.CapsuleGetMyCapsulesResponse;
import com.photopia.photopia_back.dto.capsule.CapsuleMemberResponse;
import com.photopia.photopia_back.dto.capsule.CapsuleOwnerResponse;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.CapsuleMember;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.CapsuleMemberRepository;
import com.photopia.photopia_back.repository.CapsuleRepository;
import com.photopia.photopia_back.repository.EventTypeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final CapsuleMemberRepository capsuleMemberRepository;
    private final EventTypeRepository eventTypeRepository;

    public CapsuleService(
            CapsuleRepository capsuleRepository,
            CapsuleMemberRepository capsuleMemberRepository,
            EventTypeRepository eventTypeRepository) {
        this.capsuleRepository = capsuleRepository;
        this.capsuleMemberRepository = capsuleMemberRepository;
        this.eventTypeRepository = eventTypeRepository;
    }

    @Transactional
    @CacheEvict(value = "my_capsules_v2", key = "#owner.id.toString()")
    public Capsule createCapsule(CapsuleCreateRequest request, User owner) {
        Capsule capsule = new Capsule();
        capsule.setName(request.name());
        capsule.setStartDate(LocalDate.now());
        capsule.setJoinToken(UUID.randomUUID().toString());
        capsule.setUser(owner);
        capsule.setIsPrivate(request.isPrivate() != null ? request.isPrivate() : true);
        capsule.setIsArchived(false);
        capsule.setMemberCount(1);

        if (request.eventTypeName() != null && !request.eventTypeName().isBlank()) {
            eventTypeRepository.findByName(request.eventTypeName())
                    .ifPresent(capsule::setEventType);
        }

        Capsule savedCapsule = capsuleRepository.save(capsule);

        CapsuleMember capsuleMember = CapsuleMember.builder()
                .capsuleId(savedCapsule.getId())
                .userId(owner.getId())
                .role(CapsuleMember.Role.ADMIN)
                .build();

        capsuleMemberRepository.save(capsuleMember);
        return savedCapsule;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "my_capsules_v2", key = "#userId.toString()")
    public List<CapsuleGetMyCapsulesResponse> getMyCapsulesByUserId(UUID userId) {
        var capsules = capsuleRepository.findByUserOrMember(userId);

        return capsules.stream()
                .map(c -> {
                    String eventTypeName = c.getEventType() != null ? c.getEventType().getName() : null;
                    return new CapsuleGetMyCapsulesResponse(
                            c.getId(),
                            c.getName(),
                            c.getCoverUrl(),
                            c.getStartDate(),
                            c.getEndDate(),
                            c.getColor(),
                            c.getIsPrivate(),
                            c.getJoinToken(),
                            new CapsuleOwnerResponse(
                                    c.getUser().getId(),
                                    c.getUser().getUsername(),
                                    c.getUser().getEmail(),
                                    c.getUser().getAvatarUrl()),
                            c.getIsArchived(),
                            c.getMemberCount(),
                            c.getLatitude(),
                            c.getLongitude(),
                            eventTypeName,
                            c.getCreatedAt(),
                            c.getUpdatedAt());
                })
                .toList();
    }

    public String generateInviteLink(UUID capsuleId, User user) {
        Capsule capsule = capsuleRepository.findByIdAndUser_Id(capsuleId, user.getId())
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        if (capsule.getJoinToken() == null || capsule.getJoinToken().isBlank()) {
            capsule.setJoinToken(UUID.randomUUID().toString());
            capsuleRepository.save(capsule);
        }

        return "https://photopia.app/join?token=" + capsule.getJoinToken();
    }

    @Transactional
    @CacheEvict(value = "my_capsules_v2", key = "#user.id.toString()")
    public UUID joinCapsule(String token, User user) {
        Capsule capsule = capsuleRepository.findByJoinToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invite token"));

        UUID capsuleId = capsule.getId();
        UUID userId = user.getId();

        if (capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, userId)) {
            return capsuleId; // Already a member, idempotent
        }

        capsuleMemberRepository.save(
                CapsuleMember.builder()
                        .capsuleId(capsuleId)
                        .userId(userId)
                        .role(CapsuleMember.Role.MEMBER)
                        .build());

        capsule.setMemberCount(capsule.getMemberCount() + 1);
        capsuleRepository.save(capsule);

        // Evict cache for the user joining
        // Note: we can't easily evict cache for OTHER members if we cached the capsule
        // details elsewhere
        // But for "my_capsules" of this user, we should evict.
        // However, since we don't have reference to "my_capsules" key easily here
        // without the ID...
        // Actually, we can evict based on userId.
        return capsuleId;
    }

    @Transactional
    @CacheEvict(value = "my_capsules_v2", key = "#me.id.toString()")
    public void deleteCapsule(UUID capsuleId, User me) {
        Capsule capsule = capsuleRepository.findByIdAndUser_Id(capsuleId, me.getId())
                .orElseThrow(() -> new RuntimeException("Capsule not found or forbidden"));

        boolean isOwner = capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, me.getId());
        boolean isAdmin = capsuleMemberRepository.existsByCapsuleIdAndUserIdAndRole(capsuleId, me.getId(),
                CapsuleMember.Role.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Forbidden");
        }

        capsuleMemberRepository.deleteByCapsuleId(capsuleId);
        capsuleRepository.delete(capsule);
    }

    public List<CapsuleMemberResponse> getCapsuleMembers(UUID capsuleId) {
        var members = capsuleMemberRepository.findByCapsuleId(capsuleId);

        return members.stream()
                .map(m -> new CapsuleMemberResponse(
                        m.getUserId(),
                        m.getUser().getUsername(),
                        m.getUser().getEmail(),
                        m.getUser().getAvatarUrl(),
                        m.getRole()))
                .toList();
    }
}
