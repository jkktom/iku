package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Match를 즉시 로딩할 필요가 없다면 LAZY로 설정
    @JoinColumn(name = "match_id") // 외래 키 컬럼 이름
    private Match match; // Match 엔티티 클래스 임포트 필요 (동일 패키지 아니면 import org.mtvs.backend.Match; 같은 형식)

    private int participantId;
    private String puuid;
    private String riotIdGameName;
    private String riotIdTagline;
    private String summonerName;
    private String championName;
    private int kills;
    private int deaths;
    private int assists;
    private int totalDamageDealtToChampions;
    private int totalDamageTaken;
    private int visionScore;
    private int goldEarned;
    private int totalMinionsKilled;
    private int neutralMinionsKilled;
    private int teamId;
    private boolean win;

    // 기본 생성자는 JPA 엔티티에 필수적입니다.
    public Participant() {
    }

    // 모든 필드를 포함하는 생성자 (선택 사항이지만 편리함)
    // 실제 사용 시 Match 객체는 인자로 받지 않거나, 필요에 따라 Builder 패턴을 고려할 수 있습니다.
    public Participant(int participantId, String puuid, String riotIdGameName, String riotIdTagline, String summonerName, String championName, int kills, int deaths, int assists, int totalDamageDealtToChampions, int totalDamageTaken, int visionScore, int goldEarned, int totalMinionsKilled, int neutralMinionsKilled, int teamId, boolean win) {
        this.participantId = participantId;
        this.puuid = puuid;
        this.riotIdGameName = riotIdGameName;
        this.riotIdTagline = riotIdTagline;
        this.summonerName = summonerName;
        this.championName = championName;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
        this.totalDamageTaken = totalDamageTaken;
        this.visionScore = visionScore;
        this.goldEarned = goldEarned;
        this.totalMinionsKilled = totalMinionsKilled;
        this.neutralMinionsKilled = neutralMinionsKilled;
        this.teamId = teamId;
        this.win = win;
    }


    // Getter 메소드들
    public Long getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public int getParticipantId() {
        return participantId;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getRiotIdGameName() {
        return riotIdGameName;
    }

    public String getRiotIdTagline() {
        return riotIdTagline;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public String getChampionName() {
        return championName;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public int getTotalDamageDealtToChampions() {
        return totalDamageDealtToChampions;
    }

    public int getTotalDamageTaken() {
        return totalDamageTaken;
    }

    public int getVisionScore() {
        return visionScore;
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public int getTotalMinionsKilled() {
        return totalMinionsKilled;
    }

    public int getNeutralMinionsKilled() {
        return neutralMinionsKilled;
    }

    public int getTeamId() {
        return teamId;
    }

    public boolean isWin() { // boolean 타입은 `is` 접두사를 사용하기도 함 (IDE 자동 생성 기준)
        return win;
    }

    // Setter 메소드들
    public void setId(Long id) {
        this.id = id;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public void setRiotIdGameName(String riotIdGameName) {
        this.riotIdGameName = riotIdGameName;
    }

    public void setRiotIdTagline(String riotIdTagline) {
        this.riotIdTagline = riotIdTagline;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public void setTotalDamageDealtToChampions(int totalDamageDealtToChampions) {
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
    }

    public void setTotalDamageTaken(int totalDamageTaken) {
        this.totalDamageTaken = totalDamageTaken;
    }

    public void setVisionScore(int visionScore) {
        this.visionScore = visionScore;
    }

    public void setGoldEarned(int goldEarned) {
        this.goldEarned = goldEarned;
    }

    public void setTotalMinionsKilled(int totalMinionsKilled) {
        this.totalMinionsKilled = totalMinionsKilled;
    }

    public void setNeutralMinionsKilled(int neutralMinionsKilled) {
        this.neutralMinionsKilled = neutralMinionsKilled;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public void setWin(boolean win) {
        this.win = win;
    }
}