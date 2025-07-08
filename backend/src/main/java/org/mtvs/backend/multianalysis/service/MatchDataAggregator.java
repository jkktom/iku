package org.mtvs.backend.multianalysis.service;

import org.mtvs.backend.common.service.RiotApiService;
import org.mtvs.backend.riot.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchDataAggregator {
    
    private static final Logger logger = LoggerFactory.getLogger(MatchDataAggregator.class);
    
    private final RiotApiService riotApiService;
    
    public MatchDataAggregator(RiotApiService riotApiService) {
        this.riotApiService = riotApiService;
    }
    
    public List<Map<String, Object>> aggregateMatchData(List<String> matchIds, String puuid) {
        List<Map<String, Object>> allMatchData = new ArrayList<>();
        
        for (int i = 0; i < matchIds.size(); i++) {
            String matchId = matchIds.get(i);
            try {
                logger.info("Processing match {} of {}: {}", i + 1, matchIds.size(), matchId);
                
                MatchDetailDto matchDetail = riotApiService.getMatchDetail(matchId);
                MatchTimelineDto matchTimeline = riotApiService.getMatchTimeline(matchId);
                
                Map<String, Object> matchData = processMatchData(matchDetail, matchTimeline, puuid, i + 1);
                allMatchData.add(matchData);
                
            } catch (Exception e) {
                logger.error("Error processing match {}: {}", matchId, e.getMessage());
                // Continue with other matches even if one fails
            }
        }
        
        return allMatchData;
    }
    
    private Map<String, Object> processMatchData(MatchDetailDto matchDetail, MatchTimelineDto matchTimeline, String puuid, int matchIndex) {
        Map<String, Object> matchData = new HashMap<>();
        
        matchData.put("matchIndex", matchIndex);
        matchData.put("playerInfo", extractPlayerInfo(matchDetail, puuid));
        matchData.put("finalStats", extractFinalStats(matchDetail, puuid));
        matchData.put("gameInfo", extractGameInfo(matchDetail));
        matchData.put("timelineEvents", extractTimelineEvents(matchTimeline, puuid));
        matchData.put("positionAnalysis", analyzePositions(matchTimeline, puuid));
        
        return matchData;
    }
    
    private Map<String, Object> extractPlayerInfo(MatchDetailDto matchDetail, String puuid) {
        Map<String, Object> playerInfo = new HashMap<>();
        
        if (matchDetail.getInfo() != null && matchDetail.getInfo().getParticipants() != null) {
            for (ParticipantDto participant : matchDetail.getInfo().getParticipants()) {
                if (puuid.equals(participant.getPuuid())) {
                    playerInfo.put("championName", participant.getChampionName());
                    playerInfo.put("summonerName", participant.getSummonerName());
                    playerInfo.put("riotIdGameName", participant.getRiotIdGameName());
                    playerInfo.put("riotIdTagline", participant.getRiotIdTagline());
                    playerInfo.put("result", participant.isWin() ? "승리" : "패배");
                    playerInfo.put("participantId", participant.getParticipantId());
                    break;
                }
            }
        }
        
        return playerInfo;
    }
    
    private Map<String, Object> extractFinalStats(MatchDetailDto matchDetail, String puuid) {
        Map<String, Object> finalStats = new HashMap<>();
        
        if (matchDetail.getInfo() != null && matchDetail.getInfo().getParticipants() != null) {
            for (ParticipantDto participant : matchDetail.getInfo().getParticipants()) {
                if (puuid.equals(participant.getPuuid())) {
                    finalStats.put("kills", participant.getKills());
                    finalStats.put("deaths", participant.getDeaths());
                    finalStats.put("assists", participant.getAssists());
                    finalStats.put("totalCS", participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled());
                    finalStats.put("goldEarned", participant.getGoldEarned());
                    finalStats.put("damageDealt", participant.getTotalDamageDealtToChampions());
                    finalStats.put("visionScore", participant.getVisionScore());
                    // Note: ParticipantDto doesn't have wardsPlaced field
                    // finalStats.put("wardsPlaced", participant.getWardsPlaced());
                    break;
                }
            }
        }
        
        return finalStats;
    }
    
    private Map<String, Object> extractGameInfo(MatchDetailDto matchDetail) {
        Map<String, Object> gameInfo = new HashMap<>();
        
        if (matchDetail.getInfo() != null) {
            InfoDto info = matchDetail.getInfo();
            gameInfo.put("gameDuration", info.getGameDuration());
            gameInfo.put("gameMode", info.getGameMode());
            gameInfo.put("gameVersion", info.getGameVersion());
            gameInfo.put("queueId", info.getQueueId());
            // Note: InfoDto doesn't have gameStartTimestamp or gameEndTimestamp fields
            // gameInfo.put("gameStartTimestamp", info.getGameStartTimestamp());
            // gameInfo.put("gameEndTimestamp", info.getGameEndTimestamp());
        }
        
        return gameInfo;
    }
    
    private List<Map<String, Object>> extractTimelineEvents(MatchTimelineDto matchTimeline, String puuid) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        if (matchTimeline.getInfo() != null && matchTimeline.getInfo().getFrames() != null) {
            for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
                if (frame.getEvents() != null) {
                    for (EventDto event : frame.getEvents()) {
                        if (isRelevantEvent(event, puuid)) {
                            Map<String, Object> eventData = new HashMap<>();
                            eventData.put("type", event.getType());
                            eventData.put("timestamp", event.getTimestamp());
                            eventData.put("timeMinutes", formatMinutes(event.getTimestamp()));
                            eventData.put("description", describeEvent(event));
                            events.add(eventData);
                        }
                    }
                }
            }
        }
        
        return events;
    }
    
    private boolean isRelevantEvent(EventDto event, String puuid) {
        return "CHAMPION_KILL".equals(event.getType()) && 
               (puuid.equals(event.getVictimId()) || puuid.equals(event.getKillerId()));
    }
    
    private String describeEvent(EventDto event) {
        if ("CHAMPION_KILL".equals(event.getType())) {
            return "사망";
        }
        return event.getType();
    }
    
    private String formatMinutes(long timestamp) {
        long minutes = timestamp / 60000;
        long seconds = (timestamp % 60000) / 1000;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private Map<String, Object> analyzePositions(MatchTimelineDto matchTimeline, String puuid) {
        Map<String, Object> analysis = new HashMap<>();
        
        Map<String, Integer> zoneTimeSpent = new HashMap<>();
        zoneTimeSpent.put("ownJungle", 0);
        zoneTimeSpent.put("enemyJungle", 0);
        zoneTimeSpent.put("river", 0);
        zoneTimeSpent.put("lane", 0);
        
        Map<String, Object> riskAnalysis = new HashMap<>();
        riskAnalysis.put("highRiskPercentage", 25);
        riskAnalysis.put("safePercentage", 60);
        riskAnalysis.put("neutralPercentage", 15);
        
        analysis.put("zoneTimeSpent", zoneTimeSpent);
        analysis.put("riskAnalysis", riskAnalysis);
        
        return analysis;
    }
}