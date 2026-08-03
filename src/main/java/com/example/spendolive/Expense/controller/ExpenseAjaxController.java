package com.example.spendolive.Expense.controller;

import java.time.Duration;
import java.time.YearMonth;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.spendolive.Expense.domain.ExpenseDTO;
import com.example.spendolive.Expense.service.ExpenseService;
import com.example.spendolive.common.ajax.AjaxAuthSupport;
import com.example.spendolive.common.ajax.AjaxDuplicateGuard;
import com.example.spendolive.common.ajax.AjaxResponse;
import com.example.spendolive.member.domain.MemberVO;

import jakarta.servlet.http.HttpSession;

/**
 * 지출관리 AJAX Controller.
 *
 * 예산 저장과 지출 등록·수정·삭제 요청을 JSON으로 처리한다.
 * 지출 목록 화면 조회는 ExpenseController가 담당한다.
 */
@Controller
@RequestMapping("/spendolive/expense")
public class ExpenseAjaxController {

    private final ExpenseService expenseService;
    private final AjaxDuplicateGuard duplicateGuard;

    public ExpenseAjaxController(ExpenseService expenseService,
                                 AjaxDuplicateGuard duplicateGuard) {
        this.expenseService = expenseService;
        this.duplicateGuard = duplicateGuard;
    }

    // -------------------------------------------------------------------------
    // AJAX 데이터 처리
    // -------------------------------------------------------------------------

    @ResponseBody
    @PostMapping("/budget/save.do")
    public ResponseEntity<?> saveBudgetAjax(@RequestParam(value = "budget_month", required = false) String budgetMonth,
                                            @RequestParam(value = "budget_amount", required = false) String budgetAmount,
                                            HttpSession session) {

        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) {
            return AjaxAuthSupport.unauthorized();
        }

        try {
            int parsedBudgetAmount = Integer.parseInt(budgetAmount);
            String targetMonth = normalizeYearMonth(budgetMonth);

            expenseService.saveMonthlyBudget(
                    Long.valueOf(member.getMember_id()),
                    targetMonth,
                    Math.max(0, parsedBudgetAmount)
            );

            return ResponseEntity.ok(
                    AjaxResponse.success(
                            "선택한 달의 예산이 저장되었습니다.",
                            Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)
                    )
            );
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(AjaxResponse.failure("INVALID_REQUEST", "예산 저장에 실패했습니다."));
        }
    }

    @ResponseBody
    @PostMapping("/add.do")
    public ResponseEntity<?> addExpenseAjax(@ModelAttribute ExpenseDTO expenseDTO,
                                            BindingResult bindingResult,
                                            @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                            HttpSession session) {

        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) {
            return AjaxAuthSupport.unauthorized();
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(AjaxResponse.failure("INVALID_REQUEST", "지출 입력값을 확인해주세요."));
        }

        String duplicateKey = "expense-add:"
                + member.getId() + ':'
                + String.valueOf(expenseDTO.getExpense_title()) + ':'
                + expenseDTO.getAmount() + ':'
                + String.valueOf(expenseDTO.getExpense_date());

        if (!duplicateGuard.tryAcquire(duplicateKey, Duration.ofSeconds(5))) {
            return ResponseEntity.status(409)
                    .body(AjaxResponse.failure(
                            "DUPLICATE_REQUEST",
                            "같은 지출 등록 요청이 이미 처리 중입니다."
                    ));
        }

        try {
            expenseDTO.setMember_id(Long.valueOf(member.getMember_id()));
            applyRepeatSettings(expenseDTO);
            expenseService.addExpense(expenseDTO);

            String targetMonth = normalizeYearMonth(yearMonth);

            return ResponseEntity.ok(
                    AjaxResponse.success(
                            "지출 내역이 등록되었습니다.",
                            Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)
                    )
            );
        } catch (Exception exception) {
            duplicateGuard.release(duplicateKey);
            return ResponseEntity.badRequest()
                    .body(AjaxResponse.failure(
                            "INVALID_REQUEST",
                            "지출 등록에 실패했습니다. 입력값을 확인해주세요."
                    ));
        }
    }

    @ResponseBody
    @PostMapping("/modify.do")
    public ResponseEntity<?> modifyExpenseAjax(@ModelAttribute ExpenseDTO expenseDTO,
                                               BindingResult bindingResult,
                                               @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                               HttpSession session) {

        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) {
            return AjaxAuthSupport.unauthorized();
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(AjaxResponse.failure("INVALID_REQUEST", "지출 수정값을 확인해주세요."));
        }

        try {
            expenseDTO.setMember_id(Long.valueOf(member.getMember_id()));
            applyRepeatSettings(expenseDTO);
            expenseService.modifyExpense(expenseDTO);

            String targetMonth = normalizeYearMonth(yearMonth);

            return ResponseEntity.ok(
                    AjaxResponse.success(
                            "지출 내역이 수정되었습니다.",
                            Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)
                    )
            );
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(AjaxResponse.failure("INVALID_REQUEST", "지출 수정에 실패했습니다."));
        }
    }

    @ResponseBody
    @PostMapping("/delete.do")
    public ResponseEntity<?> deleteExpenseAjax(@RequestParam(value = "expense_id", required = false) String expenseId,
                                               @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                               HttpSession session) {

        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) {
            return AjaxAuthSupport.unauthorized();
        }

        try {
            Long parsedExpenseId = Long.valueOf(expenseId);

            expenseService.removeExpense(
                    parsedExpenseId,
                    Long.valueOf(member.getMember_id())
            );

            String targetMonth = normalizeYearMonth(yearMonth);

            return ResponseEntity.ok(
                    AjaxResponse.success(
                            "지출 내역이 삭제되었습니다.",
                            Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)
                    )
            );
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(AjaxResponse.failure(
                            "NOT_FOUND",
                            "삭제할 지출 내역을 찾지 못했거나 삭제에 실패했습니다."
                    ));
        }
    }

    // -------------------------------------------------------------------------
    // AJAX 요청값 보정
    // -------------------------------------------------------------------------

    private String normalizeYearMonth(String yearMonth) {
        if (yearMonth != null && !yearMonth.isBlank()) {
            try {
                return YearMonth.parse(yearMonth).toString();
            } catch (Exception ignored) {
                // 잘못된 연월은 아래 현재 연월로 보정한다.
            }
        }

        return YearMonth.now().toString();
    }

    private void applyRepeatSettings(ExpenseDTO expenseDTO) {
        boolean repeatTarget = "FIXED".equals(expenseDTO.getExpense_type())
                || "OTT".equals(expenseDTO.getExpense_type());

        if (!repeatTarget) {
            expenseDTO.setFixed_yn("N");
            expenseDTO.setRepeat_yn("N");
            expenseDTO.setRepeat_cycle(null);
            return;
        }

        expenseDTO.setFixed_yn("Y");

        if (expenseDTO.getRepeat_cycle() == null || expenseDTO.getRepeat_cycle().isBlank()) {
            expenseDTO.setRepeat_yn("N");
            expenseDTO.setRepeat_cycle(null);
        } else {
            expenseDTO.setRepeat_yn("Y");
        }
    }
}
