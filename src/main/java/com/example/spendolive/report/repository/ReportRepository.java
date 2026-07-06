package com.example.spendolive.report.repository;

import java.util.List;

import com.example.spendolive.report.domain.ReportVO;

public interface ReportRepository {
    public void insertReport(ReportVO reportInfo) throws Exception;
    public List<ReportVO> selectReport() throws Exception;
    public void updateComment(String comment, Long report_id) throws Exception;
}
