package com.photopia.photopia_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photopia.photopia_back.dto.MediaRegisterRequest;
import com.photopia.photopia_back.dto.PresignedUrlResponse;
import com.photopia.photopia_back.jwt.JwtAuthenticationFilter;
import com.photopia.photopia_back.jwt.JwtUtil;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.Media;

import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.service.CommentService;
import com.photopia.photopia_back.service.MediaService;
import com.photopia.photopia_back.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MediaController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MediaService mediaService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private ReactionService reactionService;

    @MockBean
    private JwtUtil jwtUtil;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");

        // Manually set authentication context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getUploadUrl_ShouldReturnPresignedUrlResponse() throws Exception {
        UUID userId = mockUser.getId();
        PresignedUrlResponse response = new PresignedUrlResponse(
                "http://original-upload", "key", "sig",
                "http://preview-upload", "previewKey", "previewSig");

        when(mediaService.getPresignedUrl("image/jpeg", userId)).thenReturn(response);

        mockMvc.perform(get("/api/media/upload-url")
                .param("contentType", "image/jpeg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("http://original-upload"))
                .andExpect(jsonPath("$.key").value("key"));
    }

    @Test
    void registerMedia_ShouldReturnMediaResponse() throws Exception {
        MediaRegisterRequest request = new MediaRegisterRequest(
                "key", "previewKey", "sig", "previewSig",
                UUID.randomUUID(), 1920, 1080, 48.8, 2.3, Instant.now(), "image/jpeg");

        Media media = new Media();
        media.setId(UUID.randomUUID());
        media.setOriginalUrl("key");
        media.setPreviewUrl("previewKey");
        media.setThumbnailUrl("previewKey");
        media.setMediaType(Media.MediaType.PHOTO);
        media.setUser(mockUser);

        // Setup MediaService mocks
        when(mediaService.registerMedia(any(MediaRegisterRequest.class), any(User.class))).thenReturn(media);
        when(mediaService.getPresignedGetUrl("key")).thenReturn("http://signed-get-url");
        when(mediaService.getPresignedGetUrl("previewKey")).thenReturn("http://signed-preview-url");

        mockMvc.perform(post("/api/media/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // .andExpect(jsonPath("$.originalUrl").value("http://signed-get-url")) // Check
                // mapped response
                .andExpect(jsonPath("$.id").exists());
    }
}
