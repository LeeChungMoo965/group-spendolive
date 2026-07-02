package com.example.spendolive.report.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.report.domain.ReportVO;

@Repository
public class ReportRepositoryImpl implements ReportRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String insertReport = "INSERT INTO report_tb (room_id, reporter_id, reported_member_id, report_reason, report_status) "
                                        +"VALUES (?,?,?,?,?) ";
    @Override
    public void insertReport(ReportVO reportInfo){
        jdbcTemplate.update(insertReport, reportInfo.getRoom_id(), reportInfo.getReporter_id() ,reportInfo.getReported_member_id(), reportInfo.getReport_reason(),reportInfo.getReport_status());
    }
 



}
