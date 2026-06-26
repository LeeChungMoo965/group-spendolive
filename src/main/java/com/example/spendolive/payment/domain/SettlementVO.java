package com.example.spendolive.payment.domain;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SettlementVO {
    private int settlementId;       // PK
    private int roomId;             // 방 번호 외래키
    private String settlementMonth;  // 정산 월 (YYYY-MM)
    private int totalPrice;         // 방 전체 가격
    private int totalFee;           // 플랫폼 수수료 총합
    private int totalPayAmount;     // 파티원들이 내야 할 총합 (수수료 포함)
    private LocalDate dueDate;       // 최종 정산 마감일
    
    // OTT 스케줄링 관련 날짜들
    private LocalDate paymentStartDate; // 결제 가능 시작일
    private LocalDate paymentCloseDate; // 결제 마감일
    private LocalDate serviceStartDate; // 이용 시작일
    private LocalDate serviceEndDate;   // 이용 종료일
    private LocalDate replaceStartDate; // 대체 모집 시작일
    private LocalDate replaceEndDate;   // 대체 모집 종료일
    
    private LocalDate closedAt;         // 정산 종료 일시
    private String status;              // 상태 (READY, PAYMENT_OPEN, DONE 등)
    private LocalDateTime createdAt;    // 생성 일시
}