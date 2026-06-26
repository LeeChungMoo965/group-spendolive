package com.example.spendolive.payment.controller;

import java.io.PrintWriter;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.member.domain.MemberVO;
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
    @Value("${toss.client-key}")
    private String clientKey;

    @Value("${toss.base-url}")
    private String baseUrl;
    @Override
    @RequestMapping(value = "/detail.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView calendar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return layout("/WEB-INF/views/payment/detail.jsp");
    }

    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }
    @Override
    @RequestMapping(value = "/payment.do", method = {RequestMethod.GET})
    public ModelAndView payment(SettlementPaymentVO paymentInfo , HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        paymentInfo.setTotalAmount(10000);
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        paymentService.processWithdraw(paymentInfo, memberInfo);
        return layout("/WEB-INF/views/payment/detail.jsp");
    }
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
    public void tossCallback(
            @RequestParam("customerKey") String customerKey,
            @RequestParam("authKey") String authKey,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws Exception {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 💥 서비스단 호출해서 토스 API 최종 연동 후 진짜 빌링키 뜯어내서 DB 저장!
            paymentService.issueAndSaveBillingKey(customerKey, authKey);

            // 성공하면 얼럿 띄우고 자연스럽게 원래 메인이나 마이페이지로 이동!
            out.print("<script>");
            out.print("alert('결제 카드가 정상적으로 등록되었습니다! 정산 준비 완료.');");
            out.print("location.href='" + request.getContextPath() + "/spendolive/mypage.do';");
            out.print("</script>");
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            out.print("<script>");
            out.print("alert('카드 등록 최종 승인 중 에러가 발생했습니다.');");
            out.print("location.href='" + request.getContextPath() + "/spendolive/main.do';");
            out.print("</script>");
            out.flush();
            out.close();
        }
    }

}
