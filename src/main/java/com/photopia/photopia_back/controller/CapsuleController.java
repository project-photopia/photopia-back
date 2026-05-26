package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.capsule.CapsuleCreateRequest;
import com.photopia.photopia_back.dto.capsule.CapsuleUpdateRequest;
import com.photopia.photopia_back.dto.capsule.CapsuleGetMyCapsulesResponse;
import com.photopia.photopia_back.dto.capsule.CapsuleMemberResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.service.CapsuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/capsules")
public class CapsuleController {

    private final CapsuleService capsuleService;

    public CapsuleController(CapsuleService capsuleService) {
        this.capsuleService = capsuleService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(
            @RequestBody CapsuleCreateRequest request,
            Authentication authentication) {
        User owner = (User) authentication.getPrincipal();
        Capsule savedCapsule = capsuleService.createCapsule(request, owner);
        return ResponseEntity.ok(ApiSuccessResponse.of(savedCapsule, "Album created"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse> getMyCapsules(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<CapsuleGetMyCapsulesResponse> data = capsuleService.getMyCapsulesByUserId(currentUser.getId());

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiSuccessResponse.of(data, "No capsules found"));
        }

        return ResponseEntity.ok(ApiSuccessResponse.of(data, "Capsules fetched"));
    }

    // TODO: Passer l'invite-link et le join dans un controller
    // CapsuleMemberController
    @GetMapping("/{capsuleId}/invite-link")
    public ResponseEntity<ApiResponse> getInviteLink(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String link = capsuleService.generateInviteLink(capsuleId, user);
        return ResponseEntity.ok(ApiSuccessResponse.of(link, "Invite link generated"));
    }

    @PostMapping("/join/{token}")
    public ResponseEntity<ApiResponse> joinCapsule(
            @PathVariable String token,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UUID capsuleId = capsuleService.joinCapsule(token, user);
        return ResponseEntity.ok(ApiSuccessResponse.of(capsuleId.toString(), "Joined capsule successfully"));
    }

    @DeleteMapping("/{capsuleId}")
    public ResponseEntity<ApiResponse> deleteCapsule(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        User me = (User) authentication.getPrincipal();
        capsuleService.deleteCapsule(capsuleId, me);
        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Capsule deleted"));
    }

    @GetMapping("/{capsuleId}/members")
    public ResponseEntity<ApiResponse> getCapsuleMembers(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        User me = (User) authentication.getPrincipal();
        List<CapsuleMemberResponse> data = capsuleService.getCapsuleMembers(capsuleId, me);
        return ResponseEntity.ok(ApiSuccessResponse.of(data, "Members fetched"));
    }

    @PutMapping("/{capsuleId}")
    public ResponseEntity<ApiResponse> updateCapsule(
            @PathVariable UUID capsuleId,
            @RequestBody CapsuleUpdateRequest request,
            Authentication authentication) {
        User me = (User) authentication.getPrincipal();
        capsuleService.updateCapsule(capsuleId, request, me);
        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Capsule updated"));
    }

    @DeleteMapping("/{capsuleId}/members/{userId}")
    public ResponseEntity<ApiResponse> removeMember(
            @PathVariable UUID capsuleId,
            @PathVariable UUID userId,
            Authentication authentication) {
        User me = (User) authentication.getPrincipal();
        capsuleService.removeMember(capsuleId, userId, me);
        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Member removed"));
    }

    @PostMapping("/{capsuleId}/leave")
    public ResponseEntity<ApiResponse> leaveCapsule(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        User me = (User) authentication.getPrincipal();
        capsuleService.leaveCapsule(capsuleId, me);
        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Left capsule"));
    }
}
