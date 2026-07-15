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
    private int account_idx;        // account_idx (NUMBER -> int 또는 Integer)
    private String account_holder_nam;
    private String id;             // id (VARCHAR2)
    private String bank_code;       // bank_code (VARCHAR2)
    private String account_number;  // account_number (VARCHAR2)
    private String fintech_use_num;  // fintech_use_num (VARCHAR2)
    private int balance;           // balance (NUMBER)
    private String open_bank_token;  // open_bank_token (VARCHAR2)
    private String open_bank_user_seq;// open_bank_user_seq (VARCHAR2)
    private  LocalDateTime reg_date;          // reg_date (DATE)
}