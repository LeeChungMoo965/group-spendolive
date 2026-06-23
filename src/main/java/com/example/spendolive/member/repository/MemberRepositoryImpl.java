package com.example.spendolive.member.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final String signup = "INSERT INTO member_tb(id, email, password, member_name, nickname, phone,login_type ,verify_type) values(?,?,?,?,?,?,?,?)";
    
    private final String login = "SELECT member_id, id, email, password, member_name, nickname, phone, login_type, blocked_until, warning_count, role, status, verify_type, "
    + "TO_CHAR(created_at, 'YYYY-MM-DD') AS created_at, "
    + "TO_CHAR(updated_at, 'YYYY-MM-DD') AS updated_at, "
    + "TO_CHAR(last_login_at, 'YYYY-MM-DD') AS last_login_at "
    + "FROM member_tb WHERE id = ? AND password = ?";
    private final String checkId = "select decode(count(*),1, 'false', 0, 'true') as id"
    +" from member_tb where id =?";
    private final String checkEmail = "select decode(count(*),1, 'false', 0, 'true') as email"
    +" from member_tb where email =?";
    private final String checkPhone = "select decode(count(*),1, 'false', 0, 'true') as phone"
    +" from member_tb where phone =?";


    public MemberRepositoryImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void insertNewMember(MemberVO member){
        jdbcTemplate.update(signup, member.getId(), member.getEmail(), member.getPassword(), member.getMember_name(), member.getNickname(), member.getPhone(),member.getLogin_type() ,"PHONE");
    }
    @Override
    public boolean checkId(String id){
        try {
            return jdbcTemplate.queryForObject(checkId, (rs, rowNum) -> {
                String ids = rs.getString("id");
                if(ids.equals("true")){
                    return  true;}
                return false;
                } ,id);
        }catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
            return false; 
        }
    }
    @Override
    public boolean checkEmail(String email){
        try {
            return jdbcTemplate.queryForObject(checkEmail, (rs, rowNum) -> {
                String emails = rs.getString("email");
                if(emails.equals("true")){
                    return  true;}
                return false;
                } ,email);
        }catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
            return false; 
        }
    }
    @Override
    public boolean checkPhone(String phone){
        try {
            return jdbcTemplate.queryForObject(checkPhone, (rs, rowNum) -> {
                String phones = rs.getString("phone");
                if(phones.equals("true")){
                    return  true;}
                return false;
                } ,phone);
        }catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
            return false; 
        }
    }
    @Override
    public MemberVO login(Map loginMap) throws DataAccessException {
        String id = (String) loginMap.get("id");
        String password = (String) loginMap.get("password");
        try {
        return jdbcTemplate.queryForObject(login, (rs, rowNum) -> {
        MemberVO member = new MemberVO();
        member.setMember_id(rs.getInt("member_id"));
        member.setWarning_count(rs.getInt("warning_count"));
        member.setId(rs.getString("id"));
        member.setEmail(rs.getString("email"));
        member.setLogin_type(rs.getString("login_type"));
        member.setVerify_type(rs.getString("verify_type"));
        member.setMember_name(rs.getString("member_name"));
        member.setNickname(rs.getString("nickname"));
        member.setPassword(rs.getString("password"));
        member.setPhone(rs.getString("phone"));
        member.setRole(rs.getString("role"));
        member.setStatus(rs.getString("status"));
        member.setCreated_at(rs.getString("created_at"));
        member.setUpdate_at(rs.getString("updated_at"));
        member.setBlocked_until(rs.getString("blocked_until"));
        member.setLast_login_at(rs.getString("last_login_at"));
        member.setVerify_type(rs.getString("verify_type"));
        member.setWarning_count(rs.getInt("warning_count"));
        return member;
        },id, password);
    }catch (org.springframework.dao.EmptyResultDataAccessException e) {
        // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
        return null; 
    }
    }
    
}
