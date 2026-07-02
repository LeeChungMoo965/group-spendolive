package com.example.spendolive.report.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WarningVO {
    private Long warning_id;            // NUMBER -> Long
    private String member_id;           // VARCHAR2(20)
    private Long report_id;             // NUMBER -> Long (Null 허용)
    private String warning_reason;      // VARCHAR2(500)
    private Integer warning_level;      // NUMBER -> Integer (CHECK 1, 2, 3)
    private Integer penalty_days;       // NUMBER -> Integer
    private String permanent_yn;        // CHAR(1) DEFAULT 'N'
    private LocalDateTime created_at;   // DATE -> LocalDateTime
}