package com.example.spendolive.payment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberCardVO;
import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.ott.service.OttService;
import com.example.spendolive.payment.domain.PaymentAjaxResponse;
import com.example.spendolive.payment.domain.PaymentAmountDTO;
import com.example.spendolive.payment.domain.SettlementPaymentVO;
import com.example.spendolive.payment.exception.PaymentProcessException;
import com.example.spendolive.payment.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller("paymentController")
@RequestMapping(value = "/payment")
public class PaymentControllerImpl implements PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private OttService ottService;

    /** 결제 금액을 확인하는 상세 화면을 표시합니다. */
    @Override
    @RequestMapping(value = "/detail.do", method = { RequestMethod.GET, RequestMethod.POST })
    public ModelAndView detail(
            @RequestParam("room_id") int roomId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        HttpSession session = request.getSession(false);
        MemberVO memberVO = session == null
                ? null
                : (MemberVO) session.getAttribute("memberInfo");
        List<MemberCardVO> cardList = memberService.getCardById(memberVO.getId());
        session.setAttribute("cardList", cardList);
        if (!isLoggedIn(memberVO)) {
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        try {
            String paymentStatus = paymentService.getRoomPaymentStatus(memberVO.getId(), roomId);
            PaymentAmountDTO paymentAmount = paymentService.getPaymentAmount(roomId);
            ModelAndView mav = layout("/WEB-INF/views/payment/detail.jsp");
            mav.addObject("paymentAmount", paymentAmount);
            return mav;

        } catch (PaymentProcessException e) {
            ModelAndView mav = new ModelAndView("redirect:/spendolive/ott.do");
            mav.addObject("msg", e.getMessage());
            return mav;
        }
    }

    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }

    /** Toss 카드 등록 성공 콜백입니다. */
    @Override
    @GetMapping("/callback.do")
    public String tossCallback(
            @RequestParam("customerKey") String customerKey,
            @RequestParam("authKey") String authKey,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws Exception {
        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");

        if (!isLoggedIn(memberVO)) {
            redirectAttributes.addFlashAttribute(
                    "msg",
                    "로그인 정보가 만료되었습니다. 다시 로그인해주세요.");
            return "redirect:/member/loginForm.do";
        }

        try {
            paymentService.issueAndSaveBillingKey(
                    customerKey,
                    authKey,
                    memberVO.getId());
            redirectAttributes.addFlashAttribute(
                    "msg",
                    "결제 카드가 정상적으로 등록되었습니다.");
            return "redirect:/spendolive/main.do";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "msg",
                    "카드 등록 최종 승인 중 오류가 발생했습니다.");
            return "redirect:/spendolive/main.do";
        }
    }

    /** 카드 등록 실패 콜백입니다. */
    @Override
    @GetMapping("/fail.do")
    public String tossCallback(RedirectAttributes redirectAttributes) throws Exception {
        redirectAttributes.addFlashAttribute(
                "msg",
                "카드 등록에 실패했습니다. 카드 정보를 확인한 뒤 다시 시도해주세요.");
        return "redirect:/spendolive/main.do";
    }

    /**
     * 등록된 카드로 자동결제를 실행하고 JSON 결과를 반환합니다.
     * 결제는 조회가 아니므로 GET이 아닌 POST로만 받습니다.
     */
    @Override
    @PostMapping(value = "/paymenting.do", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<PaymentAjaxResponse> payment(
            @RequestParam("roomId") int roomId,
            HttpServletRequest request,
            HttpSession session) throws Exception {

        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String contextPath = request.getContextPath();

        if (!isLoggedIn(memberVO)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new PaymentAjaxResponse(
                            false,
                            "LOGIN_REQUIRED",
                            "로그인이 필요합니다.",
                            "UNPAID",
                            roomId,
                            contextPath + "/member/loginForm.do"));
        }

        String userId = memberVO.getId();
        String roomUrl = buildRoomUrl(contextPath, roomId);

        try {
            PaymentAmountDTO paymentAmount = paymentService.executeRoomPayment(userId, roomId);

            // Toss 결제가 끝난 뒤에만 실제 OTT 방 멤버로 입장 처리합니다.


            return ResponseEntity.ok(new PaymentAjaxResponse(
                    true,
                    "PAYMENT_COMPLETED",
                    "자동결제가 완료되었습니다. 매월 "
                            + paymentAmount.getAutomaticPaymentDay()
                            + "일에 자동결제됩니다.",
                    "PAID",
                    roomId,
                    roomUrl));

        } catch (PaymentProcessException e) {
            // 이미 결제된 요청은 재결제하지 않고 기존 결제 결과를 사용합니다.
            if ("ALREADY_PAID".equals(e.getCode())
                    || "ALREADY_MEMBER".equals(e.getCode())) {
                ottService.completePaidRoomEntry((long) roomId, userId);

                return ResponseEntity.ok(new PaymentAjaxResponse(
                        true,
                        e.getCode(),
                        e.getMessage(),
                        "PAID",
                        roomId,
                        roomUrl));
            }

            return ResponseEntity
                    .status(resolveHttpStatus(e.getCode()))
                    .body(new PaymentAjaxResponse(
                            false,
                            e.getCode(),
                            e.getMessage(),
                            paymentService.getRoomPaymentStatus(userId, roomId),
                            roomId,
                            null));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentAjaxResponse(
                            false,
                            "PAYMENT_FAILED",
                            "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            paymentService.getRoomPaymentStatus(userId, roomId),
                            roomId,
                            null));
        }
    }

    /** 통신이 끊겼을 때 실제 결제 완료 여부를 다시 확인합니다. */
    @Override
    @GetMapping(value = "/status.do", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<PaymentAjaxResponse> paymentStatus(
            @RequestParam("roomId") int roomId,
            HttpServletRequest request,
            HttpSession session) throws Exception {

        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String contextPath = request.getContextPath();

        if (!isLoggedIn(memberVO)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new PaymentAjaxResponse(
                            false,
                            "LOGIN_REQUIRED",
                            "로그인이 필요합니다.",
                            "UNPAID",
                            roomId,
                            contextPath + "/member/loginForm.do"));
        }

        String userId = memberVO.getId();
        String paymentStatus = paymentService.getRoomPaymentStatus(userId, roomId);

        if (isPaidStatus(paymentStatus)) {
            try{
            ottService.completePaidRoomEntry((long) roomId, userId);
            }catch(Exception e){
                SettlementPaymentVO payment = paymentService.getSettlement_PaymentByRoomId(userId,roomId);
            
                paymentService.updatePaymentstatusRefund(payment);
                
                return ResponseEntity.ok(new PaymentAjaxResponse(
                    false,
                    "PAYMENT_FAILED",
                    "방 정원이 초과하여 결제를 취소 합니다.",
                    paymentStatus,
                    roomId,
                    null));
            }
            return ResponseEntity.ok(new PaymentAjaxResponse(
                    true,
                    "PAYMENT_COMPLETED",
                    "결제가 완료된 것을 확인했습니다.",
                    paymentStatus,
                    roomId,
                    buildRoomUrl(contextPath, roomId)));
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
                roomId,
                null));
    }

    private boolean isLoggedIn(MemberVO memberVO) {
        return memberVO != null
                && memberVO.getId() != null
                && !memberVO.getId().isBlank();
    }

    private boolean isPaidStatus(String paymentStatus) {
        return "PAID".equals(paymentStatus)
                || "CONFIRMED".equals(paymentStatus);
    }

    private String buildRoomUrl(String contextPath, int roomId) {
        return contextPath
                + "/spendolive/ott/chat/room.do?room_id="
                + roomId;
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
    @PostMapping("/updatePrimaryCard.do")
    public ResponseEntity<PaymentAjaxResponse> updatePrimaryCard(@RequestParam("card_idx") String card_idxstr,@RequestHeader(value = "Referer", required = false) String referer,  HttpServletRequest request,HttpSession session) throws Exception {
        int card_idx = Integer.parseInt(card_idxstr);
        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String userId = memberVO.getId();
        try{
        memberService.updatePrimaryCard(userId,card_idx);
        return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new PaymentAjaxResponse(
                            true,
                            "UPDATE_COMPLETED",
                            "변경에 성공하였습니다.",
                            "SUCCESS",
                            null,
                            null));
        }catch(PaymentProcessException e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new PaymentAjaxResponse(
                            false,
                            "UPDATE_FAILED",
                            "변경에 실패하였습니다.",
                            "FAILED",
                            null,
                            null ));
        }
    }
}
