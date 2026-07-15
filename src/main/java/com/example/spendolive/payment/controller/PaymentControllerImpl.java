package com.example.spendolive.payment.controller;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.ott.service.OttService;
import com.example.spendolive.payment.domain.SettlementPaymentVO;
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
        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String userId = memberVO.getId();
        SettlementPaymentVO settlement_PaymentInfo =  paymentService.getSettlement_PaymentByRoomId(userId, room_id);
        OttSettlementDTO settlementInfo = (OttSettlementDTO) paymentService.selectMySettlements(room_id);
        if (settlementInfo == null) {
            ModelAndView mav =
                    new ModelAndView("redirect:/spendolive/ott.do");
        
            mav.addObject(
                    "msg",
                    "해당 방의 정산 정보가 없습니다."
            );
        
            return mav;
        }
        session.setAttribute("settlementInfo", settlementInfo);
        int member_limit = settlementInfo.getMember_limit();
        Integer total_price = settlementInfo.getTotal_price();
        Integer base_amount = total_price / member_limit;
        Integer fee_amount = (base_amount / 100) * member_limit;
        total_price = base_amount + fee_amount;
        session.setAttribute("total_amount", total_price);
        session.setAttribute("base_amount", base_amount);
        session.setAttribute("fee_amount", fee_amount);
        session.setAttribute("Settlement_PaymentInfo", settlement_PaymentInfo);
       
        session.setAttribute("room_id", room_id);  
        return layout("/WEB-INF/views/payment/detail.jsp");
    }

    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }/*
    @Override
    @RequestMapping(value = "/payment.do", method = {RequestMethod.GET})
    public ModelAndView payment(SettlementPaymentVO paymentInfo , HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        paymentInfo.setTotal_amount(10000);
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        paymentService.processWithdraw(paymentInfo, memberInfo);
        return layout("/WEB-INF/views/payment/detail.jsp");
    } */
    @Override
    @GetMapping("/tossRequest.do")
    public void requestTossBillingKey(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
    
        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String userId = (memberVO != null) ? memberVO.getId() : "CHUNGMOO_TEST_USER";
    
        String contextPath = request.getContextPath();
        
        // 🚀 하드코딩 지우고 상단의 baseUrl 변수와 정확한 컨트롤러 경로(/payment) 적용!
        String successUrl = URLEncoder.encode(baseUrl + contextPath + "/payment/callback.do", "UTF-8");
        String failUrl = URLEncoder.encode(baseUrl + contextPath + "/payment/fail.do", "UTF-8");
        
        String tossUrl = "https://api.tosspayments.com/v1/billing/authorizations"
               + "?clientKey=" + clientKey
               + "&customerKey=" + userId
               + "&successUrl=" + successUrl
               + "&failUrl=" + failUrl; // (오타 수정: 파라미터 연결이므로 &failUrl 이 맞음)
                       
        System.out.println("👉 [토스 출발] 카드창으로 강제 이송: " + tossUrl);
        response.sendRedirect(tossUrl);
    }
    // ────────────────────────────────────────────────────────
    // [B단계] 카드인증 성공 시 토스가 형 서버를 찌르는 성공 콜백
    // ────────────────────────────────────────────────────────
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
            // 💥 서비스단 호출해서 토스 API 최종 연동 후 진짜 빌링키 뜯어내서 DB 저장!
            paymentService.issueAndSaveBillingKey(customerKey, authKey, userId);
            redirectAttributes.addFlashAttribute("msg", "'결제 카드가 정상적으로 등록되었습니다! 정산 준비 완료.");
            return "redirect:/spendolive/main.do";

        } catch (Exception e) {
           
            redirectAttributes.addFlashAttribute("msg", "'카드 등록 최종 승인 중 에러가 발생했습니다.");
            return "redirect:/spendolive/main.do";
        }
    }
    @Override
    @GetMapping("/paymenting.do")
    public String payment(
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception {

        response.setContentType("text/html; charset=UTF-8");

        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        OttSettlementDTO settlementInfo = (OttSettlementDTO) session.getAttribute("settlementInfo");
        if (settlementInfo == null) {
            redirectAttributes.addFlashAttribute(
                    "msg",
                    "정산 정보가 없어 결제할 수 없습니다."
            );
        
            return "redirect:/spendolive/ott.do";
        }
        int base_amount = (int) session.getAttribute("total_amount");
        int member_limit = settlementInfo.getMember_limit();
        int fee_amount = (base_amount / 100) * member_limit;
        int total_price = base_amount + fee_amount; 
        String host_login_id = settlementInfo.getHost_login_id();
        String userId = memberVO.getId();
        int settlement_id = (int) settlementInfo.getSettlement_id().longValue();
        int room_id = (int) settlementInfo.getRoom_id().longValue();

        try {
            paymentService.executeAutomaticPayment(userId, total_price, room_id,fee_amount ,base_amount, settlement_id, host_login_id);
            if(paymentService.roomMemberByroomIdCount(room_id,userId).equals("false")){
                ottService.completePaidRoomEntry((long) room_id, userId);
                redirectAttributes.addFlashAttribute("msg", "자동결제가 완료 되었습니다 !");
                return "redirect:/spendolive/main.do";
            }
            
            redirectAttributes.addFlashAttribute("msg", "결제가 완료 되었습니다 !");
            return "redirect:/admin/settlement/paymentlist.do";
      
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "자동결제가 실패 되었습니다 다시 시도 해주세요");
            return "redirect:/spendolive/main.do";
        }
    }

}
