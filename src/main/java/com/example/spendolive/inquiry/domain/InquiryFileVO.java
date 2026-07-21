package com.example.spendolive.inquiry.domain;

import lombok.Getter;
import lombok.Setter;

/*
 * 테이블 DDL: 07_inquiry.sql 의 inquiry_file_tb 참고
 */
@Getter
@Setter
public class InquiryFileVO {
    private int fileId;             //첨부파일 고유 번호(PK).
    private int inquiryId;          //이 파일이 속한 문의글 번호(FK).
    private String originName;      // 업로드 당시 원본 파일명
    private String savedName;       // 서버에 저장된 파일명 (UUID + 확장자)
    private String filePath;        // 서버에 실제 저장된 경로
    private long fileSize;          // 파일 용량(byte)
    private String regDate;         // 첨부파일 등록일

     /** 확장자로 이미지 파일 여부 판별 (inquiryList.jsp에서 썸네일 vs 파일 링크 분기용) */
     public boolean isImage() {
        if (origin_name == null) return false;
        String lower = origin_name.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".gif");
    }
}

