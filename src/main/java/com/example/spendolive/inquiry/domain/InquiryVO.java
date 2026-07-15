package com.example.spendolive.inquiry.domain;

import java.util.ArrayList;
import java.util.List;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryVO {
    private int inquiry_id;
    private String id;            // member_tb.id (작성자)
    private String category;      // 계정·로그인 / 지출관리 / OTT관리 / 캘린더 / 공지·알림 / 결제·정산 / 기타
    private String inquiry_type;   // 오류/버그 신고 / 기능 개선 제안 / 사용 방법 문의 / 기타 문의
    private String title;
    private String content;
    private String status;        // WAIT | DONE | REVIEW (DB 저장값, 대문자)
    private String reg_date;
    private String reply_content;
    private String reply_date;


    /** 관리자 화면용: 작성자 닉네임 (member_tb.nickname 조인 결과, inquiry_tb 자체 컬럼 아님).
     *  카카오 등 소셜 로그인 회원은 id가 사람이 읽기 힘든 숫자라서 별도로 채워서 보여줌. */
    private String writer_nickname;


    /** 이 문의에 달린 첨부파일 목록. inquiry_tb 자체 컬럼이 아니라
     *  InquiryService에서 inquiry_file_tb를 별도 조회해서 채워 넣는다. */
    private List<InquiryFileVO> files = new ArrayList<>();

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
        return reply_content != null && !reply_content.isBlank();
    }

    /** inquiryList.jsp 카드 미리보기 (100자 제한) */
    public String getPreview() {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}
