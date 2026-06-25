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
}
