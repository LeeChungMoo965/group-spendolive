package com.example.spendolive.ott.admin.service;

import java.util.List;

import com.example.spendolive.ott.domain.OttServiceDTO;

/**
 * 관리자 OTT 관리 Service
 *
 * 패키지 위치:
 * com.example.spendolive.ott.admin.service
 */
public interface AdminOttService {

    List<OttServiceDTO> getOttServiceList();

    OttServiceDTO getOttService(Long ott_service_id);

    void addOttService(OttServiceDTO ottService);

    boolean modifyOttService(OttServiceDTO ottService);

    boolean hideOttService(Long ott_service_id);
}
