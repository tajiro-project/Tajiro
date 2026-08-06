/**
 * 프로필, 가치관(선호 조건), 찜, 대시보드 서비스.
 */
package org.tajiro.user.service;

import org.tajiro.user.dto.UserProfileDTO;
import org.tajiro.user.dto.UserProfileRequest;

import java.time.LocalDateTime;

public interface UserProfileService {
    public UserProfileDTO getUserProfileById(Long userId);

    public LocalDateTime saveProfile(Long userId, UserProfileRequest request);
}
