package org.tajiro.auth.domain;

import java.util.List;

/**
 * 온보딩 투어 그룹 순서 — users.onboarding_seen 컬럼의 각 자릿수와 1:1 매칭된다.
 * 프론트 frontend/src/constants/onboardingSteps.js의 TOUR_GROUPS와 순서를 반드시 맞춰야 한다.
 */
public final class OnboardingTourGroups {

    public static final List<String> ORDER = List.of(
            "home",
            "preferences",
            "property-list",
            "property-detail",
            "compare-box",
            "compare"
    );

    public static final String DEFAULT_SEEN = "0".repeat(ORDER.size());

    private OnboardingTourGroups() {
    }
}
