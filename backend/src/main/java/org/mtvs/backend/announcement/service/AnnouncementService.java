package org.mtvs.backend.announcement.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.entity.Announcement;
import org.mtvs.backend.announcement.repository.AnnouncementRepository;
import org.mtvs.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.mtvs.backend.user.entity.User;
import org.mtvs.backend.global.exception.UnauthorizedException;
import org.mtvs.backend.global.exception.NotFoundException;


@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AnnouncementDto.Response> getAllAnnouncements(Pageable pageable) {
        return announcementRepository.findAllByOrderByImportantDescCreatedAtDesc(pageable)
                .map(AnnouncementDto.Response::new);
    }

    // 기존 메서드도 유지 (하위 호환성을 위해)
    @Transactional(readOnly = true)
    public List<AnnouncementDto.Response> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByImportantDescCreatedAtDesc()
                .stream()
                .map(AnnouncementDto.Response::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementDto.Response createAnnouncement(AnnouncementDto.Request request, User author) {
        if (!author.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("Only admins can create announcements");
        }

        Announcement announcement = new Announcement(
                request.getTitle(),
                request.getContent(),
                author,
                request.isImportant()
        );

        return new AnnouncementDto.Response(announcementRepository.save(announcement));
    }

    // 개발용: 인증 없이 공지사항 생성
    @Transactional
    public AnnouncementDto.Response createAnnouncementWithoutAuth(AnnouncementDto.Request request) {
        // 시스템 사용자를 찾거나 생성
        User systemUser = userRepository.findByUsername("system")
                .orElseGet(() -> {
                    User newSystemUser = new User(
                            "system", 
                            "system@iku.life", 
                            "password", 
                            User.Role.ADMIN
                    );
                    return userRepository.save(newSystemUser);
                });

        Announcement announcement = new Announcement(
                request.getTitle(),
                request.getContent(),
                systemUser,
                request.isImportant()
        );

        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return new AnnouncementDto.Response(savedAnnouncement);
    }

    @Transactional
    public void deleteAnnouncement(Long id, User user) {
        if (!user.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("Only admins can delete announcements");
        }

        Optional<Announcement> announcement = announcementRepository.findById(id);
        if (announcement.isEmpty()) {
            throw new NotFoundException("Announcement not found");
        }

        announcementRepository.delete(announcement.get());
    }

    // 개발용: 인증 없이 공지사항 삭제
    @Transactional
    public void deleteAnnouncementWithoutAuth(Long id) {
        Optional<Announcement> announcement = announcementRepository.findById(id);
        if (announcement.isEmpty()) {
            throw new NotFoundException("Announcement not found");
        }

        announcementRepository.delete(announcement.get());
    }

    public AnnouncementDto.Response findbyid(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));

        return AnnouncementDto.fromEntity(announcement);
    }

    @Transactional
    public AnnouncementDto.Response updateAnnouncement(Long id, AnnouncementDto.Request dto) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));

        // updateInfo 메서드 사용으로 더 명확한 업데이트
        announcement.updateInfo(dto.getTitle(), dto.getContent(), dto.isImportant());

        // JPA에서 자동으로 저장됨 (@Transactional에 의해)
        return AnnouncementDto.fromEntity(announcement);
    }
}
