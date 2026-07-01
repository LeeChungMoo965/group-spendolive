package com.example.spendolive.report.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepositoryImpl implements ReportRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String insertReport = "INSERT INTO report_tb ";




}
