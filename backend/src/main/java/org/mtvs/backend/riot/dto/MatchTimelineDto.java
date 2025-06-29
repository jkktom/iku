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
        private long timestamp;
        private Map<String, ParticipantFrame> participantFrames;
        private List<Event> events;

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
        private int totalGold;
        private int level;
        private int minionsKilled;
        private int jungleMinionsKilled;
        // ============= 수정 부분 START =============
        // 기존: private int x; private int y; (직접 필드)
        // 변경: position 객체로 중첩 구조 처리
        private Position position; // Riot API의 실제 JSON 구조에 맞게 수정
        // ============= 수정 부분 END =============

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

        // ============= 수정 부분 START =============
        // 기존: getX(), setX(), getY(), setY() 직접 필드 접근
        // 변경: position 객체를 통한 x, y 접근
        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

        // 편의를 위한 x, y 접근 메소드 추가 (기존 코드 호환성)
        public int getX() {
            return position != null ? position.getX() : 0;
        }

        public int getY() {
            return position != null ? position.getY() : 0;
        }
        // ============= 수정 부분 END =============

        // toString() for ParticipantFrame (optional)
        @Override
        public String toString() {
            return "ParticipantFrame{" +
                    "totalGold=" + totalGold +
                    ", level=" + level +
                    ", minionsKilled=" + minionsKilled +
                    ", jungleMinionsKilled=" + jungleMinionsKilled +
                    // ============= 수정 부분 START =============
                    // 기존: ", x=" + x + ", y=" + y +
                    // 변경: position 객체 출력
                    ", position=" + position +
                    // ============= 수정 부분 END =============
                    '}';
        }
    }

    // ============= 추가된 부분 START =============
    // Riot API의 position JSON 구조를 매핑하기 위한 새로운 클래스
    public static class Position {
        private int x;
        private int y;

        public Position() {
        }

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
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

        @Override
        public String toString() {
            return "Position{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    // ============= 추가된 부분 END =============

    public static class Event {
        private String type;
        private long timestamp;
        private Integer participantId; // int 대신 Integer로 변경하여 null 처리 가능성 대비
        private Integer killerId;      // int 대신 Integer로 변경하여 null 처리 가능성 대비
        private Integer victimId;      // int 대신 Integer로 변경하여 null 처리 가능성 대비
        private List<Integer> assistingParticipantIds;
        private String monsterType;
        private String buildingType;
        private String laneType;
        private String towerType;
        private Integer itemId; // int 대신 Integer로 변경하여 null 처리 가능성 대비

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
