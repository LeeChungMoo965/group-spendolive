package com.example.spendolive.Expense.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseCategoryDTO {

    private Long categoryId;
    private String categoryName;
    private String expenseType;
    private Integer sortOrder;
}