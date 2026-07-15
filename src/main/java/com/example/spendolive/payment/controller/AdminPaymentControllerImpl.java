package com.example.spendolive.payment.controller;

import java.text.DecimalFormat;
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
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.payment.service.PaymentService;
import com.example.spendolive.report.domain.ReportVO;
import com.example.spendolive.report.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
@Controller("AdminPaymentController")
@RequestMapping(value="/admin/settlement")
public class AdminPaymentControllerImpl implements AdminPaymentController{
    @Autowired
    private PaymentService paymentService;
    @Override
    @GetMapping("/list.do")
    public ModelAndView listUpSettlement(@RequestParam(value = "status", required = false) String status,HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        if(status==null){status = "READY";}
        
        try {
            List<OttRoomDTO> settlementList = paymentService.selectTodaysettlement(status);
           
            session.setAttribute("settlementList", settlementList);
            return layout("/WEB-INF/views/admin/settlement/settlement.jsp");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "리스트업에 실패 하였습니다. ");
            return layout("/WEB-INF/views/admin/settlement/settlement.jsp");
        }
    }
    @Override
    @GetMapping("/paymentlist.do")
    public ModelAndView paymentlistUpSettlement(@RequestParam(value = "status", required = false) String status,HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        if(status==null){status = "READY";}
        
        try {
            List<OttRoomMemberDTO> paymentList = paymentService.selectTodaysettlementmember(status);

            session.setAttribute("paymentList", paymentList);
            return layout("/WEB-INF/views/admin/settlement/payment.jsp");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "리스트업에 실패 하였습니다. ");
            return layout("/WEB-INF/views/admin/settlement/payment.jsp");
        }
    }
    @Override
    @PostMapping("/pay.do")
    public String pay(@RequestParam("roomId") String roomIdStr, HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        int roomId = Integer.parseInt(roomIdStr);
        
        try {
            String msg = paymentService.updateExcrow(roomId);
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:/admin/settlement/list.do";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "정산에 실패 하였습니다. ");
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