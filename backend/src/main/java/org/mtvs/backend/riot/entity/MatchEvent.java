package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "match_events")
public class MatchEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // MatchTimeline을 즉시 로딩할 필요가 없다면 LAZY로 설정
    @JoinColumn(name = "timeline_id") // 외래 키 컬럼 이름
    private MatchTimeline timeline; // MatchTimeline 엔티티 클래스 임포트 필요

    private String type; // 이벤트 타입 (CHAMPION_KILL, BUILDING_KILL, ELITE_MONSTER_KILL 등)
    private long timestamp; // 이벤트 발생 시간
    private int participantId; // 관련 참여자 ID
    private int killerId; // 킬을 낸 유저 ID
    private int victimId; // 죽은 유저 ID

    @ElementCollection
    @CollectionTable(name = "event_assisting_participants",
            joinColumns = @JoinColumn(name = "event_id")) // 연관된 이벤트의 ID를 참조하는 컬럼
    @Column(name = "participant_id") // 컬렉션에 저장될 값의 컬럼 이름
    private List<Integer> assistingParticipantIds; //어시스트 기여자 ID들

    private String monsterType; // 몬스터 타입 (드래곤, 바론 등)
    private String buildingType; // 건물 타입 (포탑, 억제기 등)
    private String laneType; // 라인 타입 (탑, 미드, 바텀 등)
    private String towerType; // 타워 타입 (OUTER, INNER, BASE 등)
    private int itemId; // 아이템 ID

    // 기본 생성자는 JPA 엔티티에 필수적입니다.
    public MatchEvent() {
    }

    // 모든 필드를 포함하는 생성자 (선택 사항이지만 편리함)
    public MatchEvent(String type, long timestamp, int participantId, int killerId, int victimId, List<Integer> assistingParticipantIds, String monsterType, String buildingType, String laneType, String towerType, int itemId) {
        this.type = type;
        this.timestamp = timestamp;
        this.participantId = participantId;
        this.killerId = killerId;
        this.victimId = victimId;
        this.assistingParticipantIds = assistingParticipantIds;
        this.monsterType = monsterType;
        this.buildingType = buildingType;
        this.laneType = laneType;
        this.towerType = towerType;
        this.itemId = itemId;
    }

    // Getter 메소드들
    public Long getId() {
        return id;
    }

    public MatchTimeline getTimeline() {
        return timeline;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getParticipantId() {
        return participantId;
    }

    public int getKillerId() {
        return killerId;
    }

    public int getVictimId() {
        return victimId;
    }

    public List<Integer> getAssistingParticipantIds() {
        return assistingParticipantIds;
    }

    public String getMonsterType() {
        return monsterType;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public String getLaneType() {
        return laneType;
    }

    public String getTowerType() {
        return towerType;
    }

    public int getItemId() {
        return itemId;
    }

    // Setter 메소드들
    public void setId(Long id) {
        this.id = id;
    }

    public void setTimeline(MatchTimeline timeline) {
        this.timeline = timeline;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public void setKillerId(int killerId) {
        this.killerId = killerId;
    }

    public void setVictimId(int victimId) {
        this.victimId = victimId;
    }

    public void setAssistingParticipantIds(List<Integer> assistingParticipantIds) {
        this.assistingParticipantIds = assistingParticipantIds;
    }

    public void setMonsterType(String monsterType) {
        this.monsterType = monsterType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
    }

    public void setLaneType(String laneType) {
        this.laneType = laneType;
    }

    public void setTowerType(String towerType) {
        this.towerType = towerType;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
