package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByCapsuleIdOrderByTakenAtDesc(UUID capsuleId);
}
