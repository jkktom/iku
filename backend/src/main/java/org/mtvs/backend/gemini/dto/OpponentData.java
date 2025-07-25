package org.mtvs.backend.gemini.dto;

/**
 * 선별적 상대방 데이터 - 핵심 지표만 포함하여 토큰 사용량 최소화
 */
public class OpponentData {
    private String championName;
    private String summonerName;
    private int participantId;
    private int teamId;
    
    // 핵심 성능 지표 (5개로 제한)
    private int kills;
    private int deaths;
    private int assists;
    private int totalCS;
    private int goldEarned;
    
    // 라인전 비교 지표
    private int csDifference;  // 플레이어 대비 CS 차이
    private int goldDifference; // 플레이어 대비 골드 차이
    private String laneResult; // "won", "even", "lost"
    
    // 키 이벤트 (최소한만)
    private int killsAgainstPlayer; // 분석 대상에 대한 킷수
    private int deathsToPlayer;     // 분석 대상에게 당한 데스 수
    
    public OpponentData() {}
    
    public OpponentData(String championName, String summonerName, int participantId, int teamId) {
        this.championName = championName;
        this.summonerName = summonerName;
        this.participantId = participantId;
        this.teamId = teamId;
    }
    
    // Getters and Setters
    public String getChampionName() {
        return championName;
    }
    
    public void setChampionName(String championName) {
        this.championName = championName;
    }
    
    public String getSummonerName() {
        return summonerName;
    }
    
    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }
    
    public int getParticipantId() {
        return participantId;
    }
    
    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }
    
    public int getTeamId() {
        return teamId;
    }
    
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public int getAssists() {
        return assists;
    }
    
    public void setAssists(int assists) {
        this.assists = assists;
    }
    
    public int getTotalCS() {
        return totalCS;
    }
    
    public void setTotalCS(int totalCS) {
        this.totalCS = totalCS;
    }
    
    public int getGoldEarned() {
        return goldEarned;
    }
    
    public void setGoldEarned(int goldEarned) {
        this.goldEarned = goldEarned;
    }
    
    public int getCsDifference() {
        return csDifference;
    }
    
    public void setCsDifference(int csDifference) {
        this.csDifference = csDifference;
    }
    
    public int getGoldDifference() {
        return goldDifference;
    }
    
    public void setGoldDifference(int goldDifference) {
        this.goldDifference = goldDifference;
    }
    
    public String getLaneResult() {
        return laneResult;
    }
    
    public void setLaneResult(String laneResult) {
        this.laneResult = laneResult;
    }
    
    public int getKillsAgainstPlayer() {
        return killsAgainstPlayer;
    }
    
    public void setKillsAgainstPlayer(int killsAgainstPlayer) {
        this.killsAgainstPlayer = killsAgainstPlayer;
    }
    
    public int getDeathsToPlayer() {
        return deathsToPlayer;
    }
    
    public void setDeathsToPlayer(int deathsToPlayer) {
        this.deathsToPlayer = deathsToPlayer;
    }
    
    /**
     * KDA 문자열 반환
     */
    public String getKdaString() {
        return kills + "/" + deaths + "/" + assists;
    }
    
    /**
     * 간단한 성능 요약 반환 (토큰 절약용)
     */
    public String getPerformanceSummary() {
        return String.format("%s (%s) - %s, CS:%d, 골드:%dk", 
            championName, getKdaString(), laneResult, totalCS, goldEarned/1000);
    }
}