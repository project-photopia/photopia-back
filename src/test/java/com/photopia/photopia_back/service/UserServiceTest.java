package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.LoginRequest;
import com.photopia.photopia_back.dto.RegisterRequest;
import com.photopia.photopia_back.jwt.JwtUtil;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_ShouldReturnUser_WhenFound() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(id));
    }

    @Test
    void register_ShouldReturnUserAndToken_WhenSuccess() {
        RegisterRequest request = new RegisterRequest("newuser", "test@test.com", "password");
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername("newuser");
        savedUser.setEmail("test@test.com");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock-token");
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");

        Map<String, Object> result = userService.register(request);

        assertNotNull(result);
        assertEquals(savedUser, result.get("user"));
        assertEquals("mock-token", result.get("token"));
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void login_ShouldReturnUserAndToken_WhenSuccess() {
        LoginRequest request = new LoginRequest("test@test.com", "password");
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("mock-token");
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

        Map<String, Object> result = userService.login(request);

        assertNotNull(result);
        assertEquals(user, result.get("user"));
        assertEquals("mock-token", result.get("token"));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpassword");
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }
}
