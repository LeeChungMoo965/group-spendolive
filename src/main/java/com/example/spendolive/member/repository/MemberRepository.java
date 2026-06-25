package com.example.spendolive.member.repository;

import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberVO;

public interface MemberRepository {
    public MemberVO login(Map loginMap) throws DataAccessException;
    public void insertNewMember(MemberVO memberVO) throws DataAccessException;
    public boolean checkId(String id) throws DataAccessException;
    public boolean checkPhone(String phone) throws DataAccessException;
    public boolean checkEmail(String email) throws DataAccessException;

    // [develop 반영] 오픈뱅킹 토큰 + 사용자번호 + 핀테크 이용번호 저장
    public void updateOpenBankingInfo(String userId, String accessToken, String userSeqNo, String fintechNum) throws DataAccessException;

    // [마이페이지 반영] 로그인한 회원 정보 조회/수정
    public MemberVO selectMemberById(String id) throws DataAccessException;
    public void updateMyInfo(MemberVO memberVO, String newPassword) throws DataAccessException;
}
