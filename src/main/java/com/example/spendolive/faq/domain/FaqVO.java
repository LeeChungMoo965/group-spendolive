package com.example.spendolive.faq.domain;

import lombok.Getter;
import lombok.Setter;

/*
 * 테이블 DDL: faq_tb (faq_id, category, question, answer CLOB, sort_order, use_yn, created_at)
 */
@Getter
@Setter
public class FaqVO {
    private int faqId;
    private String category;   // account/expense/ott/notice/etc — faqList.jsp의 cat-btn data-cat 값과 동일하게 사용
    private String question;
    private String answer;
    private int sortOrder;     // 관리자가 지정하는 노출 순서 (작을수록 먼저)
    private String useYn;      // Y/N — 사용자 화면 노출 여부
    private String created_at;

    /** faqList.jsp의 section-label, adminFaqList.jsp 표에 쓰는 한글 카테고리명 */
    public String getCategoryLabel() {
        if (category == null) return "";
        switch (category) {
            case "account": return "계정·로그인";
            case "expense": return "지출관리";
            case "ott":     return "OTT관리";
            case "notice":  return "공지·알림";
            case "etc":     return "기타";
            default:        return category;
        }
    }
}
