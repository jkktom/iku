package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.ParticipantFrame;
import org.mtvs.backend.riot.entity.ParticipantFrameId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantFrameRepository extends JpaRepository<ParticipantFrame, ParticipantFrameId> {
}
