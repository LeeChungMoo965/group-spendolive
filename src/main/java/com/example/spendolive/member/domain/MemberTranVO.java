package com.example.spendolive.member.domain;


import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberTranVO {
    private int member_tran_idx;
    private int account_idx;        // account_idx (NUMBER -> int 또는 Integer)
    private String id;             // id (VARCHAR2)
    private int tran_amt;           // 계좌잔액
    private String inout_type;  // 거래내역 조회 시작 날짜
    private String tran_date;// 거래내역 조회 종료 날짜
    private String reg_date;  // 거래내역 조회 시작 날짜

    
}