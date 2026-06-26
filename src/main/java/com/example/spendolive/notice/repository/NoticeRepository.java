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

    public List<NoticeDTO> findAll(String id) {
        String sql = """
    SELECT
        n.notice_id,
        n.admin_id,
        n.title,
        n.content,
        n.pinned_yn,

        CASE
            WHEN nr.notice_id IS NULL THEN 'N'
            ELSE 'Y'
        END AS read_yn,

        TO_CHAR(n.created_at, 'YYYY.MM.DD') AS created_at,
        TO_CHAR(n.updated_at, 'YYYY.MM.DD') AS updated_at

    FROM notice_tb n

    LEFT JOIN notice_read_tb nr
        ON n.notice_id = nr.notice_id
       AND nr.id = ?

    ORDER BY n.pinned_yn DESC,
             n.created_at DESC
""";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
            NoticeDTO notice = new NoticeDTO();

            notice.setNoticeId(rs.getInt("notice_id"));
            notice.setAdminId(rs.getString("admin_id"));
            notice.setTitle(rs.getString("title"));
            notice.setContent(rs.getString("content"));
            notice.setPinnedYn(rs.getString("pinned_yn"));
            notice.setCreatedAt(rs.getString("created_at"));
            notice.setUpdatedAt(rs.getString("updated_at"));
            notice.setReadYn(rs.getString("read_yn"));
            return notice;
        }, id);
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
            notice.setAdminId(rs.getString("admin_id"));
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


    public List<NoticeDTO> findImportantList(String id) {

        String sql = """
            SELECT
                n.notice_id,
                n.admin_id,
                n.title,
                n.content,
                n.pinned_yn,
                CASE
                    WHEN nr.notice_id IS NULL THEN 'N'
                    ELSE 'Y'
                END AS read_yn,
                TO_CHAR(n.created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(n.updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb n
            LEFT JOIN notice_read_tb nr
                ON n.notice_id = nr.notice_id
               AND nr.id = ?
            WHERE n.pinned_yn = 'Y'
            ORDER BY n.created_at DESC
        """;
    
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            NoticeDTO notice = new NoticeDTO();
    
            notice.setNoticeId(rs.getInt("notice_id"));
            notice.setAdminId(rs.getString("admin_id"));
            notice.setTitle(rs.getString("title"));
            notice.setContent(rs.getString("content"));
            notice.setPinnedYn(rs.getString("pinned_yn"));
            notice.setCreatedAt(rs.getString("created_at"));
            notice.setUpdatedAt(rs.getString("updated_at"));
            notice.setReadYn(rs.getString("read_yn"));
    
            return notice;
        }, id);
    }
    
    public void insertNoticeRead(int noticeId, String id) {
        String sql = """
            MERGE INTO notice_read_tb nr
            USING dual
            ON (nr.notice_id = ? AND nr.id = ?)
            WHEN NOT MATCHED THEN
                INSERT (notice_id, id)
                VALUES (?, ?)
        """;
    
        jdbcTemplate.update(sql, noticeId, id, noticeId, id);}


    public List<NoticeDTO> findUnreadByMemberId(String id) {
        String sql = """
            SELECT
                n.notice_id,
                n.admin_id,
                n.title,
                n.content,
                n.pinned_yn,
                TO_CHAR(n.created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(n.updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb n
            WHERE NOT EXISTS (
                SELECT 1
                FROM notice_read_tb nr
                WHERE nr.notice_id = n.notice_id
                  AND nr.id = ?
            )
            ORDER BY n.pinned_yn DESC, n.created_at DESC
        """;
    
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            NoticeDTO notice = new NoticeDTO();
    
            notice.setNoticeId(rs.getInt("notice_id"));
            notice.setAdminId(rs.getString("admin_id"));
            notice.setTitle(rs.getString("title"));
            notice.setContent(rs.getString("content"));
            notice.setPinnedYn(rs.getString("pinned_yn"));
            notice.setCreatedAt(rs.getString("created_at"));
            notice.setUpdatedAt(rs.getString("updated_at"));
    
            return notice;
        }, id);
    }

    

}