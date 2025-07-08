package org.mtvs.backend.common.service;

public interface AiAnalysisService {
    
    String analyzeContent(String analysisPrompt);
    
    String analyzeMatchData(String matchDataPrompt);
    
    String analyzePlayerPerformance(String performancePrompt);
}