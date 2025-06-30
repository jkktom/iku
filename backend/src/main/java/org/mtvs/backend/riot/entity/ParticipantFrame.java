package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "participant_frames")
public class ParticipantFrame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // MatchTimeline을 즉시 로딩할 필요가 없다면 LAZY로 설정
    @JoinColumn(name = "timeline_id") // 외래 키 컬럼 이름
    private MatchTimeline timeline; //

    private int participantId; // 참여자 ID
    private int totalGold; // 총 골드
    private int level; // 레벨
    private int minionsKilled; // 미니언 킬 수
    private int jungleMinionsKilled; // 정글 몬스터 킬 수
    private int x; // X 좌표
    private int y; // Y 좌표

    // 기본 생성자는 JPA 엔티티에 필수적입니다.
    public ParticipantFrame() {
    }

    // 모든 필드를 포함하는 생성자 (선택 사항이지만 편리함)
    public ParticipantFrame(int participantId, int totalGold, int level, int minionsKilled, int jungleMinionsKilled, int x, int y) {
        this.participantId = participantId;
        this.totalGold = totalGold;
        this.level = level;
        this.minionsKilled = minionsKilled;
        this.jungleMinionsKilled = jungleMinionsKilled;
        this.x = x;
        this.y = y;
    }

    // Getter 메소드들
    public Long getId() {
        return id;
    }

    public MatchTimeline getTimeline() {
        return timeline;
    }

    public int getParticipantId() {
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
    public void setId(Long id) {
        this.id = id;
    }

    public void setTimeline(MatchTimeline timeline) {
        this.timeline = timeline;
    }

    public void setParticipantId(int participantId) {
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
