package org.mtvs.backend.analysis.repository;

import org.mtvs.backend.analysis.entity.AnalysisStatus;
import org.mtvs.backend.analysis.entity.BaseAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * 모든 분석 리포지토리의 공통 메서드를 정의한 기본 인터페이스
 * @param <T> 분석 엔티티 타입 (BaseAnalysis를 상속받는 클래스)
 */
@NoRepositoryBean
public interface BaseAnalysisRepository<T extends BaseAnalysis> extends JpaRepository<T, Long> {
    
    // PUUID 기반 조회
    List<T> findByPuuidOrderByCreatedAtDesc(String puuid);
    
    // 상태 기반 조회
    List<T> findByAnalysisStatusOrderByCreatedAtDesc(AnalysisStatus status);
    
    Page<T> findByAnalysisStatusOrderByCreatedAtDesc(AnalysisStatus status, Pageable pageable);
    
    List<T> findByAnalysisStatusOrderByUpdatedAtDesc(AnalysisStatus status);
    
    List<T> findByAnalysisStatusInOrderByCreatedAtAsc(List<AnalysisStatus> statuses);
    
    // 상태별 카운트
    long countByAnalysisStatus(AnalysisStatus status);
    
    // PUUID와 상태 조합 조회
    List<T> findByPuuidAndAnalysisStatusOrderByCreatedAtDesc(String puuid, AnalysisStatus status);
    
    // 최근 분석 결과 조회
    List<T> findTop10ByAnalysisStatusOrderByCreatedAtDesc(AnalysisStatus status);
}