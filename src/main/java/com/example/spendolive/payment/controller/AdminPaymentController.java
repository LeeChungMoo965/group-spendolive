package com.example.spendolive.payment.controller;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface AdminPaymentController {
    public ModelAndView listUpSettlement(@RequestParam(value = "status", required = false) String status,HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
    public String pay(@RequestParam("roomId") String roomIdStr, HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
    public ModelAndView paymentlistUpSettlement(@RequestParam(value = "status", required = false) String status,HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
    public String payment(
        @RequestParam("userId") String userId,@RequestParam("roomId") String roomIdStr,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
    public String paymentlate(
        @RequestParam("userId") String userId,@RequestParam("roomId") String roomIdStr,@RequestParam("late_day") String late_day,
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
}