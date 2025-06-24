package org.mtvs.backend.riot.dto;

import java.util.List;

public class EventDto {
    private String type;
    private long timestamp;
    private Integer participantId;
    private Integer killerId;
    private Integer victimId;
    private List<Integer> assistingParticipantIds;
    private String monsterType;
    private String buildingType;
    private String laneType;
    private String towerType;
    private Integer itemId;

    public EventDto() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    public Integer getKillerId() {
        return killerId;
    }

    public void setKillerId(Integer killerId) {
        this.killerId = killerId;
    }

    public Integer getVictimId() {
        return victimId;
    }

    public void setVictimId(Integer victimId) {
        this.victimId = victimId;
    }

    public List<Integer> getAssistingParticipantIds() {
        return assistingParticipantIds;
    }

    public void setAssistingParticipantIds(List<Integer> assistingParticipantIds) {
        this.assistingParticipantIds = assistingParticipantIds;
    }

    public String getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(String monsterType) {
        this.monsterType = monsterType;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
    }

    public String getLaneType() {
        return laneType;
    }

    public void setLaneType(String laneType) {
        this.laneType = laneType;
    }

    public String getTowerType() {
        return towerType;
    }

    public void setTowerType(String towerType) {
        this.towerType = towerType;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "EventDto{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", participantId=" + participantId +
                ", killerId=" + killerId +
                ", victimId=" + victimId +
                ", assistingParticipantIds=" + assistingParticipantIds +
                ", monsterType='" + monsterType + '\'' +
                ", buildingType='" + buildingType + '\'' +
                ", laneType='" + laneType + '\'' +
                ", towerType='" + towerType + '\'' +
                ", itemId=" + itemId +
                '}';
    }
}
