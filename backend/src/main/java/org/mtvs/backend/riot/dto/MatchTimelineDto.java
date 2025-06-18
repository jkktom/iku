package org.mtvs.backend.riot.dto;

import java.util.List;
import java.util.Map;

public class MatchTimelineDto {
    private Info info;

    public MatchTimelineDto() {
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "MatchTimelineDto{" +
                "info=" + info +
                '}';
    }

    //여러개의 프레임 리스트
    public static class Info{
        private List<Frame> frames;

        public Info() {
        }

        public List<Frame> getFrames() {
            return frames;
        }

        public void setFrames(List<Frame> frames) {
            this.frames = frames;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "frames=" + frames +
                    '}';
        }
    }

    public static class Frame{
        private long timestamp; //프레임의 시간(밀리초)
        private Map<String, ParticipantFrame> participantFrames; //각 참가자(1~10번)의 상태정보
        private List<Event> events; //이 프레임에서 발생한 인게임 리스트

        public Frame() {
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, ParticipantFrame> getParticipantFrames() {
            return participantFrames;
        }

        public void setParticipantFrames(Map<String, ParticipantFrame> participantFrames) {
            this.participantFrames = participantFrames;
        }

        public List<Event> getEvents() {
            return events;
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }

        @Override
        public String toString() {
            return "Frame{" +
                    "timestamp=" + timestamp +
                    ", participantFrames=" + participantFrames +
                    ", events=" + events +
                    '}';
        }
    }

    public static class ParticipantFrame {
        private int totalGold; //현재까지 획득한 총 골드
        private int level; //현재 레벨
        private int minionsKilled; //라인 미니언 처치 수
        private int jungleMinionsKilled; //정글 몬스터 처치 수
        private int x; // 위치 정보 (선택적)
        private int y; // 위치 정보 (선택적)

        // 기본 생성자
        public ParticipantFrame() {
        }

        // Getter & Setter for ParticipantFrame fields
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

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        // toString() for ParticipantFrame (optional)
        @Override
        public String toString() {
            return "ParticipantFrame{" +
                    "totalGold=" + totalGold +
                    ", level=" + level +
                    ", minionsKilled=" + minionsKilled +
                    ", jungleMinionsKilled=" + jungleMinionsKilled +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    // 이벤트 정보
    // int 대신 Integer로 변경하여 null 처리 가능성 대비
    public static class Event {
        private String type; //이벤트의 종류 : ITEM_PURCHASED, CHAMPION_KILL
        private long timestamp; //이벤트 발생 시각(밀리초)
        private Integer participantId; //이벤트와 관련된 참가자 ID
        private Integer killerId; // 킬을 한 참가자 ID
        private Integer victimId; // 희생된 참가자 ID
        private List<Integer> assistingParticipantIds; //어시스트한 참가자 ID 리스트
        private String monsterType; //몬스터 종류(드래곤, 바론 등, 해당 이벤트에 따라 null)
        private String buildingType; //건물 종류(타워, 억제기 등, 해당 이벤트에 따라 null)
        private String laneType; //라인 종류(해당 이벤트에 따라 null)
        private String towerType; //타워 종류(해당 이벤트에 따라 null)
        private Integer itemId; //아이템 ID(해당 이벤트에 따라 null)

        // 기본 생성자
        public Event() {
        }

        // Getter & Setter for Event fields
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

        // toString() for Event (optional)
        @Override
        public String toString() {
            return "Event{" +
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


}
