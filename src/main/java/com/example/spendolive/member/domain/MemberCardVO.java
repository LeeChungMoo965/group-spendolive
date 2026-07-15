package com.example.spendolive.member.domain;

import java.sql.Date;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCardVO {
    private int cardIdx;          // card_idx (NUMBER -> int)
    private String id;            // id (VARCHAR2)
    private String billingKey;    // billing_key (VARCHAR2) 카드별 결제 api 사용 인증키
    private String cardCompany;   // card_company (VARCHAR2)ㄴ
    private String cardNumber;    // card_number (VARCHAR2)
    private LocalDateTime regDate;         // reg_date (DATE)YYYY/ mm/DD HH:MM
    private String status;   
}