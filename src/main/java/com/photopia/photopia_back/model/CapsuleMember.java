package com.photopia.photopia_back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "capsule_members")
@IdClass(CapsuleMemberId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapsuleMember {

    @Id
    @Column(name = "capsule_id")
    private UUID capsuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capsule_id", insertable = false, updatable = false)
    private Capsule capsule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private Instant joinedAt;

    public enum Role {
        ADMIN, MEMBER
    }
}
