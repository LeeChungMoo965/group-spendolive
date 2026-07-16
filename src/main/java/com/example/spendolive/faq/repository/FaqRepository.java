package com.example.spendolive.faq.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.faq.domain.FaqVO;

/*
 * 테이블 DDL: faq_tb (faq_id, category, question, answer CLOB, sort_order, use_yn, created_at)
 */
@Repository
public class FaqRepository {

    // faqList.jsp 화면에 보여줄 카테고리 고정 순서 (계정→지출→OTT→공지→기타)
    private static final String CATEGORY_ORDER_SQL =
            "CASE category " +
            "WHEN 'account' THEN 1 " +
            "WHEN 'expense' THEN 2 " +
            "WHEN 'ott' THEN 3 " +
            "WHEN 'notice' THEN 4 " +
            "WHEN 'etc' THEN 5 " +
            "ELSE 99 END";

    private final JdbcTemplate jdbcTemplate;

    public FaqRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private FaqVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        FaqVO faq = new FaqVO();
        faq.setFaq_id(rs.getInt("faq_id"));
        faq.setCategory(rs.getString("category"));
        faq.setQuestion(rs.getString("question"));
        faq.setAnswer(rs.getString("answer"));

        faq.setSort_order(rs.getInt("sort_order"));
        faq.setUse_yn(rs.getString("use_yn"));

        faq.setCreated_at(rs.getString("created_at"));
        return faq;
    }

    /* ─── 사용자 화면용: 노출(use_yn='Y')인 것만 ─────────────── */
    public List<FaqVO> findAllVisible() {
        String sql = """
            SELECT faq_id, category, question, answer, sort_order, use_yn,
                   TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM faq_tb
            WHERE use_yn = 'Y'
            ORDER BY """ + CATEGORY_ORDER_SQL + """
, sort_order ASC, faq_id ASC
        """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs));
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.findAllVisible] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 관리자 화면용: 숨김(N) 포함 전체 ────────────────────── */
    public List<FaqVO> findAll() {
        String sql = """
            SELECT faq_id, category, question, answer, sort_order, use_yn,
                   TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM faq_tb
            ORDER BY """ + CATEGORY_ORDER_SQL + """
, sort_order ASC, faq_id ASC
        """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs));
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.findAll] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ─── 단건 조회 (관리자 수정 폼) ──────────────────────────── */
    public FaqVO findById(int faq_id) {
        String sql = """
            SELECT faq_id, category, question, answer, sort_order, use_yn,
                   TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM faq_tb
            WHERE faq_id = ?
        """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRow(rs), faq_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.findById] DB 오류: " + e.getMessage());
            return null;
        }
    }

    /* ─── 등록 ────────────────────────────────────────────────── */
    public int insertFaq(FaqVO faq) {
        Long faq_id = jdbcTemplate.queryForObject("SELECT seq_faq.NEXTVAL FROM dual", Long.class);
        String sql = """
            INSERT INTO faq_tb(faq_id, category, question, answer, sort_order, use_yn, created_at)
            VALUES(?, ?, ?, ?, ?, ?, SYSDATE)
        """;
        try {
            jdbcTemplate.update(sql,
                    faq_id, faq.getCategory(), faq.getQuestion(), faq.getAnswer(),
                    faq.getSort_order(), faq.getUse_yn());
            return faq_id.intValue();
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.insertFaq] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    /* ─── 수정 ────────────────────────────────────────────────── */
    public void updateFaq(FaqVO faq) {
        String sql = """
            UPDATE faq_tb
            SET category = ?, question = ?, answer = ?, sort_order = ?, use_yn = ?
            WHERE faq_id = ?
        """;
        try {
            jdbcTemplate.update(sql,
                    faq.getCategory(), faq.getQuestion(), faq.getAnswer(),
                    faq.getSort_order(), faq.getUse_yn(), faq.getFaq_id());
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.updateFaq] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    /* ─── 삭제 ────────────────────────────────────────────────── */
    public void deleteFaq(int faq_id) {
        String sql = "DELETE FROM faq_tb WHERE faq_id = ?";
        try {
            jdbcTemplate.update(sql, faq_id);
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.deleteFaq] DB 오류: " + e.getMessage());
            throw e;
        }
    }
}
