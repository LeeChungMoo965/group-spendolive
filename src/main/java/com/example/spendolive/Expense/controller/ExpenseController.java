
package com.example.spendolive.Expense.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.Expense.domain.ExpenseDTO;
import com.example.spendolive.Expense.service.ExpenseService;
import com.example.spendolive.common.ajax.AjaxAuthSupport;
import com.example.spendolive.common.ajax.AjaxDuplicateGuard;
import com.example.spendolive.common.ajax.AjaxResponse;
import com.example.spendolive.member.domain.MemberVO;

import jakarta.servlet.http.HttpSession;

/**
 * 지출관리 통합 Controller.
 *
 * 화면 조회는 SSR 방식으로 처리하고,
 * 예산 저장 및 지출 등록·수정·삭제는 AJAX(JSON) 방식만 사용한다.
 * 기존 일반 POST 매핑은 AJAX 매핑과 기능이 중복되므로 제거했다.
 */
@Controller
@RequestMapping("/spendolive/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final AjaxDuplicateGuard duplicateGuard;

    public ExpenseController(ExpenseService expenseService,
                             AjaxDuplicateGuard duplicateGuard) {
        this.expenseService = expenseService;
        this.duplicateGuard = duplicateGuard;
    }

    // -------------------------------------------------------------------------
    // SSR 화면 조회
    // -------------------------------------------------------------------------

    @GetMapping("/list.do")
    public String expenseList(@RequestParam(value = "yearMonth", required = false) String yearMonth,
                              @RequestParam(value = "date", required = false) String date,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {

        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요한 기능입니다. 로그인해 주세요.");
            return "redirect:/member/loginForm.do?log=expense";
        }

        String selectedDate = normalizeDate(date);
        String selectedYearMonth = normalizeYearMonth(yearMonth, selectedDate);
        YearMonth selectedMonth = YearMonth.parse(selectedYearMonth);

        List<ExpenseDTO> monthExpenseList = expenseService.getExpenseList(memberId, selectedYearMonth);
        List<ExpenseDTO> tableExpenseList = monthExpenseList;

        if (selectedDate != null) {
            tableExpenseList = monthExpenseList.stream()
                    .filter(expense -> selectedDate.equals(formatDate(expense.getExpense_date())))
                    .toList();
        }

        model.addAttribute("selectedYearMonth", selectedYearMonth);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("monthList", makeMonthList(selectedYearMonth));
        model.addAttribute("expenseList", tableExpenseList);
        model.addAttribute("categoryList", expenseService.getCategoryList());
        model.addAttribute("monthlyBudget", expenseService.getMonthlyBudget(memberId, selectedYearMonth));

        model.addAttribute("expenseTypeSummary", makeExpenseTypeSummary(monthExpenseList));
        model.addAttribute("selectedMonthTotal", sumAmount(monthExpenseList));
        model.addAttribute("monthChartList", makeMonthChartList(memberId, selectedMonth));
        model.addAttribute("categorySummaryList", makeCategorySummaryList(monthExpenseList));
        model.addAttribute("rankingList", makeRankingList(monthExpenseList));

        model.addAttribute("body_page", "/WEB-INF/views/expense/expense.jsp");
        session.removeAttribute("log");

        return "common/layout";
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
            String targetMonth = normalizeYearMonth(budgetMonth, null);

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

            String targetMonth = normalizeYearMonth(yearMonth, null);

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

            String targetMonth = normalizeYearMonth(yearMonth, null);

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

            String targetMonth = normalizeYearMonth(yearMonth, null);

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
    // 공통 보정 및 화면 데이터 구성
    // -------------------------------------------------------------------------

    private String formatDate(java.util.Date date) {
        if (date == null) {
            return null;
        }

        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String normalizeYearMonth(String yearMonth, String date) {
        if (yearMonth != null && !yearMonth.isBlank()) {
            try {
                return YearMonth.parse(yearMonth).toString();
            } catch (Exception ignored) {
                // 아래 기본값으로 보정한다.
            }
        }

        if (date != null) {
            try {
                return YearMonth.from(LocalDate.parse(date)).toString();
            } catch (Exception ignored) {
                // 아래 현재 연월로 보정한다.
            }
        }

        return YearMonth.now().toString();
    }

    private String normalizeDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(date).toString();
        } catch (Exception exception) {
            return null;
        }
    }

    private Long getLoginMemberId(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object memberInfo = session.getAttribute("memberInfo");
        if (!(memberInfo instanceof MemberVO member)) {
            return null;
        }

        return Long.valueOf(member.getMember_id());
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

    private List<String> makeMonthList(String selectedYearMonth) {
        YearMonth selected = YearMonth.parse(selectedYearMonth);
        List<String> monthList = new ArrayList<>();

        for (int i = -2; i <= 4; i++) {
            monthList.add(selected.plusMonths(i).toString());
        }

        return monthList;
    }

    private Map<String, Integer> makeExpenseTypeSummary(List<ExpenseDTO> expenseList) {
        Map<String, Integer> typeSummary = new LinkedHashMap<>();
        typeSummary.put("FIXED", 0);
        typeSummary.put("VARIABLE", 0);
        typeSummary.put("OTT", 0);

        for (ExpenseDTO expense : expenseList) {
            String expenseType = expense.getExpense_type();

            if (expenseType == null || !typeSummary.containsKey(expenseType)) {
                continue;
            }

            typeSummary.put(expenseType, typeSummary.get(expenseType) + safeAmount(expense));
        }

        return typeSummary;
    }

    private List<Map<String, Object>> makeMonthChartList(Long memberId, YearMonth selectedMonth) {
        List<Map<String, Object>> monthChartList = new ArrayList<>();
        List<Integer> totalList = new ArrayList<>();

        for (int i = -2; i <= 0; i++) {
            YearMonth month = selectedMonth.plusMonths(i);
            int total = sumAmount(expenseService.getExpenseList(memberId, month.toString()));
            totalList.add(total);
        }

        int maxTotal = totalList.stream().max(Integer::compareTo).orElse(0);

        for (int i = -2; i <= 0; i++) {
            YearMonth month = selectedMonth.plusMonths(i);
            int total = totalList.get(i + 2);
            int barPercent = 0;

            if (maxTotal > 0 && total > 0) {
                barPercent = (int) Math.round((total * 100.0) / maxTotal);
            }

            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", month.toString());
            monthData.put("monthLabel", month.getMonthValue() + "월");
            monthData.put("total", total);
            monthData.put("barPercent", barPercent);
            monthChartList.add(monthData);
        }

        return monthChartList;
    }

    private List<Map<String, Object>> makeCategorySummaryList(List<ExpenseDTO> expenseList) {
        Map<String, Integer> categoryTotalMap = new LinkedHashMap<>();
        int totalAmount = sumAmount(expenseList);

        for (ExpenseDTO expense : expenseList) {
            String categoryName = expense.getCategory_name();

            if (categoryName == null || categoryName.isBlank()) {
                categoryName = "기타";
            }

            categoryTotalMap.put(
                    categoryName,
                    categoryTotalMap.getOrDefault(categoryName, 0) + safeAmount(expense)
            );
        }

        List<Map<String, Object>> categorySummaryList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : categoryTotalMap.entrySet()) {
            int percent = 0;

            if (totalAmount > 0) {
                percent = (int) Math.round((entry.getValue() * 100.0) / totalAmount);
            }

            Map<String, Object> categoryData = new LinkedHashMap<>();
            categoryData.put("category_name", entry.getKey());
            categoryData.put("total", entry.getValue());
            categoryData.put("percent", percent);
            categorySummaryList.add(categoryData);
        }

        categorySummaryList.sort(
                (first, second) -> Integer.compare(
                        (Integer) second.get("total"),
                        (Integer) first.get("total")
                )
        );

        return categorySummaryList;
    }

    private List<ExpenseDTO> makeRankingList(List<ExpenseDTO> expenseList) {
        return expenseList.stream()
                .sorted(Comparator.comparing(this::safeAmount).reversed())
                .limit(10)
                .toList();
    }

    private int sumAmount(List<ExpenseDTO> expenseList) {
        int total = 0;

        for (ExpenseDTO expense : expenseList) {
            total += safeAmount(expense);
        }

        return total;
    }

    private int safeAmount(ExpenseDTO expense) {
        if (expense == null || expense.getAmount() == null) {
            return 0;
        }

        return expense.getAmount();
    }
}
