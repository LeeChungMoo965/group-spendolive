package com.example.spendolive.Expense.service;

import java.util.List;

import com.example.spendolive.Expense.domain.ExpenseCategoryDTO;
import com.example.spendolive.Expense.domain.ExpenseDTO;

public interface ExpenseService {

    List<ExpenseDTO> getExpenseList(Long member_id, String yearMonth);

    ExpenseDTO getExpense(Long expense_id);

    void addExpense(ExpenseDTO expenseDTO);

    void modifyExpense(ExpenseDTO expenseDTO);

    void removeExpense(Long expense_id, Long member_id);

    List<ExpenseCategoryDTO> getCategoryList();

    List<ExpenseCategoryDTO> getCategoryListByType(String expense_type);
}
