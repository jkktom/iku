package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.Champion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChampionRepository extends JpaRepository<Champion,String> {
    List<Champion> findByVersion(String version);
    void deleteByVersion(String version);
}
