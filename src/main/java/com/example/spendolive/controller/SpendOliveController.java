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
        addMainDashboardData(mav, request.getSession());
        return mav;
    }

    @RequestMapping(value = {"/admin/main.do"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView adminmain(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return layout("/WEB-INF/views/admin/main.jsp");
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
     *
     * 역할:
     * - 로그인 상태이면 DB에 저장된 이번 달 지출 데이터를 조회해서 JSP에 넘긴다.
     * - 비로그인 상태이면 JSP/JS에서 랜덤 대시보드를 돌릴 수 있도록 로그인 여부만 넘긴다.
     *
     * 데이터 기준:
     * - FIXED    : 고정지출
     * - VARIABLE : 변동지출
     * - OTT      : OTT지출
     * - 총지출   : 고정지출 + 변동지출 + OTT지출
     */
    private void addMainDashboardData(ModelAndView mav, HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        // 비로그인 사용자는 DB 조회를 하지 않고, 화면 JS에서 랜덤 값으로 보여준다.
        if (memberInfo == null) {
            mav.addObject("mainLoggedIn", false);
            mav.addObject("mainFixedTotal", 0);
            mav.addObject("mainVariableTotal", 0);
            mav.addObject("mainOttTotal", 0);
            return;
        }

        long memberId = memberInfo.getMember_id();
        String currentYearMonth = YearMonth.now().toString();

        int fixedTotal = 0;
        int variableTotal = 0;
        int ottTotal = 0;

        List<ExpenseDTO> expenseList = expenseService.getExpenseList(memberId, currentYearMonth);

        for (ExpenseDTO expense : expenseList) {
            int amount = expense.getAmount() == null ? 0 : expense.getAmount();
            String expenseType = expense.getExpenseType();

            if ("FIXED".equals(expenseType)) {
                fixedTotal += amount;
            } else if ("VARIABLE".equals(expenseType)) {
                variableTotal += amount;
            } else if ("OTT".equals(expenseType)) {
                ottTotal += amount;
            }
        }

        mav.addObject("mainLoggedIn", true);
        mav.addObject("mainFixedTotal", fixedTotal);
        mav.addObject("mainVariableTotal", variableTotal);
        mav.addObject("mainOttTotal", ottTotal);
    }
}
