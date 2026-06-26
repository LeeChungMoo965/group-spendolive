package com.example.spendolive.notice.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.notice.domain.NoticeDTO;
import com.example.spendolive.notice.service.NoticeService;

@Controller
@RequestMapping("/spendolive/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/center.do")
    public ModelAndView noticeCenter(
            @RequestParam(value = "tab", required = false, defaultValue = "notice") String tab,
            HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");


        String id =
                memberInfo == null ? null : memberInfo.getId();
      

        ModelAndView mav = new ModelAndView();

        mav.addObject("loginYn", memberInfo != null);
        mav.setViewName("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/notice/noticeCenter.jsp");
        mav.addObject("tab", tab);

        mav.addObject("noticeList", noticeService.getNoticeList(id));
        mav.addObject("noticeCount", noticeService.getNoticeCount());
        mav.addObject("importantCount", noticeService.getPinnedCount());
        

        return mav;
    }

    @GetMapping("/detail.do")
    public ModelAndView noticeDetail(
            @RequestParam("noticeId") int noticeId,
            HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo != null && memberInfo.getId() != null) {
            noticeService.readNotice(
                    noticeId,
                    memberInfo.getId());
        }

        ModelAndView mav = new ModelAndView();

        mav.setViewName("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/notice/noticeDetail.jsp");
        mav.addObject("notice", noticeService.getNoticeDetail(noticeId));

        return mav;
    }

    @GetMapping("/ajax/noticeList.do")
    @ResponseBody
    public List<NoticeDTO> ajaxNoticeList(HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        String id =
                memberInfo == null ? null : memberInfo.getId();

        return noticeService.getNoticeList(id);
    }

    @GetMapping("/ajax/importantList.do")
    @ResponseBody
    public List<NoticeDTO> ajaxImportantList(HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        String id =
                memberInfo == null ? null : memberInfo.getId();

        return noticeService.getImportantList(id);
    }

    @GetMapping("/ajax/unreadNoticeList.do")
    @ResponseBody
    public List<NoticeDTO> ajaxUnreadNoticeList(HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null || memberInfo.getId() == null) {
            return List.of();
        }

        return noticeService.getUnreadNoticeList(
                memberInfo.getId());
    }

    
}