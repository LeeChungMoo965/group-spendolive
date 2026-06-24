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

        String loginId = (String) session.getAttribute("loginId");

        if (loginId == null) {
            return Collections.emptyList();
        }

        return notificationService.getNotificationList(loginId);
    }

    @GetMapping("/ajax/unreadCount.do")
    public Map<String, Integer> unreadCount(HttpSession session) {

        String loginId = (String) session.getAttribute("loginId");

        if (loginId == null) {
            return Map.of("unreadCount", 0);
        }

        int unreadCount = notificationService.getUnreadCount(loginId);

        return Map.of("unreadCount", unreadCount);
    }

    @PostMapping("/ajax/read.do")
    public Map<String, String> readNotification(
            @RequestParam("notificationId") int notificationId,
            HttpSession session) {

        String loginId = (String) session.getAttribute("loginId");

        if (loginId == null) {
            return Map.of("result", "LOGIN_REQUIRED");
        }

        notificationService.readNotification(notificationId, loginId);

        return Map.of("result", "OK");
    }

    @PostMapping("/ajax/star.do")
    public Map<String, String> toggleStar(
            @RequestParam("notificationId") int notificationId,
            HttpSession session) {

        String loginId = (String) session.getAttribute("loginId");

        if (loginId == null) {
            return Map.of("result", "LOGIN_REQUIRED");
        }

        notificationService.toggleStar(notificationId, loginId);

        return Map.of("result", "OK");
    }
}