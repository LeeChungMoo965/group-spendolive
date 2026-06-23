package com.example.spendolive.notice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.notice.service.NoticeService;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/spendolive/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/center.do")
    public ModelAndView noticeCenter() {

        ModelAndView mav = new ModelAndView();

        mav.setViewName("common/layout");

        mav.addObject("body_page",
                "/WEB-INF/views/notice/noticeCenter.jsp");

        mav.addObject("noticeList",
                noticeService.getNoticeList());

        mav.addObject("noticeCount",
                noticeService.getNoticeCount());

        mav.addObject("importantCount",
                noticeService.getPinnedCount());

        mav.addObject("unreadCount", 0);

        return mav;
        
    }


        @GetMapping("/detail.do")
        public ModelAndView noticeDetail(@RequestParam("noticeId") int noticeId) {

        ModelAndView mav = new ModelAndView();

        mav.setViewName("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/notice/noticeDetail.jsp");

        mav.addObject("notice", noticeService.getNoticeDetail(noticeId));

        return mav;
}
}