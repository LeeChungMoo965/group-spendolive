package com.example.spendolive.member.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPageReportDTO {
    private Long reportId;
    private String reportedMemberId;
    private String reportedMemberName;
    private String reportedMemberNickname;
    private String reportReason;
    private String reportStatus;
    private String adminComment;
    private String createdAt;
    private String processedAt;
    private String blockedYn;
}
