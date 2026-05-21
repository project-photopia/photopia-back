package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.CapsuleMember;
import com.photopia.photopia_back.model.CapsuleMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapsuleMemberRepository extends JpaRepository<CapsuleMember, CapsuleMemberId> {

    boolean existsByCapsuleIdAndUserId(UUID capsuleId, UUID userId);

    @Modifying
    @Query("DELETE FROM CapsuleMember cm WHERE cm.capsuleId = :capsuleId")
    void deleteByCapsuleId(@Param("capsuleId") UUID capsuleId);

    @Modifying
    @Query("DELETE FROM CapsuleMember cm WHERE cm.capsuleId = :capsuleId AND cm.userId = :userId")
    void deleteByCapsuleIdAndUserId(@Param("capsuleId") UUID capsuleId, @Param("userId") UUID userId);

    boolean existsByCapsuleIdAndUserIdAndRole(UUID capsuleId, UUID userId, CapsuleMember.Role role);

    List<CapsuleMember> findByCapsuleId(UUID capsuleId);

    @Query("SELECT u.email FROM CapsuleMember cm JOIN cm.user u WHERE cm.capsuleId = :capsuleId AND cm.userId != :excludeUserId AND u.email IS NOT NULL")
    List<String> findMemberEmails(@Param("capsuleId") UUID capsuleId, @Param("excludeUserId") UUID excludeUserId);
}
