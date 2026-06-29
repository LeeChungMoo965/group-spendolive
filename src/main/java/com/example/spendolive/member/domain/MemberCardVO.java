package com.example.spendolive.member.domain;

import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCardVO {
    private int cardIdx;          // card_idx (NUMBER -> int)
    private String id;            // id (VARCHAR2)
    private String billingKey;    // billing_key (VARCHAR2)
    private String cardCompany;   // card_company (VARCHAR2)
    private String cardNumber;    // card_number (VARCHAR2)
    private Date regDate;         // reg_date (DATE)
    private String status;   
}