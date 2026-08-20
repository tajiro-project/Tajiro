package org.tajiro.seller.domain;

import java.util.List;

public final class SafetyCategoryConfig {

    public static final int SAFETY_RADIUS_M = 500;

    public static final List<String> CATEGORIES = List.of(
            "CCTV",
            "CHILD_SAFE_ZONE",
            "SAFETY_BELL",
            "SECURITY_LIGHT",
            "POLICE_CENTER",
            "CHILD_GUARD_HOUSE",
            "CHILD_ACCIDENT_ZONE",
            "PEDESTRIAN_ACCIDENT_ZONE"
    );

    private SafetyCategoryConfig() {
    }
}