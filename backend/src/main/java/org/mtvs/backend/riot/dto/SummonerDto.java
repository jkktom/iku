package org.mtvs.backend.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Riot API Summoner 정보 DTO
 * /lol/summoner/v4/summoners/by-puuid/{puuid} 응답용
 */
public class SummonerDto {
    
    @JsonProperty("id")
    private String id; // Summoner ID (암호화됨)
    
    @JsonProperty("accountId")
    private String accountId; // Account ID (암호화됨)
    
    @JsonProperty("puuid")
    private String puuid; // PUUID
    
    @JsonProperty("name")
    private String name; // 소환사 이름 (deprecated)
    
    @JsonProperty("profileIconId")
    private int profileIconId; // 프로필 아이콘 ID
    
    @JsonProperty("revisionDate")
    private long revisionDate; // 마지막 수정 날짜
    
    @JsonProperty("summonerLevel")
    private long summonerLevel; // 소환사 레벨

    public SummonerDto() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProfileIconId() {
        return profileIconId;
    }

    public void setProfileIconId(int profileIconId) {
        this.profileIconId = profileIconId;
    }

    public long getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(long revisionDate) {
        this.revisionDate = revisionDate;
    }

    public long getSummonerLevel() {
        return summonerLevel;
    }

    public void setSummonerLevel(long summonerLevel) {
        this.summonerLevel = summonerLevel;
    }

    @Override
    public String toString() {
        return "SummonerDto{" +
                "id='" + id + '\'' +
                ", accountId='" + accountId + '\'' +
                ", puuid='" + puuid + '\'' +
                ", name='" + name + '\'' +
                ", profileIconId=" + profileIconId +
                ", revisionDate=" + revisionDate +
                ", summonerLevel=" + summonerLevel +
                '}';
    }
}