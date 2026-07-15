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
                            @RequestParam(value = "date", required = false) String date,                        
                            Model model,
                            RedirectAttributes redirectAttributes,  
                            HttpSession session) {
        Long member_id = getLoginMemberId(session);

        if (member_id == null) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요한 기능 입니다 로그인을 해주세요 !");
            return "redirect:/member/loginForm.do?log=expense";
        }

        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = (date != null && !date.isBlank())
                ? date.substring(0, 7)
                : YearMonth.now().toString();
    }
        

        YearMonth selectedMonth = YearMonth.parse(yearMonth);
    List<ExpenseDTO> monthExpenseList = expenseService.getExpenseList(member_id, yearMonth);

    // date가 지정된 경우, 테이블에는 그 날짜 지출만 필터링
    List<ExpenseDTO> tableExpenseList = monthExpenseList;
        if (date != null && !date.isBlank()) {
            tableExpenseList = monthExpenseList.stream()
                    .filter(expense -> date.equals(formatDate(expense.getExpense_date())))
                    .toList();
        }

        model.addAttribute("selectedYearMonth", yearMonth);
        model.addAttribute("selectedDate", date); // JSP에서 "OO일만 보는 중" 안내용
        model.addAttribute("monthList", makeMonthList(yearMonth));
        model.addAttribute("expenseList", tableExpenseList);
        model.addAttribute("categoryList", expenseService.getCategoryList());

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

    // 헬퍼 하나 추가
    private String formatDate(java.util.Date date) {
        if (date == null) return null;
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    
    @PostMapping("/add.do")
    public String addExpense(@ModelAttribute ExpenseDTO expenseDTO,
                             @RequestParam(value = "yearMonth", required = false) String yearMonth,
                             HttpSession session) {

        Long member_id = getLoginMemberId(session);

        if (member_id == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseDTO.setMember_id(member_id);
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

        Long member_id = getLoginMemberId(session);

        if (member_id == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseDTO.setMember_id(member_id);
        applyRepeatSettings(expenseDTO);

        expenseService.modifyExpense(expenseDTO);

        if (yearMonth == null || yearMonth.isBlank()) {
            yearMonth = YearMonth.now().toString();
        }

        return "redirect:/spendolive/expense/list.do?yearMonth=" + yearMonth;
    }

    @PostMapping("/delete.do")
    public String deleteExpense(@RequestParam("expense_id") Long expense_id,
                                @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                HttpSession session) {

        Long member_id = getLoginMemberId(session);

        if (member_id == null) {
            return "redirect:/member/loginForm.do";
        }

        expenseService.removeExpense(expense_id, member_id);

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
