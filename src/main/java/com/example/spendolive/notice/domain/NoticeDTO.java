package com.example.spendolive.notice.domain;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class NoticeDTO {

    private int notice_id;
    private String admin_id;

    private String title;
    private String content;

    private String pinned_yn;

    private String created_at;
    private String updated_at;
    private String read_yn;
    private String star_yn;
    

    
   
}