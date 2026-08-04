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
    public OttServiceDTO selectOttService(Long ott_service_id) {
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
            return jdbcTemplate.queryForObject(sql, ottServiceRowMapper(), ott_service_id);
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
                ottService.getService_name(),
                ottService.getDefault_price(),
                ottService.getFixed_plan_name(),
                ottService.getBase_price(),
                ottService.getExtra_member_fee(),
                ottService.getExtra_member_count(),
                ottService.getMax_member_limit(),
                ottService.getPlatform_fee_rate(),
                ottService.getShare_yn(),
                ottService.getRisk_level(),
                ottService.getBlock_reason());
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
                ottService.getService_name(),
                ottService.getDefault_price(),
                ottService.getFixed_plan_name(),
                ottService.getBase_price(),
                ottService.getExtra_member_fee(),
                ottService.getExtra_member_count(),
                ottService.getMax_member_limit(),
                ottService.getPlatform_fee_rate(),
                ottService.getShare_yn(),
                ottService.getRisk_level(),
                ottService.getBlock_reason(),
                ottService.getOtt_service_id());
    }

    @Override
    public int hideOttService(Long ott_service_id) {
        String sql = """
                UPDATE ott_service_tb
                SET share_yn = 'N'
                WHERE ott_service_id = ?
                """;

        return jdbcTemplate.update(sql, ott_service_id);
    }

    private RowMapper<OttServiceDTO> ottServiceRowMapper() {
        return new RowMapper<>() {
            @Override
            public OttServiceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                OttServiceDTO dto = new OttServiceDTO();

                dto.setOtt_service_id(rs.getLong("ott_service_id"));
                dto.setService_name(rs.getString("service_name"));
                dto.setDefault_price(rs.getInt("default_price"));
                dto.setShare_yn(rs.getString("share_yn"));
                dto.setRisk_level(rs.getString("risk_level"));
                dto.setBlock_reason(rs.getString("block_reason"));
                dto.setFixed_plan_name(rs.getString("fixed_plan_name"));
                dto.setBase_price(rs.getInt("base_price"));
                dto.setExtra_member_fee(rs.getInt("extra_member_fee"));
                dto.setExtra_member_count(rs.getInt("extra_member_count"));
                dto.setMax_member_limit(rs.getInt("max_member_limit"));
                dto.setPlatform_fee_rate(rs.getDouble("platform_fee_rate"));

                return dto;
            }
        };
    }
}
