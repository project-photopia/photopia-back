package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.repository.CapsuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capsules")
public class CapsuleController {

    private final CapsuleRepository capsuleRepository;

    public CapsuleController(CapsuleRepository capsuleRepository) {
        this.capsuleRepository = capsuleRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestBody Capsule capsule) {

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
