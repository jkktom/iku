package org.mtvs.backend.analysis.entity;

public enum AnalysisStatus {
    REQUESTED,    // 분석 요청됨
    PROCESSING,   // 분석 중
    COMPLETED,    // 분석 완료
    FAILED        // 분석 실패
}
