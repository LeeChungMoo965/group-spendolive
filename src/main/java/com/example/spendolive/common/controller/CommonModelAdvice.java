package com.example.spendolive.common.controller;

import java.util.Collections;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.service.OttService;

@ControllerAdvice
public class CommonModelAdvice {

    private final OttService ottService;

    public CommonModelAdvice(OttService ottService) {
        this.ottService = ottService;
    }

    @ModelAttribute
    public void addCommonModel(Model model, HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null) {
            model.addAttribute("chatRoomSummaryList", Collections.emptyList());
            model.addAttribute("chatTotalUnreadCount", 0);
            return;
        }

        String loginId = memberInfo.getId();
        if (loginId == null || loginId.isBlank()) {
            loginId = String.valueOf(memberInfo.getMember_id());
        }

        try {
            model.addAttribute("chatRoomSummaryList", ottService.getMyChatRooms(loginId));
            model.addAttribute("chatTotalUnreadCount", ottService.getUnreadChatCount(loginId));
        } catch (DataAccessException e) {
            // OTT 관련 테이블이 아직 생성되지 않았거나 SQL 스키마가 정리되지 않은 상태여도
            // 다른 페이지까지 깨지지 않도록 공통 채팅 위젯은 빈 값으로 둔다.
            model.addAttribute("chatRoomSummaryList", Collections.emptyList());
            model.addAttribute("chatTotalUnreadCount", 0);
        }
    }
}
