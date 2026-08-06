package org.tajiro.preference.service;

import org.tajiro.preference.dto.PreferenceDTO;

public interface PreferenceService {
    PreferenceDTO get(Long userId);

    PreferenceDTO save(Long userId, PreferenceDTO preference);
}
