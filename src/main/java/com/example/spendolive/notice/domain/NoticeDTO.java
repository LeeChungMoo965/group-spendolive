package com.example.spendolive.notice.domain;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class NoticeDTO {

    private int noticeId;
    private String adminId;

    private String title;
    private String content;

    private String pinnedYn;

    private String createdAt;
    private String updatedAt;
    private String readYn;
    private String starYn;
   
}