package com.example.spendolive.notice.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.notice.domain.NoticeDTO;

@Repository
public class NoticeRepository {

    private final JdbcTemplate jdbcTemplate;

    public NoticeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* ─── 공통 RowMapper ──────────────────────────────────── */
    private NoticeDTO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        NoticeDTO notice = new NoticeDTO();
        notice.setNoticeId(rs.getInt("notice_id"));
        notice.setAdminId(rs.getString("admin_id"));
        notice.setTitle(rs.getString("title"));
        notice.setContent(rs.getString("content"));
        notice.setPinnedYn(rs.getString("pinned_yn"));
        notice.setCreatedAt(rs.getString("created_at"));
        notice.setUpdatedAt(rs.getString("updated_at"));
        return notice;
    }

    /* ─── 전체 목록 ───────────────────────────────────────── */
    public List<NoticeDTO> findAll(String id) {
        String sql = """
            SELECT
                n.notice_id,
                n.admin_id,
                n.title,
                n.content,
                n.pinned_yn,
                CASE WHEN nr.notice_id IS NULL THEN 'N' ELSE 'Y' END AS read_yn,
                CASE WHEN nf.notice_id IS NULL THEN 'N' ELSE 'Y' END AS star_yn,
                TO_CHAR(n.created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(n.updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb n
            LEFT JOIN notice_read_tb nr
                ON n.notice_id = nr.notice_id AND nr.id = ?
            LEFT JOIN notice_favorite_tb nf
                ON n.notice_id = nf.notice_id AND nf.id = ?
            ORDER BY n.pinned_yn DESC, n.created_at DESC
        """;

        try {
            String safeId = (id != null) ? id : "";
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                NoticeDTO notice = mapRow(rs);
                notice.setReadYn(rs.getString("read_yn"));
                notice.setStarYn(rs.getString("star_yn"));
                return notice;
            }, safeId, safeId);
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.findAll] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 단건 조회 ───────────────────────────────────────── */
    public NoticeDTO findById(int noticeId) {
        String sql = """
            SELECT
                notice_id, admin_id, title, content, pinned_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb
            WHERE notice_id = ?
        """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRow(rs), noticeId);
        } catch (EmptyResultDataAccessException e) {
            System.err.println("[NoticeRepository.findById] noticeId=" + noticeId + " 존재하지 않음");
            return null;
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.findById] DB 오류: " + e.getMessage());
            return null;
        }
    }

    /* ─── 카운트 ──────────────────────────────────────────── */
    public int countAll() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notice_tb", Integer.class);
            return (count != null) ? count : 0;
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.countAll] DB 오류: " + e.getMessage());
            return 0;
        }
    }

    public int countPinned() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notice_tb WHERE pinned_yn = 'Y'", Integer.class);
            return (count != null) ? count : 0;
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.countPinned] DB 오류: " + e.getMessage());
            return 0;
        }
    }

    /* ─── 중요 공지 목록 ──────────────────────────────────── */
    public List<NoticeDTO> findImportantList(String id) {
        String sql = """
            SELECT
                n.notice_id, n.admin_id, n.title, n.content, n.pinned_yn,
                CASE WHEN nr.notice_id IS NULL THEN 'N' ELSE 'Y' END AS read_yn,
                CASE WHEN nf.notice_id IS NULL THEN 'N' ELSE 'Y' END AS star_yn,
                TO_CHAR(n.created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(n.updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb n
            LEFT JOIN notice_read_tb nr
                ON n.notice_id = nr.notice_id AND nr.id = ?
            LEFT JOIN notice_favorite_tb nf
                ON n.notice_id = nf.notice_id AND nf.id = ?
            WHERE n.pinned_yn = 'Y'
            ORDER BY n.created_at DESC
        """;

        try {
            String safeId = (id != null) ? id : "";
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                NoticeDTO notice = mapRow(rs);
                notice.setReadYn(rs.getString("read_yn"));
                notice.setStarYn(rs.getString("star_yn"));
                return notice;
            }, safeId, safeId);
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.findImportantList] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 읽음 처리 ───────────────────────────────────────── */
    public void insertNoticeRead(int noticeId, String id) {
        if (id == null || id.isBlank()) return;

        String sql = """
            MERGE INTO notice_read_tb nr
            USING dual
            ON (nr.notice_id = ? AND nr.id = ?)
            WHEN NOT MATCHED THEN
                INSERT (notice_id, id) VALUES (?, ?)
        """;

        try {
            jdbcTemplate.update(sql, noticeId, id, noticeId, id);
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.insertNoticeRead] DB 오류: " + e.getMessage());
        }
    }

    /* ─── 안 읽은 목록 ────────────────────────────────────── */
    public List<NoticeDTO> findUnreadByMemberId(String id) {
        if (id == null || id.isBlank()) return Collections.emptyList();

        String sql = """
            SELECT
                n.notice_id, n.admin_id, n.title, n.content, n.pinned_yn,
                TO_CHAR(n.created_at, 'YYYY.MM.DD') AS created_at,
                TO_CHAR(n.updated_at, 'YYYY.MM.DD') AS updated_at
            FROM notice_tb n
            WHERE NOT EXISTS (
                SELECT 1 FROM notice_read_tb nr
                WHERE nr.notice_id = n.notice_id AND nr.id = ?
            )
            ORDER BY n.pinned_yn DESC, n.created_at DESC
        """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                NoticeDTO notice = mapRow(rs);
                notice.setReadYn("N");
                return notice;
            }, id);
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.findUnreadByMemberId] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 찜 토글 ─────────────────────────────────────────── */
    public void toggleNoticeStar(int noticeId, String id) {
        if (id == null || id.isBlank()) return;

        // Oracle은 MERGE WHEN MATCHED THEN DELETE 단독 불가
        // → 존재 여부 확인 후 DELETE 또는 INSERT
        String checkSql = "SELECT COUNT(*) FROM notice_favorite_tb WHERE notice_id = ? AND id = ?";
        String deleteSql = "DELETE FROM notice_favorite_tb WHERE notice_id = ? AND id = ?";
        String insertSql = "INSERT INTO notice_favorite_tb (notice_id, id) VALUES (?, ?)";

        try {
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, noticeId, id);
            if (count != null && count > 0) {
                jdbcTemplate.update(deleteSql, noticeId, id);
            } else {
                jdbcTemplate.update(insertSql, noticeId, id);
            }
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.toggleNoticeStar] DB 오류: " + e.getMessage());
        }
    }

    /* ─── 전체 회원 ID 조회 (알림 발송용) ─────────────────── */
    public List<String> findAllMemberIds() {
        try {
            return jdbcTemplate.queryForList(
                "SELECT id FROM member_tb WHERE status = 'ACTIVE'",
                String.class
            );
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.findAllMemberIds] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 공지 알림 전체 회원 발송 ────────────────────────── */
    public void insertNoticeAlertForAll(String title, String noticeId) {
        List<String> memberIds = findAllMemberIds();
        if (memberIds.isEmpty()) return;

        String sql = """
            INSERT INTO notification_tb
                (id, notification_type, title, message, link_url, read_yn, star_yn, created_at)
            VALUES (?, 'HOME', ?, ?, ?, 'N', 'N', SYSDATE)
        """;
        String message = "새 공지사항이 등록되었습니다.";
        String linkUrl = "/spendolive/notice/detail.do?noticeId=" + noticeId;

        for (String memberId : memberIds) {
            try {
                jdbcTemplate.update(sql, memberId, title, message, linkUrl);
            } catch (DataAccessException e) {
                System.err.println("[NoticeRepository.insertNoticeAlertForAll] " + memberId + " 실패: " + e.getMessage());
            }
        }
    }

    /* ─── 관리자: 공지 등록 (notice_id 반환) ──────────────── */
    public int insertNotice(NoticeDTO notice) {
        if (notice == null
                || notice.getTitle() == null || notice.getTitle().isBlank()
                || notice.getContent() == null || notice.getContent().isBlank()
                || notice.getAdminId() == null || notice.getAdminId().isBlank()) {
            throw new IllegalArgumentException("공지 등록: 필수 항목이 비어 있습니다.");
        }
        org.springframework.jdbc.support.KeyHolder keyHolder =
            new org.springframework.jdbc.support.GeneratedKeyHolder();
        try {
            jdbcTemplate.update(conn -> {
                java.sql.PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO notice_tb (admin_id, title, content, pinned_yn, created_at) " +
                    "VALUES (?, ?, ?, ?, SYSDATE)",
                    new String[]{"notice_id"}
                );
                ps.setString(1, notice.getAdminId());
                ps.setString(2, notice.getTitle());
                ps.setString(3, notice.getContent());
                ps.setString(4, notice.getPinnedYn() != null ? notice.getPinnedYn() : "N");
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            return (key != null) ? key.intValue() : -1;
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.insertNotice] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    /* ─── 관리자: 공지 수정 ────────────────────────────────── */
    public void updateNotice(NoticeDTO notice) {
        if (notice == null || notice.getNoticeId() <= 0)
            throw new IllegalArgumentException("공지 수정: 유효하지 않은 noticeId.");
        if (notice.getTitle() == null || notice.getTitle().isBlank()
                || notice.getContent() == null || notice.getContent().isBlank())
            throw new IllegalArgumentException("공지 수정: 제목/내용은 필수입니다.");

        String sql = """
            UPDATE notice_tb
            SET title = ?, content = ?, pinned_yn = ?, updated_at = SYSDATE
            WHERE notice_id = ?
        """;
        try {
            int rows = jdbcTemplate.update(sql,
                notice.getTitle(), notice.getContent(),
                notice.getPinnedYn() != null ? notice.getPinnedYn() : "N",
                notice.getNoticeId());
            if (rows == 0)
                System.err.println("[NoticeRepository.updateNotice] 대상 없음: " + notice.getNoticeId());
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.updateNotice] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    /* ─── 관리자: 공지 삭제 ────────────────────────────────── */
    public void deleteNotice(int noticeId) {
        if (noticeId <= 0)
            throw new IllegalArgumentException("공지 삭제: 유효하지 않은 noticeId.");
        try {
            jdbcTemplate.update("DELETE FROM notice_favorite_tb WHERE notice_id = ?", noticeId);
            jdbcTemplate.update("DELETE FROM notice_read_tb WHERE notice_id = ?", noticeId);
            int rows = jdbcTemplate.update("DELETE FROM notice_tb WHERE notice_id = ?", noticeId);
            if (rows == 0)
                System.err.println("[NoticeRepository.deleteNotice] 대상 없음: " + noticeId);
        } catch (DataAccessException e) {
            System.err.println("[NoticeRepository.deleteNotice] DB 오류: " + e.getMessage());
            throw e;
        }
    }
}