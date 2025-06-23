package org.mtvs.backend.announcement.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.service.AnnouncementService;
import org.mtvs.backend.global.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.mtvs.backend.auth.security.CustomUserDetails;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*") // CORS 문제 방지용
@Validated // 클래스 레벨 validation 활성화
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<Page<AnnouncementDto.Response>> getAllAnnouncements(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.") @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            log.info("공지사항 목록 조회 요청 - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
            
            // 정렬 방향 검증
            Sort.Direction direction = validateSortDirection(sortDir);
            
            // 정렬 필드 검증
            String validatedSortBy = validateSortField(sortBy);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, validatedSortBy));
            Page<AnnouncementDto.Response> announcements = announcementService.getAllAnnouncements(pageable);
            
            log.info("공지사항 목록 조회 완료 - 총 {}개, 현재 페이지: {}", announcements.getTotalElements(), page);
            return ResponseEntity.ok(announcements);
            
        } catch (Exception e) {
            log.error("공지사항 목록 조회 중 오류 발생", e);
            throw e;
        }
    }

    // 개발용: 인증 없이 공지사항 생성
    @PostMapping
    public ResponseEntity<AnnouncementDto.Response> createAnnouncementForDev(
            @Valid @RequestBody AnnouncementDto.Request request) {
        try {
            log.info("공지사항 생성 요청 (개발용) - 제목: {}", request.getTitle());
            
            AnnouncementDto.Response response = announcementService.createAnnouncementWithoutAuth(request);
            
            log.info("공지사항 생성 완료 (개발용) - ID: {}", response.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("공지사항 생성 중 오류 발생 (개발용)", e);
            throw e;
        }
    }

    // 운영용: 인증이 필요한 공지사항 생성
    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AnnouncementDto.Response> createAnnouncement(
            @Valid @RequestBody AnnouncementDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("공지사항 생성 요청 (운영용) - 제목: {}, 작성자: {}", request.getTitle(), userDetails.getUsername());
            
            AnnouncementDto.Response response = announcementService.createAnnouncement(request, userDetails.getUser());
            
            log.info("공지사항 생성 완료 (운영용) - ID: {}", response.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("공지사항 생성 중 오류 발생 (운영용)", e);
            throw e;
        }
    }

    // 개발용: 인증 없이 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncementForDev(
            @PathVariable @Min(value = 1, message = "공지사항 ID는 1 이상이어야 합니다.") Long id) {
        try {
            log.info("공지사항 삭제 요청 (개발용) - ID: {}", id);
            
            announcementService.deleteAnnouncementWithoutAuth(id);
            
            log.info("공지사항 삭제 완료 (개발용) - ID: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("공지사항 삭제 중 오류 발생 (개발용) - ID: {}", id, e);
            throw e;
        }
    }

    // 운영용: 인증이 필요한 공지사항 삭제
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("공지사항 삭제 요청 (운영용) - ID: {}, 요청자: {}", id, userDetails.getUsername());
            
            announcementService.deleteAnnouncement(id, userDetails.getUser());
            
            log.info("공지사항 삭제 완료 (운영용) - ID: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("공지사항 삭제 중 오류 발생 (운영용) - ID: {}", id, e);
            throw e;
        }
    }

    // 공지사항 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementDto.Response> getAnnouncementById(
            @PathVariable @Min(value = 1, message = "공지사항 ID는 1 이상이어야 합니다.") Long id) {
        try {
            log.info("공지사항 단일 조회 요청 - ID: {}", id);
            
            AnnouncementDto.Response response = announcementService.findbyid(id);
            
            log.info("공지사항 단일 조회 완료 - ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("공지사항 단일 조회 중 오류 발생 - ID: {}", id, e);
            throw e;
        }
    }

    // 공지사항 수정
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementDto.Response> updateAnnouncement(
            @PathVariable @Min(value = 1, message = "공지사항 ID는 1 이상이어야 합니다.") Long id,
            @Valid @RequestBody AnnouncementDto.Request request) {
        try {
            log.info("공지사항 수정 요청 - ID: {}, 제목: {}", id, request.getTitle());
            
            AnnouncementDto.Response response = announcementService.updateAnnouncement(id, request);
            
            log.info("공지사항 수정 완료 - ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("공지사항 수정 중 오류 발생 - ID: {}", id, e);
            throw e;
        }
    }

    // 검색 기능
    @GetMapping("/search")
    public ResponseEntity<List<AnnouncementDto.Response>> searchAnnouncements(
            @RequestParam String keyword) {
        try {
            log.info("공지사항 검색 요청 - 키워드: {}", keyword);
            
            List<AnnouncementDto.Response> results = announcementService.searchAnnouncements(keyword);
            
            log.info("공지사항 검색 완료 - 키워드: {}, 결과: {}개", keyword, results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("공지사항 검색 중 오류 발생 - 키워드: {}", keyword, e);
            throw e;
        }
    }

    // 중요 공지사항만 조회
    @GetMapping("/important")
    public ResponseEntity<List<AnnouncementDto.Response>> getImportantAnnouncements() {
        try {
            log.info("중요 공지사항 조회 요청");
            
            List<AnnouncementDto.Response> results = announcementService.getImportantAnnouncements();
            
            log.info("중요 공지사항 조회 완료 - 결과: {}개", results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("중요 공지사항 조회 중 오류 발생", e);
            throw e;
        }
    }

    // 디버그용: 데이터베이스 내 전체 공지사항 수 확인
    @GetMapping("/debug/count")
    public ResponseEntity<String> getAnnouncementCount() {
        try {
            long count = announcementService.getAnnouncementCount();
            log.info("공지사항 총 개수 조회 - count: {}", count);
            return ResponseEntity.ok("데이터베이스 내 공지사항 수: " + count);
        } catch (Exception e) {
            log.error("공지사항 개수 조회 중 오류 발생", e);
            return ResponseEntity.ok("오류: " + e.getMessage());
        }
    }

    // 정렬 방향 검증 메서드
    private Sort.Direction validateSortDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.DESC;
        }
        
        return switch (sortDir.toLowerCase()) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> {
                log.warn("잘못된 정렬 방향: {}. 기본값 DESC 사용", sortDir);
                yield Sort.Direction.DESC;
            }
        };
    }

    // 정렬 필드 검증 메서드
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }
        
        return switch (sortBy.toLowerCase()) {
            case "id", "title", "createdat", "updatedat", "important" -> sortBy;
            default -> {
                log.warn("잘못된 정렬 필드: {}. 기본값 id 사용", sortBy);
                yield "id";
            }
        };
    }
}