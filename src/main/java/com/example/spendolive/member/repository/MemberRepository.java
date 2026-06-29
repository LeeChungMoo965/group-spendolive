package com.example.spendolive.member.repository;

import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberCardVO;
import com.example.spendolive.member.domain.MemberVO;

public interface MemberRepository {
    public MemberVO login(Map loginMap) throws DataAccessException;

	public void insertNewMember(MemberVO memberVO) throws DataAccessException;
	public boolean checkId(String id) throws DataAccessException;
	public boolean checkPhone(String phone) throws DataAccessException;
	public boolean checkEmail(String email) throws DataAccessException;
	public void updateOpenBankingInfo(String userId,String accessToken,String userSeqNo, String fintech_num, String bank_code, String account_num, int balance)throws DataAccessException;
	public void updateTossInfo(String userId, String card_num, String card_company, String billingkey)throws DataAccessException;
    public MemberVO selectMemberById(String id) throws DataAccessException;
    public void updateMyInfo(MemberVO memberVO, String newPassword) throws DataAccessException;
	public MemberCardVO getCardInfoByUserId(String userId) throws DataAccessException;

}
