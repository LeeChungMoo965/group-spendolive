package com.example.spendolive.member.service;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.example.spendolive.member.domain.MemberVO;

public interface MemberService {
    public MemberVO login(Map loginMap) throws Exception;
    public void addMember(MemberVO memberVO) throws Exception;
    public String overlapped(String id) throws Exception;
    public String sendVerificationEmail(String toEmail) throws Exception;
    public String sendSmsVerification(String toNumber) throws Exception;
    public boolean checkId(String id) throws Exception;
    public Map<String, String> getKakaoUserInfo(String code) throws Exception;
    public boolean checkEmail(String email) throws Exception;
    public boolean checkPhone(String phone) throws Exception;

    // [develop 반영] 오픈뱅킹 토큰 발급 후 계좌 목록까지 조회하는 방식 유지
    public void registerOpenBankingToken(String code, String userId, HttpHeaders headers, ResponseEntity<Map> response) throws Exception;

    // [마이페이지 반영] 로그인한 회원 정보 조회/수정
    public MemberVO getMemberById(String id) throws Exception;
    public void updateMyInfo(MemberVO memberVO, String newPassword) throws Exception;
}
