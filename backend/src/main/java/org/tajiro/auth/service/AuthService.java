package org.tajiro.auth.service;

import org.tajiro.auth.dto.LoginRequest;
import org.tajiro.auth.dto.LoginResponse;
import org.tajiro.auth.dto.RegisterRequest;
import org.tajiro.auth.dto.RegisterResponse;
import org.tajiro.auth.dto.UserInfoResponse;
import org.tajiro.auth.dto.UserInfoUpdateRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    void withdraw(Long userId);

    String markOnboardingTourSeen(Long userId, String group);

    String getOnboardingSeen(Long userId);

    UserInfoResponse getMyInfo(Long userId);

    void updateMyInfo(Long userId, UserInfoUpdateRequest request);
}
