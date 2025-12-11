package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.CapsuleRepository;
import com.photopia.photopia_back.repository.UserRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/capsules")
public class CapsuleController {

    private final CapsuleRepository capsuleRepository;
    private final UserRepository userRepository;

    public CapsuleController(CapsuleRepository capsuleRepository, UserRepository userRepository) {
        this.capsuleRepository = capsuleRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(
            @RequestBody Capsule capsule
    ) {

        // Start Date
        capsule.setStartDate(LocalDate.now());

        // End Date
        capsule.setEndDate(null);

        // Join Token
        capsule.setJoinToken(String.valueOf(Keys.secretKeyFor(SignatureAlgorithm.HS256)));

        // Owner
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        capsule.setOwner(currentUser);

        if (
                capsule.getStartDate() != null &&
                capsule.getEndDate() != null &&
                capsule.getEndDate().isBefore(capsule.getStartDate())
        ) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .message("endDate cannot be before startDate")
                            .build()
            );
        }

        // Défault Values
        if (capsule.getIsPrivate() == null) {
            capsule.setIsPrivate(true);
        }
        if (capsule.getIsArchived() == null) {
            capsule.setIsArchived(true);
        }
        if (capsule.getMemberCount() == null) {
            capsule.setMemberCount(1);
        }

        Capsule savedCapsule = capsuleRepository.save(capsule);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Album created")
                        .data(savedCapsule)
                        .build()
        );
    }
}
