package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
