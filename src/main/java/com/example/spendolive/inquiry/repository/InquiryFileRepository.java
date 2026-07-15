package com.example.spendolive.inquiry.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.inquiry.domain.InquiryFileVO;

/*
 * 테이블 DDL: 07_inquiry.sql 참고 (inquiry_file_tb, inquiry_file_seq)
 */
@Repository
public class InquiryFileRepository {

    private final JdbcTemplate jdbcTemplate;

    public InquiryFileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private InquiryFileVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        InquiryFileVO file = new InquiryFileVO();
        file.setFile_id(rs.getInt("file_id"));
        file.setInquiry_id(rs.getInt("inquiry_id"));
        file.setOrigin_name(rs.getString("origin_name"));
        file.setSaved_name(rs.getString("saved_name"));
        file.setFile_path(rs.getString("file_path"));
        file.setFile_size(rs.getLong("file_size"));
        file.setReg_date(rs.getString("reg_date"));
        return file;
    }

    /* ─── 첨부파일 등록 ───────────────────────────────────── */
    public void insertFile(InquiryFileVO file) {
        String sql = """
            INSERT INTO inquiry_file_tb(file_id, inquiry_id, origin_name, saved_name, file_path, file_size, reg_date)
            VALUES(inquiry_file_seq.NEXTVAL, ?, ?, ?, ?, ?, SYSDATE)
        """;
        try {
            jdbcTemplate.update(sql,
                    file.getInquiry_id(), file.getOrigin_name(), file.getSaved_name(),
                    file.getFile_path(), file.getFile_size());
        } catch (DataAccessException e) {
            System.err.println("[InquiryFileRepository.insertFile] DB 오류: " + e.getMessage());
            throw e;
        }
    }

    /* ─── 특정 문의의 첨부파일 목록 ───────────────────────── */
    public List<InquiryFileVO> findByInquiryId(int inquiry_id) {
        String sql = """
            SELECT file_id, inquiry_id, origin_name, saved_name, file_path, file_size,
                   TO_CHAR(reg_date, 'YYYY.MM.DD') AS reg_date
            FROM inquiry_file_tb
            WHERE inquiry_id = ?
            ORDER BY file_id
        """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), inquiry_id);
        } catch (DataAccessException e) {
            System.err.println("[InquiryFileRepository.findByInquiryId] DB 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }
/* ─── 첨부파일 단건 조회 (다운로드/미리보기용) ────────── */
public InquiryFileVO findById(int file_id) {
    String sql = """
        SELECT file_id, inquiry_id, origin_name, saved_name, file_path, file_size,
               TO_CHAR(reg_date, 'YYYY.MM.DD') AS reg_date
        FROM inquiry_file_tb
        WHERE file_id = ?
    """;
    try {
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRow(rs), file_id);
    } catch (org.springframework.dao.EmptyResultDataAccessException e) {
        return null;
    } catch (DataAccessException e) {
        System.err.println("[InquiryFileRepository.findById] DB 오류: " + e.getMessage());
        return null;
    }
}
}