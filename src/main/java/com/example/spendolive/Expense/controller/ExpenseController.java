package com.example.spendolive.Expense.controller;

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
                            Model model,
                            RedirectAttributes redirectAttributes,  
                            HttpSession session) {
        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요한 기능 입니다 로그인을 해주세요 !");
            return "redirect:/member/loginForm.do?log=expense";
        }

        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().toString();
        }

        YearMonth selectedMonth = YearMonth.parse(yearMonth);
        List<ExpenseDTO> expenseList = expenseService.getExpenseList(memberId, yearMonth);

        model.addAttribute("selectedYearMonth", yearMonth);
        model.addAttribute("monthList", makeMonthList(yearMonth));
        model.addAttribute("expenseList", expenseList);
        model.addAttribute("categoryList", expenseService.getCategoryList());

        // 지출관리 상단 월별 요약 영역
        model.addAttribute("expenseTypeSummary", makeExpenseTypeSummary(expenseList));

        // 지출관리 하단 분석 영역
        model.addAttribute("selectedMonthTotal", sumAmount(expenseList));
        model.addAttribute("monthChartList", makeMonthChartList(memberId, selectedMonth));
        model.addAttribute("categorySummaryList", makeCategorySummaryList(expenseList));
        model.addAttribute("rankingList", makeRankingList(expenseList));

        model.addAttribute("body_page", "/WEB-INF/views/expense/expense.jsp");
        session.removeAttribute("log");
        return "common/layout";
    }

    @PostMapping("/add.do")
    public String addExpense(@ModelAttribute ExpenseDTO expenseDTO,
                             @RequestParam(value = "yearMonth", required = false) String yearMonth,
                             HttpSession session) {

        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseDTO.setMemberId(memberId);
        applyRepeatSettings(expenseDTO);

        expenseService.addExpense(expenseDTO);

        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().toString();
        }

        return "redirect:/spendolive/expense/list.do?yearMonth=" + yearMonth;
    }

    @PostMapping("/modify.do")
    public String modifyExpense(@ModelAttribute ExpenseDTO expenseDTO,
                                @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                HttpSession session) {

        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseDTO.setMemberId(memberId);
        applyRepeatSettings(expenseDTO);

        expenseService.modifyExpense(expenseDTO);

        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().toString();
        }

        return "redirect:/spendolive/expense/list.do?yearMonth=" + yearMonth;
    }

    @PostMapping("/delete.do")
    public String deleteExpense(@RequestParam("expenseId") Long expenseId,
                                @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                HttpSession session) {

        Long memberId = getLoginMemberId(session);

        if (memberId == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseService.removeExpense(expenseId, memberId);

        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().toString();
        }

        return "redirect:/spendolive/expense/list.do?yearMonth=" + yearMonth;
    }

    private Long getLoginMemberId(HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null) {
            return null;
        }

        return Long.valueOf(memberInfo.getMember_id());
    }

    private boolean isRepeatTargetType(String expenseType) {
        return "FIXED".equals(expenseType) || "OTT".equals(expenseType);
    }

    private void applyRepeatSettings(ExpenseDTO expenseDTO) {
        if (isRepeatTargetType(expenseDTO.getExpenseType())) {
            expenseDTO.setFixedYn("Y");

            if (expenseDTO.getRepeatCycle() == null || expenseDTO.getRepeatCycle().isBlank()) {
                expenseDTO.setRepeatYn("N");
                expenseDTO.setRepeatCycle(null);
            } else {
                expenseDTO.setRepeatYn("Y");
            }
        } else {
            expenseDTO.setFixedYn("N");
            expenseDTO.setRepeatYn("N");
            expenseDTO.setRepeatCycle(null);
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
            String expenseType = expense.getExpenseType();

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

            if (maxTotal > 0) {
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
        int totalAmount = sumAmount(expenseList);

        for (ExpenseDTO expense : expenseList) {
            String categoryName = expense.getCategoryName();

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
            categoryData.put("categoryName", entry.getKey());
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
