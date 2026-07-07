package com.example.spendolive.report.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.report.domain.ReportVO;
import com.example.spendolive.report.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
@Controller("AdminReportController")
@RequestMapping(value="/admin/report")
public class AdminReportControllerImpl implements AdminReportController{
    @Autowired
    private ReportService reportService;
    @Override
    @GetMapping("/list.do")
    public ModelAndView listUpReport(HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        
        
        try {
            List<ReportVO> reportList = reportService.selectReport();
            session.setAttribute("reportList", reportList);
            return layout("/WEB-INF/views/admin/report/report.jsp");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "리스트업에 실패 하였습니다. ");
            return layout("/WEB-INF/views/admin/index.jsp");
        }
    }
    @Override
    @PostMapping("/comment.do")
    public String comment(@RequestParam String admin_comment,@RequestParam String reported_member_id,@RequestParam String report_id,@RequestParam String result,  HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        
        
        try {
            reportService.insertWarning(admin_comment, reported_member_id, report_id, result);
            redirectAttributes.addFlashAttribute("msg", "처리에 성공 하였습니다. ");
            return "redirect:/admin/settlement/list.do";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "처리에 실패 하였습니다. ");
            return "redirect:/admin/settlement/list.do";
        }
    }
    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }
    
}