package com.queueless.dev;

import com.queueless.entity.User;
import com.queueless.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local-password-reset")
public class LocalPasswordResetRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LocalPasswordResetRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!Boolean.parseBoolean(System.getenv("QUEUELESS_RESET_ENABLED"))) {
            throw new IllegalStateException("Explicit local password reset opt-in is required");
        }

        String email = System.getenv("QUEUELESS_RESET_EMAIL");
        String expectedRole = System.getenv("QUEUELESS_RESET_ROLE");
        String newPassword = System.getenv("QUEUELESS_RESET_PASSWORD");
        if (email == null || email.isBlank()
                || !"ADMIN".equals(expectedRole) && !"STAFF".equals(expectedRole)
                || newPassword == null || newPassword.length() < 8) {
            throw new IllegalStateException("A target email, ADMIN or STAFF role, and password of at least 8 characters are required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("The requested account was not found"));
        if (!expectedRole.equals(user.getRole())) {
            throw new IllegalStateException("The requested account does not have the expected role");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        System.out.println("Local BCrypt password reset completed for " + email);
    }
}