package com.example.spendolive.inquiry.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryVO {
    private int inquiryId;
    private String id;            // member_tb.id (작성자)
    private String category;      // 계정·로그인 / 지출관리 / OTT관리 / 캘린더 / 공지·알림 / 결제·정산 / 기타
    private String inquiryType;   // 오류/버그 신고 / 기능 개선 제안 / 사용 방법 문의 / 기타 문의
    private String title;
    private String content;
    private String status;        // WAIT | DONE | REVIEW (DB 저장값, 대문자)
    private String regDate;
    private String replyContent;
    private String replyDate;

    /** faq.css의 .badge.wait/.badge.done/.badge.review 클래스와 매칭되는 소문자 코드 */
    public String getStatusCode() {
        return status == null ? "wait" : status.toLowerCase();
    }

    /** 화면에 표시할 한글 라벨 */
    public String getStatusLabel() {
        if (status == null) return "답변 대기";
        switch (status) {
            case "DONE": return "답변 완료";
            case "REVIEW": return "검토 중";
            default: return "답변 대기";
        }
    }

    /** inquiryList.jsp의 c:if hasReply 분기용 */
    public boolean isHasReply() {
        return replyContent != null && !replyContent.isBlank();
    }

    /** inquiryList.jsp 카드 미리보기 (100자 제한) */
    public String getPreview() {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}