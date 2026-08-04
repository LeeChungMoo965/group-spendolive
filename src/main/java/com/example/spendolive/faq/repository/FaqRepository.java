package com.example.spendolive.faq.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.faq.domain.FaqVO;

@Repository
public class FaqRepository {

    // faqList.jsp 화면에 보여줄 카테고리 고정 순서 (계정→지출→OTT→공지→기타)
    private static final String CATEGORY_ORDER_SQL =
            " CASE category " +
            "WHEN 'account' THEN 1 " +
            "WHEN 'expense' THEN 2 " +
            "WHEN 'ott' THEN 3 " +
            "WHEN 'notice' THEN 4 " +
            "WHEN 'etc' THEN 5 " +
            "ELSE 99 END";

    // 사용자 화면용: 노출(use_yn='Y')인 것만
    private static final String FIND_ALL_VISIBLE_SQL = """
            SELECT faq_id, category, question, answer, sort_order, use_yn,
                   TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM faq_tb
            WHERE use_yn = 'Y'
            ORDER BY """ + CATEGORY_ORDER_SQL + """
            , sort_order ASC, faq_id ASC
        """;

    // 관리자 화면용: 숨김(N) 포함 전체
    private static final String FIND_ALL_SQL = """
            SELECT faq_id, category, question, answer, sort_order, use_yn,
                   TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM faq_tb
            ORDER BY """ + CATEGORY_ORDER_SQL + """
            , sort_order ASC, faq_id ASC
        """;

    // 단건 조회 (관리자 수정 폼)
    private static final String FIND_BY_ID_SQL = """
            SELECT faq_id, category, question, answer, sort_order, use_yn,
                   TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM faq_tb
            WHERE faq_id = ?
        """;

    // 특정 카테고리의 FAQ만 (moveFaq에서 사용 — 전체 조회 대신 범위를 좁힘)
        private static final String FIND_BY_CATEGORY_SQL = """
            SELECT faq_id, category, question, answer, sort_order, use_yn,
                TO_CHAR(created_at, 'YYYY.MM.DD') AS created_at
            FROM faq_tb
            WHERE category = ?
            ORDER BY sort_order ASC, faq_id ASC
        """;


    // 등록
    private static final String INSERT_SQL = """
            INSERT INTO faq_tb(faq_id, category, question, answer, sort_order, use_yn, created_at)
            VALUES(?, ?, ?, ?, ?, ?, SYSDATE)
        """;

    // 수정 (sort_order는 여기서 안 건드림 — 순서는 moveUp/moveDown 전용)
    private static final String UPDATE_SQL = """
            UPDATE faq_tb
            SET category = ?, question = ?, answer = ?, use_yn = ?
            WHERE faq_id = ?
        """;

    // 새 FAQ가 들어갈 다음 순서 (해당 카테고리 맨 뒤)
    private static final String NEXT_SORT_ORDER_SQL =
            "SELECT NVL(MAX(sort_order), -1) + 1 FROM faq_tb WHERE category = ?";

    // 순서값만 갱신 (▲▼ 버튼 swap용)
    private static final String UPDATE_SORT_ORDER_SQL =
            "UPDATE faq_tb SET sort_order = ? WHERE faq_id = ?";

    // 삭제
    private static final String DELETE_SQL = "DELETE FROM faq_tb WHERE faq_id = ?";

    // ────────────────────────────────────────────────────────────
    // 필드 / 생성자
    // ────────────────────────────────────────────────────────────

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

    // ────────────────────────────────────────────────────────────
    // 조회 / 등록 / 수정 / 삭제 메서드
    // ────────────────────────────────────────────────────────────

    public List<FaqVO> findAllVisible() {
        try {
            return jdbcTemplate.query(FIND_ALL_VISIBLE_SQL, (rs, rowNum) -> mapRow(rs));
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.findAllVisible] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<FaqVO> findAll() {
        try {
            return jdbcTemplate.query(FIND_ALL_SQL, (rs, rowNum) -> mapRow(rs));
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.findAll] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public FaqVO findById(int faq_id) {
        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID_SQL, (rs, rowNum) -> mapRow(rs), faq_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.findById] DB 오류: " + e.getMessage());
            return null;
        }
    }

    /* ─── 특정 카테고리의 FAQ 목록 (순서이동 시 사용) ────────── */
    public List<FaqVO> findByCategory(String category) {
        try {
            return jdbcTemplate.query(FIND_BY_CATEGORY_SQL, (rs, rowNum) -> mapRow(rs), category);
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.findByCategory] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }



    public int insertFaq(FaqVO faq) {
        Long faq_id = jdbcTemplate.queryForObject("SELECT seq_faq.NEXTVAL FROM dual", Long.class);
        try {
            jdbcTemplate.update(INSERT_SQL,
                    faq_id, faq.getCategory(), faq.getQuestion(), faq.getAnswer(),
                    faq.getSort_order(), faq.getUse_yn());
            return faq_id.intValue();
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.insertFaq] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    public void updateFaq(FaqVO faq) {
        try {
            jdbcTemplate.update(UPDATE_SQL,
                    faq.getCategory(), faq.getQuestion(), faq.getAnswer(),
                    faq.getUse_yn(), faq.getFaq_id());
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.updateFaq] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    public int getNextSortOrder(String category) {
        try {
            Integer next = jdbcTemplate.queryForObject(NEXT_SORT_ORDER_SQL, Integer.class, category);
            return next != null ? next : 0;
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.getNextSortOrder] DB 오류: " + e.getMessage());
            return 0;
        }
    }

    public void updateSortOrder(int faq_id, int sortOrder) {
        try {
            jdbcTemplate.update(UPDATE_SORT_ORDER_SQL, sortOrder, faq_id);
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.updateSortOrder] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    public void deleteFaq(int faq_id) {
        try {
            jdbcTemplate.update(DELETE_SQL, faq_id);
        } catch (DataAccessException e) {
            System.err.println("[FaqRepository.deleteFaq] DB 오류: " + e.getMessage());
            throw e;
        }
    }
}