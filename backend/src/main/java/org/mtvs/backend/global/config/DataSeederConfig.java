package org.mtvs.backend.global.config;

import org.mtvs.backend.user.entity.Role;
import org.mtvs.backend.user.entity.SignupCategory;
import org.mtvs.backend.user.repository.RoleRepository;
import org.mtvs.backend.user.repository.SignupCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner dataLoader(RoleRepository roleRepository, SignupCategoryRepository signupCategoryRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                roleRepository.saveAll(List.of(
                        new Role(1, "ADMIN"),
                        new Role(2, "USER"),
                        new Role(3, "GUEST")
                ));
            }

            if (signupCategoryRepository.count() == 0) {
                signupCategoryRepository.saveAll(List.of(
                        new SignupCategory(1, "Local"),
                        new SignupCategory(2, "Clerk"),
                        new SignupCategory(3, "Naver")
                ));
            }
        };
    }
} 