package com.example.spendolive.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/spendolive")
public class SpendOliveController {

    @RequestMapping(value = {"/", "/main.do"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return layout("/WEB-INF/views/main/main.jsp");
    }

    @RequestMapping(value = "/calendar.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView calendar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return layout("/WEB-INF/views/calendar/calendar.jsp");
    }

    @RequestMapping(value = "/mypage.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView mypage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return layout("/WEB-INF/views/member/mypage.jsp");
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
}
