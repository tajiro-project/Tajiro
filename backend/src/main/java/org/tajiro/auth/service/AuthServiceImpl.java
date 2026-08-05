package org.tajiro.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.auth.domain.UserVO;
import org.tajiro.auth.dto.LoginRequest;
import org.tajiro.auth.dto.LoginResponse;
import org.tajiro.auth.dto.RegisterRequest;
import org.tajiro.auth.dto.RegisterResponse;
import org.tajiro.auth.mapper.AuthMapper;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.security.jwt.JwtProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserVO user = authMapper.findByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        return LoginResponse.of(accessToken, user);
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (authMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
        }

        validateRequiredTermsAgreed(request);

        UserVO user = UserVO.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .provider("LOCAL")
                .status("ACTIVE")
                .build();
        authMapper.insertUser(user);

        for (RegisterRequest.AgreementRequest agreement : request.getAgreements()) {
            authMapper.insertTermsConsent(user.getId(), agreement.getTermsId(), Boolean.TRUE.equals(agreement.getAgreed()));
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        return RegisterResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .build();
    }

    private void validateRequiredTermsAgreed(RegisterRequest request) {
        List<Long> requiredTermsIds = authMapper.selectRequiredTermsIds();

        for (Long requiredId : requiredTermsIds) {
            boolean agreed = request.getAgreements() != null && request.getAgreements().stream()
                    .anyMatch(a -> requiredId.equals(a.getTermsId()) && Boolean.TRUE.equals(a.getAgreed()));

            if (!agreed) {
                throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
            }
        }
    }
}
