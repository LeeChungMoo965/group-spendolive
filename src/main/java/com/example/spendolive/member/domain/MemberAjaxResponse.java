package com.example.spendolive.member.domain;

import lombok.Data;

/**
 * Ajax 결제 요청 결과를 JSON으로 전달하기 위한 응답 객체입니다.
 */
@Data
public class MemberAjaxResponse {
    private final boolean success;
    private final String code;
    private final String message;
    private final String memberStatus;
    private final String id;
    private final String redirectUrl;

    public MemberAjaxResponse(
            boolean success,
            String code,
            String message,
            String memberStatus,
            String id,
            String redirectUrl) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.memberStatus = memberStatus;
        this.id = id;
        this.redirectUrl = redirectUrl;
    }
}
