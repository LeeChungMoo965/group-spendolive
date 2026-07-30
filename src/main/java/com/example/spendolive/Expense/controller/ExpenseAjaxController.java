package com.example.spendolive.Expense.controller;

import java.time.Duration;
import java.time.YearMonth;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spendolive.Expense.domain.ExpenseDTO;
import com.example.spendolive.Expense.service.ExpenseService;
import com.example.spendolive.common.ajax.AjaxAuthSupport;
import com.example.spendolive.common.ajax.AjaxDuplicateGuard;
import com.example.spendolive.common.ajax.AjaxEndpoint;
import com.example.spendolive.common.ajax.AjaxResponse;
import com.example.spendolive.member.domain.MemberVO;

import jakarta.servlet.http.HttpSession;

@RestController
@AjaxEndpoint
@RequestMapping("/spendolive/expense/ajax")
/**
 * [지출관리 AJAX 전용 Controller]
 * 기존 ExpenseService와 DB 처리 로직은 그대로 호출하고, 페이지 이동 대신 JSON과 부분 갱신 주소를 반환한다.
 * 기존 일반 POST 주소는 다른 화면과의 호환성을 위해 삭제하지 않았다.
 */
public class ExpenseAjaxController {

    private final ExpenseService expenseService;
    private final AjaxDuplicateGuard duplicateGuard;

    public ExpenseAjaxController(ExpenseService expenseService, AjaxDuplicateGuard duplicateGuard) {
        this.expenseService = expenseService;
        this.duplicateGuard = duplicateGuard;
    }

    // [AJAX 변경] 월 예산 저장 후 지출 목록 영역만 다시 불러올 주소를 반환한다.
    @PostMapping("/budget/save.do")
    public ResponseEntity<?> saveBudget(@RequestParam("budget_month") String budgetMonth,
                                        @RequestParam("budget_amount") int budgetAmount,
                                        HttpSession session) {
        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) return AjaxAuthSupport.unauthorized();
        try {
            String targetMonth = normalizeYearMonth(budgetMonth);
            expenseService.saveMonthlyBudget(Long.valueOf(member.getMember_id()), targetMonth, Math.max(0, budgetAmount));
            return ResponseEntity.ok(AjaxResponse.success("선택한 달의 예산이 저장되었습니다.",
                    Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AjaxResponse.failure("INVALID_REQUEST", "예산 저장에 실패했습니다."));
        }
    }

    // [AJAX 변경] 지출 등록은 동일 내용의 연속 요청을 5초 동안 차단한다.
    @PostMapping("/add.do")
    public ResponseEntity<?> add(@ModelAttribute ExpenseDTO expenseDTO,
                                 @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                 HttpSession session) {
        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) return AjaxAuthSupport.unauthorized();
        String duplicateKey = "expense-add:" + member.getId() + ':'
                + String.valueOf(expenseDTO.getExpense_title()) + ':'
                + expenseDTO.getAmount() + ':' + String.valueOf(expenseDTO.getExpense_date());
        if (!duplicateGuard.tryAcquire(duplicateKey, Duration.ofSeconds(5))) {
            return ResponseEntity.status(409).body(AjaxResponse.failure("DUPLICATE_REQUEST", "같은 지출 등록 요청이 이미 처리 중입니다."));
        }
        try {
            expenseDTO.setMember_id(Long.valueOf(member.getMember_id()));
            applyRepeatSettings(expenseDTO);
            expenseService.addExpense(expenseDTO);
            String targetMonth = normalizeYearMonth(yearMonth);
            return ResponseEntity.ok(AjaxResponse.success("지출 내역이 등록되었습니다.",
                    Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)));
        } catch (Exception e) {
            duplicateGuard.release(duplicateKey);
            return ResponseEntity.badRequest().body(AjaxResponse.failure("INVALID_REQUEST", "지출 등록에 실패했습니다. 입력값을 확인해주세요."));
        }
    }

    // [AJAX 변경] 기존 수정 Service를 실행한 뒤 현재 선택 월을 다시 조회한다.
    @PostMapping("/modify.do")
    public ResponseEntity<?> modify(@ModelAttribute ExpenseDTO expenseDTO,
                                    @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                    HttpSession session) {
        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) return AjaxAuthSupport.unauthorized();
        try {
            expenseDTO.setMember_id(Long.valueOf(member.getMember_id()));
            applyRepeatSettings(expenseDTO);
            expenseService.modifyExpense(expenseDTO);
            String targetMonth = normalizeYearMonth(yearMonth);
            return ResponseEntity.ok(AjaxResponse.success("지출 내역이 수정되었습니다.",
                    Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AjaxResponse.failure("INVALID_REQUEST", "지출 수정에 실패했습니다."));
        }
    }

    // [AJAX 변경] 로그인 회원 소유의 지출만 삭제하고 성공 후 목록을 갱신한다.
    @PostMapping("/delete.do")
    public ResponseEntity<?> delete(@RequestParam("expense_id") Long expenseId,
                                    @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                    HttpSession session) {
        MemberVO member = AjaxAuthSupport.member(session);
        if (member == null) return AjaxAuthSupport.unauthorized();
        try {
            expenseService.removeExpense(expenseId, Long.valueOf(member.getMember_id()));
            String targetMonth = normalizeYearMonth(yearMonth);
            return ResponseEntity.ok(AjaxResponse.success("지출 내역이 삭제되었습니다.",
                    Map.of("refreshUrl", "/spendolive/expense/list.do?yearMonth=" + targetMonth)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AjaxResponse.failure("NOT_FOUND", "삭제할 지출 내역을 찾지 못했거나 삭제에 실패했습니다."));
        }
    }

    // 잘못되거나 비어 있는 연월은 현재 연월로 보정해 조회 URL 오류를 막는다.
    private String normalizeYearMonth(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth).toString();
        } catch (Exception e) {
            return YearMonth.now().toString();
        }
    }

    // 기존 동기식 폼과 동일하게 고정비·OTT만 반복 등록 값을 정리한다.
    private void applyRepeatSettings(ExpenseDTO expenseDTO) {
        boolean repeatTarget = "FIXED".equals(expenseDTO.getExpense_type()) || "OTT".equals(expenseDTO.getExpense_type());
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
