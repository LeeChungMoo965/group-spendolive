package com.example.spendolive.mapper;

import com.example.spendolive.domain.ExpenseDTO;
import com.example.spendolive.domain.ExpenseCategoryDTO;
import org.apache.ibatis.annotations.Mapper;

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