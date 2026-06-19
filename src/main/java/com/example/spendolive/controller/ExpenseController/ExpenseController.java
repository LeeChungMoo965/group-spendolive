package com.example.spendolive.controller.ExpenseController;

import com.example.spendolive.domain.ExpenseDTO;
import com.example.spendolive.service.ExpenseService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

        model.addAttribute("expenseList", expenseService.getExpenseList(memberId));
        model.addAttribute("categoryList", expenseService.getCategoryList());

        return "member/expense";
    }

    @PostMapping("/add.do")
    public String addExpense(@ModelAttribute ExpenseDTO expenseDTO,
                             HttpSession session) {

        Long memberId = getLoginMemberId(session);
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
    public String deleteExpense(@RequestParam("expenseId") Long expenseId) {

        expenseService.removeExpense(expenseId);

        return "redirect:/spendolive/expense/list.do";
    }

    private Long getLoginMemberId(HttpSession session) {
        /*
         * 로그인 기능이 완성되면 세션에서 member_id를 꺼내면 됨.
         * 지금은 테스트용으로 1번 회원 고정.
         *
         * 예시:
         * MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
         * return memberInfo.getMemberId();
         */
        return 1L;
    }
}