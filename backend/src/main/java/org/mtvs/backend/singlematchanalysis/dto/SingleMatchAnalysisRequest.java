package org.mtvs.backend.singlematchanalysis.dto;

import jakarta.validation.constraints.NotBlank;

public class SingleMatchAnalysisRequest {
    
    @NotBlank(message = "PUUID is required")
    private String puuid;
    
    @NotBlank(message = "Match ID is required")
    private String matchId;
    
    public SingleMatchAnalysisRequest() {}
    
    public SingleMatchAnalysisRequest(String puuid, String matchId) {
        this.puuid = puuid;
        this.matchId = matchId;
    }
    
    public String getPuuid() {
        return puuid;
    }
    
    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }
    
    public String getMatchId() {
        return matchId;
    }
    
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
}