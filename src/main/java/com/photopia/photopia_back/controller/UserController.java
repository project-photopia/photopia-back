package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<User> list() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody User user) {
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            ApiResponse errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Email already exist")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }

        User savedUser = repo.save(user);

        ApiResponse successResponse = ApiResponse.builder()
                .success(true)
                .message("User créé avec succès")
                .data(savedUser)
                .build();

        return ResponseEntity
                .created(URI.create("/api/users/" + savedUser.getId()))
                .body(successResponse);
    }

}
