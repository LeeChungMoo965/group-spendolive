package com.example.spendolive.member.controller;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.service.MemberService;
@Controller("memberController")
@RequestMapping(value="/member")
public class MemberControllerImpl implements MemberController{
    @Autowired
    private MemberService memberService;
    private MemberVO memberVO;
    
    @Override
    @RequestMapping(value="/login.do" ,method = RequestMethod.POST)
    public ModelAndView login(Map<String, String> loginMap, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView mav = new ModelAndView();
        memberVO = memberService.login(loginMap);
        //로그인 성공 하여 memberVO객체가 생성될 시 home화면 이동
        if(memberVO != null && memberVO.getId() != null && memberVO.getId().equals("")) {
            HttpSession session = request.getSession();
            session.setAttribute("isLogOn", true);
            session.setAttribute("memberInfo", memberVO);

            mav.setViewName("redirect:/spendolive/main.do");  
        }
        //로그인 실피 시 로그인 화면 유지
        else {
            String message = "아이디나 비밀번호가 틀립니다. 다시 로그인해주세요.";
            mav.addObject("message", message);
            
            mav.setViewName("redirect:/member/loginForm.jsp"); 
        }
        return mav;
    }
    @RequestMapping(value="/loginForm.do" , method = RequestMethod.GET)
    public ModelAndView loginForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/member/loginForm.jsp");

        return mav;
    }
    @Override
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    @Override
    @RequestMapping(value="/addmember.do" , method = RequestMethod.POST)
    public ResponseEntity addMember(@ModelAttribute("memberVO") MemberVO member, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        String message = null;
        ResponseEntity resEntity = null;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        try {
            memberService.addMember(member);
            message  = "<script>";
            message +=" alert('회원 가입을 완료했습니다. 로그인창으로 이동합니다.');"; // 한글 깨짐 수정
            message += " location.href='"+request.getContextPath()+"/member/loginForm.do';";
            message += " </script>";
            
        }catch(Exception e) {
            message  = "<script>";
            message +=" alert('작업 중 오류가 발생했습니다. 다시 시도해 주세요.');"; // 한글 깨짐 수정
            message += " location.href='"+request.getContextPath()+"/member/signup.do';";
            message += " </script>";
            e.printStackTrace();
        }
        resEntity = new ResponseEntity(message, responseHeaders, HttpStatus.OK);
        return resEntity;
    }

    //회원가입 페이지 이동 메서드
    @RequestMapping(value="/signup.do" , method = RequestMethod.GET)
    public ModelAndView memberForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        
        // 전체 틀(layout.jsp)을 불러옵니다.
        mav.setViewName("redirect:/member/loginForm.jsp");
        
    
        
        return mav;
    }
    
    @Override
    public ResponseEntity overlapped(String id, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'overlapped'");
    }
    
}
