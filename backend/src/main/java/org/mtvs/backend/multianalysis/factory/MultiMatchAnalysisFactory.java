package org.mtvs.backend.multianalysis.factory;

import org.mtvs.backend.common.constants.AnalysisStatus;
import org.mtvs.backend.multianalysis.entity.MultiMatchAnalysis;
import org.springframework.stereotype.Component;

@Component
public class MultiMatchAnalysisFactory {
    
    public MultiMatchAnalysis createForMultipleMatches(String puuid, String gameName, String tagLine, int matchCount) {
        if (matchCount < 1 || matchCount > 5) {
            throw new IllegalArgumentException("Match count must be between 1 and 5");
        }
        
        MultiMatchAnalysis analysis = new MultiMatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setGameName(gameName);
        analysis.setTagLine(tagLine);
        analysis.setMatchCount(matchCount);
        analysis.setStatus(AnalysisStatus.REQUESTED);
        // createdAt is automatically set by Spring Data JPA auditing
        
        return analysis;
    }
    
    public MultiMatchAnalysis createWithMatchIds(String puuid, String gameName, String tagLine, 
                                                int matchCount, String matchIds) {
        MultiMatchAnalysis analysis = createForMultipleMatches(puuid, gameName, tagLine, matchCount);
        analysis.setMatchIds(matchIds);
        
        return analysis;
    }
    
    public MultiMatchAnalysis createCompletedAnalysis(String puuid, String gameName, String tagLine,
                                                     int matchCount, String matchIds, String aiResponse, 
                                                     long durationSeconds) {
        MultiMatchAnalysis analysis = new MultiMatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setGameName(gameName);
        analysis.setTagLine(tagLine);
        analysis.setMatchCount(matchCount);
        analysis.setMatchIds(matchIds);
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setAiResponse(aiResponse);
        analysis.setAnalysisDurationSeconds(durationSeconds);
        // createdAt is automatically set by Spring Data JPA auditing
        // CompletedAt will be handled by updatedAt in BaseEntity
        
        return analysis;
    }
    
    public MultiMatchAnalysis createFailedAnalysis(String puuid, String gameName, String tagLine,
                                                  int matchCount, String errorMessage) {
        MultiMatchAnalysis analysis = new MultiMatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setGameName(gameName);
        analysis.setTagLine(tagLine);
        analysis.setMatchCount(matchCount);
        analysis.setStatus(AnalysisStatus.FAILED);
        analysis.setErrorMessage(errorMessage);
        // createdAt is automatically set by Spring Data JPA auditing
        
        return analysis;
    }
    
    public MultiMatchAnalysis createWithCustomStatus(String puuid, String gameName, String tagLine,
                                                    int matchCount, String status) {
        MultiMatchAnalysis analysis = new MultiMatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setGameName(gameName);
        analysis.setTagLine(tagLine);
        analysis.setMatchCount(matchCount);
        analysis.setStatus(status);
        // createdAt is automatically set by Spring Data JPA auditing
        
        return analysis;
    }
}