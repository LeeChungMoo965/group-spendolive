package com.example.spendolive.calendar.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 캘린더 페이지 컨트롤러.
 * - 화면(JSP)을 보여주는 역할만 담당한다.
 * - 실제 지출 데이터는 CalendarApiController(/spendolive/calendar/expenses.do)가 AJAX로 내려준다.
 */
@Controller
@RequestMapping("/spendolive")
public class CalendarController {

    @RequestMapping(value = "/calendar.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView calendar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/calendar/calendar.jsp");
        return mav;
    }
}