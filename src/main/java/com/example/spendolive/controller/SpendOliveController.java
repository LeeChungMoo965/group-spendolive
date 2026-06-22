package com.example.spendolive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.bind.annotation.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/spendolive")
public class SpendOliveController {


    @RequestMapping(value= "/main.do" ,method={RequestMethod.POST,RequestMethod.GET})
    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView mav = new ModelAndView();
	    HttpSession session = request.getSession();
        mav.setViewName("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/main/main.jsp");
        return mav;
    }

}
