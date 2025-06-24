package org.mtvs.backend.announcement.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.entity.Announcement;
import org.mtvs.backend.announcement.repository.AnnouncementRepository;
import org.mtvs.backend.global.exception.BadRequestException;
import org.mtvs.backend.global.exception.NotFoundException;
import org.mtvs.backend.global.exception.UnauthorizedException;
import org.mtvs.backend.user.entity.Role;
import org.mtvs.backend.user.entity.SignupCategory;
import org.mtvs.backend.user.entity.User;
import org.mtvs.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    // 페이징을 이용한 공지사항 조회
    @Transactional(readOnly = true)
    public Page<AnnouncementDto.Response> getAllAnnouncements(Pageable pageable) {
        try {
            log.info("공지사항 목록 조회 시작 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
            
            Page<Announcement> announcements = announcementRepository.findAll(pageable);
            
            log.info("공지사항 목록 조회 완료 - 총 {}개 중 {}개 조회", 
                    announcements.getTotalElements(), announcements.getNumberOfElements());
            
            return announcements.map(AnnouncementDto.Response::new);
            
        } catch (Exception e) {
            log.error("공지사항 목록 조회 중 오류 발생", e);
            throw new BadRequestException("공지사항 목록을 조회하는 중 오류가 발생했습니다.");
        }
    }

    // 운영용: 인증 필요한 공지사항 생성
    @Transactional
    public AnnouncementDto.Response createAnnouncement(AnnouncementDto.Request request, User author) {
        try {
            log.info("공지사항 생성 시작 (운영용) - 제목: {}, 작성자: {}", request.getTitle(), author.getUsername());
            
            // 권한 검증
            validateAdminPermission(author);
            
            // 입력값 추가 검증
            validateAnnouncementRequest(request);
            
            // 중복 제목 검증
            validateDuplicateTitle(request.getTitle(), null);
            
            Announcement announcement = new Announcement(
                    request.getTitle().trim(),
                    request.getContent().trim(),
                    author,
                    request.isImportant()
            );

            Announcement savedAnnouncement = announcementRepository.save(announcement);
            
            log.info("공지사항 생성 완료 (운영용) - ID: {}", savedAnnouncement.getId());
            return new AnnouncementDto.Response(savedAnnouncement);
            
        } catch (Exception e) {
            log.error("공지사항 생성 중 오류 발생 (운영용)", e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new BadRequestException("공지사항 생성 중 오류가 발생했습니다.");
        }
    }

    // 개발용: 인증 없이 공지사항 생성
    @Transactional
    public AnnouncementDto.Response createAnnouncementWithoutAuth(AnnouncementDto.Request request) {
        try {
            log.info("공지사항 생성 시작 (개발용) - 제목: {}", request.getTitle());
            
            // 입력값 추가 검증
            validateAnnouncementRequest(request);
            
            // 중복 제목 검증
            validateDuplicateTitle(request.getTitle(), null);
            
            // 시스템 사용자를 찾거나 생성
            User systemUser = getOrCreateSystemUser();
            
            Announcement announcement = new Announcement(
                    request.getTitle().trim(),
                    request.getContent().trim(),
                    systemUser,
                    request.isImportant()
            );

            Announcement savedAnnouncement = announcementRepository.save(announcement);
            
            log.info("공지사항 생성 완료 (개발용) - ID: {}", savedAnnouncement.getId());
            return new AnnouncementDto.Response(savedAnnouncement);
            
        } catch (Exception e) {
            log.error("공지사항 생성 중 오류 발생 (개발용)", e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new BadRequestException("공지사항 생성 중 오류가 발생했습니다.");
        }
    }

    // 운영용: 인증 필요한 공지사항 삭제
    @Transactional
    public void deleteAnnouncement(String id, User user) {
        try {
            log.info("공지사항 삭제 시작 (운영용) - ID: {}, 요청자: {}", id, user.getUsername());
            
            // 권한 검증
            validateAdminPermission(user);
            
            // ID 검증
            Long announcementId = validateAndParseId(id);
            
            // 공지사항 존재 검증
            Announcement announcement = findAnnouncementById(announcementId);
            
            announcementRepository.delete(announcement);
            
            log.info("공지사항 삭제 완료 (운영용) - ID: {}", id);
            
        } catch (Exception e) {
            log.error("공지사항 삭제 중 오류 발생 (운영용) - ID: {}", id, e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new BadRequestException("공지사항 삭제 중 오류가 발생했습니다.");
        }
    }

    // 개발용: 인증 없이 공지사항 삭제
    @Transactional
    public void deleteAnnouncementWithoutAuth(Long id) {
        try {
            log.info("공지사항 삭제 시작 (개발용) - ID: {}", id);
            
            // ID 검증
            if (id == null || id <= 0) {
                throw new BadRequestException("유효하지 않은 공지사항 ID입니다.");
            }
            
            // 공지사항 존재 검증
            Announcement announcement = findAnnouncementById(id);
            
            announcementRepository.delete(announcement);
            
            log.info("공지사항 삭제 완료 (개발용) - ID: {}", id);
            
        } catch (Exception e) {
            log.error("공지사항 삭제 중 오류 발생 (개발용) - ID: {}", id, e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new BadRequestException("공지사항 삭제 중 오류가 발생했습니다.");
        }
    }

    // 공지사항 단일 조회
    @Transactional(readOnly = true)
    public AnnouncementDto.Response findbyid(Long id) {
        try {
            log.info("공지사항 단일 조회 시작 - ID: {}", id);
            
            // ID 검증
            if (id == null || id <= 0) {
                throw new BadRequestException("유효하지 않은 공지사항 ID입니다.");
            }
            
            Announcement announcement = findAnnouncementById(id);
            
            log.info("공지사항 단일 조회 완료 - ID: {}", id);
            return AnnouncementDto.fromEntity(announcement);
            
        } catch (Exception e) {
            log.error("공지사항 단일 조회 중 오류 발생 - ID: {}", id, e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new BadRequestException("공지사항 조회 중 오류가 발생했습니다.");
        }
    }

    // 공지사항 수정
    @Transactional
    public AnnouncementDto.Response updateAnnouncement(Long id, AnnouncementDto.Request dto) {
        try {
            log.info("공지사항 수정 시작 - ID: {}, 제목: {}", id, dto.getTitle());
            
            // ID 검증
            if (id == null || id <= 0) {
                throw new BadRequestException("유효하지 않은 공지사항 ID입니다.");
            }
            
            // 입력값 검증
            validateAnnouncementRequest(dto);
            
            // 공지사항 존재 검증
            Announcement announcement = findAnnouncementById(id);
            
            // 중복 제목 검증 (자기 자신 제외)
            validateDuplicateTitle(dto.getTitle(), id);
            
            // 수정 실행
            announcement.updateInfo(dto.getTitle().trim(), dto.getContent().trim(), dto.isImportant());
            
            log.info("공지사항 수정 완료 - ID: {}", id);
            return AnnouncementDto.fromEntity(announcement);
            
        } catch (Exception e) {
            log.error("공지사항 수정 중 오류 발생 - ID: {}", id, e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new BadRequestException("공지사항 수정 중 오류가 발생했습니다.");
        }
    }

    // 디버그용: 전체 공지사항 수 조회
    @Transactional(readOnly = true)
    public long getAnnouncementCount() {
        try {
            long count = announcementRepository.count();
            log.info("전체 공지사항 수 조회 - count: {}", count);
            return count;
        } catch (Exception e) {
            log.error("공지사항 수 조회 중 오류 발생", e);
            throw new BadRequestException("공지사항 개수 조회 중 오류가 발생했습니다.");
        }
    }

    // ========== 검증 메서드들 ==========

    private void validateAdminPermission(User user) {
        if (user == null) {
            throw new UnauthorizedException("사용자 정보가 없습니다.");
        }
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }
    }

    private void validateAnnouncementRequest(AnnouncementDto.Request request) {
        if (request == null) {
            throw new BadRequestException("공지사항 정보가 없습니다.");
        }
        
        // 제목 추가 검증
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BadRequestException("제목은 필수 입력 항목입니다.");
        }
        
        String trimmedTitle = request.getTitle().trim();
        if (trimmedTitle.length() < 2 || trimmedTitle.length() > 100) {
            throw new BadRequestException("제목은 2자 이상 100자 이하로 입력해주세요.");
        }
        
        // 내용 추가 검증
        if (!StringUtils.hasText(request.getContent())) {
            throw new BadRequestException("내용은 필수 입력 항목입니다.");
        }
        
        String trimmedContent = request.getContent().trim();
        if (trimmedContent.length() < 10 || trimmedContent.length() > 5000) {
            throw new BadRequestException("내용은 10자 이상 5000자 이하로 입력해주세요.");
        }
        
        // HTML 태그 검증 (기본적인 XSS 방지)
        if (containsHtmlTags(trimmedTitle) || containsHtmlTags(trimmedContent)) {
            throw new BadRequestException("제목과 내용에는 HTML 태그를 사용할 수 없습니다.");
        }
    }

    private void validateDuplicateTitle(String title, Long excludeId) {
        if (!StringUtils.hasText(title)) return;
        
        String trimmedTitle = title.trim();
        boolean exists;
        
        if (excludeId != null) {
            // 특정 ID를 제외하고 중복 검증
            exists = announcementRepository.existsByTitleIgnoreCaseAndIdNot(trimmedTitle, excludeId);
        } else {
            // 전체에서 중복 검증
            exists = announcementRepository.existsByTitleIgnoreCase(trimmedTitle);
        }
        
        if (exists) {
            throw new BadRequestException("이미 존재하는 제목입니다. 다른 제목을 사용해주세요.");
        }
    }

    private Long validateAndParseId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BadRequestException("공지사항 ID가 없습니다.");
        }
        
        try {
            Long parsedId = Long.parseLong(id.trim());
            if (parsedId <= 0) {
                throw new BadRequestException("유효하지 않은 공지사항 ID입니다.");
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new BadRequestException("공지사항 ID는 숫자여야 합니다.");
        }
    }

    private Announcement findAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ID " + id + "에 해당하는 공지사항을 찾을 수 없습니다."));
    }

    private User getOrCreateSystemUser() {
        return userRepository.findByUsername("system")
                .orElseGet(() -> {
                    log.info("시스템 사용자 생성 중");
                    User newSystemUser = User.builder()
                            .email("system@iku.life")
                            .username("system")
                            .role(Role.ADMIN)
                            .signupCategory(SignupCategory.Local)
                            .linkingUserId(null)
                            .password("password")
                            .build();
                    return userRepository.save(newSystemUser);
                });
    }

    // 검색 기능 추가
    @Transactional(readOnly = true)
    public List<AnnouncementDto.Response> searchAnnouncements(String keyword) {
        try {
            log.info("공지사항 검색 시작 - 키워드: {}", keyword);
            
            if (!StringUtils.hasText(keyword)) {
                throw new BadRequestException("검색 키워드를 입력해주세요.");
            }
            
            String trimmedKeyword = keyword.trim();
            if (trimmedKeyword.length() < 2) {
                throw new BadRequestException("검색 키워드는 2자 이상 입력해주세요.");
            }
            
            List<Announcement> announcements = announcementRepository.findByTitleOrContentContainingIgnoreCase(trimmedKeyword);
            
            log.info("공지사항 검색 완료 - 키워드: {}, 결과: {}개", keyword, announcements.size());
            
            return announcements.stream()
                    .map(AnnouncementDto.Response::new)
                    .toList();
                    
        } catch (Exception e) {
            log.error("공지사항 검색 중 오류 발생 - 키워드: {}", keyword, e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new BadRequestException("공지사항 검색 중 오류가 발생했습니다.");
        }
    }

    // 중요 공지사항만 조회
    @Transactional(readOnly = true)
    public List<AnnouncementDto.Response> getImportantAnnouncements() {
        try {
            log.info("중요 공지사항 조회 시작");
            
            List<Announcement> announcements = announcementRepository.findByImportantTrueOrderByCreatedAtDesc();
            
            log.info("중요 공지사항 조회 완료 - 결과: {}개", announcements.size());
            
            return announcements.stream()
                    .map(AnnouncementDto.Response::new)
                    .toList();
                    
        } catch (Exception e) {
            log.error("중요 공지사항 조회 중 오류 발생", e);
            throw new BadRequestException("중요 공지사항 조회 중 오류가 발생했습니다.");
        }
    }

    private boolean containsHtmlTags(String text) {
        if (!StringUtils.hasText(text)) return false;
        return text.matches(".*<[^>]+>.*");
    }
}