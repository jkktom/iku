package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
}
