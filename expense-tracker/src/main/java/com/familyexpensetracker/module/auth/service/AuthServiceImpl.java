package com.familyexpensetracker.module.auth.service;

import com.familyexpensetracker.exception.DuplicateResourceException;
import com.familyexpensetracker.exception.ResourceNotFoundException;
import com.familyexpensetracker.module.auth.dto.AuthResponse;
import com.familyexpensetracker.module.auth.dto.LoginRequest;
import com.familyexpensetracker.module.auth.dto.SignupRequest;
import com.familyexpensetracker.module.auth.entity.User;
import com.familyexpensetracker.module.auth.repository.UserRepository;
import com.familyexpensetracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public AuthResponse signup(SignupRequest signupRequest) {
        long startedAt = System.currentTimeMillis();
        String email = normalizeEmail(signupRequest.getEmail());
        String name = signupRequest.getName().trim();

        log.info("[Signup] request received for email={}", email);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.info("[Signup] duplicate email rejected in {} ms", System.currentTimeMillis() - startedAt);
            throw new DuplicateResourceException("Email is already taken!");
        }
        log.info("[Signup] user validation completed in {} ms", System.currentTimeMillis() - startedAt);

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        log.info("[Signup] password hashing completed in {} ms", System.currentTimeMillis() - startedAt);

        User user = User.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .build();

        User savedUser = userRepository.save(user);
        log.info("[Signup] database save completed for userId={} in {} ms", savedUser.getId(), System.currentTimeMillis() - startedAt);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        log.info("[Signup] response sent for userId={} in {} ms", savedUser.getId(), System.currentTimeMillis() - startedAt);

        return AuthResponse.builder()
                .success(true)
                .message("Account created successfully")
                .token(jwt)
                .type("Bearer")
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        long startedAt = System.currentTimeMillis();
        String email = normalizeEmail(loginRequest.getEmail());
        log.info("[Login] request received for email={}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.info("[Login] User found: false email={} elapsedMs={}", email, System.currentTimeMillis() - startedAt);
                    return new BadCredentialsException("Invalid email or password");
                });
        log.info("[Login] User found: true userId={} email={} elapsedMs={}", user.getId(), user.getEmail(), System.currentTimeMillis() - startedAt);

        boolean passwordMatched = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        log.info("[Login] Password matched: {} userId={} elapsedMs={}", passwordMatched, user.getId(), System.currentTimeMillis() - startedAt);

        if (!passwordMatched) {
            throw new BadCredentialsException("Invalid email or password");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        log.info("[Login] response sent for userId={} in {} ms", user.getId(), System.currentTimeMillis() - startedAt);

        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.builder()
                .success(true)
                .message("User profile retrieved successfully")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
