package com.example.spendolive.controller;

import java.time.YearMonth;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.Expense.domain.ExpenseDTO;
import com.example.spendolive.Expense.service.ExpenseService;
import com.example.spendolive.member.domain.MemberVO;

@Controller
@RequestMapping("/spendolive")
public class SpendOliveController {

    private final ExpenseService expenseService;

    public SpendOliveController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @RequestMapping(value = {"/", "/main.do"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = layout("/WEB-INF/views/main/main.jsp");

        // 메인 그래프에서 선택한 달의 지출과 예산을 조회한다.
        addMainDashboardData(
                mav,
                request.getSession(),
                request.getParameter("yearMonth")
        );

        return mav;
    }

    @RequestMapping(value = {"/admin/main.do"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView adminmain(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return layout("/WEB-INF/views/admin/main/main.jsp");
    }

    @RequestMapping(value = "/calendar.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView calendar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return layout("/WEB-INF/views/calendar/calendar.jsp");
    }

    @RequestMapping(value = "/expense.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView expense(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/spendolive/expense/list.do");
        return mav;
    }

    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }

    /**
     * 메인 페이지 대시보드 데이터 세팅
     * - 선택한 달의 고정·변동·OTT 지출과 월 예산을 조회한다.
     * - 비로그인 상태에서는 기존 랜덤 대시보드를 사용한다.
     */
    private void addMainDashboardData(ModelAndView mav,
                                      HttpSession session,
                                      String requestedYearMonth) {

        String selectedYearMonth = normalizeYearMonth(requestedYearMonth);
        YearMonth selectedMonth = YearMonth.parse(selectedYearMonth);

        // JSP의 달 선택 입력창과 제목에 사용할 값이다.
        mav.addObject("mainSelectedYearMonth", selectedYearMonth);
        mav.addObject("mainSelectedMonthLabel", selectedMonth.getMonthValue() + "월");

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        // 비로그인 사용자는 DB 조회 없이 기존 랜덤 값으로 보여준다.
        if (memberInfo == null) {
            mav.addObject("mainLoggedIn", false);
            mav.addObject("mainFixedTotal", 0);
            mav.addObject("mainVariableTotal", 0);
            mav.addObject("mainOttTotal", 0);
            mav.addObject("mainBudget", 1700000);
            return;
        }

        long member_id = memberInfo.getMember_id();
        int fixedTotal = 0;
        int variableTotal = 0;
        int ottTotal = 0;

        // 선택한 달의 예산과 지출 목록을 조회한다.
        int monthlyBudget = expenseService.getMonthlyBudget(member_id, selectedYearMonth);
        List<ExpenseDTO> expenseList = expenseService.getExpenseList(member_id, selectedYearMonth);

        for (ExpenseDTO expense : expenseList) {
            int amount = expense.getAmount() == null ? 0 : expense.getAmount();
            String expense_type = expense.getExpense_type();

            if ("FIXED".equals(expense_type)) {
                fixedTotal += amount;
            } else if ("VARIABLE".equals(expense_type)) {
                variableTotal += amount;
            } else if ("OTT".equals(expense_type)) {
                ottTotal += amount;
            }
        }

        mav.addObject("mainLoggedIn", true);
        mav.addObject("mainFixedTotal", fixedTotal);
        mav.addObject("mainVariableTotal", variableTotal);
        mav.addObject("mainOttTotal", ottTotal);
        mav.addObject("mainBudget", monthlyBudget);
    }

    // 잘못된 연월 값은 현재 달로 바꿔서 조회 오류를 막는다.
    private String normalizeYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            return YearMonth.now().toString();
        }

        try {
            return YearMonth.parse(yearMonth).toString();
        } catch (Exception e) {
            return YearMonth.now().toString();
        }
    }
}
