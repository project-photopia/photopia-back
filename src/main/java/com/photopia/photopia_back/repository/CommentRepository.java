package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByMediaIdOrderByCreatedAtAsc(UUID mediaId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.media.id IN (SELECT m.id FROM Media m WHERE m.capsule.id = :capsuleId)")
    void deleteByCapsuleId(@Param("capsuleId") UUID capsuleId);
}
