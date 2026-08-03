package org.tajiro.policy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.policy.dto.PolicyDTO;
import org.tajiro.policy.mapper.PolicyMapper;
import org.tajiro.user.dto.UserProfileDTO;
import org.tajiro.user.service.UserProfileService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PolicyServiceImpl implements PolicyService{

    private final PolicyMapper mapper;
    private final UserProfileService userProfileService;


    @Override
    public List<PolicyDTO> getList(String keyword, Long userId) {

        UserProfileDTO userProfile= userProfileService.getUserProfileById(userId);
        String region = userProfile.getTargetSggCode();
        int age = Period.between(userProfile.getBirthDate(), LocalDate.now()).getYears();
        return mapper.getList(region,age,keyword).stream().map(PolicyDTO::of).toList();
    }

    @Override
    public PolicyDTO get(Long policyid, Long userId) {
        UserProfileDTO userProfile= userProfileService.getUserProfileById(userId);
        String region = userProfile.getTargetSggCode();
        int age = Period.between(userProfile.getBirthDate(), LocalDate.now()).getYears();
        return PolicyDTO.of(mapper.get(policyid,region,age));
    }
}
