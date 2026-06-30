package com.example.spendolive.payment.domain;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EscrowPayoutVO {
    private int escrowPayoutId;    // PK
    private int settlementId;      // 정산 마스터 외래키
    private int roomId;            // 방 번호 외래키
    private String payerId;         // 돈을 낸 파티원 ID
    private String hostId;          // 돈을 받을 방장 ID
    private int amount;            // 방장에게 지급될 순수 정산금
    private String status;          // 상태 (HELD: 보관, RELEASED: 방장지급완료, REFUNDED)
    private LocalDateTime createdAt; // 파티원 결제 시점 (금고 입고)
    private LocalDate payoutDueDate; // 방장 정산 예정일 (이용 기간 끝나고 몇 일 뒤)
    private LocalDateTime payoutAt;  // 방장 계좌로 입금이체(or 포인트) 쏴준 시점
}
//