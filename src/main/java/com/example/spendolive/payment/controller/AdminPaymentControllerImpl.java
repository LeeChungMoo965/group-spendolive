package com.example.spendolive.payment.controller;

import java.text.DecimalFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.payment.domain.PaymentAjaxResponse;
import com.example.spendolive.payment.domain.SettlementPaymentVO;
import com.example.spendolive.payment.exception.PaymentProcessException;
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
    @GetMapping("/paymentdetaillist.do")
    public ModelAndView paymentdetaillistUpSettlement(@RequestParam(value = "status", required = false) String status,HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        if(status==null){status = "READY";}
        
        try {
            List<SettlementPaymentVO> paymentdetailList = paymentService.selectpaymentAll();
            session.setAttribute("paymentdetailList", paymentdetailList);
            return layout("/WEB-INF/views/admin/settlement/paymentdetail.jsp");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "리스트업에 실패 하였습니다. ");
            return layout("/WEB-INF/views/admin/settlement/paymentdetail.jsp");
        }
    }
    @Override
    @PostMapping("/pay.do")
    public ResponseEntity<PaymentAjaxResponse> pay(@RequestParam("room_id") int room_id, HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        
        try {
            String msg = paymentService.updateExcrow(room_id);
            
            return ResponseEntity.ok(new PaymentAjaxResponse(
                    true,
                    "SETTLEMENT_COMPLETED",
                    msg,
                    "PAID",
                    null,
                    "/admin/settlement/list.do"));
        } catch (PaymentProcessException e) {
            return ResponseEntity
                    .status(resolveHttpStatus(e.getCode()))
                    .body(new PaymentAjaxResponse(
                            false,
                            e.getCode(),
                            e.getMessage(),
                            "FAILED",
                            null,
                            null));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentAjaxResponse(
                            false,
                            "SETTLEMENT_FAILED",
                            "송금 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            "FAILED",
                            null,
                            null));
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
    public ResponseEntity<PaymentAjaxResponse> payment(
        @RequestParam("member_login_id") String member_login_id,@RequestParam("room_id") String room_idStr,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {


        int room_id = Integer.parseInt(room_idStr);
        OttSettlementDTO settlementInfo = (OttSettlementDTO) paymentService.selectMySettlements(room_id);
        int total_amount =  settlementInfo.getTotal_price();
        int member_limit = settlementInfo.getMember_limit();
        int base_amount = total_amount / member_limit;
        int fee_amount = (base_amount / 100) * member_limit;
        int total_price = base_amount + fee_amount; 
        String host_id = settlementInfo.getHost_login_id();
        int settlement_id = (int) settlementInfo.getSettlement_id().longValue();

        try {
            paymentService.executeAutomaticPayment(member_login_id, total_price, room_id,fee_amount ,base_amount, settlement_id, host_id);
            return ResponseEntity.ok(new PaymentAjaxResponse(
                    true,
                    "PAYMENT_COMPLETED",
                    "결제가 완료되었습니다.",
                    "PAID",
                    null,
                    "/admin/settlement/paymentlist.do"));
      
            
        } catch (PaymentProcessException e) {
            return ResponseEntity
                    .status(resolveHttpStatus(e.getCode()))
                    .body(new PaymentAjaxResponse(
                            false,
                            e.getCode(),
                            e.getMessage(),
                            "FAILED",
                            null,
                            null));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentAjaxResponse(
                            false,
                            "PAYMENT_FAILED",
                            "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            "FAILED",
                            null,
                            null));
        }
    }
    @Override
    @PostMapping("/paymentlate.do")
    public ResponseEntity<PaymentAjaxResponse> paymentlate(
        @RequestParam("member_login_id") String member_login_id ,@RequestParam("room_id") int room_id,@RequestParam("pay_late_day") int pay_late_day,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {

        try {
            paymentService.updateTodaysettlementroommemberlate(room_id,member_login_id,pay_late_day);
            return ResponseEntity.ok(new PaymentAjaxResponse(
                true,
                "LATEDAY_COMPLETED",
                "연기가 완료되었습니다.",
                "COMPLETE",
                null,
                "/admin/settlement/paymentlist.do"));
            
        } catch (PaymentProcessException e) {
            return ResponseEntity
                    .status(resolveHttpStatus(e.getCode()))
                    .body(new PaymentAjaxResponse(
                            false,
                            e.getCode(),
                            e.getMessage(),
                            "FAILED",
                            null,
                            "/admin/settlement/paymentlist.do"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentAjaxResponse(
                            false,
                            "LATEDAY_FAILED",
                            "정산 연기 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            "FAILED",
                            null,
                            "/admin/settlement/paymentlist.do"));
        }
    }
    @Override
    @PostMapping("/cancelpaymenting.do")
    public ResponseEntity<PaymentAjaxResponse> calcelpayment(
            SettlementPaymentVO payment,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {

        try {
            
            paymentService.executeRoomRefund(payment);
            return ResponseEntity.ok(new PaymentAjaxResponse(
                    true,
                    "REFUND_COMPLETED",
                    "취소가 완료되었습니다.",
                    "REFUNDED",
                    null,
                    "/admin/settlement/paymentdetaillist.do"));
        } catch (PaymentProcessException e) {
            return ResponseEntity
                    .status(resolveHttpStatus(e.getCode()))
                    .body(new PaymentAjaxResponse(
                            false,
                            e.getCode(),
                            e.getMessage(),
                            "FAILED",
                            null,
                            "/admin/settlement/paymentdetaillist.do"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentAjaxResponse(
                            false,
                            "REFUND_FAILED",
                            "결제 취소 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            "FAILED",
                            null,
                            "/admin/settlement/paymentdetaillist.do"));
        }
    }
    private HttpStatus resolveHttpStatus(String code) {
        if ("PAYMENT_PROCESSING".equals(code)
                || "ROOM_FULL".equals(code)
                || "PAYMENT_NOT_ALLOWED".equals(code)) {
            return HttpStatus.CONFLICT;
        }

        if ("CARD_REQUIRED".equals(code)
                || "INVALID_PAYMENT_INFO".equals(code)
                || "HOST_CANNOT_PAY".equals(code)) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    @Override
    @GetMapping(value = "/status.do", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<PaymentAjaxResponse> paymentStatus(
            @RequestParam(value = "room_id", required = false) int room_id,
            @RequestParam(value = "member_login_id", required = false) String member_login_id,
            @RequestParam(value = "host_id", required = false) String host_id,
            @RequestParam(value = "payment", required = false) SettlementPaymentVO payment,
            HttpServletRequest request,
            HttpSession session) throws Exception {
      // settlement_tb date들 다빼고 status랑 settlement_month 로만 사용
      String paymentStatus = "";
      int payment_id = payment.getPayment_id();
      if (host_id != null && !host_id.trim().isEmpty()){
        paymentStatus = paymentService.selectEscrowStatus(room_id, host_id); 
      }else if(member_login_id != null && !member_login_id.trim().isEmpty()){ paymentStatus = paymentService.getRoomPaymentStatus(member_login_id, room_id);
      }else {paymentStatus = paymentService.selectRefundStatus(payment_id);}
      
        if (isPaidStatus(paymentStatus)) {
            return ResponseEntity.ok(new PaymentAjaxResponse(
                    true,
                    "PAYMENT_COMPLETED",
                    "결제가 완료된 것을 확인했습니다.",
                    paymentStatus,
                    room_id,
                    null));
        }

        String message = "PROCESSING".equals(paymentStatus)
                ? "결제를 처리하고 있습니다."
                : "아직 결제가 완료되지 않았습니다.";

        return ResponseEntity.ok(new PaymentAjaxResponse(
                false,
                "PROCESSING".equals(paymentStatus)
                        ? "PAYMENT_PROCESSING"
                        : "PAYMENT_NOT_COMPLETED",
                message,
                paymentStatus,
                room_id,
                null));
    }
    private boolean isPaidStatus(String paymentStatus) {
        return "PAID".equals(paymentStatus)
                || "CONFIRMED".equals(paymentStatus);
    }
}