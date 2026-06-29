package com.example.spendolive.payment.controller;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.payment.domain.SettlementPaymentVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface PaymentController {
    public ModelAndView payment(SettlementPaymentVO paymentInfo ,HttpServletRequest request, HttpServletResponse response) throws Exception;
    public ModelAndView calendar(HttpServletRequest request, HttpServletResponse response) throws Exception;
    public void tossCallback(
        @RequestParam("customerKey") String customerKey,
        @RequestParam("authKey") String authKey,
        HttpServletRequest request, HttpServletResponse response,
        HttpSession session) throws Exception;
    public void requestTossBillingKey(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception;
    public void payment(
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws Exception;
}
