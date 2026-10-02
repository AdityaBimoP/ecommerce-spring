package com.superstyleshop.service;

import com.superstyleshop.model.UserAccount;
import com.superstyleshop.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount register(String email, String password, String fullName) {
        String normalizedEmail = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }
        return users.save(new UserAccount(
                normalizedEmail, passwordEncoder.encode(password), fullName.trim(), ""));
    }

    @Transactional
    public void updateProfile(UserAccount user, String fullName, String address) {
        if (!user.isAdmin() && (address == null || address.isBlank())) {
            throw new IllegalArgumentException("Add an address to your account before checkout.");
        }
        user.updateProfile(fullName.trim(), address == null ? user.getAddress() : address.trim());
        users.save(user);
    }
}
