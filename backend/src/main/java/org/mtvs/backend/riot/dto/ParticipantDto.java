package org.mtvs.backend.riot.dto;

//매치내에 각 플레이별 정보
public class ParticipantDto {
    private int participantId; //참가자 번호(1~10)
    private String puuid; //고유 식별자
    private String riotIdGameName; //라이엇 계정 게임명
    private String riotIdTagline; //라이엇 태그
    private String summonerName; //인게임 소환사명 -> 라이엇 계정 게임명과 동일
    private String championName; //플레이한 챔피언 이름
    private int kills; //킬 수
    private int deaths; //데스 수
    private int assists; //어시스트 수
    private int totalDamageDealtToChampions; //챔피언에게 가한 총 피해량
    private int totalDamageTaken; //받은 총 피해량
    private int visionScore; //시야 점수
    private int goldEarned; //획득한 총 골드
    private int totalMinionsKilled; //라인 미니언 처치 수
    private int neutralMinionsKilled; //정글 몬슽터 처치 수
    private boolean win; //승리 여부
    private int teamId; //팀 ID

    public ParticipantDto() {
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

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return "ParticipantDto{" +
                "participantId=" + participantId +
                ", puuid='" + puuid + '\'' +
                ", riotIdGameName='" + riotIdGameName + '\'' +
                ", riotIdTagline='" + riotIdTagline + '\'' +
                ", summonerName='" + summonerName + '\'' +
                ", championName='" + championName + '\'' +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", assists=" + assists +
                ", totalDamageDealtToChampions=" + totalDamageDealtToChampions +
                ", totalDamageTaken=" + totalDamageTaken +
                ", visionScore=" + visionScore +
                ", goldEarned=" + goldEarned +
                ", totalMinionsKilled=" + totalMinionsKilled +
                ", neutralMinionsKilled=" + neutralMinionsKilled +
                ", win=" + win +
                ", teamId=" + teamId +
                '}';
    }
}