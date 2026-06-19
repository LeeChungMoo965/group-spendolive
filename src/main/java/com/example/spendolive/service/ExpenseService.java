package com.example.spendolive.service;

import com.example.spendolive.domain.ExpenseDTO;
import com.example.spendolive.domain.ExpenseCategoryDTO;

import java.util.List;

public interface ExpenseService {

    List<ExpenseDTO> getExpenseList(Long memberId);

    ExpenseDTO getExpense(Long expenseId);

    void addExpense(ExpenseDTO expenseDTO);

    void modifyExpense(ExpenseDTO expenseDTO);

    void removeExpense(Long expenseId);

    List<ExpenseCategoryDTO> getCategoryList();

    List<ExpenseCategoryDTO> getCategoryListByType(String expenseType);
}