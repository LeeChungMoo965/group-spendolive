package com.example.spendolive.alert.service;

import java.util.List;

import com.example.spendolive.alert.domain.AlertDTO;

public interface AlertService {

    List<AlertDTO> getAlertList();

    AlertDTO getAlertDetail(int alertId);
}