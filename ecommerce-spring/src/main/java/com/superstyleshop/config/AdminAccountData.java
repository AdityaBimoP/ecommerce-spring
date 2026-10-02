package com.superstyleshop.config;

import com.superstyleshop.model.UserAccount;
import com.superstyleshop.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountData {
    @Bean
    CommandLineRunner seedAdminAccount(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password) {
        return args -> {
            if (email.isBlank() || password.isBlank()) {
                throw new IllegalStateException("ADMIN_EMAIL and ADMIN_PASSWORD must be configured.");
            }
            if (password.length() < 12) {
                throw new IllegalStateException("ADMIN_PASSWORD must contain at least 12 characters.");
            }

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            users.findByEmailIgnoreCase(normalizedEmail).ifPresentOrElse(
                    user -> {
                        if (!user.isAdmin()) {
                            user.grantAdminRole(passwordEncoder.encode(password));
                            users.save(user);
                        }
                    },
                    () -> users.save(new UserAccount(
                            normalizedEmail,
                            passwordEncoder.encode(password),
                            "Store Administrator",
                            "",
                            "ADMIN")));
        };
    }
}
