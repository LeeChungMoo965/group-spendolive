package com.example.spendolive.member.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.admin.service.AdminMemberService;
import com.example.spendolive.member.domain.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller("AdminMemberController")
@RequestMapping(value="/admin/member")
public class AdminMemberControllerImpl implements AdminMemberController{
    @Autowired
    private AdminMemberService adminmemberService;

    @Override
    @GetMapping("/list.do")
    public ModelAndView listUpSettlement(@RequestParam(value = "status", required = false) String status,HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        session = request.getSession();
        if(status==null){status = "READY";}
        
        try {
            List<MemberVO> memberList = adminmemberService.selectMemberAll();
            session.setAttribute("memberList", memberList);
            return layout("/WEB-INF/views/admin/member/member.jsp");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "리스트업에 실패 하였습니다. ");
            return layout("/WEB-INF/views/admin/member/member.jsp");
        }
    }
    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }
}
