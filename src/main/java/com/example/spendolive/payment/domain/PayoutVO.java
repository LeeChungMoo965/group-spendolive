package com.example.spendolive.payment.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * 15. 방장 지급 내역 테이블 도메인 객체
 */
@Getter
@Setter
@ToString
public class PayoutVO {

    private int payout_id;              // 정산금 정산 고유 ID (PK, 시퀀스 자동 생성)
    private int settlement_id;          // 월별 정산 고유 ID (FK)
    private int room_id;                // OTT 매칭방 고유 ID (FK)
    private String host_id;             // 정산금을 수령할 파티장 아이디 (FK)
    private int total_amount;           // 파티장에게 최종 입금해 줄 금액
    
    // 상태값: READY(지급 대기), PAID(지급 완료), FAILED(지급 실패), CANCELLED(지급 취소)
    private String payout_status;       // 방장 지급 상태 (기본값: 'READY')
    
    private LocalDateTime requested_at; // 정산금 지급 요청 일시 (기본값: SYSDATE)
    private LocalDateTime paid_at;      // 파티장 계좌로 실제 송금이 완료된 일시
    private String memo;                // 은행 이체 실패 사유 등 정산 관련 메모
}