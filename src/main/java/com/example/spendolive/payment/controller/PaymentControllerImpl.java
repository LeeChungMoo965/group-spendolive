package com.example.spendolive.payment.controller;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.ott.repository.OttRepository;
import com.example.spendolive.ott.service.OttService;
import com.example.spendolive.payment.domain.SettlementPaymentVO;
import com.example.spendolive.payment.repository.PaymentRepository;
import com.example.spendolive.payment.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller("paymentController")
@RequestMapping(value="/payment")
public class PaymentControllerImpl implements PaymentController{
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OttService ottService;

    @Value("${toss.client-key}")
    private String clientKey;

    @Value("${toss.base-url}")
    private String baseUrl;
    @Override
    @RequestMapping(value = "/detail.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView detail(@RequestParam Map<String, Object> roomid, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        String roomIdStr = request.getParameter("room_id");
        int room_id = Integer.parseInt(roomIdStr);
        OttRoomDTO roomInfo = paymentService.selectRoomByRoomId(room_id);
        OttSettlementDTO settlementInfo = (OttSettlementDTO) paymentService.selectMySettlements(room_id);
        if (settlementInfo == null) {
            ModelAndView mav = new ModelAndView("redirect:/spendolive/ott.do");
            mav.addObject("msg","해당 방의 정산 정보가 없습니다.");
            return mav;
        }
        //정산금 계산 로직
        int member_limit = settlementInfo.getMember_limit();
        Integer total_price = settlementInfo.getTotal_price();
        Integer base_amount = total_price / member_limit;
        Integer fee_amount = (base_amount / 100) * member_limit;
        total_price = base_amount + fee_amount;
        session.setAttribute("total_amount", total_price);
        session.setAttribute("base_amount", base_amount);
        session.setAttribute("fee_amount", fee_amount);
        session.setAttribute("roomInfo", roomInfo);
        session.setAttribute("settlementInfo", settlementInfo);
       
        session.setAttribute("room_id", room_id);  
        return layout("/WEB-INF/views/payment/detail.jsp");
    }

    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }
    // 토스 카드 등록 성공시 callback
    @Override
    @GetMapping("/callback.do")
    public String tossCallback(
            @RequestParam("customerKey") String customerKey,
            @RequestParam("authKey") String authKey,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {

        response.setContentType("text/html; charset=UTF-8");

        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        
        String userId = memberVO.getId();

        try {
            paymentService.issueAndSaveBillingKey(customerKey, authKey, userId);
            redirectAttributes.addFlashAttribute("msg", "'결제 카드가 정상적으로 등록되었습니다! 정산 준비 완료.");
            return "redirect:/spendolive/main.do";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "'카드 등록 최종 승인 중 에러가 발생했습니다.");
            return "redirect:/spendolive/main.do";
        }
    }
    @Override
    @GetMapping("/fail.do")
    public String tossCallback(RedirectAttributes redirectAttributes) throws Exception {
            redirectAttributes.addFlashAttribute("msg", "'카드 등록에 실패 하였습니다. 카드 정보를 확인 후 다시 시도해 주세요");
            return "redirect:/spendolive/main.do";
    }
    @Override
    @GetMapping("/paymenting.do")
    public String payment(
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {

        response.setContentType("text/html; charset=UTF-8");
        OttRoomDTO roomInfo = (OttRoomDTO) session.getAttribute("roomInfo");

        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        OttSettlementDTO settlementInfo = (OttSettlementDTO) session.getAttribute("settlementInfo");
        if (settlementInfo == null) {
            redirectAttributes.addFlashAttribute("msg","정산 정보가 없어 결제할 수 없습니다.");
            return "redirect:/spendolive/ott.do";
        }
        //정산금 계산 로직
        int base_amount = (int) session.getAttribute("total_amount");
        int member_limit = settlementInfo.getMember_limit();
        int fee_amount = (base_amount / 100) * member_limit;
        int total_price = base_amount + fee_amount; 
        String host_login_id = settlementInfo.getHost_login_id();
        String userId = memberVO.getId();
        int settlement_id = (int) settlementInfo.getSettlement_id().longValue();
        int room_id = (int) settlementInfo.getRoom_id().longValue();
        int pay_day = roomInfo.getBilling_day() - 10;
        if(pay_day <= 0){
            pay_day = 30 + pay_day;
        }
        try {
            paymentService.executeAutomaticPayment(userId, total_price, room_id,fee_amount ,base_amount, settlement_id, host_login_id);
            ottService.completePaidRoomEntry((long) room_id, userId);
            redirectAttributes.addFlashAttribute("msg", "자동결제가 등록 되었습니다 ! 자동 결제일은 매월 "+ pay_day +"일 입니다.");
            session.removeAttribute("settlementInfo");
            session.removeAttribute("total_amount");
            session.removeAttribute("base_amount");
            session.removeAttribute("fee_amount");
            session.removeAttribute("roomInfo");
            session.removeAttribute("settlementInfo");
            return "redirect:/spendolive/ott/chat/room.do?room_id=" + room_id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "자동결제가 실패 되었습니다 다시 시도 해주세요");
            return "redirect:/spendolive/main.do";
        }
    }
    
}
