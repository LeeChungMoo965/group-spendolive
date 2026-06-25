package com.example.spendolive.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
        paymentInfo.setTotal_amount(10000);
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        paymentService.processWithdraw(paymentInfo, memberInfo);
        return layout("/WEB-INF/views/payment/detail.jsp");
    }

}
