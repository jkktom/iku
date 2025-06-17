package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.MatchTimeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchTimelineRepository extends JpaRepository<MatchTimeline,Long> {
}
