package org.mtvs.backend.common.service;

import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;

import java.util.List;

public interface RiotApiService {
    
    AccountDto getAccountInfo(String gameName, String tagLine);
    
    List<String> getMatchIds(String puuid, int start, int count);
    
    MatchDetailDto getMatchDetail(String matchId);
    
    MatchTimelineDto getMatchTimeline(String matchId);
}