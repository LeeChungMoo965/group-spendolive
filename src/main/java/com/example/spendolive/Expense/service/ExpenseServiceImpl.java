package com.example.spendolive.Expense.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.Expense.domain.ExpenseCategoryDTO;
import com.example.spendolive.Expense.domain.ExpenseDTO;
import com.example.spendolive.Expense.repository.ExpenseRepository;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public List<ExpenseDTO> getExpenseList(Long member_id, String yearMonth) {
        return expenseRepository.selectExpenseList(member_id, yearMonth);
    }

    @Override
    public ExpenseDTO getExpense(Long expenseId) {
        return expenseRepository.selectExpense(expenseId);
    }

    @Override
    public void addExpense(ExpenseDTO expenseDTO) {
        expenseRepository.insertExpense(expenseDTO);
    }

    @Override
    public void modifyExpense(ExpenseDTO expenseDTO) {
        expenseRepository.updateExpense(expenseDTO);
    }

    @Override
    public void removeExpense(Long expenseId, Long member_id) {
        expenseRepository.deleteExpense(expenseId, member_id);
    }

    @Override
    public List<ExpenseCategoryDTO> getCategoryList() {
        return expenseRepository.selectCategoryList();
    }

    @Override
    public List<ExpenseCategoryDTO> getCategoryListByType(String expenseType) {
        return expenseRepository.selectCategoryListByType(expenseType);
    }
}
