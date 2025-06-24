package org.mtvs.backend.announcement.repository;

import org.mtvs.backend.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    // 기존 메서드
    List<Announcement> findAllByOrderByImportantDescCreatedAtDesc();
    
    // 제목으로 공지사항 찾기
    Optional<Announcement> findByTitle(String title);
    
    // 제목 중복 검증 (대소문자 구분 없이)
    @Query("SELECT COUNT(a) > 0 FROM Announcement a WHERE LOWER(TRIM(a.title)) = LOWER(TRIM(:title))")
    boolean existsByTitleIgnoreCase(@Param("title") String title);
    
    // 특정 ID를 제외하고 제목 중복 검증
    @Query("SELECT COUNT(a) > 0 FROM Announcement a WHERE LOWER(TRIM(a.title)) = LOWER(TRIM(:title)) AND a.id != :excludeId")
    boolean existsByTitleIgnoreCaseAndIdNot(@Param("title") String title, @Param("excludeId") Long excludeId);
    
    // 중요 공지사항만 조회
    List<Announcement> findByImportantTrueOrderByCreatedAtDesc();
    
    // 제목 키워드 검색 (LIKE 쿼리)
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.important DESC, a.createdAt DESC")
    List<Announcement> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);
    
    // 내용 키워드 검색 (LIKE 쿼리)
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.important DESC, a.createdAt DESC")
    List<Announcement> findByContentContainingIgnoreCase(@Param("keyword") String keyword);
    
    // 제목 또는 내용에서 키워드 검색
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.important DESC, a.createdAt DESC")
    List<Announcement> findByTitleOrContentContainingIgnoreCase(@Param("keyword") String keyword);
    
    // 작성자별 공지사항 조회
    @Query("SELECT a FROM Announcement a WHERE a.author.id = :authorId ORDER BY a.createdAt DESC")
    List<Announcement> findByAuthorId(@Param("authorId") Long authorId);
}