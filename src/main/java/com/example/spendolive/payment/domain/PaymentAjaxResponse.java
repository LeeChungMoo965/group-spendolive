package com.example.spendolive.payment.domain;

import lombok.Data;

/**
 * Ajax 결제 요청 결과를 JSON으로 전달하기 위한 응답 객체입니다.
 */

 @Data
public class PaymentAjaxResponse {
    private final boolean success;
    private final String code;
    private final String message;
    private final String paymentStatus;
    private final Integer roomId;
    private final String redirectUrl;

    public PaymentAjaxResponse(
            boolean success,
            String code,
            String message,
            String paymentStatus,
            Integer roomId,
            String redirectUrl) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.paymentStatus = paymentStatus;
        this.roomId = roomId;
        this.redirectUrl = redirectUrl;
    }
}
