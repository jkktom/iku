package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Byte> {
    
    /**
     * Find EventType by name
     */
    Optional<EventType> findByName(String name);
    
    /**
     * Get all event type IDs for checking which ones exist
     */
    @Query("SELECT e.id FROM EventType e")
    List<Byte> findAllIds();
    
    /**
     * Check if an event type with given name exists
     */
    boolean existsByName(String name);
}