package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.CapsuleMember;
import com.photopia.photopia_back.model.CapsuleMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapsuleMemberRepository extends JpaRepository<CapsuleMember, CapsuleMemberId> {
    List<CapsuleMember> findByCapsuleId(UUID capsuleId);

    List<CapsuleMember> findByUserId(UUID userId);
}
