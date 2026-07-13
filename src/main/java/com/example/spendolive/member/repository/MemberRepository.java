package com.example.spendolive.member.repository;

import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberAccountVO;
import com.example.spendolive.member.domain.MemberCardVO;
import com.example.spendolive.member.domain.MemberVO;

public interface MemberRepository {
    public MemberVO login(Map loginMap) throws DataAccessException;

	public void insertNewMember(MemberVO memberVO) throws DataAccessException;
	public boolean checkId(String id) throws DataAccessException;
	public boolean checkPhone(String phone) throws DataAccessException;
	public boolean checkEmail(String email) throws DataAccessException;
	public void updateOpenBankingInfo(String userId,String accessToken,String userSeqNo, String fintech_num, String bank_code, String account_num, int balance, String account_holder_nam)throws DataAccessException;
	public void updateTossInfo(String userId, String card_num, String card_company, String billingkey)throws DataAccessException;
    public MemberVO selectMemberById(String id) throws DataAccessException;
    public void updateMyInfo(MemberVO memberVO, String newPassword) throws DataAccessException;
	public MemberCardVO getCardInfoByUserId(String userId) throws DataAccessException;
	public void updateMember_account_status(String id)throws DataAccessException;
	public void updateMember_card_status(String id)throws DataAccessException;
	public MemberCardVO selectCardById(String userId)throws DataAccessException;
    public MemberAccountVO selectAccountById(String userId)throws DataAccessException;
	public void updateWarning(String userId, int count)throws DataAccessException;

	/* =========================================================
	   [추가 기능] 아이디/비밀번호 찾기용 Repository 메서드
	   ---------------------------------------------------------
	   member_tb를 직접 조회/수정하는 구간이다.
	   휴대폰 번호는 하이픈 유무와 상관없이 비교하기 위해 구현체에서 숫자만 남겨 비교한다.
	   ========================================================= */
	// 휴대폰 번호로 ACTIVE 회원의 id 조회
	public String findIdByPhone(String phone) throws DataAccessException;

	// 입력한 id가 ACTIVE 회원으로 존재하는지 확인
	public boolean existsActiveId(String id) throws DataAccessException;

	// id + 휴대폰 번호가 같은 ACTIVE 회원 정보인지 확인
	public boolean existsActiveMemberByIdAndPhone(String id, String phone) throws DataAccessException;

	// 인증 완료 후 비밀번호 변경
	public void updatePasswordById(String id, String newPassword) throws DataAccessException;

}
