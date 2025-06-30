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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public RiotUser getRiotUser() {
        return riotUser;
    }

    public void setRiotUser(RiotUser riotUser) {
        this.riotUser = riotUser;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getRiotIdGameName() {
        return riotIdGameName;
    }

    public void setRiotIdGameName(String riotIdGameName) {
        this.riotIdGameName = riotIdGameName;
    }

    public String getRiotIdTagline() {
        return riotIdTagline;
    }

    public void setRiotIdTagline(String riotIdTagline) {
        this.riotIdTagline = riotIdTagline;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public String getChampionName() {
        return championName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
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

    public int getTotalDamageDealtToChampions() {
        return totalDamageDealtToChampions;
    }

    public void setTotalDamageDealtToChampions(int totalDamageDealtToChampions) {
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
    }

    public int getTotalDamageTaken() {
        return totalDamageTaken;
    }

    public void setTotalDamageTaken(int totalDamageTaken) {
        this.totalDamageTaken = totalDamageTaken;
    }

    public int getVisionScore() {
        return visionScore;
    }

    public void setVisionScore(int visionScore) {
        this.visionScore = visionScore;
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public void setGoldEarned(int goldEarned) {
        this.goldEarned = goldEarned;
    }

    public int getTotalMinionsKilled() {
        return totalMinionsKilled;
    }

    public void setTotalMinionsKilled(int totalMinionsKilled) {
        this.totalMinionsKilled = totalMinionsKilled;
    }

    public int getNeutralMinionsKilled() {
        return neutralMinionsKilled;
    }

    public void setNeutralMinionsKilled(int neutralMinionsKilled) {
        this.neutralMinionsKilled = neutralMinionsKilled;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }
}
