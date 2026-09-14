package com.taskOrchestrator.app.auth.infrastructure.security;

import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Implementation of CurrentUserProvider.
// Spring Security remains isolated in infrastructure.
// If authentication changes (JWT -> OAuth, etc.), this class can change
// without requiring the business layer to depend directly on Spring Security.
@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {
    private final UserRepository userRepository;

    public SpringSecurityCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CurrentUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new RuntimeException(
                    "No authenticated user"
            );
        }

        User.Role role = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(User.Role::valueOf)
                .findFirst()
                .orElse(User.Role.USER);

        User user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        return new CurrentUser(
                user.getId(),
                user.getUsername(),
                role
        );
    }
}
