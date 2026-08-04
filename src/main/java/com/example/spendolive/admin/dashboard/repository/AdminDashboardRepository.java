package com.example.spendolive.admin.dashboard.repository;

import com.example.spendolive.admin.dashboard.domain.AdminDashboardDTO;

public interface AdminDashboardRepository {

    AdminDashboardDTO selectDashboardSummary();
}
