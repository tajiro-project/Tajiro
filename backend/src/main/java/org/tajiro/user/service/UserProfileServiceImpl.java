package org.tajiro.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.user.dto.UserProfileDTO;
import org.tajiro.user.mapper.UserProfileMapper;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService{

    private final UserProfileMapper mapper;

    @Override
    public UserProfileDTO getUserProfileById(Long userId) {
        return UserProfileDTO.of(mapper.getUserProfileById(userId));
    }
}
