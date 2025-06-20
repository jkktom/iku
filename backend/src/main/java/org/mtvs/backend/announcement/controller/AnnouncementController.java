package org.mtvs.backend.announcement.controller;

import lombok.RequiredArgsConstructor;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.mtvs.backend.auth.security.CustomUserDetails;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementDto.Response>> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AnnouncementDto.Response> createAnnouncement(
            @RequestBody AnnouncementDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            announcementService.createAnnouncement(request, userDetails.getUser())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        announcementService.deleteAnnouncement(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
