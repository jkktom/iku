package org.mtvs.backend.riot.service;

import org.mtvs.backend.riot.Repository.RiotUserRepository;
import org.mtvs.backend.riot.entity.RiotUser;
import org.springframework.stereotype.Service;

@Service
public class RiotUserService {
    public RiotUserRepository riotUserRepository;

    public RiotUserService(RiotUserRepository riotUserRepository) {
        this.riotUserRepository = riotUserRepository;
    }

    public RiotUser saveOrUpdate(String puuid, String gameName, String tagLine){
        RiotUser riotUser = riotUserRepository.findByPuuid(puuid);
        if(riotUser == null){
            riotUser = new RiotUser();
            riotUser.setPuuid(puuid);
        }
        riotUser.setGameName(gameName);
        riotUser.setTagLine(tagLine);
        return riotUserRepository.save(riotUser);
    }
}
