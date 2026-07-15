package com.example.spendolive.member.admin.service;

import java.util.List;

import com.example.spendolive.member.domain.MemberVO;

public interface AdminMemberService {
    public List<MemberVO> selectMemberAll() throws Exception;
}
