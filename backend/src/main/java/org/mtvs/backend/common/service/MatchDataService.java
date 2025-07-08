package org.mtvs.backend.common.service;

import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;

import java.util.Map;

public interface MatchDataService {
    
    void saveMatchData(String matchId, MatchDetailDto matchDetail, MatchTimelineDto matchTimeline);
    
    Map<String, Object> convertMatchDetailToMap(MatchDetailDto matchDetail);
    
    Map<String, Object> convertMatchTimelineToMap(MatchTimelineDto matchTimeline);
    
    boolean isMatchDataExists(String matchId);
}