package com.example.spendolive.domain;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

public class ExpenseDTO {

    private Long expenseId;
    private Long memberId;
    private Long categoryId;

    private String categoryName;
    private String expenseType;

    private String expenseTitle;
    private Integer amount;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expenseDate;
    
    private String paymentMethod;
    private String memo;

    private String repeatYn;
    private String repeatCycle;
    private String fixedYn;

    private Date createdAt;
    private Date updatedAt;

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }

    public String getExpenseTitle() { return expenseTitle; }
    public void setExpenseTitle(String expenseTitle) { this.expenseTitle = expenseTitle; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Date getExpenseDate() { return expenseDate; }
    public void setExpenseDate(Date expenseDate) { this.expenseDate = expenseDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getRepeatYn() { return repeatYn; }
    public void setRepeatYn(String repeatYn) { this.repeatYn = repeatYn; }

    public String getRepeatCycle() { return repeatCycle; }
    public void setRepeatCycle(String repeatCycle) { this.repeatCycle = repeatCycle; }

    public String getFixedYn() { return fixedYn; }
    public void setFixedYn(String fixedYn) { this.fixedYn = fixedYn; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}