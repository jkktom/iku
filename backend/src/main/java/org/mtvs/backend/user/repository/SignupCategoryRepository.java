package org.mtvs.backend.user.repository;

import org.mtvs.backend.user.entity.SignupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SignupCategoryRepository extends JpaRepository<SignupCategory, Integer> {
    Optional<SignupCategory> findByName(String name);
} 