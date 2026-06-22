package com.example.spendolive.Expense.repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final String selectExpenseListSql = """
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
            ORDER BY e.expense_date DESC, e.expense_id DESC
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
            """;

    private final String selectCategoryListSql = """
            SELECT
                category_id,
                category_name,
                expense_type,
                sort_order
            FROM expense_category_tb
            ORDER BY sort_order ASC, category_id ASC
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
    public List<ExpenseDTO> selectExpenseList(Long memberId) {
        return jdbcTemplate.query(selectExpenseListSql, expenseRowMapper(), memberId);
    }

    @Override
    public ExpenseDTO selectExpense(Long expenseId) {
        try {
            return jdbcTemplate.queryForObject(selectExpenseSql, expenseRowMapper(), expenseId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void insertExpense(ExpenseDTO expenseDTO) {
        jdbcTemplate.update(
                insertExpenseSql,
                expenseDTO.getMemberId(),
                expenseDTO.getCategoryId(),
                expenseDTO.getExpenseTitle(),
                expenseDTO.getAmount(),
                toSqlDate(expenseDTO.getExpenseDate()),
                expenseDTO.getPaymentMethod(),
                expenseDTO.getMemo(),
                expenseDTO.getRepeatYn(),
                expenseDTO.getRepeatCycle(),
                expenseDTO.getFixedYn()
        );
    }

    @Override
    public void updateExpense(ExpenseDTO expenseDTO) {
        jdbcTemplate.update(
                updateExpenseSql,
                expenseDTO.getCategoryId(),
                expenseDTO.getExpenseTitle(),
                expenseDTO.getAmount(),
                toSqlDate(expenseDTO.getExpenseDate()),
                expenseDTO.getPaymentMethod(),
                expenseDTO.getMemo(),
                expenseDTO.getRepeatYn(),
                expenseDTO.getRepeatCycle(),
                expenseDTO.getFixedYn(),
                expenseDTO.getExpenseId(),
                expenseDTO.getMemberId()
        );
    }

    @Override
    public void deleteExpense(Long expenseId) {
        jdbcTemplate.update(deleteExpenseSql, expenseId);
    }

    @Override
    public List<ExpenseCategoryDTO> selectCategoryList() {
        return jdbcTemplate.query(selectCategoryListSql, categoryRowMapper());
    }

    @Override
    public List<ExpenseCategoryDTO> selectCategoryListByType(String expenseType) {
        return jdbcTemplate.query(selectCategoryListByTypeSql, categoryRowMapper(), expenseType);
    }

    private RowMapper<ExpenseDTO> expenseRowMapper() {
        return new RowMapper<>() {
            @Override
            public ExpenseDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                ExpenseDTO expense = new ExpenseDTO();

                expense.setExpenseId(rs.getLong("expense_id"));
                expense.setMemberId(rs.getLong("member_id"));
                expense.setCategoryId(rs.getLong("category_id"));
                expense.setCategoryName(rs.getString("category_name"));
                expense.setExpenseType(rs.getString("expense_type"));
                expense.setExpenseTitle(rs.getString("expense_title"));
                expense.setAmount(rs.getInt("amount"));
                expense.setExpenseDate(rs.getDate("expense_date"));
                expense.setPaymentMethod(rs.getString("payment_method"));
                expense.setMemo(rs.getString("memo"));
                expense.setRepeatYn(rs.getString("repeat_yn"));
                expense.setRepeatCycle(rs.getString("repeat_cycle"));
                expense.setFixedYn(rs.getString("fixed_yn"));
                expense.setCreatedAt(rs.getTimestamp("created_at"));
                expense.setUpdatedAt(rs.getTimestamp("updated_at"));

                return expense;
            }
        };
    }

    private RowMapper<ExpenseCategoryDTO> categoryRowMapper() {
        return new RowMapper<>() {
            @Override
            public ExpenseCategoryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                ExpenseCategoryDTO category = new ExpenseCategoryDTO();

                category.setCategoryId(rs.getLong("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                category.setExpenseType(rs.getString("expense_type"));
                category.setSortOrder(rs.getInt("sort_order"));

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
}
