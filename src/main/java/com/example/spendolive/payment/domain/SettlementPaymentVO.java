package com.example.spendolive.payment.domain;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SettlementPaymentVO {
    private int paymentId;         // PK
    private int settlementId;      // 정산 마스터 외래키
    private String id;              // 파티원 ID (member_tb 외래키)
    private int baseAmount;        // 순수 분담금 (넷플릭스 1/N 가격)
    private Double feeRate;         // 수수료율 (ex: 3.00)
    private int feeAmount;         // 수수료 금액 (플랫폼 수익)
    private int totalAmount;       // 최종 토스 결제 금액 (분담금 + 수수료)
    private String paymentStatus;   // 상태 (UNPAID, PAID, CONFIRMED, REFUNDED 등)
    private LocalDateTime paidAt;       // 토스 카드 결제 완료 시점
    private LocalDateTime confirmedAt;  // 정산 확정 시점 (방장에게 돈 가도 된다고 확정)
    private LocalDateTime expiredAt;    // 안 내고 버티다 추방된 시점
    private LocalDateTime cancelledAt;  // 환불/취소 완료 시점
    private String memo;                // 환불 사유 등 비고란
}