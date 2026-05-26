package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.LoginRequest;
import com.photopia.photopia_back.dto.RegisterRequest;
import com.photopia.photopia_back.dto.UpdateUserRequest;
import com.photopia.photopia_back.model.ApiError;
import com.photopia.photopia_back.model.ApiErrorResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        // TEST
        @GetMapping
        public List<User> list() {
                return userService.getAllUsers();
        }

        /**
         * Create user response entity.
         *
         * @param request the registration request
         * @return the response entity
         */
        @PostMapping("/register")
        public ResponseEntity<ApiResponse> createUser(@RequestBody RegisterRequest request) {
                try {
                        Map<String, Object> data = userService.register(request);
                        User user = (User) data.get("user");
                        return ResponseEntity
                                        .created(URI.create("/api/users/" + user.getId()))
                                        .body(ApiSuccessResponse.of(data, "User created successfully"));
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message(e.getMessage())
                                                        .code("REGISTRATION_FAILED")
                                                        .build()));
                }
        }

        /**
         * Login user response entity.
         *
         * @param request the login request
         * @return the response entity
         */
        @PostMapping("/login")
        public ResponseEntity<ApiResponse> loginUser(@RequestBody LoginRequest request) {
                try {
                        Map<String, Object> data = userService.login(request);
                        return ResponseEntity.ok(ApiSuccessResponse.of(data, "Login successful"));
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(
                                        ApiErrorResponse.of(ApiError.builder()
                                                        .status(400)
                                                        .message(e.getMessage())
                                                        .code("LOGIN_FAILED")
                                                        .build()));
                }
        }

        @PatchMapping("/me")
        public ResponseEntity<ApiResponse> updateMe(@RequestBody UpdateUserRequest request) {
                User currentUser = (User) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();
                try {
                        User updated = userService.updateProfile(currentUser.getId(), request);
                        return ResponseEntity.ok(ApiSuccessResponse.of(updated, "Profile updated"));
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(
                                ApiErrorResponse.of(ApiError.builder()
                                        .status(400)
                                        .message(e.getMessage())
                                        .code("PROFILE_UPDATE_FAILED")
                                        .build()));
                }
        }

        @GetMapping("/me")
        public ResponseEntity<ApiResponse> getMe() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User currentUser = (User) authentication.getPrincipal(); // This principal might be a UserDetails object
                                                                         // or our User entity depending on
                                                                         // implementation

                // Use service to get cached version if available, or fresh from DB
                User user = userService.getUserById(currentUser.getId());

                return ResponseEntity.ok(ApiSuccessResponse.of(user, "Current user"));
        }
}
