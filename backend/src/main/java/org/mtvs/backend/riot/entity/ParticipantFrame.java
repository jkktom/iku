package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "participant_frames")
@IdClass(ParticipantFrameId.class)
public class ParticipantFrame {
    
    @Id
    @Column(name = "match_id")
    private String matchId; // Part of composite key
    
    @Id
    @Column(name = "timestamp")
    private long timestamp; // Part of composite key - timeline timestamp
    
    @Id
    @Column(name = "participant_id")
    private byte participantId; // Part of composite key - participant ID (1-10, perfect for byte)

    // Relationship to MatchTimeline (not part of key but for navigation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "match_id", referencedColumnName = "match_id", insertable = false, updatable = false),
        @JoinColumn(name = "timestamp", referencedColumnName = "timestamp", insertable = false, updatable = false)
    })
    private MatchTimeline timeline;
    private int totalGold; // 총 골드
    private int level; // 레벨
    private int minionsKilled; // 미니언 킬 수
    private int jungleMinionsKilled; // 정글 몬스터 킬 수
    private int x; // X 좌표
    private int y; // Y 좌표

    // 기본 생성자는 JPA 엔티티에 필수적입니다.
    public ParticipantFrame() {
    }

    // Constructor with composite key fields
    public ParticipantFrame(String matchId, long timestamp, byte participantId, int totalGold, int level, int minionsKilled, int jungleMinionsKilled, int x, int y) {
        this.matchId = matchId;
        this.timestamp = timestamp;
        this.participantId = participantId;
        this.totalGold = totalGold;
        this.level = level;
        this.minionsKilled = minionsKilled;
        this.jungleMinionsKilled = jungleMinionsKilled;
        this.x = x;
        this.y = y;
    }

    // Getter 메소드들
    public String getMatchId() {
        return matchId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MatchTimeline getTimeline() {
        return timeline;
    }

    public byte getParticipantId() {
        return participantId;
    }

    public int getTotalGold() {
        return totalGold;
    }

    public int getLevel() {
        return level;
    }

    public int getMinionsKilled() {
        return minionsKilled;
    }

    public int getJungleMinionsKilled() {
        return jungleMinionsKilled;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Setter 메소드들
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimeline(MatchTimeline timeline) {
        this.timeline = timeline;
    }

    public void setParticipantId(byte participantId) {
        this.participantId = participantId;
    }

    public void setTotalGold(int totalGold) {
        this.totalGold = totalGold;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setMinionsKilled(int minionsKilled) {
        this.minionsKilled = minionsKilled;
    }

    public void setJungleMinionsKilled(int jungleMinionsKilled) {
        this.jungleMinionsKilled = jungleMinionsKilled;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
