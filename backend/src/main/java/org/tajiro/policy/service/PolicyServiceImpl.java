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
        String region = userProfile != null ? userProfile.getTargetSggCode() : null;
        Integer age = calculateAge(userProfile);
        return mapper.getList(region,age,keyword,categoryCode).stream().map(PolicyDTO::of).toList();
    }

    @Override
    public PolicyDTO get(Long policyid, Long userId) {
        UserProfileDTO userProfile= userProfileService.getUserProfileById(userId);
        String region = userProfile != null ? userProfile.getTargetSggCode() : null;
        Integer age = calculateAge(userProfile);
        return PolicyDTO.of(mapper.get(policyid,region,age));
    }

    // 매도자 등 프로필(user_profile)이 없는 계정은 지역/나이 기준 매칭이 불가능하므로
    // null을 넘겨서 매칭 결과 없음으로 자연스럽게 처리한다(NPE 방지).
    private Integer calculateAge(UserProfileDTO userProfile) {
        if (userProfile == null || userProfile.getBirthDate() == null) {
            return null;
        }
        return Period.between(userProfile.getBirthDate(), LocalDate.now()).getYears();
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

        // 2. 사용자 나이 계산(프로필 없는 계정은 null)
        Integer age = calculateAge(userProfile);


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
