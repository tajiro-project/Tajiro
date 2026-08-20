package org.tajiro.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.user.domain.UserProfileVO;
import org.tajiro.user.dto.UserProfileDTO;
import org.tajiro.user.dto.UserProfileRequest;
import org.tajiro.user.mapper.UserProfileMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService{

    private final UserProfileMapper mapper;

    @Override
    public UserProfileDTO getUserProfileById(Long userId) {
        return UserProfileDTO.of(mapper.getUserProfileById(userId));
    }

    @Override
    public LocalDateTime saveProfile(Long userId, UserProfileRequest request) {
        // 회원가입 시점에 프로필이 항상 먼저 생성되므로, 여기서는 요청에 없는 필드는
        // 기존 값을 그대로 유지한다 (예: 마이페이지 "선호 위치"에서 지역만 보내는 경우).
        UserProfileVO existing = mapper.getUserProfileById(userId);

        String targetRegion = request.getTargetRegion() != null && !request.getTargetRegion().isBlank()
                ? request.getTargetRegion()
                : (existing != null ? existing.getTargetRegion() : null);
        LocalDate birthDate = request.getBirthDate() != null
                ? request.getBirthDate()
                : (existing != null ? existing.getBirthDate() : null);

        if (targetRegion == null || targetRegion.isBlank() || birthDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        LocalDateTime updatedAt = LocalDateTime.now();

        // monthlyIncome/assetAmount/jobStatus/targetSggCode는 화면상 선택 입력이지만
        // DB 컬럼이 NOT NULL이라 값이 없으면 기존 값(없으면 기본값)을 유지한다.
        UserProfileVO vo = UserProfileVO.builder()
                .userId(userId)
                .targetRegion(targetRegion)
                .birthDate(birthDate)
                .monthlyIncome(request.getMonthlyIncome() != null
                        ? request.getMonthlyIncome()
                        : defaultInt(existing != null ? existing.getMonthlyIncome() : null))
                .assetAmount(request.getAssetAmount() != null
                        ? request.getAssetAmount()
                        : defaultInt(existing != null ? existing.getAssetAmount() : null))
                .jobStatus(request.getJobStatus() != null
                        ? request.getJobStatus()
                        : defaultStr(existing != null ? existing.getJobStatus() : null))
                .targetSggCode(request.getTargetSggCode() != null
                        ? request.getTargetSggCode()
                        : defaultStr(existing != null ? existing.getTargetSggCode() : null))
                .updatedAt(updatedAt)
                .build();

        mapper.upsertProfile(vo);
        return updatedAt;
    }

    private int defaultInt(Integer value) {
        return value != null ? value : 0;
    }

    private String defaultStr(String value) {
        return value != null ? value : "";
    }
}
