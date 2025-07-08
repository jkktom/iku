package org.mtvs.backend.singlematchanalysis.factory;

import org.mtvs.backend.analysis.entity.MatchAnalysis;
import org.mtvs.backend.common.constants.AnalysisStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SingleMatchAnalysisFactory {
    
    public MatchAnalysis createForSingleMatch(String puuid, String matchId) {
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setMatchId(matchId);
        analysis.setAnalysisStatus(AnalysisStatus.REQUESTED);
        
        return analysis;
    }
    
    public MatchAnalysis createInitialRecord(String puuid) {
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setAnalysisStatus(AnalysisStatus.REQUESTED);
        
        return analysis;
    }
    
    public MatchAnalysis createWithCustomStatus(String puuid, String matchId, String status) {
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setMatchId(matchId);
        analysis.setAnalysisStatus(status);
        
        return analysis;
    }
    
    public MatchAnalysis createCompletedAnalysis(String puuid, String matchId, String aiResponse, long durationSeconds) {
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setMatchId(matchId);
        analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
        analysis.setAnalysisSummary(aiResponse);
        
        return analysis;
    }
    
    public MatchAnalysis createFailedAnalysis(String puuid, String matchId, String errorMessage) {
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setMatchId(matchId);
        analysis.setAnalysisStatus(AnalysisStatus.FAILED);
        analysis.setErrorMessage(errorMessage);
        
        return analysis;
    }
}