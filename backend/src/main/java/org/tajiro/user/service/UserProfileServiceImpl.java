package org.tajiro.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.user.domain.UserProfileVO;
import org.tajiro.user.dto.UserProfileDTO;
import org.tajiro.user.dto.UserProfileRequest;
import org.tajiro.user.mapper.UserProfileMapper;

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
        if (request.getTargetRegion() == null || request.getTargetRegion().isBlank()
                || request.getBirthDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        LocalDateTime updatedAt = LocalDateTime.now();

        // monthlyIncome/assetAmount/jobStatus/targetSggCode는 화면상 선택 입력이지만
        // DB 컬럼이 NOT NULL이라 값이 없으면 기본값으로 채워 저장한다.
        UserProfileVO vo = UserProfileVO.builder()
                .userId(userId)
                .targetRegion(request.getTargetRegion())
                .birthDate(request.getBirthDate())
                .monthlyIncome(request.getMonthlyIncome() != null ? request.getMonthlyIncome() : 0)
                .assetAmount(request.getAssetAmount() != null ? request.getAssetAmount() : 0)
                .jobStatus(request.getJobStatus() != null ? request.getJobStatus() : "")
                .targetSggCode(request.getTargetSggCode() != null ? request.getTargetSggCode() : "")
                .updatedAt(updatedAt)
                .build();

        mapper.upsertProfile(vo);
        return updatedAt;
    }
}
