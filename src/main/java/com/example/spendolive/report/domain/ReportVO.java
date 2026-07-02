package com.example.spendolive.report.domain; 

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long report_id;             // NUMBER -> Long
    private String reporter_id;         // VARCHAR2(20)
    private String reported_member_id;  // VARCHAR2(20)
    private Long room_id;               // NUMBER -> Long (Null 허용이므로 래퍼 클래스 사용)
    private String report_reason;       // VARCHAR2(500)
    private String report_status;       // VARCHAR2(20) DEFAULT 'WAIT'
    private String admin_comment;       // VARCHAR2(1000)
    private LocalDateTime created_at;   // DATE -> LocalDateTime
    private LocalDateTime processed_at; // DATE -> LocalDateTime
}