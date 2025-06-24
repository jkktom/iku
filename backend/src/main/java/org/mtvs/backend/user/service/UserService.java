package org.mtvs.backend.user.service;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.models.components.EmailAddress;
import com.clerk.backend_api.models.operations.GetUserResponse;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mtvs.backend.user.entity.User;
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

    public User findOrCreateUserFromClerk(JWTClaimsSet claims) {
        String clerkUserId = claims.getSubject();
        return userRepository.findByLinkingUserId(clerkUserId)
            .orElseGet(() -> {
                try {
                    log.info("New user signup from Clerk: {}. Creating local user profile.", clerkUserId);

                    GetUserResponse response = clerk.users().get(clerkUserId);
                    
                    if (response.statusCode() != 200 || response.user().isEmpty()) {
                        throw new RuntimeException("Could not fetch user details from Clerk. Status: " + response.statusCode());
                    }
                    
                    com.clerk.backend_api.models.components.User clerkUser = response.user().get();

                    List<EmailAddress> emails = clerkUser.emailAddresses()
                        .orElseThrow(() -> new RuntimeException("Clerk user has no email addresses list."));

                    if (emails.isEmpty()) {
                        throw new RuntimeException("Clerk user has no email address.");
                    }

                    String primaryEmailId = clerkUser.primaryEmailAddressId().orElse(null);

                    // Find the primary email or use the first one
                    String email = emails.stream()
                        .filter(emailAddr -> primaryEmailId != null && primaryEmailId.equals(emailAddr.id().orElse(null)))
                        .findFirst()
                        .or(() -> emails.stream().findFirst())
                        .map(emailAddr -> emailAddr.emailAddress())
                        .orElseThrow(() -> new RuntimeException("Could not extract email address from Clerk user"));

                    String username = clerkUser.username().isPresent() 
                        ? clerkUser.username().get() 
                        : null;
                    
                    if (username == null || username.trim().isEmpty()) {
                        throw new RuntimeException("Username is required but not provided by Clerk");
                    }

                    Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Default role not found"));
                    SignupCategory clerkCategory = signupCategoryRepository.findByName("Clerk").orElseThrow(() -> new RuntimeException("Clerk signup category not found"));

                    User newUser = User.builder()
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

    public Optional<User> findByLinkingUserId(String linkingUserId) {
        return userRepository.findByLinkingUserId(linkingUserId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}