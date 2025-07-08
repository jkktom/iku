package org.mtvs.backend.multianalysis.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class MultiMatchAnalysisRequest {
    
    @NotBlank(message = "Game name is required")
    private String gameName;
    
    @NotBlank(message = "Tag line is required")
    private String tagLine;
    
    @Min(value = 1, message = "Match count must be at least 1")
    @Max(value = 5, message = "Match count must be at most 5")
    private int matchCount;
    
    public MultiMatchAnalysisRequest() {}
    
    public MultiMatchAnalysisRequest(String gameName, String tagLine, int matchCount) {
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.matchCount = matchCount;
    }
    
    public String getGameName() {
        return gameName;
    }
    
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    
    public String getTagLine() {
        return tagLine;
    }
    
    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }
    
    public int getMatchCount() {
        return matchCount;
    }
    
    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }
}