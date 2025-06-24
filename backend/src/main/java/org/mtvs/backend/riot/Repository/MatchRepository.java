package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, String> {
    // 기본적인 CRUD 작업은 JpaRepository에서 제공
    // 추가적인 쿼리 메서드가 필요한 경우 여기에 정의
}
