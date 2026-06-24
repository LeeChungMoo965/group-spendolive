package com.example.spendolive.payment.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * 11. 팀원별 입금 상태 테이블 도메인 객체
 */
@Getter
@Setter
@ToString
public class SettlementPaymentVO {

    private int payment_id;             // 입금 내역 고유 ID (PK, 시퀀스 자동 생성)
    private int settlement_id;          // 월별 정산 고유 ID (FK)
    private String id;                  // 회원 아이디 (FK, member_tb)
    private int base_amount;            // 수수료 미포함 순수 이용 금액
    private double fee_rate;            // 적용된 수수료율 (기본값: 3.00%)
    private int fee_amount;             // 계산된 수수료 금액 (기본값: 0)
    private int total_amount;           // 회원이 최종 결제해야 할 총 금액 (이용료 + 수수료)
    
    // 상태값: UNPAID, PAID, CONFIRMED, EXPIRED, CANCELLED, REFUND_REQUESTED, REFUNDED
    private String payment_status;      // 팀원의 입금 상태 (기본값: 'UNPAID')
    
    private LocalDateTime paid_at;      // 팀원이 실제 돈을 보낸 일시
    private LocalDateTime confirmed_at; // 파티장 혹은 시스템이 입금 확인을 확정한 일시
    private LocalDateTime expired_at;    // 결제 마감일까지 미결제되어 만료 처리된 일시
    private LocalDateTime cancelled_at;  // 결제 취소 처리된 일시
    private String memo;                // 정산/입금 관련 특이사항 메모
}