package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {
    Optional<Capsule> findByJoinToken(String joinToken);

    List<Capsule> findByUser(User user);

    Optional<Capsule> findByIdAndUser_Id(UUID id, UUID userId);

}
