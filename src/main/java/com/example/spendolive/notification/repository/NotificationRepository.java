package com.example.spendolive.notification.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.notification.domain.NotificationDTO;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* ─── 목록 조회 ───────────────────────────────────────── */
    public List<NotificationDTO> findById(String id) {
        if (id == null || id.isBlank()) return Collections.emptyList();

        String sql = """
            SELECT
                notification_id, id, notification_type,
                title, message, link_url,
                read_yn, star_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM notification_tb
            WHERE id = ?
            ORDER BY star_yn DESC, read_yn ASC, created_at DESC
        """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                NotificationDTO dto = new NotificationDTO();
                dto.setNotificationId(rs.getInt("notification_id"));
                dto.setId(rs.getString("id"));
                dto.setNotificationType(rs.getString("notification_type"));
                dto.setTitle(rs.getString("title"));
                dto.setMessage(rs.getString("message"));
                dto.setLinkUrl(rs.getString("link_url"));
                dto.setReadYn(rs.getString("read_yn"));
                dto.setStarYn(rs.getString("star_yn"));
                dto.setCreated_at(rs.getString("created_at"));
                return dto;
            }, id);
        } catch (DataAccessException e) {
            System.err.println("[NotificationRepository.findById] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 안 읽은 개수 ────────────────────────────────────── */
    public int countUnread(String id) {
        if (id == null || id.isBlank()) return 0;

        String sql = """
            SELECT COUNT(*)
            FROM notification_tb
            WHERE id = ? AND read_yn = 'N'
        """;

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
            return (count != null) ? count : 0;
        } catch (DataAccessException e) {
            System.err.println("[NotificationRepository.countUnread] DB 오류: " + e.getMessage());
            return 0;
        }
    }

    /* ─── 읽음 처리 ───────────────────────────────────────── */
    public void updateReadYn(int notificationId, String id) {
        if (notificationId <= 0 || id == null || id.isBlank()) return;

        String sql = """
            UPDATE notification_tb
            SET read_yn = 'Y'
            WHERE notification_id = ? AND id = ?
        """;

        try {
            int rows = jdbcTemplate.update(sql, notificationId, id);
            if (rows == 0) {
                System.err.println("[NotificationRepository.updateReadYn] 대상 없음: notificationId=" + notificationId);
            }
        } catch (DataAccessException e) {
            System.err.println("[NotificationRepository.updateReadYn] DB 오류: " + e.getMessage());
        }
    }

    /* ─── 찜 토글 ─────────────────────────────────────────── */
    /* ─── 단건 조회 ──────────────────────────────────────── */
    public NotificationDTO findByNotificationId(int notificationId, String id) {
        if (notificationId <= 0 || id == null || id.isBlank()) return null;

        String sql = """
            SELECT notification_id, id, notification_type,
                   title, message, link_url, read_yn, star_yn,
                   TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM notification_tb
            WHERE notification_id = ? AND id = ?
        """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                NotificationDTO dto = new NotificationDTO();
                dto.setNotificationId(rs.getInt("notification_id"));
                dto.setId(rs.getString("id"));
                dto.setNotificationType(rs.getString("notification_type"));
                dto.setTitle(rs.getString("title"));
                dto.setMessage(rs.getString("message"));
                dto.setLinkUrl(rs.getString("link_url"));
                dto.setReadYn(rs.getString("read_yn"));
                dto.setStarYn(rs.getString("star_yn"));
                dto.setCreated_at(rs.getString("created_at"));
                return dto;
            }, notificationId, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            System.err.println("[NotificationRepository.findByNotificationId] 오류: " + e.getMessage());
            return null;
        }
    }

        public void toggleStar(int notificationId, String id) {
        if (notificationId <= 0 || id == null || id.isBlank()) return;

        String sql = """
            UPDATE notification_tb
            SET star_yn = CASE WHEN star_yn = 'Y' THEN 'N' ELSE 'Y' END
            WHERE notification_id = ? AND id = ?
        """;

        try {
            int rows = jdbcTemplate.update(sql, notificationId, id);
            if (rows == 0) {
                System.err.println("[NotificationRepository.toggleStar] 대상 없음: notificationId=" + notificationId);
            }
        } catch (DataAccessException e) {
            System.err.println("[NotificationRepository.toggleStar] DB 오류: " + e.getMessage());
        }
    }
}