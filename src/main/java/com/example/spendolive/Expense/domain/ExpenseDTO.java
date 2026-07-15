package com.example.spendolive.Expense.domain;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseDTO {


    private Long expense_id;
    private Long member_id;
    private Long category_id;
    private String category_name;
    private String expense_type;

    private String expense_title;
    private Integer amount;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expense_date;

    private String payment_method;
    private String memo;

    private String repeat_yn;
    private String repeat_cycle;
    private String fixed_yn;

    private String auto_generated_yn;

    private Date created_at;
    private Date updated_at;
}