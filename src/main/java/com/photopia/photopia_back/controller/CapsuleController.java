package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
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
import java.util.UUID;

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
            @RequestBody com.photopia.photopia_back.dto.CapsuleCreateRequest request) {

        Capsule capsule = new Capsule();
        capsule.setName(request.name());

        // Start Date
        capsule.setStartDate(LocalDate.now());
        // End Date
        capsule.setEndDate(null);
        // Join Token (Generated)
        capsule.setJoinToken(UUID.randomUUID().toString());

        // User Handling
        User owner;
        if (request.userId() != null) {
            // If userId provided, fetch from DB
            owner = userRepository.findById(request.userId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + request.userId()));
        } else {
            // Fallback to authenticated user
            owner = (User) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
        }
        capsule.setUser(owner);

        // Default Values
        capsule.setIsPrivate(request.isPrivate() != null ? request.isPrivate() : true);
        capsule.setIsArchived(false); // Default logic
        capsule.setMemberCount(1);

        Capsule savedCapsule = capsuleRepository.save(capsule);

        return ResponseEntity.ok(ApiSuccessResponse.of(savedCapsule, "Album created"));
    }
}
