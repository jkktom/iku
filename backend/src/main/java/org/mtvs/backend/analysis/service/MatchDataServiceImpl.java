package org.mtvs.backend.analysis.service;

import org.mtvs.backend.common.service.MatchDataService;
import org.mtvs.backend.riot.dto.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchDataServiceImpl implements MatchDataService {
    
    private final MatchDataPersister matchDataPersister;
    
    public MatchDataServiceImpl(MatchDataPersister matchDataPersister) {
        this.matchDataPersister = matchDataPersister;
    }
    
    @Override
    public void saveMatchData(String matchId, MatchDetailDto matchDetail, MatchTimelineDto matchTimeline) {
        matchDataPersister.saveMatchData(matchId, matchDetail, matchTimeline);
    }
    
    @Override
    public Map<String, Object> convertMatchDetailToMap(MatchDetailDto matchDetail) {
        Map<String, Object> matchData = new HashMap<>();
        
        if (matchDetail.getInfo() != null) {
            InfoDto info = matchDetail.getInfo();
            
            matchData.put("gameDuration", info.getGameDuration());
            matchData.put("gameMode", info.getGameMode());
            matchData.put("gameVersion", info.getGameVersion());
            matchData.put("queueId", info.getQueueId());
            // Note: gameStartTimestamp and gameEndTimestamp methods not available in current DTO
            // matchData.put("gameStartTimestamp", info.getGameStartTimestamp());
            // matchData.put("gameEndTimestamp", info.getGameEndTimestamp());
            
            // participants 변환
            List<Map<String, Object>> participants = new ArrayList<>();
            if (info.getParticipants() != null) {
                for (ParticipantDto participant : info.getParticipants()) {
                    Map<String, Object> participantMap = new HashMap<>();
                    participantMap.put("participantId", participant.getParticipantId());
                    participantMap.put("puuid", participant.getPuuid());
                    participantMap.put("riotIdGameName", participant.getRiotIdGameName());
                    participantMap.put("riotIdTagline", participant.getRiotIdTagline());
                    participantMap.put("summonerName", participant.getSummonerName());
                    participantMap.put("championName", participant.getChampionName());
                    participantMap.put("kills", participant.getKills());
                    participantMap.put("deaths", participant.getDeaths());
                    participantMap.put("assists", participant.getAssists());
                    participantMap.put("totalMinionsKilled", participant.getTotalMinionsKilled());
                    participantMap.put("neutralMinionsKilled", participant.getNeutralMinionsKilled());
                    participantMap.put("goldEarned", participant.getGoldEarned());
                    participantMap.put("totalDamageDealtToChampions", participant.getTotalDamageDealtToChampions());
                    participantMap.put("visionScore", participant.getVisionScore());
                    // Note: wardsPlaced method not available in current ParticipantDto
                    // participantMap.put("wardsPlaced", participant.getWardsPlaced());
                    participantMap.put("win", participant.isWin());
                    
                    participants.add(participantMap);
                }
            }
            matchData.put("participants", participants);
        }
        
        return matchData;
    }
    
    @Override
    public Map<String, Object> convertMatchTimelineToMap(MatchTimelineDto matchTimeline) {
        Map<String, Object> timelineData = new HashMap<>();
        
        if (matchTimeline.getInfo() != null) {
            timelineData.put("frameInterval", matchTimeline.getInfo().getFrameInterval());
            
            List<Map<String, Object>> frames = new ArrayList<>();
            if (matchTimeline.getInfo().getFrames() != null) {
                for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
                    Map<String, Object> frameData = new HashMap<>();
                    frameData.put("timestamp", frame.getTimestamp());
                    
                    // 참가자 프레임 데이터
                    Map<String, Object> participantFrames = new HashMap<>();
                    if (frame.getParticipantFrames() != null) {
                        frame.getParticipantFrames().forEach((participantId, frameInfo) -> {
                            Map<String, Object> participantData = new HashMap<>();
                            // Note: currentGold and xp methods not available in current ParticipantFrameDto
                            // participantData.put("currentGold", frameInfo.getCurrentGold());
                            participantData.put("totalGold", frameInfo.getTotalGold());
                            participantData.put("level", frameInfo.getLevel());
                            // participantData.put("xp", frameInfo.getXp());
                            participantData.put("minionsKilled", frameInfo.getMinionsKilled());
                            participantData.put("jungleMinionsKilled", frameInfo.getJungleMinionsKilled());
                            
                            if (frameInfo.getPosition() != null) {
                                Map<String, Integer> position = new HashMap<>();
                                position.put("x", frameInfo.getPosition().getX());
                                position.put("y", frameInfo.getPosition().getY());
                                participantData.put("position", position);
                            }
                            
                            participantFrames.put(participantId, participantData);
                        });
                    }
                    frameData.put("participantFrames", participantFrames);
                    
                    // 이벤트 데이터
                    List<Map<String, Object>> events = new ArrayList<>();
                    if (frame.getEvents() != null) {
                        for (EventDto event : frame.getEvents()) {
                            Map<String, Object> eventData = new HashMap<>();
                            eventData.put("timestamp", event.getTimestamp());
                            eventData.put("type", event.getType());
                            eventData.put("participantId", event.getParticipantId());
                            eventData.put("killerId", event.getKillerId());
                            eventData.put("victimId", event.getVictimId());
                            eventData.put("itemId", event.getItemId());
                            // Note: Some event DTO methods not available in current EventDto
                            // eventData.put("skillSlot", event.getSkillSlot());
                            // eventData.put("levelUpType", event.getLevelUpType());
                            // eventData.put("wardType", event.getWardType());
                            // eventData.put("creatorId", event.getCreatorId());
                            
                            // Note: position methods not available in current EventDto
                            // if (event.getPosition() != null) {
                            //     Map<String, Integer> position = new HashMap<>();
                            //     position.put("x", event.getPosition().getX());
                            //     position.put("y", event.getPosition().getY());
                            //     eventData.put("position", position);
                            // }
                            
                            if (event.getAssistingParticipantIds() != null) {
                                eventData.put("assistingParticipantIds", event.getAssistingParticipantIds());
                            }
                            
                            events.add(eventData);
                        }
                    }
                    frameData.put("events", events);
                    
                    frames.add(frameData);
                }
            }
            timelineData.put("frames", frames);
        }
        
        return timelineData;
    }
    
    @Override
    public boolean isMatchDataExists(String matchId) {
        return matchDataPersister.isMatchDataExists(matchId);
    }
}