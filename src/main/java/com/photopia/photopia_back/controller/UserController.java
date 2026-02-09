package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.jwt.JwtUtil;
import com.photopia.photopia_back.model.ApiError;
import com.photopia.photopia_back.model.ApiErrorResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.UserRepository;
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
        @PostMapping("/register")
        public ResponseEntity<ApiResponse> createUser(
                        @RequestBody com.photopia.photopia_back.dto.RegisterRequest request) {
                if (repo.findByEmail(request.email()).isPresent()) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message("Email already exist")
                                                        .code("EMAIL_ALREADY_EXISTS")
                                                        .build()));
                }

                User user = new User();
                user.setUsername(request.username());
                user.setEmail(request.email());
                user.setPassword(request.password());

                User savedUser = repo.save(user);

                String token = jwtUtil.generateToken(savedUser);
                Map<String, Object> data = new HashMap<>();
                data.put("user", savedUser);
                data.put("token", token);

                return ResponseEntity
                                .created(URI.create("/api/users/" + savedUser.getId()))
                                .body(ApiSuccessResponse.of(data, "User created successfully"));
        }

        /**
         * Login user response entity.
         *
         * @param body the body
         * @return the response entity
         */
        @PostMapping("/login")
        public ResponseEntity<ApiResponse> loginUser(
                        @RequestBody com.photopia.photopia_back.dto.LoginRequest request) {

                String email = request.email();
                String password = request.password();

                Optional<User> optUser = repo.findByEmail(email);
                if (optUser.isEmpty()) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message("User not found")
                                                        .code("USER_NOT_FOUND")
                                                        .build()));
                }

                User user = optUser.get();

                if (user.getPassword() == null || !user.getPassword().equals(password)) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message("Incorrect password")
                                                        .code("INCORRECT_PASSWORD")
                                                        .build()));
                }

                String token = jwtUtil.generateToken(user);
                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("token", token);

                return ResponseEntity.ok(ApiSuccessResponse.of(data, "Login successful"));
        }

        @GetMapping("/me")
        public ResponseEntity<ApiResponse> getMe() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User currentUser = (User) authentication.getPrincipal();

                return ResponseEntity.ok(ApiSuccessResponse.of(currentUser, "Current user"));
        }
}
