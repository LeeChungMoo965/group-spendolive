package com.example.spendolive.notification.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.notification.domain.NotificationDTO;
import com.example.spendolive.notification.service.NotificationService;

@Controller
@RequestMapping("/spendolive/notification")
public class NotificationPageController {

    private final NotificationService notificationService;

    public NotificationPageController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /* ─── 알림 상세 페이지 ────────────────────────────────── */
    @GetMapping("/detail.do")
    public ModelAndView notificationDetail(
            @RequestParam(value = "notificationId", defaultValue = "0") int notificationId,
            HttpSession session) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        // 비로그인 → 로그인 페이지로
        if (memberInfo == null) {
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        if (notificationId <= 0) {
            ModelAndView mav = new ModelAndView("common/layout");
            mav.addObject("body_page", "/WEB-INF/views/notification/notificationDetail.jsp");
            mav.addObject("errorMsg", "잘못된 알림 번호입니다.");
            return mav;
        }

        NotificationDTO notification = null;
        try {
            notification = notificationService.getNotificationDetail(notificationId, memberInfo.getId());
        } catch (Exception e) {
            System.err.println("[NotificationPageController.detail] 조회 실패: " + e.getMessage());
        }

        if (notification == null) {
            ModelAndView mav = new ModelAndView("common/layout");
            mav.addObject("body_page", "/WEB-INF/views/notification/notificationDetail.jsp");
            mav.addObject("errorMsg", "존재하지 않는 알림입니다.");
            return mav;
        }

        // 읽음 처리
        try {
            notificationService.readNotification(notificationId, memberInfo.getId());
        } catch (Exception e) {
            System.err.println("[NotificationPageController.detail] 읽음 처리 실패: " + e.getMessage());
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/notification/notificationDetail.jsp");
        mav.addObject("notification", notification);
        return mav;
    }
}