package com.example.spendolive.controller;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller("homeController")
@EnableAspectJAutoProxy
public class HomeController {

    @RequestMapping(value = "/spendolive/main.do", method={RequestMethod.POST, RequestMethod.GET})
    public String home(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView mav = new ModelAndView();
    
	    HttpSession session = request.getSession();
        return "redirect:/spendolive/main.do";
    }
}
