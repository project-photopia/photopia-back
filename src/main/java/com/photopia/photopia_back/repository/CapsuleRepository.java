package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Capsule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {
    Optional<Capsule> findByJoinToken(String joinToken);
}
