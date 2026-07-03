package com.example.spendolive.faq.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/spendolive/faq")
public class FaqController {

    @GetMapping("/list.do")
    public ModelAndView faqList() {
        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/faq/faqList.jsp");
        return mav;
    }
}