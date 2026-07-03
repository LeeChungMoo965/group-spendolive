package com.example.spendolive.inquiry.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.inquiry.domain.InquiryVO;

/*
 * 테이블 DDL: 07_inquiry.sql 참고 (inquiry_tb, inquiry_file_tb, 시퀀스, 샘플 데이터 포함)
 */
@Repository
public class InquiryRepository {

    private final JdbcTemplate jdbcTemplate;

    public InquiryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* ─── 공통 RowMapper ──────────────────────────────────── */
    private InquiryVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        InquiryVO inquiry = new InquiryVO();
        inquiry.setInquiryId(rs.getInt("inquiry_id"));
        inquiry.setId(rs.getString("id"));
        inquiry.setCategory(rs.getString("category"));
        inquiry.setInquiryType(rs.getString("inquiry_type"));
        inquiry.setTitle(rs.getString("title"));
        inquiry.setContent(rs.getString("content"));
        inquiry.setStatus(rs.getString("status"));
        inquiry.setRegDate(rs.getString("reg_date"));
        inquiry.setReplyContent(rs.getString("reply_content"));
        inquiry.setReplyDate(rs.getString("reply_date"));
        return inquiry;
    }

    /* ─── 등록 ────────────────────────────────────────────── */
    public void insertInquiry(InquiryVO inquiry) {
        String sql = """
            INSERT INTO inquiry_tb(inquiry_id, id, category, inquiry_type, title, content, status, reg_date)
            VALUES(inquiry_seq.NEXTVAL, ?, ?, ?, ?, ?, 'WAIT', SYSDATE)
        """;
        try {
            jdbcTemplate.update(sql,
                    inquiry.getId(), inquiry.getCategory(), inquiry.getInquiryType(),
                    inquiry.getTitle(), inquiry.getContent());
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.insertInquiry] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    /* ─── 내 문의 목록 (페이지네이션) ─────────────────────── */
    public List<InquiryVO> findByMemberId(String id, int offset, int limit) {
        if (id == null || id.isBlank()) return Collections.emptyList();

        String sql = """
            SELECT inquiry_id, id, category, inquiry_type, title, content, status, reply_content,
                   TO_CHAR(reg_date, 'YYYY.MM.DD') AS reg_date,
                   TO_CHAR(reply_date, 'YYYY.MM.DD') AS reply_date
            FROM inquiry_tb
            WHERE id = ?
            ORDER BY inquiry_id DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id, offset, limit);
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.findByMemberId] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 내 문의 총 개수 ─────────────────────────────────── */
    public int countByMemberId(String id) {
        if (id == null || id.isBlank()) return 0;

        String sql = "SELECT COUNT(*) FROM inquiry_tb WHERE id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
            return (count != null) ? count : 0;
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.countByMemberId] DB 오류: " + e.getMessage());
            return 0;
        }
    }

    /* ─── 단건 조회 ───────────────────────────────────────── */
    public InquiryVO findById(int inquiryId) {
        String sql = """
            SELECT inquiry_id, id, category, inquiry_type, title, content, status, reply_content,
                   TO_CHAR(reg_date, 'YYYY.MM.DD') AS reg_date,
                   TO_CHAR(reply_date, 'YYYY.MM.DD') AS reply_date
            FROM inquiry_tb
            WHERE inquiry_id = ?
        """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRow(rs), inquiryId);
        } catch (EmptyResultDataAccessException e) {
            System.err.println("[InquiryRepository.findById] inquiryId=" + inquiryId + " 존재하지 않음");
            return null;
        } catch (DataAccessException e) {
            System.err.println("[InquiryRepository.findById] DB 오류: " + e.getMessage());
            return null;
        }
    }
}