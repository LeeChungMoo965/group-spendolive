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
    private final String signup = "INSERT INTO member_tb(id, email, password, member_name, nickname, phone, login_type, role, status) values(?,?,?,?,?,?,?,?,?)";
    private final String login = "SELECT member_id, id, email, password, member_name, nickname, phone, login_type, role, status TO_CHAR(created_at, 'YYYY-MM-DD'), status TO_CHAR(update_at, 'YYYY-MM-DD') from member_tb where id =? and password =? ";
    public MemberRepositoryImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void insertNewMember(MemberVO member){
        jdbcTemplate.update(signup, member.getId(), member.getEmail(), member.getPassword(), member.getMember_name(),member.getNickname(),member.getPhone(),member.getLogin_type(),member.getRole(),member.getStatus());
    }

    @Override
    public MemberVO login(Map loginMap) throws DataAccessException {
        String id = (String) loginMap.get("id");
        String password = (String) loginMap.get("password");
        return jdbcTemplate.queryForObject(login, (rs, rowNum) -> {
        MemberVO member = new MemberVO();
        member.setMember_id(rs.getInt("member_id"));
        member.setId(rs.getString("id"));
        member.setEmail(rs.getString("email"));
        member.setLogin_type(rs.getString("login_type"));
        member.setMember_name(rs.getString("member_name"));
        member.setNickname(rs.getString("nickname"));
        member.setPassword(rs.getString("password"));
        member.setPhone(rs.getString("phone"));
        member.setRole(rs.getString("role"));
        member.setStatus(rs.getString("status"));
        member.setCreated_at(rs.getString("created"));
        member.setUpdate_at(rs.getString("update"));
        return member;
        },id, password);
    }
    @Override
    public String selectOverlappedID(String id) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'selectOverlappedID'");
    }
}
