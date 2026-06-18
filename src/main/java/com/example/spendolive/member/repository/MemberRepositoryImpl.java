package com.example.spendolive.member.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.member.domain.MemberVO;

@Repository("memberDAO")
public class MemberRepositoryImpl implements MemberRepository{
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String signup = "INSERT IN TO";
    @Override
    public void insertNewMember(MemberVO memberVO){
        
    }
    @Override
    public MemberVO login(Map loginMap) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }
    @Override
    public String selectOverlappedID(String id) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'selectOverlappedID'");
    }
}
