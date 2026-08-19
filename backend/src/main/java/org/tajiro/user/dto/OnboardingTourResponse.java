package org.tajiro.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OnboardingTourResponse {
    private String onboardingSeen;
}
