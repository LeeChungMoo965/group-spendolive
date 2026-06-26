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
public class MemberRepositoryImpl implements MemberRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String signup = "INSERT INTO member_tb(id, email, password, member_name, nickname, phone, login_type, verify_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

    private final String login = "SELECT member_id, id, email, password, member_name, nickname, phone, login_type, blocked_until, warning_count, role, status, verify_type, open_bank_user_seq_no, open_bank_token, fintech_use_num, "
            + "TO_CHAR(created_at, 'YYYY-MM-DD') AS created_at, "
            + "TO_CHAR(updated_at, 'YYYY-MM-DD') AS updated_at, "
            + "TO_CHAR(last_login_at, 'YYYY-MM-DD') AS last_login_at "
            + "FROM member_tb WHERE id = ? AND password = ?";

    private final String checkId = "SELECT DECODE(COUNT(*), 1, 'false', 0, 'true') AS id FROM member_tb WHERE id = ?";
    private final String checkEmail = "SELECT DECODE(COUNT(*), 1, 'false', 0, 'true') AS email FROM member_tb WHERE email = ?";
    private final String checkPhone = "SELECT DECODE(COUNT(*), 1, 'false', 0, 'true') AS phone FROM member_tb WHERE phone = ?";

    private final String updatePinNO = "UPDATE member_tb "
            + "SET open_bank_user_seq_no = ?, open_bank_token = ?, fintech_use_num = ? "
            + "WHERE id = ?";

    private final String selectMemberByIdSql = "SELECT member_id, id, email, password, member_name, nickname, phone, login_type, blocked_until, warning_count, role, status, verify_type, open_bank_user_seq_no, open_bank_token, fintech_use_num, "
            + "TO_CHAR(created_at, 'YYYY-MM-DD') AS created_at, "
            + "TO_CHAR(updated_at, 'YYYY-MM-DD') AS updated_at, "
            + "TO_CHAR(last_login_at, 'YYYY-MM-DD') AS last_login_at "
            + "FROM member_tb WHERE id = ?";

    public MemberRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertNewMember(MemberVO member) {
        jdbcTemplate.update(
                signup,
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getMember_name(),
                member.getNickname(),
                member.getPhone(),
                member.getLogin_type(),
                "PHONE"
        );
    }

    @Override
    public boolean checkId(String id) {
        try {
            return jdbcTemplate.queryForObject(checkId, (rs, rowNum) -> "true".equals(rs.getString("id")), id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public boolean checkEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(checkEmail, (rs, rowNum) -> "true".equals(rs.getString("email")), email);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public boolean checkPhone(String phone) {
        try {
            return jdbcTemplate.queryForObject(checkPhone, (rs, rowNum) -> "true".equals(rs.getString("phone")), phone);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public MemberVO login(Map loginMap) throws DataAccessException {
        String id = (String) loginMap.get("id");
        String password = (String) loginMap.get("password");

        try {
            return jdbcTemplate.queryForObject(login, (rs, rowNum) -> mapMember(rs), id, password);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
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

    @Override
    public void updateOpenBankingInfo(String userId, String accessToken, String userSeqNo, String fintechNum) throws DataAccessException {
        jdbcTemplate.update(updatePinNO, userSeqNo, accessToken, fintechNum, userId);
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
}
