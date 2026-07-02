package com.example.spendolive.report.service;

import com.example.spendolive.member.domain.MemberVO;

public interface ReportService {
    public void insertReport( String reported_member_id,String room_id,String chat_text, MemberVO memberInfo)throws Exception;
}
