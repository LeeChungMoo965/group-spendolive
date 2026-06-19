package com.example.spendolive.member.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
	private MemberRepository memberRepository;
    
    @Override
    public MemberVO login(Map loginMap) throws Exception {
        return memberRepository.login(loginMap);
    }

    @Override
    public void addMember(MemberVO memberVO) throws Exception {
        memberRepository.insertNewMember(memberVO);
	}

    @Override
    public String overlapped(String id) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'overlapped'");
    }

 
    
}
