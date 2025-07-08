package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "match_events")
@IdClass(MatchEventId.class)
public class MatchEvent {
    
    @Id
    @Column(name = "match_id")
    private String matchId; // Part of composite key
    
    @Id
    @Column(name = "timestamp")
    private long timestamp; // Part of composite key - event timestamp
    
    @Id
    @Column(name = "sequence_id")
    private short sequenceId; // Part of composite key - event sequence within same timestamp

    // Relationship to MatchTimeline (not part of key but for navigation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "match_id", referencedColumnName = "match_id", insertable = false, updatable = false),
        @JoinColumn(name = "timestamp", referencedColumnName = "timestamp", insertable = false, updatable = false)
    })
    private MatchTimeline timeline;

    @Column(name = "event_type_id")
    private byte eventTypeId; // FK to EventType table
    
    // Relationship to EventType entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", insertable = false, updatable = false)
    private EventType eventType;
    
    private int participantId; // 관련 참여자 ID
    private int killerId; // 킬을 낸 유저 ID
    private int victimId; // 죽은 유저 ID

    @ElementCollection
    @CollectionTable(name = "event_assisting_participants",
            joinColumns = {
                @JoinColumn(name = "match_id", referencedColumnName = "match_id"),
                @JoinColumn(name = "timestamp", referencedColumnName = "timestamp"),
                @JoinColumn(name = "sequence_id", referencedColumnName = "sequence_id")
            })
    @Column(name = "participant_id")
    private List<Integer> assistingParticipantIds; //어시스트 기여자 ID들

    private int itemId; // 아이템 ID

    // 기본 생성자는 JPA 엔티티에 필수적입니다.
    public MatchEvent() {
    }

    // Constructor with composite key and essential fields
    public MatchEvent(String matchId, long timestamp, short sequenceId, byte eventTypeId, int participantId, int killerId, int victimId, 
                     List<Integer> assistingParticipantIds, int itemId) {
        this.matchId = matchId;
        this.timestamp = timestamp;
        this.sequenceId = sequenceId;
        this.eventTypeId = eventTypeId;
        this.participantId = participantId;
        this.killerId = killerId;
        this.victimId = victimId;
        this.assistingParticipantIds = assistingParticipantIds;
        this.itemId = itemId;
    }

    // Getter 메소드들
    public String getMatchId() {
        return matchId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public short getSequenceId() {
        return sequenceId;
    }

    public MatchTimeline getTimeline() {
        return timeline;
    }

    public byte getEventTypeId() {
        return eventTypeId;
    }

    public EventType getEventType() {
        return eventType;
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


    public int getItemId() {
        return itemId;
    }

    // Setter 메소드들
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSequenceId(short sequenceId) {
        this.sequenceId = sequenceId;
    }

    public void setTimeline(MatchTimeline timeline) {
        this.timeline = timeline;
    }

    public void setEventTypeId(byte eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
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


    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
