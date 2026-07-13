package com.example.spendolive.ott.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.ott.admin.repository.AdminOttRepository;
import com.example.spendolive.ott.domain.OttServiceDTO;

/**
 * 관리자 OTT 관리 Service 구현체
 *
 * 패키지 위치:
 * com.example.spendolive.ott.admin.service
 *
 * 역할:
 * - 입력값 정리
 * - 기본값 처리
 * - defaultPrice 자동 계산
 * - 삭제 대신 숨김 처리 정책
 */
@Service
public class AdminOttServiceImpl implements AdminOttService {

    private final AdminOttRepository adminOttRepository;

    public AdminOttServiceImpl(AdminOttRepository adminOttRepository) {
        this.adminOttRepository = adminOttRepository;
    }

    @Override
    public List<OttServiceDTO> getOttServiceList() {
        return adminOttRepository.selectOttServiceList();
    }

    @Override
    public OttServiceDTO getOttService(Long ottServiceId) {
        if (ottServiceId == null) {
            return null;
        }

        return adminOttRepository.selectOttService(ottServiceId);
    }

    @Override
    public void addOttService(OttServiceDTO ottService) {
        prepareForSave(ottService);
        adminOttRepository.insertOttService(ottService);
    }

    @Override
    public boolean modifyOttService(OttServiceDTO ottService) {
        if (ottService.getOttServiceId() == null) {
            throw new IllegalArgumentException("수정할 OTT 항목 ID가 없습니다.");
        }

        prepareForSave(ottService);
        return adminOttRepository.updateOttService(ottService) > 0;
    }

    @Override
    public boolean hideOttService(Long ottServiceId) {
        if (ottServiceId == null) {
            throw new IllegalArgumentException("숨김 처리할 OTT 항목 ID가 없습니다.");
        }

        return adminOttRepository.hideOttService(ottServiceId) > 0;
    }

    private void prepareForSave(OttServiceDTO dto) {
        dto.setServiceName(normalizeRequired(dto.getServiceName(), "OTT 이름"));
        dto.setFixedPlanName(normalizeDefault(dto.getFixedPlanName(), "프리미엄"));

        if (!"N".equals(dto.getShareYn())) {
            dto.setShareYn("Y");
        }

        dto.setRiskLevel(normalizeNullable(dto.getRiskLevel()));
        dto.setBlockReason(normalizeNullable(dto.getBlockReason()));

        if (dto.getBasePrice() < 0) {
            throw new IllegalArgumentException("최고 멤버십 가격은 0원 이상이어야 합니다.");
        }

        if (dto.getExtraMemberFee() < 0) {
            throw new IllegalArgumentException("추가 멤버 비용은 0원 이상이어야 합니다.");
        }

        if (dto.getExtraMemberCount() < 0) {
            throw new IllegalArgumentException("추가 멤버 수는 0명 이상이어야 합니다.");
        }

        if (dto.getMaxMemberLimit() <= 0) {
            dto.setMaxMemberLimit(4);
        }

        if (dto.getPlatformFeeRate() < 0) {
            dto.setPlatformFeeRate(0.0);
        }

        /*
         * 최종 기준금액을 입력하지 않았으면 자동 계산합니다.
         * basePrice + extraMemberFee * extraMemberCount
         */
        if (dto.getDefaultPrice() <= 0) {
            int calculatedPrice = dto.getBasePrice() + (dto.getExtraMemberFee() * dto.getExtraMemberCount());
            dto.setDefaultPrice(calculatedPrice);
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }

        return value.trim();
    }

    private String normalizeDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
