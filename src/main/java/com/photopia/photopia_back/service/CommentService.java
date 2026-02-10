package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.CommentResponse;
import com.photopia.photopia_back.model.Comment;
import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final MediaService mediaService;

    public CommentService(CommentRepository commentRepository, MediaService mediaService) {
        this.commentRepository = commentRepository;
        this.mediaService = mediaService;
    }

    @Transactional
    public CommentResponse addComment(UUID mediaId, User user, String content) {
        Media media = mediaService.getMediaById(mediaId);

        Comment comment = Comment.builder()
                .media(media)
                .user(user)
                .content(content)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Update comment count on media
        media.setCommentCount(media.getCommentCount() + 1);
        // Media is managed, so it should be auto-saved at transaction end, but we can
        // verify if explicit save is better.
        // With JPA strictness, sometimes modifying the entity is enough.

        return mapToResponse(savedComment);
    }

    public List<CommentResponse> getCommentsByMedia(UUID mediaId) {
        // Ensure media exists
        mediaService.getMediaById(mediaId);

        return commentRepository.findByMediaIdOrderByCreatedAtAsc(mediaId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deleteComment(UUID commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isMediaOwner = comment.getMedia().getUser().getId().equals(user.getId());

        if (!isCommentOwner && !isMediaOwner) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }

        Media media = comment.getMedia();
        media.setCommentCount(Math.max(0, media.getCommentCount() - 1));

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser() != null ? comment.getUser().getId() : null,
                comment.getCreatedAt());
    }
}
