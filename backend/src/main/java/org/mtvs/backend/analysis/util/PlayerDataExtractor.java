package org.mtvs.backend.analysis.util;

import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.ParticipantDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 매치 데이터에서 플레이어 정보를 추출하는 유틸리티 클래스
 */
@Component
public class PlayerDataExtractor {
    
    /**
     * 매치에서 특정 PUUID를 가진 참가자 찾기
     */
    public ParticipantDto findParticipantByPuuid(MatchDetailDto matchDetail, String puuid) {
        if (matchDetail == null || matchDetail.getInfo() == null || 
            matchDetail.getInfo().getParticipants() == null) {
            throw new IllegalArgumentException("매치 데이터가 유효하지 않습니다.");
        }
        
        return matchDetail.getInfo().getParticipants().stream()
                .filter(p -> puuid.equals(p.getPuuid()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 PUUID를 가진 플레이어를 찾을 수 없습니다: " + puuid));
    }
    
    /**
     * 매치에서 플레이어의 기본 정보 추출
     */
    public Map<String, Object> extractPlayerBasicData(MatchDetailDto matchDetail, String puuid) {
        ParticipantDto participant = findParticipantByPuuid(matchDetail, puuid);
        
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("puuid", participant.getPuuid());
        playerData.put("participantId", participant.getParticipantId());
        playerData.put("championName", participant.getChampionName());
        playerData.put("teamId", participant.getTeamId());
        playerData.put("riotIdGameName", participant.getRiotIdGameName());
        playerData.put("riotIdTagline", participant.getRiotIdTagline());
        playerData.put("summonerName", participant.getSummonerName());
        
        return playerData;
    }
    
    /**
     * 매치에서 플레이어의 상세 통계 추출
     */
    public Map<String, Object> extractPlayerDetailedStats(MatchDetailDto matchDetail, String puuid) {
        ParticipantDto participant = findParticipantByPuuid(matchDetail, puuid);
        
        Map<String, Object> statsData = new HashMap<>();
        
        // KDA
        statsData.put("kills", participant.getKills());
        statsData.put("deaths", participant.getDeaths());
        statsData.put("assists", participant.getAssists());
        
        // 피해량
        statsData.put("totalDamageDealtToChampions", participant.getTotalDamageDealtToChampions());
        statsData.put("magicDamageDealtToChampions", participant.getMagicDamageDealtToChampions());
        statsData.put("physicalDamageDealtToChampions", participant.getPhysicalDamageDealtToChampions());
        statsData.put("trueDamageDealtToChampions", participant.getTrueDamageDealtToChampions());
        statsData.put("totalDamageTaken", participant.getTotalDamageTaken());
        
        // 경제
        statsData.put("goldEarned", participant.getGoldEarned());
        statsData.put("totalMinionsKilled", participant.getTotalMinionsKilled());
        statsData.put("neutralMinionsKilled", participant.getNeutralMinionsKilled());
        
        // 시야
        statsData.put("visionScore", participant.getVisionScore());
        statsData.put("wardsPlaced", participant.getWardsPlaced());
        statsData.put("wardsKilled", participant.getWardsKilled());
        statsData.put("controlWardsPlaced", participant.getControlWardsPlaced());
        
        // 게임 결과
        statsData.put("win", participant.isWin());
        
        return statsData;
    }
    
    /**
     * 플레이어의 커뮤니케이션 데이터 추출
     */
    public Map<String, Object> extractPlayerCommunicationData(MatchDetailDto matchDetail, String puuid) {
        ParticipantDto participant = findParticipantByPuuid(matchDetail, puuid);
        
        Map<String, Object> commData = new HashMap<>();
        
        // 각종 핑 데이터
        commData.put("allInPings", participant.getAllInPings());
        commData.put("assistMePings", participant.getAssistMePings());
        commData.put("baitPings", participant.getBaitPings());
        commData.put("commandPings", participant.getCommandPings());
        commData.put("dangerPings", participant.getDangerPings());
        commData.put("enemyMissingPings", participant.getEnemyMissingPings());
        commData.put("enemyVisionPings", participant.getEnemyVisionPings());
        commData.put("getBackPings", participant.getGetBackPings());
        commData.put("holdPings", participant.getHoldPings());
        commData.put("needVisionPings", participant.getNeedVisionPings());
        commData.put("onMyWayPings", participant.getOnMyWayPings());
        commData.put("pushPings", participant.getPushPings());
        commData.put("visionClearedPings", participant.getVisionClearedPings());
        
        // 총 핑 수 계산
        int totalPings = participant.getAllInPings() + participant.getAssistMePings() + 
                        participant.getBaitPings() + participant.getCommandPings() + 
                        participant.getDangerPings() + participant.getEnemyMissingPings() + 
                        participant.getEnemyVisionPings() + participant.getGetBackPings() + 
                        participant.getHoldPings() + participant.getNeedVisionPings() + 
                        participant.getOnMyWayPings() + participant.getPushPings() + 
                        participant.getVisionClearedPings();
        
        commData.put("totalPings", totalPings);
        
        return commData;
    }
    
    /**
     * 플레이어 이름을 "게임명#태그" 형식으로 생성
     */
    public String extractPlayerName(ParticipantDto participant) {
        if (participant.getRiotIdGameName() != null && participant.getRiotIdTagline() != null) {
            return participant.getRiotIdGameName() + "#" + participant.getRiotIdTagline();
        } else if (participant.getSummonerName() != null) {
            return participant.getSummonerName();
        } else {
            return "Unknown Player";
        }
    }
    
    /**
     * 매치에서 특정 팀의 모든 플레이어 추출
     */
    public Map<String, Object> extractTeamData(MatchDetailDto matchDetail, int teamId) {
        Map<String, Object> teamData = new HashMap<>();
        
        var teammates = matchDetail.getInfo().getParticipants().stream()
                .filter(p -> p.getTeamId() == teamId)
                .map(p -> Map.of(
                    "puuid", p.getPuuid(),
                    "championName", p.getChampionName(),
                    "playerName", extractPlayerName(p),
                    "participantId", p.getParticipantId()
                ))
                .toList();
        
        teamData.put("teamId", teamId);
        teamData.put("teammates", teammates);
        teamData.put("teamSize", teammates.size());
        
        return teamData;
    }
}