package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    List<Reaction> findByMediaId(UUID mediaId);

    Optional<Reaction> findByMediaIdAndUserIdAndEmoji(UUID mediaId, UUID userId, String emoji);
}
