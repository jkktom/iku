package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.MatchTimeline;
import org.mtvs.backend.riot.entity.MatchTimelineId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchTimelineRepository extends JpaRepository<MatchTimeline, MatchTimelineId> {
}
