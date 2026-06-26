package com.example.spendolive.mypage.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.mypage.domain.MyPageDTO;
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.mypage.service.MyPageService;

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
                                     @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                     @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
                                     @RequestParam(value = "passwordChecked", required = false) String passwordChecked,
                                     HttpSession session) {
        ModelAndView mav = new ModelAndView();
        MemberVO loginMember = (MemberVO) session.getAttribute("memberInfo");

        if (loginMember == null || loginMember.getId() == null || loginMember.getId().isBlank()) {
            mav.setViewName("redirect:/member/loginForm.do");
            return mav;
        }

        try {
            MemberVO savedMember = memberService.getMemberById(loginMember.getId());
            if (savedMember == null) {
                mav.setViewName("redirect:/member/loginForm.do");
                return mav;
            }

            if (isChanged(formMember.getEmail(), savedMember.getEmail()) && !isVerified(session, "mypageEmailVerified", "mypageEmailVerifiedValue", formMember.getEmail())) {
                mav.setViewName("redirect:/spendolive/mypage.do?profileError=emailNotVerified#profile-edit");
                return mav;
            }

            if (isChanged(formMember.getPhone(), savedMember.getPhone()) && !isVerified(session, "mypagePhoneVerified", "mypagePhoneVerifiedValue", formMember.getPhone())) {
                mav.setViewName("redirect:/spendolive/mypage.do?profileError=phoneNotVerified#profile-edit");
                return mav;
            }

            String newPassword = formMember.getPassword();
            boolean changePassword = newPassword != null && !newPassword.isBlank();
            if (changePassword) {
                if (currentPassword == null || currentPassword.isBlank() || !currentPassword.equals(savedMember.getPassword())) {
                    mav.setViewName("redirect:/spendolive/mypage.do?profileError=currentPasswordMismatch#profile-edit");
                    return mav;
                }

                if (passwordConfirm == null || !newPassword.equals(passwordConfirm)) {
                    mav.setViewName("redirect:/spendolive/mypage.do?profileError=passwordMismatch#profile-edit");
                    return mav;
                }

                if (!"Y".equals(passwordChecked)) {
                    mav.setViewName("redirect:/spendolive/mypage.do?profileError=passwordCheckRequired#profile-edit");
                    return mav;
                }
            }

            formMember.setId(loginMember.getId());
            memberService.updateMyInfo(formMember, changePassword ? newPassword : null);

            MemberVO refreshedMember = memberService.getMemberById(loginMember.getId());
            if (refreshedMember != null) {
                session.setAttribute("memberInfo", refreshedMember);
            }

            clearMyPageVerificationSession(session);
            mav.setViewName("redirect:/spendolive/mypage.do?profileUpdated=Y");
        } catch (Exception e) {
            e.printStackTrace();
            mav.setViewName("redirect:/spendolive/mypage.do?profileError=updateFailed#profile-edit");
        }

        return mav;
    }

    @PostMapping("/mypage/email/send.do")
    @ResponseBody
    public String sendMyPageEmailCode(@RequestParam("email") String email, HttpSession session) {
        try {
            String verificationCode = memberService.sendVerificationEmail(email);
            session.setAttribute("mypageEmailCode", verificationCode);
            session.setAttribute("mypageEmailTarget", email);
            session.removeAttribute("mypageEmailVerified");
            session.removeAttribute("mypageEmailVerifiedValue");
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    @PostMapping("/mypage/email/verify.do")
    @ResponseBody
    public boolean verifyMyPageEmailCode(@RequestParam("email") String email,
                                         @RequestParam("inputCode") String inputCode,
                                         HttpSession session) {
        String originalCode = (String) session.getAttribute("mypageEmailCode");
        String targetEmail = (String) session.getAttribute("mypageEmailTarget");

        if (originalCode != null && originalCode.equals(inputCode) && targetEmail != null && targetEmail.equals(email)) {
            session.setAttribute("mypageEmailVerified", "Y");
            session.setAttribute("mypageEmailVerifiedValue", email);
            session.removeAttribute("mypageEmailCode");
            session.removeAttribute("mypageEmailTarget");
            return true;
        }

        return false;
    }

    @PostMapping("/mypage/phone/send.do")
    @ResponseBody
    public String sendMyPagePhoneCode(@RequestParam("phone") String phone, HttpSession session) {
        try {
            String verificationCode = memberService.sendSmsVerification(phone);
            session.setAttribute("mypagePhoneCode", verificationCode);
            session.setAttribute("mypagePhoneTarget", phone);
            session.removeAttribute("mypagePhoneVerified");
            session.removeAttribute("mypagePhoneVerifiedValue");
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    @PostMapping("/mypage/phone/verify.do")
    @ResponseBody
    public boolean verifyMyPagePhoneCode(@RequestParam("phone") String phone,
                                         @RequestParam("inputCode") String inputCode,
                                         HttpSession session) {
        String originalCode = (String) session.getAttribute("mypagePhoneCode");
        String targetPhone = (String) session.getAttribute("mypagePhoneTarget");

        if (originalCode != null && originalCode.equals(inputCode) && targetPhone != null && targetPhone.equals(phone)) {
            session.setAttribute("mypagePhoneVerified", "Y");
            session.setAttribute("mypagePhoneVerifiedValue", phone);
            session.removeAttribute("mypagePhoneCode");
            session.removeAttribute("mypagePhoneTarget");
            return true;
        }

        return false;
    }

    private boolean isChanged(String newValue, String oldValue) {
        String safeNewValue = newValue == null ? "" : newValue.trim();
        String safeOldValue = oldValue == null ? "" : oldValue.trim();
        return !safeNewValue.equals(safeOldValue);
    }

    private boolean isVerified(HttpSession session, String flagName, String valueName, String requestValue) {
        String verifiedFlag = (String) session.getAttribute(flagName);
        String verifiedValue = (String) session.getAttribute(valueName);
        return "Y".equals(verifiedFlag) && verifiedValue != null && verifiedValue.equals(requestValue);
    }

    private void clearMyPageVerificationSession(HttpSession session) {
        session.removeAttribute("mypageEmailCode");
        session.removeAttribute("mypageEmailTarget");
        session.removeAttribute("mypageEmailVerified");
        session.removeAttribute("mypageEmailVerifiedValue");
        session.removeAttribute("mypagePhoneCode");
        session.removeAttribute("mypagePhoneTarget");
        session.removeAttribute("mypagePhoneVerified");
        session.removeAttribute("mypagePhoneVerifiedValue");
    }

    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }
}
