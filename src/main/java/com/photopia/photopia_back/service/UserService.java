package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.LoginRequest;
import com.photopia.photopia_back.dto.RegisterRequest;
import com.photopia.photopia_back.exception.BadRequestException;
import com.photopia.photopia_back.exception.ResourceNotFoundException;
import com.photopia.photopia_back.dto.UpdateUserRequest;
import com.photopia.photopia_back.jwt.JwtUtil;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    @Cacheable(value = "users_v2", key = "#id")
    public User getUserById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Map<String, Object> register(RegisterRequest request) {
        if (repo.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = repo.save(user);

        String token = jwtUtil.generateToken(savedUser);
        Map<String, Object> data = new HashMap<>();
        data.put("user", savedUser);
        data.put("token", token);
        return data;
    }

    public Map<String, Object> login(LoginRequest request) {
        String email = request.email();
        String password = request.password();

        Optional<User> optUser = repo.findByEmail(email);
        if (optUser.isEmpty()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = optUser.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);

        return data;
    }
    
    public User updateProfile(UUID userId, UpdateUserRequest request) {
    User user = getUserById(userId);

    if (request.username() != null && !request.username().isBlank()) {
        repo.findByUsername(request.username())
            .filter(u -> !u.getId().equals(userId))
            .ifPresent(u -> { throw new RuntimeException("Username already taken"); });
        user.setUsername(request.username().trim());
    }

    if (request.avatarUrl() != null) {
        user.setAvatarUrl(request.avatarUrl());
    }

    return updateUser(user); // réutilise @CacheEvict existant
}

    @CacheEvict(value = "users_v2", key = "#user.id")
    public User updateUser(User user) {
        return repo.save(user);
    }
}
