package com.example.spendolive.alert.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.alert.domain.AlertDTO;
import com.example.spendolive.alert.repository.AlertRepository;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    public AlertServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<AlertDTO> getAlertList() {
        return alertRepository.findAll();
    }

    @Override
    public AlertDTO getAlertDetail(int alertId) {
        return alertRepository.findById(alertId);
    }

    @Override
    public List<AlertDTO> getUnreadList() {
    return alertRepository.findUnread();
    }
}