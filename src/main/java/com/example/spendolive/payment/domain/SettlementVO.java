package com.example.spendolive.payment.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 10. 월별 정산 테이블 VO
 */
@Getter
@Setter
@ToString
public class SettlementVO {

    private int settlement_id;          // 정산 고유 ID (PK, 시퀀스 자동 생성)
    private int room_id;                // OTT 매칭방 고유 ID (FK)
    private String settlement_month;    // 정산 대상 년월 (형식: YYYY-MM)
    private int total_price;            // 해당 월 방 총 금액
    private int total_fee;              // 플랫폼 총 수수료 (기본값: 0)
    private int total_pay_amount;       // 수수료 포함 최종 정산 총 금액 (기본값: 0)
    private LocalDate due_date;         // 최종 입금/정산 마감일
    
    // [방금 만든 OTT SQL 반영 컬럼]
    private LocalDate payment_start_date; // 결제 가능 시작일 (예: 6월 결제일)
    private LocalDate payment_close_date; // 결제 마감일 (예: 7월 결제일 5일 전)
    private LocalDate service_start_date; // 해당 이용분 시작일 (예: 7월 결제일)
    private LocalDate service_end_date;   // 해당 이용분 종료일
    private LocalDate replace_start_date; // 미결제자 추방 후 대체 파티원 모집 시작일
    private LocalDate replace_end_date;   // 대체 파티원 모집 종료일
    private LocalDateTime closed_at;      // 정산 마감 일시
    
    // 상태값: READY, REQUESTED, DONE, PAYMENT_OPEN, REPLACE_RECRUITING, CONFIRMED, CANCELLED, CLOSED
    private String status;              // 정산 진행 상태 (기본값: 'READY')
    private LocalDateTime created_at;   // 정산 레코드 생성 일시 (기본값: SYSDATE)
}