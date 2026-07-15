package com.example.spendolive.calendar.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.spendolive.member.domain.MemberVO;

/**
 * 캘린더 페이지에서 쓰는 월별 지출 조회 API.
 * - NotificationPageController와 동일하게 @RestController로 분리 (프로젝트 컨벤션 유지)
 * - MyBatis Mapper 없이 JdbcTemplate 직접 사용
 */
@RestController
@RequestMapping("/spendolive/calendar")
public class CalendarApiController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 특정 연/월의 지출 내역을 JSON으로 반환.
     * GET /spendolive/calendar/expenses.do?year=2026&month=7
     *
     * 응답 예시:
     * [
     *   {
     *     "expense_id": 101,
     *     "expense_date": "2026-07-05",
     *     "expense_title": "월세",
     *     "amount": 500000,
     *     "category_name": "고정지출",
     *     "expense_type": "FIXED"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/expenses.do")
    @ResponseBody
    public List<Map<String, Object>> getMonthlyExpenses(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            HttpSession session) {

        // 로그인 시 MemberControllerImpl에서 session.setAttribute("memberInfo", memberVO)로 저장됨
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        int member_id = memberInfo.getMember_id();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        String sql =
            "SELECT e.expense_id, e.expense_title, e.amount, e.expense_date, " +
            "       c.category_name, c.expense_type " +
            "FROM expense_tb e " +
            "JOIN expense_category_tb c ON e.category_id = c.category_id " +
            "WHERE e.member_id = ? " +
            "  AND e.expense_date BETWEEN ? AND ? " +
            "ORDER BY e.expense_date, e.expense_id";

        return jdbcTemplate.query(
            sql,
            new Object[]{ member_id, Date.valueOf(start), Date.valueOf(end) },
            (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("expense_id", rs.getLong("expense_id"));
                row.put("expense_title", rs.getString("expense_title"));
                row.put("amount", rs.getLong("amount"));
                row.put("expense_date", rs.getDate("expense_date").toLocalDate().toString());
                row.put("category_name", rs.getString("category_name"));
                row.put("expense_type", rs.getString("expense_type"));
                return row;
            }
        );
    }
}