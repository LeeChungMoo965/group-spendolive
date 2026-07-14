package com.example.spendolive.member.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;

@Service
public class AdminMemberServiceImpl implements AdminMemberService{
    @Autowired
    private MemberRepository memberRepository;
    @Override
    @Transactional
    public List<MemberVO> selectMemberAll() throws Exception{
        return memberRepository.selectMemberAll();
    }
}
