package com.example.spendolive.Expense.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.spendolive.Expense.domain.ExpenseDTO;
import com.example.spendolive.Expense.service.ExpenseService;
import com.example.spendolive.member.domain.MemberVO;

@Controller
@RequestMapping("/spendolive/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/list.do")
    public String expenseList(Model model, HttpSession session) {

        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            return "redirect:/member/loginForm.do";
        }

        model.addAttribute("expenseList", expenseService.getExpenseList(memberId));
        model.addAttribute("categoryList", expenseService.getCategoryList());
        model.addAttribute("body_page", "/WEB-INF/views/expense/expense.jsp");

        return "common/layout";
    }

    @PostMapping("/add.do")
    public String addExpense(@ModelAttribute ExpenseDTO expenseDTO,
                             HttpSession session) {

        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseDTO.setMemberId(memberId);

        if (expenseDTO.getRepeatYn() == null) {
            expenseDTO.setRepeatYn("N");
        }

        if (expenseDTO.getFixedYn() == null) {
            expenseDTO.setFixedYn("N");
        }

        expenseService.addExpense(expenseDTO);

        return "redirect:/spendolive/expense/list.do";
    }

    @PostMapping("/delete.do")
    public String deleteExpense(@RequestParam("expenseId") Long expenseId,
                                HttpSession session) {

        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseService.removeExpense(expenseId);

        return "redirect:/spendolive/expense/list.do";
    }

    private Long getLoginMemberId(HttpSession session) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null) {
            return null;
        }

        return Long.valueOf(memberInfo.getMember_id());
    }
}