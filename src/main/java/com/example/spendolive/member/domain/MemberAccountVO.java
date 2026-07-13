package com.example.spendolive.member.domain;

import java.sql.Date;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberAccountVO {
    private int accountIdx;        // account_idx (NUMBER -> int 또는 Integer)
    private String account_holder_nam;
    private String id;             // id (VARCHAR2)
    private String bankCode;       // bank_code (VARCHAR2)
    private String accountNumber;  // account_number (VARCHAR2)
    private String fintechUseNum;  // fintech_use_num (VARCHAR2)
    private int balance;           // balance (NUMBER)
    private String openBankToken;  // open_bank_token (VARCHAR2)
    private String openBankUserSeq;// open_bank_user_seq (VARCHAR2)
    private  LocalDateTime regDate;          // reg_date (DATE)
}