package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.MemoryResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.service.MemoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/memories")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getMemories() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MemoryResponse> memories = memoryService.getMemoriesForUser(currentUser.getId());
        return ResponseEntity.ok(ApiSuccessResponse.of(memories, "Memories fetched"));
    }
}
