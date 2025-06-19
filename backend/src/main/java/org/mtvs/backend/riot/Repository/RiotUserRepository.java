package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.RiotUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiotUserRepository extends JpaRepository<RiotUser, Long> {
    RiotUser findByPuuid(String puuid);
}
