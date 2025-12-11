package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.jwt.JwtUtil;
import com.photopia.photopia_back.jwt.SecurityConfig;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.UserRepository;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }

    // TEST
    @GetMapping
    public List<User> list() {
        return repo.findAll();
    }

    /**
     * Create user response entity.
     *
     * @param user the user
     * @return the response entity
     */
    @PostMapping("/create")
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
                .message("User created successfully")
                .data(savedUser)
                .build();

        return ResponseEntity
                .created(URI.create("/api/users/" + savedUser.getId()))
                .body(successResponse);
    }


    /**
     * Login user response entity.
     *
     * @param body the body
     * @return the response entity
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(
            @RequestBody Map<String, String> body
    ) {

        String email = body.get("email");
        String password = body.get("password");

        Optional<User> optUser = repo.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .message("User not found")
                            .build()
            );
        }

        User user = optUser.get();

        if (!user.getPassword().equals(password)) {
            // TODO: changer par un hascode
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Incorrect password")
                            .build()
            );
        }

        String token = jwtUtil.generateToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Login successful")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Current user")
                        .data(currentUser)
                        .build()
        );
    }
}
