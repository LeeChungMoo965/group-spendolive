package com.example.spendolive.faq.domain;

import lombok.Getter;
import lombok.Setter;

/*
 * 테이블 DDL: faq_tb (faq_id, category, question, answer CLOB, sort_order, use_yn, created_at)
 */
@Getter
@Setter
public class FaqVO {
    private int faq_id;             // FAQ 게시글 고유 번호(PK). 관리자가 수정/삭제/순서이동 시 대상 FAQ를 지정하는 데 쓰임
    private String category;       // 화면에서 카테고리별 그룹핑·필터링에 쓰임(account/expense/ott/notice/etc)
    private String question;       //FAQ 질문 텍스트
    private String answer;         // FAQ 답변 텍스트 
    private int sort_order;         // 관리자가 지정하는 노출 순서 (작을수록 먼저)
    private String use_yn;          // Y/N — 사용자 화면 노출 여부
    private String created_at;      // FAQ 등록일

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
