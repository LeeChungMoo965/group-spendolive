package com.example.spendolive.ott.admin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.spendolive.ott.domain.OttServiceDTO;

/**
 * 관리자 OTT 관리 Repository 구현체
 *
 * 패키지 위치:
 * com.example.spendolive.ott.admin.repository
 *
 * 역할:
 * - ott_service_tb SQL 실행
 * - JdbcTemplate 처리
 * - RowMapper 처리
 */
@Repository
public class AdminOttRepositoryImpl implements AdminOttRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminOttRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<OttServiceDTO> selectOttServiceList() {
        String sql = """
                SELECT ott_service_id,
                       service_name,
                       default_price,
                       share_yn,
                       risk_level,
                       block_reason,
                       fixed_plan_name,
                       base_price,
                       extra_member_fee,
                       extra_member_count,
                       max_member_limit,
                       platform_fee_rate
                FROM ott_service_tb
                ORDER BY ott_service_id
                """;

        return jdbcTemplate.query(sql, ottServiceRowMapper());
    }

    @Override
    public OttServiceDTO selectOttService(Long ottServiceId) {
        String sql = """
                SELECT ott_service_id,
                       service_name,
                       default_price,
                       share_yn,
                       risk_level,
                       block_reason,
                       fixed_plan_name,
                       base_price,
                       extra_member_fee,
                       extra_member_count,
                       max_member_limit,
                       platform_fee_rate
                FROM ott_service_tb
                WHERE ott_service_id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, ottServiceRowMapper(), ottServiceId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int insertOttService(OttServiceDTO ottService) {
        String sql = """
                INSERT INTO ott_service_tb (
                    service_name,
                    default_price,
                    fixed_plan_name,
                    base_price,
                    extra_member_fee,
                    extra_member_count,
                    max_member_limit,
                    platform_fee_rate,
                    share_yn,
                    risk_level,
                    block_reason
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(sql,
                ottService.getServiceName(),
                ottService.getDefaultPrice(),
                ottService.getFixedPlanName(),
                ottService.getBasePrice(),
                ottService.getExtraMemberFee(),
                ottService.getExtraMemberCount(),
                ottService.getMaxMemberLimit(),
                ottService.getPlatformFeeRate(),
                ottService.getShareYn(),
                ottService.getRiskLevel(),
                ottService.getBlockReason());
    }

    @Override
    public int updateOttService(OttServiceDTO ottService) {
        String sql = """
                UPDATE ott_service_tb
                SET service_name = ?,
                    default_price = ?,
                    fixed_plan_name = ?,
                    base_price = ?,
                    extra_member_fee = ?,
                    extra_member_count = ?,
                    max_member_limit = ?,
                    platform_fee_rate = ?,
                    share_yn = ?,
                    risk_level = ?,
                    block_reason = ?
                WHERE ott_service_id = ?
                """;

        return jdbcTemplate.update(sql,
                ottService.getServiceName(),
                ottService.getDefaultPrice(),
                ottService.getFixedPlanName(),
                ottService.getBasePrice(),
                ottService.getExtraMemberFee(),
                ottService.getExtraMemberCount(),
                ottService.getMaxMemberLimit(),
                ottService.getPlatformFeeRate(),
                ottService.getShareYn(),
                ottService.getRiskLevel(),
                ottService.getBlockReason(),
                ottService.getOttServiceId());
    }

    @Override
    public int hideOttService(Long ottServiceId) {
        String sql = """
                UPDATE ott_service_tb
                SET share_yn = 'N'
                WHERE ott_service_id = ?
                """;

        return jdbcTemplate.update(sql, ottServiceId);
    }

    private RowMapper<OttServiceDTO> ottServiceRowMapper() {
        return new RowMapper<>() {
            @Override
            public OttServiceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                OttServiceDTO dto = new OttServiceDTO();

                dto.setOttServiceId(rs.getLong("ott_service_id"));
                dto.setServiceName(rs.getString("service_name"));
                dto.setDefaultPrice(rs.getInt("default_price"));
                dto.setShareYn(rs.getString("share_yn"));
                dto.setRiskLevel(rs.getString("risk_level"));
                dto.setBlockReason(rs.getString("block_reason"));
                dto.setFixedPlanName(rs.getString("fixed_plan_name"));
                dto.setBasePrice(rs.getInt("base_price"));
                dto.setExtraMemberFee(rs.getInt("extra_member_fee"));
                dto.setExtraMemberCount(rs.getInt("extra_member_count"));
                dto.setMaxMemberLimit(rs.getInt("max_member_limit"));
                dto.setPlatformFeeRate(rs.getDouble("platform_fee_rate"));

                return dto;
            }
        };
    }
}
