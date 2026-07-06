package com.example.spendolive.member.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.member.domain.MemberCardVO;
import com.example.spendolive.member.domain.MemberVO;

@Repository
public class MemberRepositoryImpl implements MemberRepository{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String signup = "INSERT INTO member_tb(id, email, password, member_name, nickname, phone,login_type ,verify_type) values(?,?,?,?,?,?,?,?)";
    
    private final String login = "SELECT member_id, id, email, password, member_name, nickname, phone, login_type, blocked_until, warning_count, role, status, verify_type, open_bank_user_seq_no, open_bank_token, fintech_use_num,account_num,bank_code, "
    + "TO_CHAR(created_at, 'YYYY-MM-DD') AS created_at, "
    + "TO_CHAR(updated_at, 'YYYY-MM-DD') AS updated_at, "
    + "TO_CHAR(last_login_at, 'YYYY-MM-DD') AS last_login_at "
    + "FROM member_tb WHERE id = ? AND password = ? AND STATUS ='ACTIVE'";
    private final String checkId = "select decode(count(*),1, 'false', 0, 'true') as id"
    +" from member_tb where id =? AND STATUS ='ACTIVE'";
    private final String checkEmail = "select decode(count(*),1, 'false', 0, 'true') as email"
    +" from member_tb where email =? AND STATUS ='ACTIVE'";
    private final String checkPhone = "select decode(count(*),1, 'false', 0, 'true') as phone"
    +" from member_tb where phone =? AND STATUS ='ACTIVE'";
    private final String updatePinNO = "INSERT INTO member_account_tb(id, bank_code, account_number, fintech_use_num, open_bank_token , open_bank_user_seq, balance,account_holder_nam) "
    +" values(?,?,?,?,?,?,?,?) ";
    private final String updateBillingKey = "INSERT INTO member_card_tb(id, card_number, card_company, billing_key) "
    +" values(?,?,?,?) ";
    private final String selectMemberByIdSql = "SELECT member_id, id, email, password, member_name, nickname, phone, login_type, blocked_until, warning_count, role, status, verify_type, open_bank_user_seq_no, open_bank_token, fintech_use_num, "
    + "TO_CHAR(created_at, 'YYYY-MM-DD') AS created_at, "
    + "TO_CHAR(updated_at, 'YYYY-MM-DD') AS updated_at, "
    + "TO_CHAR(last_login_at, 'YYYY-MM-DD') AS last_login_at "
    + "FROM member_tb WHERE id = ? AND STATUS ='ACTIVE'";
    private final String selectMemverCardById = "select billing_key, card_company, card_number from member_card_tb where id =? and status ='YES' ";
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
        member.setOpen_bank_token(rs.getString("open_bank_token"));
        member.setOpen_bank_user_seq_no(rs.getString("open_bank_user_seq_no"));
        member.setFintech_use_num(rs.getString("fintech_use_num"));
        member.setAccount_num(rs.getString("account_num"));
        member.setBank_code(rs.getString("bank_code"));
        return member;
        },id, password);
    }catch (org.springframework.dao.EmptyResultDataAccessException e) {
        // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
        return null; 
    }
    }
    @Override
	public void updateTossInfo(String userId, String card_num, String card_company, String billingkey)throws DataAccessException {
        jdbcTemplate.update(updateBillingKey,
        userId,
        card_num,
        card_company,
        billingkey
        );
    }
    @Override
    public void updateOpenBankingInfo(String userId, String accessToken, String userSeqNo, String fintech_num, String bank_code, String account_num, int balance,String account_holder_nam) throws DataAccessException {
        jdbcTemplate.update(updatePinNO,
        userId,
        bank_code,
        account_num,
        fintech_num,
        accessToken,
        userSeqNo,
        balance,
        account_holder_nam
        );
    }

    @Override
    public MemberVO selectMemberById(String id) throws DataAccessException {
        try {
            return jdbcTemplate.queryForObject(selectMemberByIdSql, (rs, rowNum) -> mapMember(rs), id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateMyInfo(MemberVO memberVO, String newPassword) throws DataAccessException {
        boolean changePassword = newPassword != null && !newPassword.isBlank();

        if (changePassword) {
            String sql = """
                    UPDATE member_tb
                    SET member_name = ?,
                        nickname = ?,
                        email = ?,
                        phone = ?,
                        password = ?,
                        updated_at = SYSDATE
                    WHERE id = ?
                    """;
            jdbcTemplate.update(
                    sql,
                    memberVO.getMember_name(),
                    memberVO.getNickname(),
                    memberVO.getEmail(),
                    memberVO.getPhone(),
                    newPassword,
                    memberVO.getId()
            );
        } else {
            String sql = """
                    UPDATE member_tb
                    SET member_name = ?,
                        nickname = ?,
                        email = ?,
                        phone = ?,
                        updated_at = SYSDATE
                    WHERE id = ?
                    """;
            jdbcTemplate.update(
                    sql,
                    memberVO.getMember_name(),
                    memberVO.getNickname(),
                    memberVO.getEmail(),
                    memberVO.getPhone(),
                    memberVO.getId()
            );
        }
    }
    private MemberVO mapMember(ResultSet rs) throws SQLException {
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
        member.setOpen_bank_token(rs.getString("open_bank_token"));
        member.setOpen_bank_user_seq_no(rs.getString("open_bank_user_seq_no"));
        member.setFintech_use_num(rs.getString("fintech_use_num"));
        return member;
    }

    @Override
    public MemberCardVO getCardInfoByUserId(String userId) {
    try{
        return jdbcTemplate.queryForObject(selectMemverCardById, (rs, rowNum) ->{
            MemberCardVO card = new MemberCardVO();
            card.setBillingKey(rs.getString("billing_key"));
            card.setCardCompany(rs.getString("card_company"));
            card.setCardNumber(rs.getString("card_number"));
            return card;
        }, userId);
    }catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
            return null; 
        }
    }
    
}
