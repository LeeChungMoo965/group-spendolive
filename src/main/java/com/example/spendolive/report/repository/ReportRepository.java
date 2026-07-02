package com.example.spendolive.report.repository;

import com.example.spendolive.report.domain.ReportVO;

public interface ReportRepository {
    public void insertReport(ReportVO reportInfo) throws Exception;
}
