package com.taskOrchestrator.app.auth.infrastructure.security;

import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

//implementation of CurrentUserProvider
//Spring Security is isolated in infrastructure
//in case of any changes auth (JWT → OAuth), only this class changes
@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {
    @Override
    public CurrentUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        User.Role role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(User.Role::valueOf)
                .findFirst()
                .orElse(User.Role.USER);

        return new CurrentUser(auth.getName(), role);
    }
}
