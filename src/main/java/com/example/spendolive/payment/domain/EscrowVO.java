package com.example.spendolive.payment.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 13. 홈페이지 보관금 테이블 도메인 객체
 */
@Getter
@Setter
@ToString
public class EscrowVO {

    private int escrow_id;              // 보관금 고유 ID (PK, 시퀀스 자동 생성)
    private int settlement_id;          // 월별 정산 고유 ID (FK)
    private int room_id;                // OTT 매칭방 고유 ID (FK)
    private String payer_id;            // 돈을 낸 사람(파티원) 아이디 (FK)
    private String host_id;             // 돈을 받을 사람(방장) 아이디 (FK)
    private int amount;                 // 플랫폼에 묶여있는 안전 보관 금액
    
    // 상태값: HELD(보관 중), RELEASED(방장에게 지급 완료), REFUNDED(환불됨), CANCELLED(취소됨)
    private String escrow_status;       // 에스크로 보관 상태 (기본값: 'HELD')
    
    private LocalDateTime paid_at;      // 에스크로에 돈이 입금되어 묶인 일시 (기본값: SYSDATE)
    private LocalDate release_due_date; // 방장에게 안전하게 인도될 예정일
    private LocalDateTime released_at;  // 실제로 방장에게 보관금이 풀려 지급된 일시
    private LocalDateTime refunded_at;  // 문제가 생겨 파티원에게 다시 환불 처리된 일시
}