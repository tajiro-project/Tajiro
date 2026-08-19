package org.tajiro.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.auth.domain.OnboardingTourGroups;
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
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

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

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        boolean hasListings = "SELLER".equals(user.getRole())
                && authMapper.existsPropertyBySellerId(user.getId());
        return LoginResponse.of(accessToken, user, hasListings);
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        UserVO existing = authMapper.findByEmail(request.getEmail());
        if (existing != null) {
            if (!"ACTIVE".equals(existing.getStatus())) {
                authMapper.releaseWithdrawnEmail(existing.getId());
            } else {
                throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
            }
        }

        validateRequiredTermsAgreed(request);

        boolean isSeller = Boolean.TRUE.equals(request.getIsSeller());
        UserVO user = UserVO.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(isSeller ? "SELLER" : "USER")
                .phone(isSeller ? request.getPhone() : null)
                .agencyName(isSeller ? request.getAgencyName() : null)
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

    @Override
    @Transactional
    public void withdraw(Long userId) {
        authMapper.softDeleteUser(userId);
    }

    @Override
    @Transactional
    public String markOnboardingTourSeen(Long userId, String group) {
        int index = OnboardingTourGroups.ORDER.indexOf(group);
        if (index == -1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String current = authMapper.findOnboardingSeenByUserId(userId);
        if (current == null || current.length() != OnboardingTourGroups.ORDER.size()) {
            current = OnboardingTourGroups.DEFAULT_SEEN;
        }

        StringBuilder updated = new StringBuilder(current);
        updated.setCharAt(index, '1');

        authMapper.updateOnboardingSeen(userId, updated.toString());
        return updated.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String getOnboardingSeen(Long userId) {
        String current = authMapper.findOnboardingSeenByUserId(userId);
        if (current == null || current.length() != OnboardingTourGroups.ORDER.size()) {
            return OnboardingTourGroups.DEFAULT_SEEN;
        }
        return current;
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || !EMAIL_PATTERN.matcher(request.getEmail()).matches()
                || request.getPassword() == null || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
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
