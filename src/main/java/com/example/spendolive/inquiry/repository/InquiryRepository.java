package com.example.spendolive.inquiry.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.inquiry.domain.InquiryVO;

@Repository
public class InquiryRepository {

    // ────────────────────────────────────────────────────────────
    // SQL 정의
    // ────────────────────────────────────────────────────────────

    // findByMemberId / findById / findAllForAdmin에서 공통으로 쓰는 SELECT 컬럼 목록
    private static final String SELECT_COLUMNS = """
            inquiry_id, id, category, inquiry_type, title, content, status, reply_content,
            TO_CHAR(reg_date, 'YYYY.MM.DD') AS reg_date,
            TO_CHAR(reply_date, 'YYYY.MM.DD') AS reply_date
        """;

    // 등록
    private static final String INSERT_SQL = """
            INSERT INTO inquiry_tb(inquiry_id, id, category, inquiry_type, title, content, status, reg_date)
            VALUES(?, ?, ?, ?, ?, ?, 'WAIT', SYSDATE)
        """;

    // 단건 조회
    private static final String FIND_BY_ID_SQL = """
            SELECT """ + SELECT_COLUMNS + """
            FROM inquiry_tb
            WHERE inquiry_id = ?
        """;

    // 관리자: 답변 등록/수정 (상태도 같이 변경)
    private static final String REPLY_SQL = """
            UPDATE inquiry_tb
            SET reply_content = ?, reply_date = SYSDATE, status = ?
            WHERE inquiry_id = ?
        """;

    // ────────────────────────────────────────────────────────────
    // 필드 / 생성자 / RowMapper
    // ────────────────────────────────────────────────────────────

    private final JdbcTemplate jdbcTemplate;

    public InquiryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* ─── 공통 RowMapper ──────────────────────────────────── */
    private InquiryVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        InquiryVO inquiry = new InquiryVO();
        inquiry.setInquiry_id(rs.getInt("inquiry_id"));
        inquiry.setId(rs.getString("id"));
        inquiry.setCategory(rs.getString("category"));
        inquiry.setInquiry_type(rs.getString("inquiry_type"));
        inquiry.setTitle(rs.getString("title"));
        inquiry.setContent(rs.getString("content"));
        inquiry.setStatus(rs.getString("status"));
        inquiry.setReg_date(rs.getString("reg_date"));
        inquiry.setReply_content(rs.getString("reply_content"));
        inquiry.setReply_date(rs.getString("reply_date"));
        return inquiry;
    }

    /** 관리자 화면용: mapRow + 작성자 닉네임(member_tb 조인 결과) 포함 */
    private InquiryVO mapRowWithWriter(java.sql.ResultSet rs) throws java.sql.SQLException {
        InquiryVO inquiry = mapRow(rs);
        inquiry.setWriter_nickname(rs.getString("writer_nickname"));
        return inquiry;
    }

    // ────────────────────────────────────────────────────────────
    // 조회 / 등록 / 수정 메서드
    // ────────────────────────────────────────────────────────────

    /**
     * inquiry_tb에 INSERT하고, 생성된 inquiry_id를 반환한다.
     * (첨부파일을 inquiry_file_tb에 연결하려면 이 inquiry_id가 필요하기 때문에
     *  시퀀스 값을 먼저 뽑아서 INSERT문에 명시적으로 넣는 방식을 사용)
     */
    public int insertInquiry(InquiryVO inquiry) {
        Long inquiryId = jdbcTemplate.queryForObject("SELECT inquiry_seq.NEXTVAL FROM dual", Long.class);
        try {
            jdbcTemplate.update(INSERT_SQL,
                    inquiryId, inquiry.getId(), inquiry.getCategory(), inquiry.getInquiryType(),
                    inquiry.getTitle(), inquiry.getContent());
            return inquiry_id.intValue();
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.insertInquiry] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 내 문의 목록 (페이지네이션 + 상태 필터)
     * @param status null 또는 blank면 전체 조회, 아니면 해당 상태(WAIT/DONE/REVIEW)만 조회
     * (status 유무에 따라 WHERE 절이 달라져서 상수 SQL로 못 빼고 여기서 조립함)
     */
    public List<InquiryVO> findBymember_id(String id, String status, int offset, int limit) {
        if (id == null || id.isBlank()) return Collections.emptyList();

        String sql = "SELECT " + SELECT_COLUMNS + " FROM inquiry_tb WHERE id = ?"
                + (status != null && !status.isBlank() ? " AND status = ? " : "")
                + " ORDER BY inquiry_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try {
            if (status != null && !status.isBlank()) {
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id, status, offset, limit);
            }
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id, offset, limit);
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.findBymember_id] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 내 문의 총 개수 (상태 필터) ─────────────────────── */
    public int countBymember_id(String id, String status) {
        if (id == null || id.isBlank()) return 0;

        String sql = "SELECT COUNT(*) FROM inquiry_tb WHERE id = ?"
                + (status != null && !status.isBlank() ? " AND status = ?" : "");
        try {
            Integer count = (status != null && !status.isBlank())
                    ? jdbcTemplate.queryForObject(sql, Integer.class, id, status)
                    : jdbcTemplate.queryForObject(sql, Integer.class, id);
            return (count != null) ? count : 0;
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.countBymember_id] DB 오류: " + e.getMessage());
            return 0;
        }
    }

    /* ─── 단건 조회 ───────────────────────────────────────── */
    public InquiryVO findById(int inquiryId) {
        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID_SQL, (rs, rowNum) -> mapRow(rs), inquiryId);
        } catch (EmptyResultDataAccessException e) {
            System.err.println("[InquiryRepository.findById] inquiry_id=" + inquiry_id + " 존재하지 않음");
            return null;
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.findById] DB 오류: " + e.getMessage());
            return null;
        }
    }

    /**
     * 관리자용: 전체 회원 문의 목록 (페이지네이션 + 상태 필터)
     * (status 유무에 따라 WHERE 절이 달라져서 상수 SQL로 못 빼고 여기서 조립함)
     */
    public List<InquiryVO> findAllForAdmin(String status, int offset, int limit) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM inquiry_tb"
                + (status != null && !status.isBlank() ? " WHERE status = ? " : "")
                + " ORDER BY inquiry_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try {
            if (status != null && !status.isBlank()) {
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), status, offset, limit);
            }
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), offset, limit);
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.findAllForAdmin] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 관리자용: 전체 문의 개수 (상태 필터) ────────────────── */
    public int countAllForAdmin(String status) {
        String sql = "SELECT COUNT(*) FROM inquiry_tb"
                + (status != null && !status.isBlank() ? " WHERE status = ?" : "");
        try {
            Integer count = (status != null && !status.isBlank())
                    ? jdbcTemplate.queryForObject(sql, Integer.class, status)
                    : jdbcTemplate.queryForObject(sql, Integer.class);
            return (count != null) ? count : 0;
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.countAllForAdmin] DB 오류: " + e.getMessage());
            return 0;
        }
    }

    /* ─── 관리자용: 답변 등록/수정 (상태도 같이 변경) ─────────── */
    public void replyToInquiry(int inquiryId, String replyContent, String status) {
        try {
            jdbcTemplate.update(REPLY_SQL, replyContent, status, inquiryId);
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.replyToInquiry] DB 오류: " + e.getMessage());
            throw e;
        }
    }
}