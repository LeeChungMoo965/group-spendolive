package com.example.spendolive.member.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.domain.MyPageDTO;
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.member.service.MyPageService;

@Controller
@RequestMapping("/spendolive")
public class MyPageController {

    private final MyPageService myPageService;
    private final MemberService memberService;

    public MyPageController(MyPageService myPageService, MemberService memberService) {
        this.myPageService = myPageService;
        this.memberService = memberService;
    }

    @RequestMapping(value = "/mypage.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView mypage(HttpSession session) throws Exception {
        MemberVO sessionMember = (MemberVO) session.getAttribute("memberInfo");

        if (sessionMember == null || sessionMember.getId() == null || sessionMember.getId().isBlank()) {
            ModelAndView loginMav = new ModelAndView();
            loginMav.setViewName("redirect:/member/loginForm.do");
            return loginMav;
        }

        MyPageDTO myPage = myPageService.getMyPage(sessionMember.getId());
        MemberVO memberInfo = myPage.getMemberInfo();
        if (memberInfo == null) {
            memberInfo = sessionMember;
            myPage.setMemberInfo(memberInfo);
        } else {
            session.setAttribute("memberInfo", memberInfo);
        }

        ModelAndView mav = layout("/WEB-INF/views/member/mypage.jsp");
        mav.addObject("memberInfo", memberInfo);
        mav.addObject("profileInitial", myPage.getProfileInitial());
        mav.addObject("thisMonthExpenseTotal", myPage.getThisMonthExpenseTotal());
        mav.addObject("accountConnected", myPage.isAccountConnected());
        mav.addObject("warningCount", myPage.getWarningCount());
        mav.addObject("myReportCount", myPage.getMyReportCount());
        mav.addObject("myReportList", myPage.getMyReportList());
        mav.addObject("friendRoomList", myPage.getFriendRoomList());
        mav.addObject("hostedRecruitRoomList", myPage.getHostedRecruitRoomList());
        mav.addObject("joinedRecruitRoomList", myPage.getJoinedRecruitRoomList());
        return mav;
    }

    @PostMapping("/mypage/update.do")
    public ModelAndView updateMyInfo(@ModelAttribute MemberVO formMember,
                                     @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
                                     HttpSession session) {
        ModelAndView mav = new ModelAndView();
        MemberVO loginMember = (MemberVO) session.getAttribute("memberInfo");

        if (loginMember == null || loginMember.getId() == null || loginMember.getId().isBlank()) {
            mav.setViewName("redirect:/member/loginForm.do");
            return mav;
        }

        String newPassword = formMember.getPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            if (passwordConfirm == null || !newPassword.equals(passwordConfirm)) {
                mav.setViewName("redirect:/spendolive/mypage.do?profileError=passwordMismatch");
                return mav;
            }
        }

        try {
            formMember.setId(loginMember.getId());
            memberService.updateMyInfo(formMember, newPassword);

            MemberVO refreshedMember = memberService.getMemberById(loginMember.getId());
            if (refreshedMember != null) {
                session.setAttribute("memberInfo", refreshedMember);
            }

            mav.setViewName("redirect:/spendolive/mypage.do?profileUpdated=Y");
        } catch (Exception e) {
            e.printStackTrace();
            mav.setViewName("redirect:/spendolive/mypage.do?profileError=updateFailed");
        }

        return mav;
    }

    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }
}
