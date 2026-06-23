package com.example.spendolive.alert.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.alert.domain.AlertDTO;

@Repository
public class AlertRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //목록 조회//
    public List<AlertDTO> findAll() {

        String sql = """
            SELECT
                alert_id,
                id,
                alert_type,
                title,
                content,
                target_url,
                read_yn,
                banner_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(read_at, 'YYYY.MM.DD') AS read_at
            FROM alert_tb
            ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            AlertDTO alert = new AlertDTO();

            alert.setAlertId(rs.getInt("alert_id"));
            alert.setId(rs.getString("id"));
            alert.setAlertType(rs.getString("alert_type"));
            alert.setTitle(rs.getString("title"));
            alert.setContent(rs.getString("content"));
            alert.setTargetUrl(rs.getString("target_url"));
            alert.setReadYn(rs.getString("read_yn"));
            alert.setBannerYn(rs.getString("banner_yn"));
            alert.setCreatedAt(rs.getString("created_at"));
            alert.setReadAt(rs.getString("read_at"));

            return alert;
        });
    }


    public List<AlertDTO> findUnread() {

        String sql = """
            SELECT
                alert_id,
                id,
                alert_type,
                title,
                content,
                target_url,
                read_yn,
                banner_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(read_at, 'YYYY.MM.DD') AS read_at
            FROM alert_tb
            WHERE read_yn = 'N'
            ORDER BY created_at DESC
        """;
    
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
    
            AlertDTO alert = new AlertDTO();
    
            alert.setAlertId(rs.getInt("alert_id"));
            alert.setId(rs.getString("id"));
            alert.setAlertType(rs.getString("alert_type"));
            alert.setTitle(rs.getString("title"));
            alert.setContent(rs.getString("content"));
            alert.setTargetUrl(rs.getString("target_url"));
            alert.setReadYn(rs.getString("read_yn"));
            alert.setBannerYn(rs.getString("banner_yn"));
            alert.setCreatedAt(rs.getString("created_at"));
            alert.setReadAt(rs.getString("read_at"));
    
            return alert;
        });
    }

    //상세 조회 //
    public AlertDTO findById(int alertId) {

        String sql = """
            SELECT
                alert_id,
                id,
                alert_type,
                title,
                content,
                target_url,
                read_yn,
                banner_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(read_at, 'YYYY.MM.DD') AS read_at
            FROM alert_tb
            WHERE alert_id = ?
        """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {

                    AlertDTO alert = new AlertDTO();

                    alert.setAlertId(rs.getInt("alert_id"));
                    alert.setId(rs.getString("id"));
                    alert.setAlertType(rs.getString("alert_type"));
                    alert.setTitle(rs.getString("title"));
                    alert.setContent(rs.getString("content"));
                    alert.setTargetUrl(rs.getString("target_url"));
                    alert.setReadYn(rs.getString("read_yn"));
                    alert.setBannerYn(rs.getString("banner_yn"));
                    alert.setCreatedAt(rs.getString("created_at"));
                    alert.setReadAt(rs.getString("read_at"));

                    return alert;
                },
                alertId
        );
    }
}