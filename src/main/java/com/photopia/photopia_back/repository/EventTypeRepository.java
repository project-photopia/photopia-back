package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType, UUID> {
    Optional<EventType> findByName(String name);
}
