package com.example.spendolive.report.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.report.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
@Controller("ReportController")
@RequestMapping(value="/report")
public class ReportControllerImpl implements ReportController{
    @Autowired
    private ReportService reportService;
    @Override
    @GetMapping("/report.do")
    public String insertReport(@RequestParam String reported_member_id,@RequestParam String room_id,@RequestParam String chat_text, HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        
        try {
            reportService.insertReport(reported_member_id, room_id, chat_text, memberInfo);
            redirectAttributes.addFlashAttribute("msg", "신고가 완료 되었습니다. ");
            return "redirect:/spendolive/main.do";

        } catch (Exception e) {
            System.out.println(reported_member_id);
            System.out.println(room_id);
            System.out.println(chat_text);
            System.err.println("🚨 [신고 저장 오류]: " + e.getMessage());
            redirectAttributes.addFlashAttribute("msg", "신고에 실패 하였습니다. ");
            return "redirect:/spendolive/main.do";
        }
    }
}