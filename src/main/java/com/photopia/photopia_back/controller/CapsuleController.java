package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.capsule.CapsuleCreateRequest;
import com.photopia.photopia_back.dto.capsule.CapsuleGetMyCapsulesResponse;
import com.photopia.photopia_back.dto.capsule.CapsuleMemberResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.ApiErrorResponse;
import com.photopia.photopia_back.model.ApiError;
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
                try {
                        Capsule savedCapsule = capsuleService.createCapsule(request, owner);
                        return ResponseEntity.ok(ApiSuccessResponse.of(savedCapsule, "Album created"));
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message(e.getMessage())
                                                        .code("CREATION_FAILED")
                                                        .build()));
                }
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
                try {
                        String link = capsuleService.generateInviteLink(capsuleId, user);
                        return ResponseEntity.ok(ApiSuccessResponse.of(link, "Invite link generated"));
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message(e.getMessage())
                                                        .code("LINK_GENERATION_FAILED")
                                                        .build()));
                }
        }

        @PostMapping("/join/{token}")
        public ResponseEntity<ApiResponse> joinCapsule(
                        @PathVariable String token,
                        Authentication authentication) {
                User user = (User) authentication.getPrincipal();
                try {
                        UUID capsuleId = capsuleService.joinCapsule(token, user);
                        return ResponseEntity.ok(ApiSuccessResponse.of(capsuleId.toString(), "Joined capsule successfully"));
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message(e.getMessage())
                                                        .code("JOIN_FAILED")
                                                        .build()));
                }
        }

        @DeleteMapping("/{capsuleId}")
        public ResponseEntity<ApiResponse> deleteCapsule(
                        @PathVariable UUID capsuleId,
                        Authentication authentication) {
                User me = (User) authentication.getPrincipal();
                try {
                        capsuleService.deleteCapsule(capsuleId, me);
                        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Capsule deleted"));
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(403)
                                                        .message(e.getMessage())
                                                        .code("DELETE_FAILED")
                                                        .build()));
                }
        }

        @GetMapping("/{capsuleId}/members")
        public ResponseEntity<ApiResponse> getCapsuleMembers(
                        @PathVariable UUID capsuleId) {
                List<CapsuleMemberResponse> data = capsuleService.getCapsuleMembers(capsuleId);
                return ResponseEntity.ok(ApiSuccessResponse.of(data, "Members fetched"));
        }
}
