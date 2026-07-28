package com.example.spendolive.Expense.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String expenseList(@RequestParam(value = "yearMonth", required = false) String yearMonth,
                            @RequestParam(value = "date", required = false) String date,                        
                            Model model,
                            RedirectAttributes redirectAttributes,  
                            HttpSession session) {
        Long member_id = getLoginMember_id(session);

        if (member_id == null) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요한 기능입니다. 로그인해 주세요.");
            return "redirect:/member/loginForm.do?log=expense";
        }

        // URL 파라미터를 직접 substring/parse하지 않고 먼저 검증한다.
        // 잘못된 yearMonth/date 값이 들어와도 현재 달 화면으로 안전하게 이동한다.
        String selectedDate = normalizeDate(date);
        yearMonth = normalizeYearMonth(yearMonth, selectedDate);

        YearMonth selectedMonth = YearMonth.parse(yearMonth);
        List<ExpenseDTO> monthExpenseList = expenseService.getExpenseList(member_id, yearMonth);

        // date가 지정된 경우, 테이블에는 그 날짜 지출만 필터링
        List<ExpenseDTO> tableExpenseList = monthExpenseList;
        if (selectedDate != null) {
            tableExpenseList = monthExpenseList.stream()
                    .filter(expense -> selectedDate.equals(formatDate(expense.getExpense_date())))
                    .toList();
        }

        model.addAttribute("selectedYearMonth", yearMonth);
        model.addAttribute("selectedDate", selectedDate); // JSP에서 "OO일만 보는 중" 안내용
        model.addAttribute("monthList", makeMonthList(yearMonth));
        model.addAttribute("expenseList", tableExpenseList);
        model.addAttribute("categoryList", expenseService.getCategoryList());
        // 선택한 달의 예산을 지출관리 화면에 전달한다.
        model.addAttribute("monthlyBudget", expenseService.getMonthlyBudget(member_id, yearMonth));

        // 상단 요약/하단 분석은 달 전체 기준으로 유지 (monthExpenseList 사용)
        model.addAttribute("expenseTypeSummary", makeExpenseTypeSummary(monthExpenseList));
        model.addAttribute("selectedMonthTotal", sumAmount(monthExpenseList));
        model.addAttribute("monthChartList", makeMonthChartList(member_id, selectedMonth));
        model.addAttribute("categorySummaryList", makeCategorySummaryList(monthExpenseList));
        model.addAttribute("rankingList", makeRankingList(monthExpenseList));

        model.addAttribute("body_page", "/WEB-INF/views/expense/expense.jsp");
        session.removeAttribute("log");
        return "common/layout";
    }

    // java.util.Date를 화면 비교용 yyyy-MM-dd 문자열로 변환한다.
    private String formatDate(java.util.Date date) {
        if (date == null) return null;
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    

    // 지출관리 화면에서 입력한 월 예산을 저장한다.
    @PostMapping("/budget/save.do")
    public String saveMonthlyBudget(@RequestParam("budget_month") String budget_month,
                                    @RequestParam("budget_amount") int budget_amount,
                                    HttpSession session) {

        Long member_id = getLoginMember_id(session);

        if (member_id == null) {
            return "redirect:/member/loginForm.do";
        }

        String targetMonth = budget_month;

        try {
            targetMonth = YearMonth.parse(budget_month).toString();
        } catch (Exception e) {
            targetMonth = YearMonth.now().toString();
        }

        // 음수 예산은 0원으로 보정한다.
        int safeBudgetAmount = Math.max(budget_amount, 0);
        expenseService.saveMonthlyBudget(member_id, targetMonth, safeBudgetAmount);

        return "redirect:/spendolive/expense/list.do?yearMonth="
                + targetMonth
                + "&budgetSaved=Y";
    }

    @PostMapping("/add.do")
    public String addExpense(@ModelAttribute ExpenseDTO expenseDTO,
                             @RequestParam(value = "yearMonth", required = false) String yearMonth,
                             HttpSession session) {

        Long member_id = getLoginMember_id(session);

        if (member_id == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseDTO.setMember_id(member_id);
        applyRepeatSettings(expenseDTO);

        expenseService.addExpense(expenseDTO);

        yearMonth = normalizeYearMonth(yearMonth, null);
        return "redirect:/spendolive/expense/list.do?yearMonth=" + yearMonth;
    }

    @PostMapping("/modify.do")
    public String modifyExpense(@ModelAttribute ExpenseDTO expenseDTO,
                                @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                HttpSession session) {

        Long member_id = getLoginMember_id(session);

        if (member_id == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseDTO.setMember_id(member_id);
        applyRepeatSettings(expenseDTO);

        expenseService.modifyExpense(expenseDTO);

        yearMonth = normalizeYearMonth(yearMonth, null);
        return "redirect:/spendolive/expense/list.do?yearMonth=" + yearMonth;
    }

    @PostMapping("/delete.do")
    public String deleteExpense(@RequestParam("expense_id") Long expense_id,
                                @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                HttpSession session) {

        Long member_id = getLoginMember_id(session);

        if (member_id == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseService.removeExpense(expense_id, member_id);

        yearMonth = normalizeYearMonth(yearMonth, null);
        return "redirect:/spendolive/expense/list.do?yearMonth=" + yearMonth;
    }

    // yyyy-MM 형식이 아니면 date의 연월 또는 현재 연월로 보정한다.
    private String normalizeYearMonth(String yearMonth, String date) {
        if (yearMonth != null && !yearMonth.isBlank()) {
            try {
                return YearMonth.parse(yearMonth).toString();
            } catch (Exception ignored) {
                // 잘못된 주소값은 아래 기본값으로 처리한다.
            }
        }

        if (date != null) {
            return YearMonth.from(LocalDate.parse(date)).toString();
        }

        return YearMonth.now().toString();
    }

    // yyyy-MM-dd 형식이 아니면 날짜 필터를 사용하지 않는다.
    private String normalizeDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(date).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private Long getLoginMember_id(HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null) {
            return null;
        }

        return Long.valueOf(memberInfo.getMember_id());
    }

    private boolean isRepeatTargetType(String expense_type) {
        return "FIXED".equals(expense_type) || "OTT".equals(expense_type);
    }

    private void applyRepeatSettings(ExpenseDTO expenseDTO) {
        if (isRepeatTargetType(expenseDTO.getExpense_type())) {
            expenseDTO.setFixed_yn("Y");

            if (expenseDTO.getRepeat_cycle() == null || expenseDTO.getRepeat_cycle().isBlank()) {
                expenseDTO.setRepeat_yn("N");
                expenseDTO.setRepeat_cycle(null);
            } else {
                expenseDTO.setRepeat_yn("Y");
            }
        } else {
            expenseDTO.setFixed_yn("N");
            expenseDTO.setRepeat_yn("N");
            expenseDTO.setRepeat_cycle(null);
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
            String expense_type = expense.getExpense_type();

            if (expense_type == null || !typeSummary.containsKey(expense_type)) {
                continue;
            }

            typeSummary.put(expense_type, typeSummary.get(expense_type) + safeAmount(expense));
        }

        return typeSummary;
    }

    private List<Map<String, Object>> makeMonthChartList(Long member_id, YearMonth selectedMonth) {
        List<Map<String, Object>> monthChartList = new ArrayList<>();
        List<Integer> totalList = new ArrayList<>();

        for (int i = -2; i <= 0; i++) {
            YearMonth month = selectedMonth.plusMonths(i);
            int total = sumAmount(expenseService.getExpenseList(member_id, month.toString()));
            totalList.add(total);
        }

        int maxTotal = totalList.stream().max(Integer::compareTo).orElse(0);

        for (int i = -2; i <= 0; i++) {
            YearMonth month = selectedMonth.plusMonths(i);
            int total = totalList.get(i + 2);
            int barPercent = 0;

            if (maxTotal > 0 && total > 0) {
                barPercent = Math.max(8, (int) Math.round((total * 100.0) / maxTotal));
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
        int total_amount = sumAmount(expenseList);

        for (ExpenseDTO expense : expenseList) {
            String category_name = expense.getCategory_name();

            if (category_name == null || category_name.isBlank()) {
                category_name = "기타";
            }

            categoryTotalMap.put(
                    category_name,
                    categoryTotalMap.getOrDefault(category_name, 0) + safeAmount(expense)
            );
        }

        List<Map<String, Object>> categorySummaryList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : categoryTotalMap.entrySet()) {
            int percent = 0;

            if (total_amount > 0) {
                percent = (int) Math.round((entry.getValue() * 100.0) / total_amount);
            }

            Map<String, Object> categoryData = new LinkedHashMap<>();
            categoryData.put("category_name", entry.getKey());
            categoryData.put("total", entry.getValue());
            categoryData.put("percent", percent);
            categorySummaryList.add(categoryData);
        }

        categorySummaryList.sort((a, b) -> Integer.compare((Integer) b.get("total"), (Integer) a.get("total")));

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