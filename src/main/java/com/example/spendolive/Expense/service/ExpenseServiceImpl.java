package com.example.spendolive.Expense.service;

import org.springframework.stereotype.Service;

import com.example.spendolive.Expense.domain.ExpenseCategoryDTO;
import com.example.spendolive.Expense.domain.ExpenseDTO;
import com.example.spendolive.Expense.mapper.ExpenseMapper;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseMapper expenseMapper;

    public ExpenseServiceImpl(ExpenseMapper expenseMapper) {
        this.expenseMapper = expenseMapper;
    }

    @Override
    public List<ExpenseDTO> getExpenseList(Long memberId) {
        return expenseMapper.selectExpenseList(memberId);
    }

    @Override
    public ExpenseDTO getExpense(Long expenseId) {
        return expenseMapper.selectExpense(expenseId);
    }

    @Override
    public void addExpense(ExpenseDTO expenseDTO) {
        expenseMapper.insertExpense(expenseDTO);
    }

    @Override
    public void modifyExpense(ExpenseDTO expenseDTO) {
        expenseMapper.updateExpense(expenseDTO);
    }

    @Override
    public void removeExpense(Long expenseId) {
        expenseMapper.deleteExpense(expenseId);
    }

    @Override
    public List<ExpenseCategoryDTO> getCategoryList() {
        return expenseMapper.selectCategoryList();
    }

    @Override
    public List<ExpenseCategoryDTO> getCategoryListByType(String expenseType) {
        return expenseMapper.selectCategoryListByType(expenseType);
    }
}