package com.example.spendolive.payment.controller;

import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.payment.domain.SettlementPaymentVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface PaymentController {
    public ModelAndView payment(SettlementPaymentVO paymentInfo ,HttpServletRequest request, HttpServletResponse response) throws Exception;
    public ModelAndView calendar(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
