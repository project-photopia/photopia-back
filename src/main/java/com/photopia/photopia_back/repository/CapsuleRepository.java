package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {
    Optional<Capsule> findByJoinToken(String joinToken);

    @Query("SELECT c FROM Capsule c LEFT JOIN FETCH c.eventType WHERE c.user = :user")
    List<Capsule> findByUser(User user);

    @Query("SELECT DISTINCT c FROM Capsule c LEFT JOIN FETCH c.eventType " +
           "WHERE c.user.id = :userId OR c.id IN (SELECT cm.capsuleId FROM CapsuleMember cm WHERE cm.userId = :userId)")
    List<Capsule> findByUserOrMember(@Param("userId") UUID userId);

    Optional<Capsule> findByIdAndUser_Id(UUID id, UUID userId);

}
