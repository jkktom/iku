package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;

@Entity
public class RiotUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String puuid;

    @Column(nullable = false)
    private String gameName;

    @Column(nullable = false)
    private String tagLine;

    // 소환사 정보
    @Column(name = "summoner_id", length = 100)
    private String summonerId;
    
    @Column(name = "summoner_level")
    private Long summonerLevel;

    // 솔로랭크 정보
    @Column(name = "solo_tier", length = 20)
    private String soloTier; // IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, CHALLENGER
    
    @Column(name = "solo_rank", length = 5)
    private String soloRank; // I, II, III, IV
    
    @Column(name = "solo_lp")
    private Integer soloLeaguePoints;
    
    @Column(name = "solo_wins")
    private Integer soloWins;
    
    @Column(name = "solo_losses")
    private Integer soloLosses;

    // 자유랭크 정보 (선택적)
    @Column(name = "flex_tier", length = 20)
    private String flexTier;
    
    @Column(name = "flex_rank", length = 5)
    private String flexRank;
    
    @Column(name = "flex_lp")
    private Integer flexLeaguePoints;

    public RiotUser() {
    }

    public RiotUser(Long id, String puuid, String gameName, String tagLine) {
        this.id = id;
        this.puuid = puuid;
        this.gameName = gameName;
        this.tagLine = tagLine;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    // 소환사 정보 getter/setter
    public String getSummonerId() {
        return summonerId;
    }

    public void setSummonerId(String summonerId) {
        this.summonerId = summonerId;
    }

    public Long getSummonerLevel() {
        return summonerLevel;
    }

    public void setSummonerLevel(Long summonerLevel) {
        this.summonerLevel = summonerLevel;
    }

    // 솔로랭크 정보 getter/setter
    public String getSoloTier() {
        return soloTier;
    }

    public void setSoloTier(String soloTier) {
        this.soloTier = soloTier;
    }

    public String getSoloRank() {
        return soloRank;
    }

    public void setSoloRank(String soloRank) {
        this.soloRank = soloRank;
    }

    public Integer getSoloLeaguePoints() {
        return soloLeaguePoints;
    }

    public void setSoloLeaguePoints(Integer soloLeaguePoints) {
        this.soloLeaguePoints = soloLeaguePoints;
    }

    public Integer getSoloWins() {
        return soloWins;
    }

    public void setSoloWins(Integer soloWins) {
        this.soloWins = soloWins;
    }

    public Integer getSoloLosses() {
        return soloLosses;
    }

    public void setSoloLosses(Integer soloLosses) {
        this.soloLosses = soloLosses;
    }

    // 자유랭크 정보 getter/setter
    public String getFlexTier() {
        return flexTier;
    }

    public void setFlexTier(String flexTier) {
        this.flexTier = flexTier;
    }

    public String getFlexRank() {
        return flexRank;
    }

    public void setFlexRank(String flexRank) {
        this.flexRank = flexRank;
    }

    public Integer getFlexLeaguePoints() {
        return flexLeaguePoints;
    }

    public void setFlexLeaguePoints(Integer flexLeaguePoints) {
        this.flexLeaguePoints = flexLeaguePoints;
    }

    // 유틸리티 메서드들
    public boolean hasRankInfo() {
        return soloTier != null || flexTier != null;
    }

    public String getMainRankDisplay() {
        if (soloTier != null) {
            return soloTier + (soloRank != null ? " " + soloRank : "") + 
                   (soloLeaguePoints != null ? " " + soloLeaguePoints + "LP" : "");
        }
        return "UNRANKED";
    }

    public double getSoloWinRate() {
        if (soloWins != null && soloLosses != null) {
            int totalGames = soloWins + soloLosses;
            return totalGames > 0 ? (double) soloWins / totalGames * 100 : 0.0;
        }
        return 0.0;
    }
}
