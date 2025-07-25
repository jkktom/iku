package org.mtvs.backend.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

//매치내에 각 플레이별 정보
public class ParticipantDto {
    private int participantId; //참가자 번호(1~10)
    private String puuid; //고유 식별자
    private String riotIdGameName; //라이엇 계정 게임명
    private String riotIdTagline; //라이엇 태그
    private String summonerName; //인게임 소환사명 -> 라이엇 계정 게임명과 동일
    private String championName; //플레이한 챔피언 이름
    private String teamPosition; //팀 내 포지션 (TOP, JUNGLE, MIDDLE, BOTTOM, UTILITY)
    private String individualPosition; //개별 포지션 (lane 정보)
    private int kills; //킬 수
    private int deaths; //데스 수
    private int assists; //어시스트 수
    private int totalDamageDealtToChampions; //챔피언에게 가한 총 피해량
    @JsonProperty("magicDamageDealtToChampions")
    private int magicDamageDealtToChampions; //챔피언에게 가한 마법 피해량
    @JsonProperty("physicalDamageDealtToChampions")
    private int physicalDamageDealtToChampions; //챔피언에게 가한 물리 피해량
    @JsonProperty("trueDamageDealtToChampions")
    private int trueDamageDealtToChampions; //챔피언에게 가한 고정 피해량
    private int totalDamageTaken; //받은 총 피해량
    private int visionScore; //시야 점수
    @JsonProperty("wardsPlaced")
    private int wardsPlaced; //설치한 와드 수
    @JsonProperty("wardsKilled")
    private int wardsKilled; //제거한 와드 수
    @JsonProperty("detectorWardsPlaced")
    private int controlWardsPlaced; //설치한 제어 와드 수
    // 핑 데이터
    @JsonProperty("allInPings")
    private int allInPings; //전체 돌진 핑
    @JsonProperty("assistMePings")
    private int assistMePings; //도움 요청 핑
    @JsonProperty("baitPings")
    private int baitPings; //미끼 핑
    @JsonProperty("commandPings")
    private int commandPings; //명령 핑
    @JsonProperty("dangerPings")
    private int dangerPings; //위험 핑
    @JsonProperty("enemyMissingPings")
    private int enemyMissingPings; //적 실종 핑
    @JsonProperty("enemyVisionPings")
    private int enemyVisionPings; //적 시야 핑
    @JsonProperty("getBackPings")
    private int getBackPings; //후퇴 핑
    @JsonProperty("holdPings")
    private int holdPings; //대기 핑
    @JsonProperty("needVisionPings")
    private int needVisionPings; //시야 필요 핑
    @JsonProperty("onMyWayPings")
    private int onMyWayPings; //이동 중 핑
    @JsonProperty("pushPings")
    private int pushPings; //푸시 핑
    @JsonProperty("visionClearedPings")
    private int visionClearedPings; //시야 제거 핑
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

    // 고급 분석 필드들의 getter/setter
    public int getMagicDamageDealtToChampions() {
        return magicDamageDealtToChampions;
    }

    public void setMagicDamageDealtToChampions(int magicDamageDealtToChampions) {
        this.magicDamageDealtToChampions = magicDamageDealtToChampions;
    }

    public int getPhysicalDamageDealtToChampions() {
        return physicalDamageDealtToChampions;
    }

    public void setPhysicalDamageDealtToChampions(int physicalDamageDealtToChampions) {
        this.physicalDamageDealtToChampions = physicalDamageDealtToChampions;
    }

    public int getTrueDamageDealtToChampions() {
        return trueDamageDealtToChampions;
    }

    public void setTrueDamageDealtToChampions(int trueDamageDealtToChampions) {
        this.trueDamageDealtToChampions = trueDamageDealtToChampions;
    }

    public int getWardsPlaced() {
        return wardsPlaced;
    }

    public void setWardsPlaced(int wardsPlaced) {
        this.wardsPlaced = wardsPlaced;
    }

    public int getWardsKilled() {
        return wardsKilled;
    }

    public void setWardsKilled(int wardsKilled) {
        this.wardsKilled = wardsKilled;
    }

    public int getControlWardsPlaced() {
        return controlWardsPlaced;
    }

    public void setControlWardsPlaced(int controlWardsPlaced) {
        this.controlWardsPlaced = controlWardsPlaced;
    }

    // 핑 데이터 getter/setter
    public int getAllInPings() {
        return allInPings;
    }

    public void setAllInPings(int allInPings) {
        this.allInPings = allInPings;
    }

    public int getAssistMePings() {
        return assistMePings;
    }

    public void setAssistMePings(int assistMePings) {
        this.assistMePings = assistMePings;
    }

    public int getBaitPings() {
        return baitPings;
    }

    public void setBaitPings(int baitPings) {
        this.baitPings = baitPings;
    }

    public int getCommandPings() {
        return commandPings;
    }

    public void setCommandPings(int commandPings) {
        this.commandPings = commandPings;
    }

    public int getDangerPings() {
        return dangerPings;
    }

    public void setDangerPings(int dangerPings) {
        this.dangerPings = dangerPings;
    }

    public int getEnemyMissingPings() {
        return enemyMissingPings;
    }

    public void setEnemyMissingPings(int enemyMissingPings) {
        this.enemyMissingPings = enemyMissingPings;
    }

    public int getEnemyVisionPings() {
        return enemyVisionPings;
    }

    public void setEnemyVisionPings(int enemyVisionPings) {
        this.enemyVisionPings = enemyVisionPings;
    }

    public int getGetBackPings() {
        return getBackPings;
    }

    public void setGetBackPings(int getBackPings) {
        this.getBackPings = getBackPings;
    }

    public int getHoldPings() {
        return holdPings;
    }

    public void setHoldPings(int holdPings) {
        this.holdPings = holdPings;
    }

    public int getNeedVisionPings() {
        return needVisionPings;
    }

    public void setNeedVisionPings(int needVisionPings) {
        this.needVisionPings = needVisionPings;
    }

    public int getOnMyWayPings() {
        return onMyWayPings;
    }

    public void setOnMyWayPings(int onMyWayPings) {
        this.onMyWayPings = onMyWayPings;
    }

    public int getPushPings() {
        return pushPings;
    }

    public void setPushPings(int pushPings) {
        this.pushPings = pushPings;
    }

    public int getVisionClearedPings() {
        return visionClearedPings;
    }

    public void setVisionClearedPings(int visionClearedPings) {
        this.visionClearedPings = visionClearedPings;
    }

    public String getTeamPosition() {
        return teamPosition;
    }

    public void setTeamPosition(String teamPosition) {
        this.teamPosition = teamPosition;
    }

    public String getIndividualPosition() {
        return individualPosition;
    }

    public void setIndividualPosition(String individualPosition) {
        this.individualPosition = individualPosition;
    }
}