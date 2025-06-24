package org.mtvs.backend.riot.dto;

public class ParticipantFrameDto {
    private int totalGold;
    private int level;
    private int minionsKilled;
    private int jungleMinionsKilled;
    private PositionDto position;

    public ParticipantFrameDto() {
    }

    public int getTotalGold() {
        return totalGold;
    }

    public void setTotalGold(int totalGold) {
        this.totalGold = totalGold;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMinionsKilled() {
        return minionsKilled;
    }

    public void setMinionsKilled(int minionsKilled) {
        this.minionsKilled = minionsKilled;
    }

    public int getJungleMinionsKilled() {
        return jungleMinionsKilled;
    }

    public void setJungleMinionsKilled(int jungleMinionsKilled) {
        this.jungleMinionsKilled = jungleMinionsKilled;
    }

    public PositionDto getPosition() {
        return position;
    }

    public void setPosition(PositionDto position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "ParticipantFrameDto{" +
                "totalGold=" + totalGold +
                ", level=" + level +
                ", minionsKilled=" + minionsKilled +
                ", jungleMinionsKilled=" + jungleMinionsKilled +
                ", position=" + position +
                '}';
    }
}
