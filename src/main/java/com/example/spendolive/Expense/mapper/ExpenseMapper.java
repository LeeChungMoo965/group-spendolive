package com.example.spendolive.Expense.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.spendolive.Expense.domain.ExpenseCategoryDTO;
import com.example.spendolive.Expense.domain.ExpenseDTO;

import java.util.List;

@Mapper
public interface ExpenseMapper {

    List<ExpenseDTO> selectExpenseList(Long memberId);

    ExpenseDTO selectExpense(Long expenseId);

    void insertExpense(ExpenseDTO expenseDTO);

    void updateExpense(ExpenseDTO expenseDTO);

    void deleteExpense(Long expenseId);

    List<ExpenseCategoryDTO> selectCategoryList();

    List<ExpenseCategoryDTO> selectCategoryListByType(String expenseType);
}