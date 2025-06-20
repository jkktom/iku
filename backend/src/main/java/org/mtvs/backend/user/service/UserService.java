package org.mtvs.backend.user.service;

import com.clerk.backend_api.Clerk;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mtvs.backend.user.entity.Role;
import org.mtvs.backend.user.entity.SignupCategory;
import org.mtvs.backend.user.repository.RoleRepository;
import org.mtvs.backend.user.repository.SignupCategoryRepository;
import org.mtvs.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SignupCategoryRepository signupCategoryRepository;
    private final Clerk clerk;

    public org.mtvs.backend.user.entity.User findOrCreateUserFromClerk(JWTClaimsSet claims) {
        String clerkUserId = claims.getSubject();
        return userRepository.findByLinkingUserId(clerkUserId)
            .orElseGet(() -> {
                try {
                    log.info("New user signup from Clerk: {}. Creating local user profile.", clerkUserId);

                    com.clerk.backend_api.models.operations.GetUserResponse response = clerk.users().get(clerkUserId);
                    
                    if (response.statusCode() != 200 || response.user().isEmpty()) {
                        throw new RuntimeException("Could not fetch user details from Clerk. Status: " + response.statusCode());
                    }
                    
                    com.clerk.backend_api.models.components.User clerkUser = response.user().get();

                    List<com.clerk.backend_api.models.components.EmailAddress> emails = clerkUser.emailAddresses()
                        .orElseThrow(() -> new RuntimeException("Clerk user has no email addresses list."));

                    if (emails.isEmpty()) {
                        throw new RuntimeException("Clerk user has no email address.");
                    }

                    String primaryEmailId = clerkUser.primaryEmailAddressId().get().orElse(null);

                    String email = emails.stream()
                        .filter(e -> e.id().isPresent() && e.id().get().equals(primaryEmailId))
                        .findFirst()
                        .map(com.clerk.backend_api.models.components.EmailAddress::emailAddress)
                        .map(o -> o.get().orElse(null))
                        .orElse(emails.get(0).emailAddress().get().orElse(null));
                    
                    if (email == null) {
                        throw new RuntimeException("Could not determine a valid email address for the Clerk user.");
                    }

                    String username = clerkUser.username().get().orElse(email);

                    Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Default role not found"));
                    SignupCategory clerkCategory = signupCategoryRepository.findByName("Clerk").orElseThrow(() -> new RuntimeException("Clerk signup category not found"));

                    org.mtvs.backend.user.entity.User newUser = org.mtvs.backend.user.entity.User.builder()
                        .linkingUserId(clerkUserId)
                        .email(email)
                        .username(username)
                        .role(userRole)
                        .signupCategory(clerkCategory)
                        .build();
                    return userRepository.save(newUser);
                } catch (Exception e) {
                    log.error("Failed to create user from Clerk data for clerkUserId: {}", clerkUserId, e);
                    throw new RuntimeException("Failed to create user from Clerk data.", e);
                }
            });
    }

    public Optional<org.mtvs.backend.user.entity.User> findByLinkingUserId(String linkingUserId) {
        return userRepository.findByLinkingUserId(linkingUserId);
    }

    public Optional<org.mtvs.backend.user.entity.User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<org.mtvs.backend.user.entity.User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}