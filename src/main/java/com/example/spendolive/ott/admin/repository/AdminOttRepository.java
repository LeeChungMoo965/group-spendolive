package com.example.spendolive.ott.admin.repository;

import java.util.List;

import com.example.spendolive.ott.domain.OttServiceDTO;

/**
 * 관리자 OTT 관리 Repository
 *
 * 패키지 위치:
 * com.example.spendolive.ott.admin.repository
 */
public interface AdminOttRepository {

    List<OttServiceDTO> selectOttServiceList();

    OttServiceDTO selectOttService(Long ottServiceId);

    int insertOttService(OttServiceDTO ottService);

    int updateOttService(OttServiceDTO ottService);

    int hideOttService(Long ottServiceId);
}
