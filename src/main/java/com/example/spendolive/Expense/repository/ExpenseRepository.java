package com.example.spendolive.Expense.repository;

import java.util.List;

import com.example.spendolive.Expense.domain.ExpenseCategoryDTO;
import com.example.spendolive.Expense.domain.ExpenseDTO;

public interface ExpenseRepository {

    List<ExpenseDTO> selectExpenseList(Long memberId);

    ExpenseDTO selectExpense(Long expenseId);

    void insertExpense(ExpenseDTO expenseDTO);

    void updateExpense(ExpenseDTO expenseDTO);

    void deleteExpense(Long expenseId);

    List<ExpenseCategoryDTO> selectCategoryList();

    List<ExpenseCategoryDTO> selectCategoryListByType(String expenseType);
}
