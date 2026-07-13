package com.example.spendolive.mypage.repository;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MyPageRepository {

    private final JdbcTemplate jdbcTemplate;

    public MyPageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 마이페이지 지출 요약 전용 조회.
     * 회원/신고/OTT 조회는 각각 MemberRepository, MyPageReportRepository, OttRepository 쪽에서 처리한다.
     */
    public int selectThisMonthExpenseTotal(int memberId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.plusMonths(1).atDay(1);

        String sql = """
                SELECT NVL(SUM(amount), 0)
                FROM expense_tb
                WHERE member_id = ?
                  AND expense_date >= ?
                  AND expense_date < ?
                """;

        try {
            Integer total = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    memberId,
                    java.sql.Date.valueOf(startDate),
                    java.sql.Date.valueOf(endDate)
            );
            return total == null ? 0 : total;
        } catch (DataAccessException e) {
            return 0;
        }
    }

    /**
     * 회원탈퇴 처리.
     * 실제 데이터를 바로 삭제하지 않고 status를 LEAVE로 바꾸는 소프트 삭제 방식이다.
     * 로그인 재사용을 막기 위해 비밀번호를 임의 문자열로 변경하고,
     * 오픈뱅킹 토큰/핀테크 이용번호는 개인정보 보호 차원에서 제거한다.
     */
    public int withdrawMember(String loginId) {
        String sql = """
                UPDATE member_tb
                SET status = 'LEAVE',
                    password = 'LEAVE_' || RAWTOHEX(SYS_GUID()),
                    open_bank_user_seq_no = NULL,
                    open_bank_token = NULL,
                    fintech_use_num = NULL,
                    updated_at = SYSDATE
                WHERE id = ?
                  AND status <> 'LEAVE'
                """;

        return jdbcTemplate.update(sql, loginId);
    }

}
