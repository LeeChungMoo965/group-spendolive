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
    public void registerOpenBankingToken(String code, String userId, HttpHeaders headers, ResponseEntity<Map> response, MemberVO memberVO) throws Exception;

    // [마이페이지 반영] 로그인한 회원 정보 조회/수정
    public MemberVO getMemberById(String id) throws Exception;
    public void updateMyInfo(MemberVO memberVO, String newPassword) throws Exception;

    /* =========================================================
       [추가 기능] 로그인 페이지 아이디/비밀번호 찾기용 Service 메서드
       ---------------------------------------------------------
       Controller는 화면 요청과 세션 검증을 담당하고,
       Service는 실제 회원 조회/비밀번호 변경을 Repository로 위임한다.
       ========================================================= */
    // 휴대폰 번호로 ACTIVE 회원의 아이디를 찾는다. 아이디 찾기에서 사용.
    public String findIdByPhone(String phone) throws Exception;

    // 입력한 아이디가 ACTIVE 회원인지 확인한다. 비밀번호 찾기 1차 검증에서 사용.
    public boolean existsActiveId(String id) throws Exception;

    // 아이디와 휴대폰 번호가 같은 ACTIVE 회원 정보인지 확인한다. 비밀번호 찾기 인증번호 발송 전 사용.
    public boolean existsActiveMemberByIdAndPhone(String id, String phone) throws Exception;

    // 휴대폰 인증 완료 후 새 비밀번호로 DB를 업데이트한다.
    public void updatePasswordById(String id, String newPassword) throws Exception;
}
