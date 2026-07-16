package com.example.spendolive.payment.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.payment.domain.SettlementPaymentVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface PaymentController {
    public ModelAndView detail(@RequestParam Map<String, Object> roomid, HttpServletRequest request, HttpServletResponse response) throws Exception;
    public String tossCallback(
        @RequestParam("customerKey") String customerKey,
        @RequestParam("authKey") String authKey,
        HttpServletRequest request, HttpServletResponse response,
        HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
    public String payment(
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
    public String tossCallback(RedirectAttributes redirectAttributes)throws Exception;
}
