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
    private Match match; // Match 엔티티 클래스

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "riot_user_id")
    private RiotUser riotUser; // RiotUser 엔티티 클래스

    private int participantId; //참여자 ID (1~10)
    private String puuid; //유저 고유 ID
    private String riotIdGameName; //인게임 닉네임
    private String riotIdTagline; //라이엇태그
    private String summonerName; //소환사 이름
    private String championName; //(플레이한) 챔피언 이름
    private int kills; //킬 수
    private int deaths; //데스 수
    private int assists; //어시스트 수
    private int totalDamageDealtToChampions; //챔피언에게 준 총 대미지
    private int totalDamageTaken; // 받은 총 대미지
    private int visionScore; //시야 점수
    private int goldEarned; //획득한 골드
    private int totalMinionsKilled; //미니언 킬 수
    private int neutralMinionsKilled; //정글 몬스터 킬 수
    private int teamId; //팀 ID (100 = 블루팀, 200 = 레드팀)
    private boolean win; //승리 여부

    // 기본 생성자는 JPA 엔티티에 필수
    public Participant() {
    }

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