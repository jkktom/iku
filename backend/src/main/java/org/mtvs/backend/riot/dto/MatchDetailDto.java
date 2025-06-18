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

    public static class Info{
        private long gameDuration;
        private String gameMode;
        private String gameVersion;
        private int queueId;
        private List<Participant> participants;

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

    public static class Participant{
        private int participandId;
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
