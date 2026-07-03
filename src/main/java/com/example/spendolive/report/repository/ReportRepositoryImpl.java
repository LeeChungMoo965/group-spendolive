package com.example.spendolive.report.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.report.domain.ReportVO;

@Repository
public class ReportRepositoryImpl implements ReportRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String insertReport = "INSERT INTO report_tb (room_id, reporter_id, reported_member_id, report_reason, report_status)"
                                        +" VALUES (?,?,?,?, 'WAIT') ";
    private final String selectReport = "SELECT REPORT_ID,REPORTER_ID,REPORTED_MEMBER_ID,ROOM_ID,REPORT_REASON,REPORT_STATUS,ADMIN_COMMENT,created_at,processed_at "
                                        +"from report_tb ";                                    
    private final String updateComment = "UPDATE report_tb SET admin_comment =? AND processed_at =SYSDATE WHERE report_id=? ";
    @Override
    public void insertReport(ReportVO reportInfo){
        jdbcTemplate.update(insertReport, reportInfo.getRoom_id(), reportInfo.getReporter_id() ,reportInfo.getReported_member_id(), reportInfo.getReport_reason());
    }
    @Override
    public List<ReportVO> selectReport(){
        try {
            return jdbcTemplate.query(selectReport, (rs, rowNum) -> {
            ReportVO report = new ReportVO();
            report.setAdmin_comment(rs.getString("admin_comment"));
            report.setCreated_at(rs.getObject("created_at", LocalDateTime.class));
            report.setProcessed_at(rs.getObject("processed_at", LocalDateTime.class));
            report.setReport_id(rs.getLong("Report_id"));
            report.setReport_reason(rs.getString("report_reason"));
            report.setReport_status(rs.getString("report_status"));
            report.setReported_member_id(rs.getString("reported_member_id"));
            report.setReporter_id(rs.getString("reporter_id"));
            report.setRoom_id(rs.getInt("room_id"));

            return report;
            });
        }catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
            return null; 
        }
    }
    @Override
    public void updateComment(String comment, Long report_id){
        jdbcTemplate.update(updateComment, comment, report_id);
    }


}
