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
    private int card_idx;          // card_idx (NUMBER -> int)
    private String id;            // id (VARCHAR2)
    private String billing_key;    // billing_key (VARCHAR2)
    private String card_company;   // card_company (VARCHAR2)
    private String card_number;    // card_number (VARCHAR2)
    private LocalDateTime reg_date;         // reg_date (DATE)
    private String status;   
}