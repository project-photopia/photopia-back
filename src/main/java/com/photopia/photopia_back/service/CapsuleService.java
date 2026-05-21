package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.capsule.CapsuleCreateRequest;
import com.photopia.photopia_back.dto.capsule.CapsuleUpdateRequest;
import com.photopia.photopia_back.dto.capsule.CapsuleGetMyCapsulesResponse;
import com.photopia.photopia_back.dto.capsule.CapsuleMemberResponse;
import com.photopia.photopia_back.dto.capsule.CapsuleOwnerResponse;
import com.photopia.photopia_back.exception.AccessDeniedException;
import com.photopia.photopia_back.exception.BadRequestException;
import com.photopia.photopia_back.exception.ResourceNotFoundException;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.CapsuleMember;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.CapsuleMemberRepository;
import com.photopia.photopia_back.repository.CapsuleRepository;
import com.photopia.photopia_back.repository.CommentRepository;
import com.photopia.photopia_back.repository.EventTypeRepository;
import com.photopia.photopia_back.repository.MediaRepository;
import com.photopia.photopia_back.repository.ReactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final CapsuleMemberRepository capsuleMemberRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final EventTypeRepository eventTypeRepository;
    private final MediaRepository mediaRepository;
    private final R2Service r2Service;
    private final io.micrometer.core.instrument.MeterRegistry meterRegistry;

    public CapsuleService(
            CapsuleRepository capsuleRepository,
            CapsuleMemberRepository capsuleMemberRepository,
            CommentRepository commentRepository,
            ReactionRepository reactionRepository,
            EventTypeRepository eventTypeRepository,
            MediaRepository mediaRepository,
            R2Service r2Service,
            io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        this.capsuleRepository = capsuleRepository;
        this.capsuleMemberRepository = capsuleMemberRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.mediaRepository = mediaRepository;
        this.r2Service = r2Service;
        this.meterRegistry = meterRegistry;
    }

    @Transactional

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

        meterRegistry.counter("photopia.capsules.created").increment();

        return savedCapsule;
    }

    @Transactional(readOnly = true)
    public List<CapsuleGetMyCapsulesResponse> getMyCapsulesByUserId(UUID userId) {
        var capsules = capsuleRepository.findByUserOrMember(userId);

        return capsules.stream()
                .map(c -> {
                    String eventTypeName = c.getEventType() != null ? c.getEventType().getName() : null;

                    String coverUrl = null;
                    var firstMedia = mediaRepository.findFirstByCapsuleId(c.getId());
                    if (firstMedia.isPresent()) {
                        String key = firstMedia.get().getPreviewUrl() != null
                                ? firstMedia.get().getPreviewUrl()
                                : firstMedia.get().getOriginalUrl();
                        coverUrl = r2Service.generatePresignedGetUrl(key);
                    }

                    int mediaCount = mediaRepository.countByCapsuleId(c.getId());

                    return new CapsuleGetMyCapsulesResponse(
                            c.getId(),
                            c.getName(),
                            coverUrl,
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
                            mediaCount,
                            c.getCreatedAt(),
                            c.getUpdatedAt());
                })
                .toList();
    }

    public String generateInviteLink(UUID capsuleId, User user) {
        Capsule capsule = capsuleRepository.findByIdAndUser_Id(capsuleId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Capsule not found"));

        if (capsule.getJoinToken() == null || capsule.getJoinToken().isBlank()) {
            capsule.setJoinToken(UUID.randomUUID().toString());
            capsuleRepository.save(capsule);
        }

        return "https://photopia.app/join?token=" + capsule.getJoinToken();
    }

    @Transactional

    public UUID joinCapsule(String token, User user) {
        Capsule capsule = capsuleRepository.findByJoinToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid invite token"));

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

        meterRegistry.counter("photopia.capsules.joined").increment();

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
    public void deleteCapsule(UUID capsuleId, User me) {
        Capsule capsule = capsuleRepository.findByIdAndUser_Id(capsuleId, me.getId())
                .orElseThrow(() -> new AccessDeniedException("Capsule not found or forbidden"));

        boolean isAdmin = capsuleMemberRepository.existsByCapsuleIdAndUserIdAndRole(capsuleId, me.getId(),
                CapsuleMember.Role.ADMIN);

        if (!isAdmin) {
            throw new AccessDeniedException("Forbidden");
        }

        commentRepository.deleteByCapsuleId(capsuleId);
        reactionRepository.deleteByCapsuleId(capsuleId);
        mediaRepository.deleteByCapsuleId(capsuleId);
        capsuleMemberRepository.deleteByCapsuleId(capsuleId);
        capsuleRepository.delete(capsule);
    }

    public List<CapsuleMemberResponse> getCapsuleMembers(UUID capsuleId, User me) {
        if (!capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, me.getId())) {
            throw new AccessDeniedException("You are not a member of this capsule");
        }

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

    @Transactional

    public Capsule updateCapsule(UUID capsuleId, CapsuleUpdateRequest request, User me) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new ResourceNotFoundException("Capsule not found"));

        boolean isAdmin = capsuleMemberRepository.existsByCapsuleIdAndUserIdAndRole(
                capsuleId, me.getId(), CapsuleMember.Role.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can update this capsule");
        }

        if (request.name() != null && !request.name().isBlank()) {
            capsule.setName(request.name().trim());
        }
        if (request.isPrivate() != null) {
            capsule.setIsPrivate(request.isPrivate());
        }

        return capsuleRepository.save(capsule);
    }

    @Transactional

    public void removeMember(UUID capsuleId, UUID targetUserId, User me) {
        boolean isAdmin = capsuleMemberRepository.existsByCapsuleIdAndUserIdAndRole(
                capsuleId, me.getId(), CapsuleMember.Role.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can remove members");
        }

        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new ResourceNotFoundException("Capsule not found"));

        if (capsule.getUser().getId().equals(targetUserId)) {
            throw new BadRequestException("Cannot remove the capsule owner");
        }

        if (!capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, targetUserId)) {
            throw new ResourceNotFoundException("User is not a member of this capsule");
        }

        capsuleMemberRepository.deleteByCapsuleIdAndUserId(capsuleId, targetUserId);
        capsule.setMemberCount(Math.max(0, capsule.getMemberCount() - 1));
        capsuleRepository.save(capsule);
    }

    @Transactional

    public void leaveCapsule(UUID capsuleId, User me) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new ResourceNotFoundException("Capsule not found"));

        if (capsule.getUser().getId().equals(me.getId())) {
            throw new BadRequestException("Owner cannot leave. Delete the capsule instead.");
        }

        if (!capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, me.getId())) {
            throw new AccessDeniedException("You are not a member of this capsule");
        }

        capsuleMemberRepository.deleteByCapsuleIdAndUserId(capsuleId, me.getId());
        capsule.setMemberCount(Math.max(0, capsule.getMemberCount() - 1));
        capsuleRepository.save(capsule);
    }
}
