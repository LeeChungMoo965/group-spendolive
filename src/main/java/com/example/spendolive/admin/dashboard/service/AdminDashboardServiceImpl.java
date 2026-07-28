package com.example.spendolive.admin.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.admin.dashboard.domain.AdminDashboardDTO;
import com.example.spendolive.admin.dashboard.repository.AdminDashboardRepository;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;

    public AdminDashboardServiceImpl(AdminDashboardRepository adminDashboardRepository) {
        this.adminDashboardRepository = adminDashboardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardSummary() {
        return adminDashboardRepository.selectDashboardSummary();
    }
}
