package com.example.spendolive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.bind.annotation.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/spendolive")
public class SpendOliveController {

    @RequestMapping(value= "/main.do" ,method={RequestMethod.POST,RequestMethod.GET})
    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView mav = new ModelAndView();
	    HttpSession session = request.getSession();
        mav.setViewName("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/member/main.jsp");
        return mav;
    }

    @GetMapping("/expense.do")
    public String expense() {
        return "member/expense";
    }

    @GetMapping("/calendar.do")
    public String calendar() {
        return "member/calendar";
    }

    @GetMapping("/ott.do")
    public String ott() {
        return "member/ott";
    }

    @GetMapping("/mypage.do")
    public String mypage() {
        return "member/mypage";
    }

    @GetMapping("/login.do")
    public String login() {
        return "member/login";
    }

    @GetMapping("/signup.do")
    public String signup() {
        return "member/signup";
    }
}