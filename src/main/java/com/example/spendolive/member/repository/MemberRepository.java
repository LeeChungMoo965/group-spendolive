package com.example.spendolive.member.repository;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberVO;

public interface MemberRepository {
    public MemberVO login(Map loginMap) throws DataAccessException;
	public void insertNewMember(MemberVO memberVO) throws DataAccessException;
	public String selectOverlappedID(String id) throws DataAccessException;
}
