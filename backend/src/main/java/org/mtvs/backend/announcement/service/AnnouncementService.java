package org.mtvs.backend.announcement.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.entity.Announcement;
import org.mtvs.backend.announcement.repository.AnnouncementRepository;
import org.mtvs.backend.user.entity.SignupCategory;
import org.mtvs.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.mtvs.backend.user.entity.User;
import org.mtvs.backend.global.exception.UnauthorizedException;
import org.mtvs.backend.global.exception.NotFoundException;
import org.mtvs.backend.user.entity.Role;


@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;


    //페이징을 이용한 공지사항
    @Transactional(readOnly = true)
    public Page<AnnouncementDto.Response> getAllAnnouncements(Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findAll(pageable);
        return announcements.map(AnnouncementDto.Response::new);
    }


//    @Transactional(readOnly = true)
//    public List<AnnouncementDto.Response> getAllAnnouncements() {
//        try {
//            System.out.println("=== DEBUG: getAllAnnouncements 시작 ===");
//            List<Announcement> announcements = announcementRepository.findAllByOrderByImportantDescCreatedAtDesc();
//            System.out.println("=== DEBUG: 데이터베이스에서 조회된 공지사항 수: " + announcements.size() + " ===");
//
//            for (Announcement announcement : announcements) {
//                System.out.println("=== DEBUG: 공지사항 - ID: " + announcement.getId() +
//                                 ", 제목: " + announcement.getTitle() +
//                                 ", 생성일: " + announcement.getCreatedAt() + " ===");
//            }
//
//            List<AnnouncementDto.Response> responses = announcements.stream()
//                    .map(announcement -> {
//                        try {
//                            return new AnnouncementDto.Response(announcement);
//                        } catch (Exception e) {
//                            System.out.println("=== DEBUG: DTO 변환 중 오류 (ID: " + announcement.getId() + "): " + e.getMessage() + " ===");
//                            return null;
//                        }
//                    })
//                    .filter(response -> response != null)
//                    .collect(Collectors.toList());
//
//            System.out.println("=== DEBUG: DTO 변환 완료된 공지사항 수: " + responses.size() + " ===");
//            return responses;
//        } catch (Exception e) {
//            System.out.println("=== DEBUG: getAllAnnouncements 오류: " + e.getMessage() + " ===");
//            e.printStackTrace();
//            return List.of(); // 빈 리스트 반환
//        }
//    }



    //운영용 : 인증 필요
    @Transactional
    public AnnouncementDto.Response createAnnouncement(AnnouncementDto.Request request, User author) {
        if (!author.getRole().equals(Role.ADMIN)) {
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
        try {
            System.out.println("=== DEBUG: createAnnouncementWithoutAuth 시작 ===");
            
            // 시스템 사용자를 찾거나 생성
            User systemUser = userRepository.findByUsername("system")
                    .orElseGet(() -> {
                        System.out.println("=== DEBUG: 시스템 사용자 생성 중 ===");
                        // Role과 SignupCategory가 DB에 있는지 확인 필요
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

            System.out.println("=== DEBUG: 시스템 사용자 ID: " + systemUser.getId() + " ===");

            Announcement announcement = new Announcement(
                    request.getTitle(),
                    request.getContent(),
                    systemUser,
                    request.isImportant()
            );

            System.out.println("=== DEBUG: 공지사항 엔티티 생성 완료 ===");
            
            Announcement savedAnnouncement = announcementRepository.save(announcement);
            System.out.println("=== DEBUG: 공지사항 저장 완료 - ID: " + savedAnnouncement.getId() + " ===");
            
            AnnouncementDto.Response response = new AnnouncementDto.Response(savedAnnouncement);
            System.out.println("=== DEBUG: DTO 변환 완료 ===");
            
            return response;
        } catch (Exception e) {
            System.out.println("=== DEBUG: createAnnouncementWithoutAuth 오류: " + e.getMessage() + " ===");
            e.printStackTrace();
            throw e;
        }
    }

    //운영용 : 인증 필요
    @Transactional
    public void deleteAnnouncement(String id, User user) {
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("Only admins can delete announcements");
        }

        Optional<Announcement> announcement = announcementRepository.findById(Long.parseLong(id));
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

    //운영용 인증 필요
    @Transactional
    public AnnouncementDto.Response updateAnnouncement(Long id, AnnouncementDto.Request dto) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));

        // updateInfo 메서드 사용으로 더 명확한 업데이트
        announcement.updateInfo(dto.getTitle(), dto.getContent(), dto.isImportant());

        // JPA에서 자동으로 저장됨 (@Transactional에 의해)
        return AnnouncementDto.fromEntity(announcement);
    }

    // 디버그용: 전체 공지사항 수 조회
    @Transactional(readOnly = true)
    public long getAnnouncementCount() {
        long count = announcementRepository.count();
        System.out.println("=== DEBUG: 전체 공지사항 수: " + count + " ===");
        return count;
    }
}