package org.mtvs.backend.announcement.service;

import lombok.RequiredArgsConstructor;
import org.mtvs.backend.announcement.dto.AnnouncementDto;
import org.mtvs.backend.announcement.entity.Announcement;
import org.mtvs.backend.announcement.repository.AnnouncementRepository;
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

    @Transactional(readOnly = true)
    public List<AnnouncementDto.Response> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByImportantDescCreatedAtDesc()
                .stream()
                .map(AnnouncementDto.Response::new)
                .collect(Collectors.toList());
    }

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
}
