package org.mtvs.backend.global.config;

import org.mtvs.backend.user.entity.Role;
import org.mtvs.backend.user.entity.SignupCategory;
import org.mtvs.backend.user.repository.RoleRepository;
import org.mtvs.backend.user.repository.SignupCategoryRepository;
import org.mtvs.backend.riot.entity.EventType;
import org.mtvs.backend.riot.Repository.EventTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner dataLoader(RoleRepository roleRepository, 
                                        SignupCategoryRepository signupCategoryRepository,
                                        EventTypeRepository eventTypeRepository) {
        return args -> {
            // Seed Roles
            if (roleRepository.count() == 0) {
                roleRepository.saveAll(List.of(
                        new Role(1, "ADMIN"),
                        new Role(2, "USER"),
                        new Role(3, "GUEST")
                ));
            }

            // Seed Signup Categories
            if (signupCategoryRepository.count() == 0) {
                signupCategoryRepository.saveAll(List.of(
                        new SignupCategory(1, "Local"),
                        new SignupCategory(2, "Clerk"),
                        new SignupCategory(3, "Naver")
                ));
            }
            
            // Seed Event Types
            if (eventTypeRepository.count() == 0) {
                eventTypeRepository.saveAll(List.of(
                        new EventType((byte) 1, "CHAMPION_KILL", "Champion Kill"),
                        new EventType((byte) 2, "CHAMPION_SPECIAL_KILL", "Champion Special Kill"),
                        new EventType((byte) 3, "GAME_END", "Game End"),
                        new EventType((byte) 4, "ITEM_DESTROYED", "Item Destroyed"),
                        new EventType((byte) 5, "ITEM_PURCHASED", "Item Purchased"),
                        new EventType((byte) 6, "ITEM_SOLD", "Item Sold"),
                        new EventType((byte) 7, "ITEM_UNDO", "Item Undo"),
                        new EventType((byte) 8, "LEVEL_UP", "Level Up"),
                        new EventType((byte) 9, "PAUSE_END", "Pause End"),
                        new EventType((byte) 10, "SKILL_LEVEL_UP", "Skill Level Up"),
                        new EventType((byte) 11, "WARD_KILL", "Ward Kill"),
                        new EventType((byte) 12, "WARD_PLACED", "Ward Placed")
                ));
            }
        };
    }
} 