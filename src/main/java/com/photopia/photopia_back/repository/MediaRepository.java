package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
    @Query("SELECT m FROM Media m LEFT JOIN FETCH m.user WHERE m.capsule.id = :capsuleId ORDER BY m.takenAt DESC")
    List<Media> findByCapsuleIdOrderByTakenAtDesc(@Param("capsuleId") UUID capsuleId);

    @Query("SELECT m FROM Media m WHERE m.capsule.id = :capsuleId ORDER BY m.uploadedAt ASC LIMIT 1")
    Optional<Media> findFirstByCapsuleId(@Param("capsuleId") UUID capsuleId);

    @Query("SELECT COUNT(m) FROM Media m WHERE m.capsule.id = :capsuleId")
    int countByCapsuleId(@Param("capsuleId") UUID capsuleId);

    @Modifying
    @Query("DELETE FROM Media m WHERE m.capsule.id = :capsuleId")
    void deleteByCapsuleId(@Param("capsuleId") UUID capsuleId);

    @Query("SELECT m FROM Media m JOIN FETCH m.user JOIN FETCH m.capsule " +
           "WHERE m.capsule.id IN :capsuleIds " +
           "AND m.takenAt BETWEEN :start AND :end " +
           "ORDER BY m.takenAt DESC")
    List<Media> findByCapsuleIdInAndTakenAtBetween(
            @Param("capsuleIds") List<UUID> capsuleIds,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("SELECT m FROM Media m JOIN FETCH m.user JOIN FETCH m.capsule " +
           "WHERE m.capsule.id IN :capsuleIds " +
           "AND (m.reactionCount + m.commentCount) > 0 " +
           "ORDER BY (m.reactionCount + m.commentCount) DESC")
    List<Media> findTopMediasByCapsuleIds(@Param("capsuleIds") List<UUID> capsuleIds);
}

