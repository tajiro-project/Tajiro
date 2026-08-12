package org.tajiro.policy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.policy.dto.PolicyDTO;
import org.tajiro.policy.mapper.PolicyMapper;
import org.tajiro.property.dto.PropertyDetailDTO;
import org.tajiro.property.service.PropertyDetailService;
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
    private final PropertyDetailService propertyDetailService;


    @Override
    public List<PolicyDTO> getList(String keyword, String categoryCode, Long userId) {

        UserProfileDTO userProfile= userProfileService.getUserProfileById(userId);
        String region = userProfile.getTargetSggCode();
        int age = Period.between(userProfile.getBirthDate(), LocalDate.now()).getYears();
        return mapper.getList(region,age,keyword,categoryCode).stream().map(PolicyDTO::of).toList();
    }

    @Override
    public PolicyDTO get(Long policyid, Long userId) {
        UserProfileDTO userProfile= userProfileService.getUserProfileById(userId);
        String region = userProfile.getTargetSggCode();
        int age = Period.between(userProfile.getBirthDate(), LocalDate.now()).getYears();
        return PolicyDTO.of(mapper.get(policyid,region,age));
    }

    @Override
    public List<PolicyDTO> getRecommendedByPropertyId(
            Long propertyId,
            Long userId
    ) {

        // 1. 사용자 프로필 조회
        UserProfileDTO userProfile =
                userProfileService
                        .getUserProfileById(userId);

        // 2. 사용자 나이 계산
        int age =
                Period.between(
                        userProfile.getBirthDate(),
                        LocalDate.now()
                ).getYears();


        // 3. propertyId로 매물 조회
        PropertyDetailDTO property = propertyDetailService.getPropertyDetail(propertyId, userId);


        // 4. 사용자 희망지역이 아니라
        //    해당 매물의 시군구 코드 사용
        String region = property.getSigunguCd();


        // 5. 매물 지역 + 사용자 나이 +
        //    HOUSING 정책 중 similarity 상위 3개
        return mapper
                .getHousingRecommendations(
                        region,
                        age
                )
                .stream()
                .map(PolicyDTO::of)
                .toList();
    }
}
