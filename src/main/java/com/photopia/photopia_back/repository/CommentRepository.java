package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByMediaIdOrderByCreatedAtAsc(UUID mediaId);
}
