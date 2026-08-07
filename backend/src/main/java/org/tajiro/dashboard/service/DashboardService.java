package org.tajiro.dashboard.service;

import org.tajiro.dashboard.dto.DashboardDTO;

public interface DashboardService {
    DashboardDTO getDashboard(Long userId);
}
