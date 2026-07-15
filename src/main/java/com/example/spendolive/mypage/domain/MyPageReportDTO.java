package com.example.spendolive.mypage.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPageReportDTO {
    private Long reportId;
    private String reportedmember_id;
    private String reportedmember_name;
    private String reportedMemberNickname;
    private String reportReason;
    private String reportStatus;
    private String adminComment;
    private String created_at;
    private String processedAt;
    private String blockedYn;
}
