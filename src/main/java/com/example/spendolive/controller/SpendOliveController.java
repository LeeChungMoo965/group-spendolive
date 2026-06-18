package com.example.spendolive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/spendolive")
public class SpendOliveController {

    @GetMapping({"", "/", "/main.do"})
    public String main() {
        return "member/main";
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