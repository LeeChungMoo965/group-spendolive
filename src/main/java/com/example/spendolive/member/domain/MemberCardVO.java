package com.example.spendolive.member.domain;

import java.time.LocalDateTime;

/**
 * 회원 카드 정보.
 * 카드 이름 기능을 추가하면서 Lombok 생성 메서드에만 의존하지 않도록
 * 명시적인 getter/setter를 사용한다.
 */
public class MemberCardVO {
    private int card_idx;
    private String id;
    private String billing_key;
    private String card_company;
    private String card_number;
    private String card_name;
    private LocalDateTime reg_date;
    private String status;

    public MemberCardVO() {
    }

    public MemberCardVO(int card_idx, String id, String billing_key, String card_company,
                        String card_number, String card_name, LocalDateTime reg_date, String status) {
        this.card_idx = card_idx;
        this.id = id;
        this.billing_key = billing_key;
        this.card_company = card_company;
        this.card_number = card_number;
        this.card_name = card_name;
        this.reg_date = reg_date;
        this.status = status;
    }

    public int getCard_idx() { return card_idx; }
    public void setCard_idx(int card_idx) { this.card_idx = card_idx; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBilling_key() { return billing_key; }
    public void setBilling_key(String billing_key) { this.billing_key = billing_key; }
    public String getCard_company() { return card_company; }
    public void setCard_company(String card_company) { this.card_company = card_company; }
    public String getCard_number() { return card_number; }
    public void setCard_number(String card_number) { this.card_number = card_number; }
    public String getCard_name() { return card_name; }
    public void setCard_name(String card_name) { this.card_name = card_name; }
    public LocalDateTime getReg_date() { return reg_date; }
    public void setReg_date(LocalDateTime reg_date) { this.reg_date = reg_date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
