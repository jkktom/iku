package org.mtvs.backend.announcement.controller;

import lombok.RequiredArgsConstructor;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.service.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<Page<AnnouncementDto.Response>> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(announcementService.getAllAnnouncements(pageable));
    }

    @PostMapping
    public ResponseEntity<AnnouncementDto.Response> createAnnouncement(
            @RequestBody AnnouncementDto.Request request) {

        // 개발용: 임시 사용자 정보 (실제로는 인증된 사용자 정보를 사용)
        return ResponseEntity.ok(
                announcementService.createAnnouncementWithoutAuth(request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long id) {
        announcementService.deleteAnnouncementWithoutAuth(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementDto.Response> getAnnouncement(
            @PathVariable Long id
    ){
        AnnouncementDto.Response response = announcementService.findbyid(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementDto.Response> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody AnnouncementDto.Request announcementDto
    ){
        AnnouncementDto.Response response = announcementService.updateAnnouncement(id, announcementDto);
        return ResponseEntity.ok(response);
    }

}