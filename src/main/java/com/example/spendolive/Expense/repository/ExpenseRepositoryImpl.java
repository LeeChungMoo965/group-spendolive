package com.example.spendolive.Expense.repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.spendolive.Expense.domain.ExpenseCategoryDTO;
import com.example.spendolive.Expense.domain.ExpenseDTO;

@Repository
public class ExpenseRepositoryImpl implements ExpenseRepository {

    private final JdbcTemplate jdbcTemplate;

    public ExpenseRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final String selectExpenseListByMonthSql = """
            SELECT
                e.expense_id,
                e.member_id,
                e.category_id,
                c.category_name,
                c.expense_type,
                e.expense_title,
                e.amount,
                e.expense_date,
                e.payment_method,
                e.memo,
                e.repeat_yn,
                e.repeat_cycle,
                e.fixed_yn,
                e.created_at,
                e.updated_at
            FROM expense_tb e
            JOIN expense_category_tb c
              ON e.category_id = c.category_id
            WHERE e.member_id = ?
              AND e.expense_date >= ?
              AND e.expense_date < ?
            ORDER BY e.expense_date DESC, e.expense_id DESC
            """;

    private final String selectRepeatBaseExpenseSql = """
        SELECT
            e.expense_id,
            e.member_id,
            e.category_id,
            c.category_name,
            c.expense_type,
            e.expense_title,
            e.amount,
            e.expense_date,
            e.payment_method,
            e.memo,
            e.repeat_yn,
            e.repeat_cycle,
            e.fixed_yn,
            e.created_at,
            e.updated_at
        FROM expense_tb e
        JOIN expense_category_tb c
          ON e.category_id = c.category_id
        WHERE e.member_id = ?
          AND c.expense_type IN ('FIXED', 'OTT')
          AND e.repeat_yn = 'Y'
          AND e.repeat_cycle IN ('MONTHLY', 'WEEKLY', 'YEARLY')
          AND e.expense_date < ?
        ORDER BY e.expense_date ASC, e.expense_id ASC
        """;

    private final String selectExpenseSql = """
            SELECT
                e.expense_id,
                e.member_id,
                e.category_id,
                c.category_name,
                c.expense_type,
                e.expense_title,
                e.amount,
                e.expense_date,
                e.payment_method,
                e.memo,
                e.repeat_yn,
                e.repeat_cycle,
                e.fixed_yn,
                e.created_at,
                e.updated_at
            FROM expense_tb e
            JOIN expense_category_tb c
              ON e.category_id = c.category_id
            WHERE e.expense_id = ?
            """;

    private final String insertExpenseSql = """
            INSERT INTO expense_tb (
                member_id,
                category_id,
                expense_title,
                amount,
                expense_date,
                payment_method,
                memo,
                repeat_yn,
                repeat_cycle,
                fixed_yn
            ) VALUES (?, ?, ?, ?, ?, ?, ?, NVL(?, 'N'), ?, NVL(?, 'N'))
            """;

    private final String updateExpenseSql = """
            UPDATE expense_tb
            SET
                category_id = ?,
                expense_title = ?,
                amount = ?,
                expense_date = ?,
                payment_method = ?,
                memo = ?,
                repeat_yn = NVL(?, 'N'),
                repeat_cycle = ?,
                fixed_yn = NVL(?, 'N'),
                updated_at = SYSDATE
            WHERE expense_id = ?
              AND member_id = ?
            """;

    private final String deleteExpenseSql = """
            DELETE FROM expense_tb
            WHERE expense_id = ?
              AND member_id = ?
            """;

    private final String selectCategoryListSql = """
            SELECT
                category_id,
                category_name,
                expense_type,
                sort_order
            FROM expense_category_tb
            ORDER BY expense_type ASC, sort_order ASC, category_id ASC
            """;

    private final String selectCategoryListByTypeSql = """
            SELECT
                category_id,
                category_name,
                expense_type,
                sort_order
            FROM expense_category_tb
            WHERE expense_type = ?
            ORDER BY sort_order ASC, category_id ASC
            """;

    @Override
    public List<ExpenseDTO> selectExpenseList(Long member_id, String yearMonth) {
        YearMonth targetMonth = YearMonth.parse(yearMonth);
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.plusMonths(1).atDay(1);

        List<ExpenseDTO> result = jdbcTemplate.query(
                selectExpenseListByMonthSql,
                expenseRowMapper(),
                member_id,
                Date.valueOf(startDate),
                Date.valueOf(endDate)
        );

        for (ExpenseDTO expense : result) {
            expense.setAuto_generated_yn("N");
        }

        List<ExpenseDTO> repeatBaseList = jdbcTemplate.query(
                selectRepeatBaseExpenseSql,
                expenseRowMapper(),
                member_id,
                Date.valueOf(startDate)
        );

        result.addAll(makeRepeatedExpenses(repeatBaseList, targetMonth));

        result.sort(
                Comparator.comparing(ExpenseDTO::getExpense_date, Comparator.nullsLast(Comparator.reverseOrder()))
                          .thenComparing(ExpenseDTO::getExpense_id, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        return result;
    }

    @Override
    public ExpenseDTO selectExpense(Long expense_id) {
        try {
            return jdbcTemplate.queryForObject(selectExpenseSql, expenseRowMapper(), expense_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void insertExpense(ExpenseDTO expenseDTO) {
        jdbcTemplate.update(
                insertExpenseSql,
                expenseDTO.getMember_id(),

                expenseDTO.getCategory_id(),
                expenseDTO.getExpense_title(),

                expenseDTO.getAmount(),
                toSqlDate(expenseDTO.getExpense_date()),
                expenseDTO.getPayment_method(),
                expenseDTO.getMemo(),
                expenseDTO.getRepeat_yn(),
                expenseDTO.getRepeat_cycle(),
                expenseDTO.getFixed_yn()
        );
    }

    @Override
    public void updateExpense(ExpenseDTO expenseDTO) {
        jdbcTemplate.update(
                updateExpenseSql,
                expenseDTO.getCategory_id(),
                expenseDTO.getExpense_title(),
                expenseDTO.getAmount(),
                toSqlDate(expenseDTO.getExpense_date()),
                expenseDTO.getPayment_method(),
                expenseDTO.getMemo(),
                expenseDTO.getRepeat_yn(),
                expenseDTO.getRepeat_cycle(),
                expenseDTO.getFixed_yn(),
                expenseDTO.getExpense_id(),
                expenseDTO.getMember_id()
        );
    }

    @Override

    public void deleteExpense(Long expense_id, Long member_id) {
        jdbcTemplate.update(deleteExpenseSql, expense_id, member_id);

    }

    @Override
    public List<ExpenseCategoryDTO> selectCategoryList() {
        return jdbcTemplate.query(selectCategoryListSql, categoryRowMapper());
    }

    @Override
    public List<ExpenseCategoryDTO> selectCategoryListByType(String expense_type) {
        return jdbcTemplate.query(selectCategoryListByTypeSql, categoryRowMapper(), expense_type);
    }

    private List<ExpenseDTO> makeRepeatedExpenses(List<ExpenseDTO> repeatBaseList, YearMonth targetMonth) {
        List<ExpenseDTO> repeatedList = new ArrayList<>();

        LocalDate targetStart = targetMonth.atDay(1);
        LocalDate targetEnd = targetMonth.plusMonths(1).atDay(1);

        for (ExpenseDTO base : repeatBaseList) {
            LocalDate baseDate = toLocalDate(base.getExpense_date());

            if (baseDate == null) {
                continue;
            }

            String repeat_cycle = base.getRepeat_cycle();

            if ("MONTHLY".equals(repeat_cycle)) {
                int day = Math.min(baseDate.getDayOfMonth(), targetMonth.lengthOfMonth());
                LocalDate repeatedDate = targetMonth.atDay(day);

                if (!repeatedDate.isBefore(targetStart) && repeatedDate.isBefore(targetEnd) && repeatedDate.isAfter(baseDate)) {
                    repeatedList.add(copyAsRepeatedExpense(base, repeatedDate));
                }
            }

            if ("WEEKLY".equals(repeat_cycle)) {
                LocalDate repeatedDate = baseDate.plusWeeks(1);

                while (repeatedDate.isBefore(targetStart)) {
                    repeatedDate = repeatedDate.plusWeeks(1);
                }

                while (repeatedDate.isBefore(targetEnd)) {
                    if (repeatedDate.isAfter(baseDate)) {
                        repeatedList.add(copyAsRepeatedExpense(base, repeatedDate));
                    }
                    repeatedDate = repeatedDate.plusWeeks(1);
                }
            }

            if ("YEARLY".equals(repeat_cycle)) {
                if (baseDate.getMonth() == targetMonth.getMonth()) {
                    int day = Math.min(baseDate.getDayOfMonth(), targetMonth.lengthOfMonth());
                    LocalDate repeatedDate = targetMonth.atDay(day);

                    if (repeatedDate.isAfter(baseDate)) {
                        repeatedList.add(copyAsRepeatedExpense(base, repeatedDate));
                    }
                }
            }
        }

        return repeatedList;
    }

    private ExpenseDTO copyAsRepeatedExpense(ExpenseDTO base, LocalDate repeatedDate) {
        ExpenseDTO repeated = new ExpenseDTO();

        repeated.setExpense_id(base.getExpense_id());
        repeated.setMember_id(base.getMember_id());
        repeated.setCategory_id(base.getCategory_id());
        repeated.setCategory_name(base.getCategory_name());
        repeated.setExpense_type(base.getExpense_type());
        repeated.setExpense_title(base.getExpense_title());

        repeated.setAmount(base.getAmount());
        repeated.setExpense_date(Date.valueOf(repeatedDate));
        repeated.setPayment_method(base.getPayment_method());
        repeated.setMemo(base.getMemo());

        repeated.setRepeat_yn(base.getRepeat_yn());
        repeated.setRepeat_cycle(base.getRepeat_cycle());
        repeated.setFixed_yn(base.getFixed_yn());
        repeated.setAuto_generated_yn("Y");
        repeated.setCreated_at(base.getCreated_at());
        repeated.setUpdated_at(base.getUpdated_at());


        return repeated;
    }

    private RowMapper<ExpenseDTO> expenseRowMapper() {
        return new RowMapper<>() {
            @Override
            public ExpenseDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                ExpenseDTO expense = new ExpenseDTO();


                expense.setExpense_id(rs.getLong("expense_id"));
                expense.setMember_id(rs.getLong("member_id"));
                expense.setCategory_id(rs.getLong("category_id"));
                expense.setCategory_name(rs.getString("category_name"));
                expense.setExpense_type(rs.getString("expense_type"));
                expense.setExpense_title(rs.getString("expense_title"));

                expense.setAmount(rs.getInt("amount"));
                expense.setExpense_date(rs.getDate("expense_date"));
                expense.setPayment_method(rs.getString("payment_method"));
                expense.setMemo(rs.getString("memo"));

                expense.setRepeat_yn(rs.getString("repeat_yn"));
                expense.setRepeat_cycle(rs.getString("repeat_cycle"));
                expense.setFixed_yn(rs.getString("fixed_yn"));
                expense.setAuto_generated_yn("N");
                expense.setCreated_at(rs.getTimestamp("created_at"));
                expense.setUpdated_at(rs.getTimestamp("updated_at"));


                return expense;
            }
        };
    }

    private RowMapper<ExpenseCategoryDTO> categoryRowMapper() {
        return new RowMapper<>() {
            @Override
            public ExpenseCategoryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                ExpenseCategoryDTO category = new ExpenseCategoryDTO();

                category.setCategory_id(rs.getLong("category_id"));
                category.setCategory_name(rs.getString("category_name"));
                category.setExpense_type(rs.getString("expense_type"));
                category.setSort_order(rs.getInt("sort_order"));

                return category;
            }
        };
    }

    private Date toSqlDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return new Date(date.getTime());
    }

    private LocalDate toLocalDate(java.util.Date date) {

        if (date == null) {
            return null;
        }
    
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
    
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDate();
    }
}
