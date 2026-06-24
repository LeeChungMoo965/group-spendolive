package com.example.spendolive.notification.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.notification.domain.NotificationDTO;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NotificationDTO> findById(String id) {
        String sql = """
            SELECT
                notification_id,
                id,
                notification_type,
                title,
                message,
                link_url,
                read_yn,
                star_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM notification_tb
            WHERE id = ?
            ORDER BY star_yn DESC, read_yn ASC, created_at DESC
        """;

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
            dto.setCreatedAt(rs.getString("created_at"));

            return dto;
        }, id);
    }

    public int countUnread(String id) {
        String sql = """
            SELECT COUNT(*)
            FROM notification_tb
            WHERE id = ?
              AND read_yn = 'N'
        """;

        return jdbcTemplate.queryForObject(sql, Integer.class, id);
    }

    public void updateReadYn(int notificationId, String id) {
        String sql = """
            UPDATE notification_tb
            SET read_yn = 'Y'
            WHERE notification_id = ?
              AND id = ?
        """;

        jdbcTemplate.update(sql, notificationId, id);
    }

    public void toggleStar(int notificationId, String id) {
        String sql = """
            UPDATE notification_tb
            SET star_yn =
                CASE
                    WHEN star_yn = 'Y' THEN 'N'
                    ELSE 'Y'
                END
            WHERE notification_id = ?
              AND id = ?
        """;

        jdbcTemplate.update(sql, notificationId, id);
    }
}