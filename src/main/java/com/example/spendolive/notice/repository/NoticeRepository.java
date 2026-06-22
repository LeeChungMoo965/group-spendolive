package com.example.spendolive.notice.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.notice.domain.NoticeDTO;

@Repository
public class NoticeRepository {

    private final JdbcTemplate jdbcTemplate;

    public NoticeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NoticeDTO> findAll() {
        String sql = """
            SELECT
                notice_id,
                admin_id,
                title,
                content,
                pinned_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb
            ORDER BY pinned_yn DESC, created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            NoticeDTO notice = new NoticeDTO();
            notice.setNoticeId(rs.getInt("notice_id"));
            notice.setAdminId(rs.getInt("admin_id"));
            notice.setTitle(rs.getString("title"));
            notice.setContent(rs.getString("content"));
            notice.setPinnedYn(rs.getString("pinned_yn"));
            notice.setCreatedAt(rs.getString("created_at"));
            notice.setUpdatedAt(rs.getString("updated_at"));
            return notice;
        });
    }

    public NoticeDTO findById(int noticeId) {
        String sql = """
            SELECT
                notice_id,
                admin_id,
                title,
                content,
                pinned_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb
            WHERE notice_id = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            NoticeDTO notice = new NoticeDTO();
            notice.setNoticeId(rs.getInt("notice_id"));
            notice.setAdminId(rs.getInt("admin_id"));
            notice.setTitle(rs.getString("title"));
            notice.setContent(rs.getString("content"));
            notice.setPinnedYn(rs.getString("pinned_yn"));
            notice.setCreatedAt(rs.getString("created_at"));
            notice.setUpdatedAt(rs.getString("updated_at"));
            return notice;
        }, noticeId);
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM notice_tb";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int countPinned() {
        String sql = "SELECT COUNT(*) FROM notice_tb WHERE pinned_yn = 'Y'";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}