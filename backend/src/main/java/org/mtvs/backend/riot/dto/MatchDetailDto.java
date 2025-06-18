package org.mtvs.backend.riot.dto;

import java.util.List;

public class MatchDetailDto {
    private Info info;

    public MatchDetailDto() {
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "MatchDetailDto{" +
                "info=" + info +
                '}';
    }

    //매치 전체 정보
    public static class Info{
        private long gameDuration; //게임 총 시간
        private String gameMode; //게임 모드
        private String gameVersion; //게임 버전
        private int queueId; //큐 타입 (ex : 420 = 솔로랭크)
        private List<Participant> participants; //참가자별 상세 기록 리스트

        public Info() {
        }

        public long getGameDuration() {
            return gameDuration;
        }

        public void setGameDuration(long gameDuration) {
            this.gameDuration = gameDuration;
        }

        public String getGameMode() {
            return gameMode;
        }

        public void setGameMode(String gameMode) {
            this.gameMode = gameMode;
        }

        public String getGameVersion() {
            return gameVersion;
        }

        public void setGameVersion(String gameVersion) {
            this.gameVersion = gameVersion;
        }

        public int getQueueId() {
            return queueId;
        }

        public void setQueueId(int queueId) {
            this.queueId = queueId;
        }

        public List<Participant> getParticipants() {
            return participants;
        }

        public void setParticipants(List<Participant> participants) {
            this.participants = participants;
        }
    }

    //각 플레이별 정보
    public static class Participant{
        private int participandId; //참가자 번호(1~10)
        private String puuid; //고유 식별자
        private String riotIdGameName; //라이엇 계정 게임명
        private String riotIdTagline; //라이엇 태그
        private String summonerName; //인게임 소환사명 -> 라이엇 계정 게임명과 동일
        private String championName; //플리아한 챔피언 이름
        private int kills; //킬 수
        private int deaths; //데스 수
        private int assists; //어시스트 수
        private int totalDamageDealtToChampions; //챔피언에게 가한 총 피해량
        private int totalDamageTaken; //받은 총 피해량
        private int visionScore; //시야 점수
        private int goldEarned; //획득한 총 골드
        private int totalMinionsKilled; //라인 미니언 처치 수
        private int neutralMinionsKilled; //정글 몬슽터 처치 수

        public Participant() {
        }

        public int getParticipandId() {
            return participandId;
        }

        public void setParticipandId(int participandId) {
            this.participandId = participandId;
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

        @Override
        public String toString() {
            return "Participant{" +
                    "participandId=" + participandId +
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
                    '}';
        }
    }


}
