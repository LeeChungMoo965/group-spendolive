package com.example.spendolive.payment.domain;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MemberBillingVO {
    private int billingId;       // PK (시퀀스)
    private String id;        // MEMBER_TB 외래키
    private String customerKey;   // 토스용 유저 고유 ID (ex: member_123)
    private String billingKey;    // 토스에서 준 진짜 빌링키 (출금 치트키)
    private String cardCompany;   // 카드사 이름 (신한, 현대 등)
    private String cardNumber;    // 마스킹된 카드번호
    private String cardType;      // 신용 / 체크 구분
    private String isActive;      // 현재 주 결제 카드 여부 (Y/N)
    private LocalDateTime createdAt; // 등록 일시
}