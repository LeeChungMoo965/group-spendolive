package com.example.spendolive.notice.domain;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class NoticeDTO {

    private int noticeId;           // 공지 고유 번호(PK)
    private String adminId;         // 작성한 관리자 ID
    private String title;           // 공지 제목
    private String content;         // 공지 내용
    private String pinnedYn;        // 상단 공지 여부
    private String createdAt;       // 등록일
    private String updatedAt;       // 최종 수정일
    private String readYn;          // notice_tb 자체 컬럼 X, notice_read_tb 조인 결과 | 로그인 회원이 읽었으면 Y, 안 읽었으면 N
    private String starYn;          // notice_tb 자체 컬럼 X, notice_favorite_tb 조인 결과 | 로그인 회원이 찜했으면 Y, 안 했으면 N
    

    
   
}