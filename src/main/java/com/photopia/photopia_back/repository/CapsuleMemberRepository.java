package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.CapsuleMember;
import com.photopia.photopia_back.model.CapsuleMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleMemberRepository extends JpaRepository<CapsuleMember, CapsuleMemberId> {

    boolean existsByCapsuleIdAndUserId(UUID capsuleId, UUID userId);

    void deleteByCapsuleId(UUID capsuleId);

    boolean existsByCapsuleIdAndUserIdAndRole(UUID capsuleId, UUID userId, CapsuleMember.Role role);

    List<CapsuleMember> findByCapsuleId(UUID capsuleId);

    @org.springframework.data.jpa.repository.Query("SELECT u.email FROM CapsuleMember cm JOIN cm.user u WHERE cm.capsuleId = :capsuleId AND cm.userId != :excludeUserId")
    List<String> findMemberEmails(UUID capsuleId, UUID excludeUserId);
}
