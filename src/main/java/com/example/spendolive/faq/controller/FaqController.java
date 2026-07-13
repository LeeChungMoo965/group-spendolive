package com.example.spendolive.faq.controller;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.faq.service.FaqService;

@Controller
@RequestMapping("/spendolive/faq")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping("/list.do")
    public ModelAndView faqList() {
        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/faq/faqList.jsp");

        try {
            mav.addObject("faqGroups", faqService.getVisibleFaqGroupedByCategory());
        } catch (DataAccessException e) {
            System.err.println("[FaqController.faqList] FAQ 목록 로드 실패: " + e.getMessage());
            mav.addObject("faqGroups", Map.of());
            mav.addObject("errorMsg", "FAQ를 불러오는 중 오류가 발생했습니다.");
        }
        return mav;
    }
}
