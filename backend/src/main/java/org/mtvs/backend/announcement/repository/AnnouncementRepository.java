package org.mtvs.backend.announcement.repository;

import org.mtvs.backend.announcement.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByImportantDescCreatedAtDesc();
    Page<Announcement> findAllByOrderByImportantDescCreatedAtDesc(Pageable pageable);
}
