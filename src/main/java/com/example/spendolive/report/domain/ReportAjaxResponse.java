package com.example.spendolive.report.domain;

import lombok.Data;

/**
 * Ajax 결제 요청 결과를 JSON으로 전달하기 위한 응답 객체입니다.
 */

 @Data
public class ReportAjaxResponse {
    private final boolean success;
    private final String code;
    private final String message;
    private final String reportStatus;
    private final Integer roomId;
    private final String redirectUrl;

    public ReportAjaxResponse(
            boolean success,
            String code,
            String message,
            String reportStatus,
            Integer roomId,
            String redirectUrl) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.reportStatus = reportStatus;
        this.roomId = roomId;
        this.redirectUrl = redirectUrl;
    }
}
