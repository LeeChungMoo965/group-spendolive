package com.example.spendolive.alert.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.alert.service.AlertService;

import java.util.List;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.spendolive.alert.domain.AlertDTO;

@Controller
@RequestMapping("/spendolive/alert")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/center.do")
    public ModelAndView alertCenter() {

        ModelAndView mav = new ModelAndView();

        mav.setViewName("common/layout");

        mav.addObject(
                "body_page",
                "/WEB-INF/views/alert/alertCenter.jsp");

        mav.addObject(
                "alertList",
                alertService.getAlertList());

        return mav;
    }

    @GetMapping("/detail.do")
    public ModelAndView alertDetail(
            @RequestParam("alertId") int alertId) {

        ModelAndView mav = new ModelAndView();

        mav.setViewName("common/layout");

        mav.addObject(
                "body_page",
                "/WEB-INF/views/alert/alertDetail.jsp");

        mav.addObject(
                "alert",
                alertService.getAlertDetail(alertId));

        return mav;
    }

    @GetMapping("/ajax/alertList.do")
        @ResponseBody
        public List<AlertDTO> ajaxAlertList() {
        return alertService.getAlertList();
        }

        @GetMapping("/ajax/unreadList.do")
        @ResponseBody
        public List<AlertDTO> ajaxUnreadList() {
        return alertService.getUnreadList();
        }
}