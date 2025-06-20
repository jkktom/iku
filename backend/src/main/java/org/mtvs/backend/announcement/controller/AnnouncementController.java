package org.mtvs.backend.announcement.controller;

import lombok.RequiredArgsConstructor;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.service.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.mtvs.backend.auth.security.CustomUserDetails;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*") // CORS 문제 방지용
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<Page<AnnouncementDto.Response>> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AnnouncementDto.Response> announcements = announcementService.getAllAnnouncements(pageable);
        return ResponseEntity.ok(announcements);
    }

    // 개발용: 인증 없이 공지사항 생성
    @PostMapping
    public ResponseEntity<AnnouncementDto.Response> createAnnouncementForDev(
            @RequestBody AnnouncementDto.Request request) {
        try {
            System.out.println("=== DEBUG: 공지사항 생성 요청 - 제목: " + request.getTitle() + " ===");
            AnnouncementDto.Response response = announcementService.createAnnouncementWithoutAuth(request);
            System.out.println("=== DEBUG: 공지사항 생성 완료 - ID: " + response.getId() + " ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== DEBUG: 공지사항 생성 중 오류: " + e.getMessage() + " ===");
            e.printStackTrace();
            throw e;
        }
    }

    // 운영용: 인증이 필요한 공지사항 생성
    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AnnouncementDto.Response> createAnnouncement(
            @RequestBody AnnouncementDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            announcementService.createAnnouncement(request, userDetails.getUser())
        );
    }

    // 개발용: 인증 없이 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncementForDev(@PathVariable Long id) {
        announcementService.deleteAnnouncementWithoutAuth(id);
        return ResponseEntity.noContent().build();
    }

    // 운영용: 인증이 필요한 공지사항 삭제
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        announcementService.deleteAnnouncement(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    // 공지사항 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementDto.Response> getAnnouncementById(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.findbyid(id));
    }

    // 공지사항 수정
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementDto.Response> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody AnnouncementDto.Request request) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, request));
    }

    // 디버그용: 데이터베이스 내 전체 공지사항 수 확인
    @GetMapping("/debug/count")
    public ResponseEntity<String> getAnnouncementCount() {
        try {
            long count = announcementService.getAnnouncementCount();
            return ResponseEntity.ok("데이터베이스 내 공지사항 수: " + count);
        } catch (Exception e) {
            return ResponseEntity.ok("오류: " + e.getMessage());
        }
    }
}