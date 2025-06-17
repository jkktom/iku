package org.mtvs.backend.riot.entity;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "match_timelines")
public class MatchTimeline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private DataFormatReaders.Match match;

    private long timestamp;

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL)
    private List<ParticipantFrame> participantFrames;

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL)
    private List<MatchEvent> events;
}