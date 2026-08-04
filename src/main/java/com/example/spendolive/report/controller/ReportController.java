package com.example.spendolive.report.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.report.domain.ReportAjaxResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface ReportController {
    public ResponseEntity<ReportAjaxResponse> insertReport(@RequestParam("reported_member_id") String reported_member_id,@RequestParam("room_id") String room_id,@RequestParam("chat_text") String chat_text,  HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception;
}
