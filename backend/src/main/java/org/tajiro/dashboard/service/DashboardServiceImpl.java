package org.tajiro.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.dashboard.dto.DashboardDTO;
import org.tajiro.dashboard.mapper.DashboardMapper;
import org.tajiro.user.dto.UserProfileDTO;
import org.tajiro.user.service.UserProfileService;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper mapper;
    private final UserProfileService userProfileService;

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO getDashboard(Long userId) {
        UserProfileDTO profile = userProfileService.getUserProfileById(userId);

        return DashboardDTO.builder()
                .name(mapper.getUserName(userId))
                .targetRegion(profile != null ? profile.getTargetRegion() : null)
                .birthDate(profile != null ? profile.getBirthDate() : null)
                .favoriteCount(mapper.countFavorites(userId))
                .reportCount(mapper.countReports(userId))
                .priorities(mapper.getPriorities(userId))
                .build();
    }
}
