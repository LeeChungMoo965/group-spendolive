package com.example.spendolive.notification.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.notification.domain.NotificationDTO;
import com.example.spendolive.notification.service.NotificationService;

@RestController
@RequestMapping("/spendolive/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/ajax/list.do")
    public List<NotificationDTO> notificationList(HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null || memberInfo.getId() == null) {
            return Collections.emptyList();
        }

        System.out.println("로그인 ID = " + memberInfo.getId());

        return notificationService.getNotificationList(memberInfo.getId());
    }

    @GetMapping("/ajax/unreadCount.do")
    public Map<String, Integer> unreadCount(HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null || memberInfo.getId() == null) {
            return Map.of("unreadCount", 0);
        }

        int unreadCount =
                notificationService.getUnreadCount(memberInfo.getId());

        return Map.of("unreadCount", unreadCount);
    }

    @PostMapping("/ajax/read.do")
    public Map<String, String> readNotification(
            @RequestParam("notificationId") int notificationId,
            HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null || memberInfo.getId() == null) {
            return Map.of("result", "LOGIN_REQUIRED");
        }

        notificationService.readNotification(
                notificationId,
                memberInfo.getId());

        return Map.of("result", "OK");
    }

    @PostMapping("/ajax/star.do")
    public Map<String, String> toggleStar(
            @RequestParam("notificationId") int notificationId,
            HttpSession session) {

        MemberVO memberInfo =
                (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null || memberInfo.getId() == null) {
            return Map.of("result", "LOGIN_REQUIRED");
        }

        notificationService.toggleStar(
                notificationId,
                memberInfo.getId());

        return Map.of("result", "OK");
    }
}