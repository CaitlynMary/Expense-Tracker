package com.familyexpensetracker.module.auth.service;

import com.familyexpensetracker.module.auth.dto.AuthResponse;
import com.familyexpensetracker.module.auth.dto.LoginRequest;
import com.familyexpensetracker.module.auth.dto.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest signupRequest);
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse getCurrentUserProfile(String email);
}
