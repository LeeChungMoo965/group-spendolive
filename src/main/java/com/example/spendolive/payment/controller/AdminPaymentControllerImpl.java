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
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
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
    @Autowired
    private MemberService memberService;
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
    public String pay(@RequestParam("room_id") String roomIdStr, HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        int room_id = Integer.parseInt(roomIdStr);
        
        try {
            String msg = paymentService.updateExcrow(room_id);
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
    @Override
    @PostMapping("/paymenting.do")
    public String payment(
        @RequestParam("userId") String userId,@RequestParam("roomId") String roomIdStr,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {

        response.setContentType("text/html; charset=UTF-8");
        int roomId = Integer.parseInt(roomIdStr);
        OttSettlementDTO settlementInfo = (OttSettlementDTO) paymentService.selectMySettlements(roomId);
        int total_amount =  settlementInfo.getTotal_price();
        int member_limit = settlementInfo.getMember_limit();
        int base_amount = total_amount / member_limit;
        int fee_amount = (base_amount / 100) * member_limit;
        int total_price = base_amount + fee_amount; 
        String host_id = settlementInfo.getHost_login_id();
        int settlement_id = (int) settlementInfo.getSettlement_id().longValue();

        try {
            paymentService.executeAutomaticPayment(userId, total_price, roomId,fee_amount ,base_amount, settlement_id, host_id);
            redirectAttributes.addFlashAttribute("msg", "결제가 완료 되었습니다 !");
            return "redirect:/admin/settlement/paymentlist.do";
      
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "결제가 실패 되었습니다 다시 시도 해주세요");
            return "redirect:/admin/settlement/paymentlist.do";
        }
    }
    @Override
    @PostMapping("/paymentlate.do")
    public String paymentlate(
        @RequestParam("userId") String userId,@RequestParam("roomId") String roomIdStr,@RequestParam("late_day") String late_dayStr,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {

        response.setContentType("text/html; charset=UTF-8");
        int roomId = Integer.parseInt(roomIdStr);
        int late_day = Integer.parseInt(late_dayStr);

        try {
            paymentService.updateTodaysettlementroommemberlate(roomId,userId,late_day);
            redirectAttributes.addFlashAttribute("msg", "정산이 하루 연기 되었습니다 !");
            return "redirect:/admin/settlement/paymentlist.do";
      
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "정산 연기 실패 되었습니다 다시 시도 해주세요");
            return "redirect:/admin/settlement/paymentlist.do";
        }
    }
}