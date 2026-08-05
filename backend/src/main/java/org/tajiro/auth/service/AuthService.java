package org.tajiro.auth.service;

import org.tajiro.auth.dto.LoginRequest;
import org.tajiro.auth.dto.LoginResponse;
import org.tajiro.auth.dto.RegisterRequest;
import org.tajiro.auth.dto.RegisterResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
}
