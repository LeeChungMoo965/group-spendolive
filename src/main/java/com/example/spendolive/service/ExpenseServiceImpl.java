package com.example.spendolive.service;

import com.example.spendolive.domain.ExpenseDTO;
import com.example.spendolive.domain.ExpenseCategoryDTO;
import com.example.spendolive.mapper.ExpenseMapper;
import org.springframework.stereotype.Service;

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