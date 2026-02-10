package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.CapsuleCreateRequest;
import com.photopia.photopia_back.dto.CapsuleGetMyCapsulesResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.User;
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

    public CapsuleController(CapsuleRepository capsuleRepository, UserRepository userRepository) {
        this.capsuleRepository = capsuleRepository;
        this.userRepository = userRepository;
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

        return ResponseEntity.ok(ApiSuccessResponse.of(savedCapsule, "Album created"));
    }

    @GetMapping("/get")
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

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Capsules fetched")
                        .data(data)
                        .build()
        );
    }

}
