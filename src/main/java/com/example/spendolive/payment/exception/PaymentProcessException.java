package com.example.spendolive.payment.exception;

/**
 * 결제 가능 여부나 카드 상태처럼 사용자에게 구체적으로 알려줄 수 있는 결제 예외입니다.
 */
public class PaymentProcessException extends RuntimeException {
    private final String code;

    public PaymentProcessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public PaymentProcessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
