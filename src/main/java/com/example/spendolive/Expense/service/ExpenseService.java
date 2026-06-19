package com.example.spendolive.Expense.service;

import java.util.List;

import com.example.spendolive.Expense.domain.ExpenseCategoryDTO;
import com.example.spendolive.Expense.domain.ExpenseDTO;

public interface ExpenseService {

    List<ExpenseDTO> getExpenseList(Long memberId);

    ExpenseDTO getExpense(Long expenseId);

    void addExpense(ExpenseDTO expenseDTO);

    void modifyExpense(ExpenseDTO expenseDTO);

    void removeExpense(Long expenseId);

    List<ExpenseCategoryDTO> getCategoryList();

    List<ExpenseCategoryDTO> getCategoryListByType(String expenseType);
}