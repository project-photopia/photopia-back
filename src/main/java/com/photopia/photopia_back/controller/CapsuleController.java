package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.CapsuleCreateRequest;
import com.photopia.photopia_back.dto.CapsuleGetMyCapsulesResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.CapsuleMember;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.CapsuleMemberRepository;
import com.photopia.photopia_back.repository.CapsuleRepository;
import com.photopia.photopia_back.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/capsules")
public class CapsuleController {

    private final CapsuleRepository capsuleRepository;
    private final UserRepository userRepository;
    private final CapsuleMemberRepository capsuleMemberRepository;

    public CapsuleController(
            CapsuleRepository capsuleRepository,
            UserRepository userRepository,
            CapsuleMemberRepository capsuleMemberRepository
    ) {
        this.capsuleRepository = capsuleRepository;
        this.userRepository = userRepository;
        this.capsuleMemberRepository = capsuleMemberRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(
            @RequestBody CapsuleCreateRequest request,
            Authentication authentication
            ) {

        User owner = (User) authentication.getPrincipal();

        Capsule capsule = new Capsule();
        capsule.setName(request.name());

        // Start Date
        capsule.setStartDate(LocalDate.now());
        // End Date
        capsule.setEndDate(null);
        // Join Token (Generated)
        capsule.setJoinToken(UUID.randomUUID().toString());

        capsule.setUser(owner);

        // Default Values
        capsule.setIsPrivate(request.isPrivate() != null ? request.isPrivate() : true);
        capsule.setIsArchived(false); // Default logic
        capsule.setMemberCount(1);

        Capsule savedCapsule = capsuleRepository.save(capsule);

        CapsuleMember capsuleMember = CapsuleMember.builder()
                .capsuleId(savedCapsule.getId())
                .userId(owner.getId())
                .role(CapsuleMember.Role.ADMIN)
                .build();

        capsuleMemberRepository.save(capsuleMember);

        return ResponseEntity.ok(ApiSuccessResponse.of(savedCapsule, "Album created"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse> getMyCapsules(Authentication authentication) {

        User owner = (User) authentication.getPrincipal();
        var capsules = capsuleRepository.findByUser(owner);

        var data = capsules.stream()
                .map(c -> new CapsuleGetMyCapsulesResponse(
                        c.getId(),
                        c.getName(),
                        c.getStartDate(),
                        c.getEndDate(),
                        c.getJoinToken(),
                        c.getIsPrivate(),
                        c.getIsArchived(),
                        c.getMemberCount()
                ))
                .toList();

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiSuccessResponse.of(data, "No capsules found"));
        }

        return ResponseEntity.ok(ApiSuccessResponse.of(data, "Capsules fetched"));
    }

    @GetMapping("/{capsuleId}/invite-link")
    public ResponseEntity<ApiResponse> getInviteLink(
            @PathVariable UUID capsuleId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Capsule capsule = capsuleRepository.findByIdAndUser_Id(capsuleId, user.getId())
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        if (capsule.getJoinToken() == null || capsule.getJoinToken().isBlank()) {
            capsule.setJoinToken(UUID.randomUUID().toString());
            capsuleRepository.save(capsule);
        }

        String link = "https://photopia.app/join?token=" + capsule.getJoinToken();
        return ResponseEntity.ok(ApiSuccessResponse.of(link, "Invite link generated"));
    }

    @PostMapping("/join/{token}")
    public ResponseEntity<ApiResponse> joinCapsule(
            @PathVariable String token,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Capsule capsule = capsuleRepository.findByJoinToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invite token"));

        UUID capsuleId = capsule.getId();
        UUID userId = user.getId();

        if (capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, userId)) {
            return ResponseEntity.ok(ApiSuccessResponse.of(null, "Already a member"));
        }

        capsuleMemberRepository.save(
                CapsuleMember.builder()
                        .capsuleId(capsuleId)
                        .userId(userId)
                        .role(CapsuleMember.Role.MEMBER)
                        .build()
        );

        capsule.setMemberCount(capsule.getMemberCount() + 1);
        capsuleRepository.save(capsule);

        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Joined capsule successfully"));

    }

}
