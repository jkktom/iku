package org.mtvs.backend.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Riot API League Entry 정보 DTO
 * /lol/league/v4/entries/by-summoner/{summonerId} 응답용
 */
public class LeagueEntryDto {
    
    @JsonProperty("leagueId")
    private String leagueId; // 리그 ID
    
    @JsonProperty("summonerId")
    private String summonerId; // 소환사 ID
    
    @JsonProperty("summonerName")
    private String summonerName; // 소환사 이름
    
    @JsonProperty("queueType")
    private String queueType; // 큐 타입 (RANKED_SOLO_5x5, RANKED_FLEX_SR)
    
    @JsonProperty("tier")
    private String tier; // 티어 (IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, CHALLENGER)
    
    @JsonProperty("rank")
    private String rank; // 랭크 (I, II, III, IV)
    
    @JsonProperty("leaguePoints")
    private int leaguePoints; // 리그 포인트 (LP)
    
    @JsonProperty("wins")
    private int wins; // 승리 수
    
    @JsonProperty("losses")
    private int losses; // 패배 수
    
    @JsonProperty("veteran")
    private boolean veteran; // 베테랑 여부
    
    @JsonProperty("inactive")
    private boolean inactive; // 비활성 여부
    
    @JsonProperty("freshBlood")
    private boolean freshBlood; // 신규 진입 여부
    
    @JsonProperty("hotStreak")
    private boolean hotStreak; // 연승 중 여부

    public LeagueEntryDto() {}

    // Getters and Setters
    public String getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(String leagueId) {
        this.leagueId = leagueId;
    }

    public String getSummonerId() {
        return summonerId;
    }

    public void setSummonerId(String summonerId) {
        this.summonerId = summonerId;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getLeaguePoints() {
        return leaguePoints;
    }

    public void setLeaguePoints(int leaguePoints) {
        this.leaguePoints = leaguePoints;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public boolean isVeteran() {
        return veteran;
    }

    public void setVeteran(boolean veteran) {
        this.veteran = veteran;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public boolean isFreshBlood() {
        return freshBlood;
    }

    public void setFreshBlood(boolean freshBlood) {
        this.freshBlood = freshBlood;
    }

    public boolean isHotStreak() {
        return hotStreak;
    }

    public void setHotStreak(boolean hotStreak) {
        this.hotStreak = hotStreak;
    }

    /**
     * 승률 계산
     */
    public double getWinRate() {
        int totalGames = wins + losses;
        return totalGames > 0 ? (double) wins / totalGames * 100 : 0.0;
    }

    /**
     * 전체 게임 수
     */
    public int getTotalGames() {
        return wins + losses;
    }

    @Override
    public String toString() {
        return "LeagueEntryDto{" +
                "leagueId='" + leagueId + '\'' +
                ", summonerId='" + summonerId + '\'' +
                ", summonerName='" + summonerName + '\'' +
                ", queueType='" + queueType + '\'' +
                ", tier='" + tier + '\'' +
                ", rank='" + rank + '\'' +
                ", leaguePoints=" + leaguePoints +
                ", wins=" + wins +
                ", losses=" + losses +
                ", veteran=" + veteran +
                ", inactive=" + inactive +
                ", freshBlood=" + freshBlood +
                ", hotStreak=" + hotStreak +
                '}';
    }
}