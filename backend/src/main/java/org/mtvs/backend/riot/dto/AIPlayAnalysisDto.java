package org.mtvs.backend.riot.dto;

import java.util.List;
import java.util.Map;

public class AIPlayAnalysisDto {
    private String matchId;
    private String targetPlayerName;
    private String targetChampion;
    private List<PlayEventDto> playEvents;
    private MatchSummaryDto matchSummary;

    public AIPlayAnalysisDto() {}

    public AIPlayAnalysisDto(String matchId, String targetPlayerName, String targetChampion, List<PlayEventDto> playEvents, MatchSummaryDto matchSummary) {
        this.matchId = matchId;
        this.targetPlayerName = targetPlayerName;
        this.targetChampion = targetChampion;
        this.playEvents = playEvents;
        this.matchSummary = matchSummary;
    }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getTargetPlayerName() { return targetPlayerName; }
    public void setTargetPlayerName(String targetPlayerName) { this.targetPlayerName = targetPlayerName; }

    public String getTargetChampion() { return targetChampion; }
    public void setTargetChampion(String targetChampion) { this.targetChampion = targetChampion; }

    public List<PlayEventDto> getPlayEvents() { return playEvents; }
    public void setPlayEvents(List<PlayEventDto> playEvents) { this.playEvents = playEvents; }

    public MatchSummaryDto getMatchSummary() { return matchSummary; }
    public void setMatchSummary(MatchSummaryDto matchSummary) { this.matchSummary = matchSummary; }
}

