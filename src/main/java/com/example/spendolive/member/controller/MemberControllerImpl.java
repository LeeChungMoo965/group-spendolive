package com.example.spendolive.member.controller;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.OverridesAttribute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    @Value("${kakao.client.id}")
    private String clientId;
    @Value("${kakao.redirect.uri}")
    private String redirectUri;
    @Override
    @RequestMapping(value="/login.do" ,method = RequestMethod.POST )
    public ModelAndView login(@RequestParam Map<String, String> loginMap, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView mav = new ModelAndView();
        memberVO = memberService.login(loginMap);
        //로그인 성공 하여 memberVO객체가 생성될 시 home화면 이동
        if(memberVO != null && memberVO.getId() != null && !memberVO.getId().equals("")) {
            HttpSession session = request.getSession();
            session.setAttribute("isLogOn", true);
            session.setAttribute("memberInfo", memberVO);
            
            mav.setViewName("redirect:/spendolive/main.do");  
        }
        //로그인 실피 시 로그인 화면 유지
        else {
            String message = "아이디나 비밀번호가 틀립니다. 다시 로그인해주세요.";
            mav.addObject("message", message);
            
            mav.setViewName("/member/loginForm"); 
        }
        return mav;
    }
    @Override
    @RequestMapping(value="/loginForm.do" , method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView loginForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                            + "?client_id=" + clientId 
                            + "&redirect_uri=" +redirectUri
                            + "&response_type=code";
        ModelAndView mav = new ModelAndView();
        HttpSession session = request.getSession();
        mav.addObject("kakaoAuthUrl", kakaoAuthUrl);
        mav.setViewName("member/loginForm");

        return mav;
    }
    @Override
    @RequestMapping(value="/logout.do" ,method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        HttpSession session=request.getSession();
        session.setAttribute("isLogOn", false);
        session.removeAttribute("memberInfo");
        mav.setViewName("/spendolive/main.do");
        return mav;
    }

    @Override
    @RequestMapping(value="/addmember.do" ,method = RequestMethod.POST)
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
            message +=" alert('회원 가입을 완료했습니다. 로그인창으로 이동합니다.');"; 
            message += " location.href='"+request.getContextPath()+"/member/loginForm.do';";
            message += " </script>";
            
        }catch(Exception e) {
            message  = "<script>";
            message +=" alert('작업 중 오류가 발생했습니다. 다시 시도해 주세요.');"; 
            message += " location.href='"+request.getContextPath()+"/member/signup.do';";
            message += " </script>";
            e.printStackTrace();
        }
        resEntity = new ResponseEntity(message, responseHeaders, HttpStatus.OK);
        return resEntity;
    }

    //회원가입 페이지 이동 메서드
    @Override
    @RequestMapping(value="/signup.do" , method = RequestMethod.GET)
    public ModelAndView memberForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        

        mav.setViewName("member/signup");
        
    
        
        return mav;
    }
    
    @Override
    public ResponseEntity overlapped(String id, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'overlapped'");
    }
    @Override
    @ResponseBody
    @RequestMapping(value="/sendEmail", method = RequestMethod.POST)
    public String sendEmail(@RequestParam("email") String email, HttpServletRequest request) {
        try {
            // 메일 발송 후 생성된 6자리 코드 반환받기
            String verificationCode = memberService.sendVerificationEmail(email);
            
            // 사용자가 나중에 입력한 값과 비교할 수 있도록 세션에 인증코드 저장
            HttpSession session = request.getSession();
            session.setAttribute("verificationCode", verificationCode);
            
            return "SUCCESS"; // 프론트엔드(JSP)에 성공 신호 보냄
        } catch (Exception e) {
            return "ERROR";
        }
        
    }
 
    @Override
    @RequestMapping(value="/verifyEmail", method = RequestMethod.POST)
    @ResponseBody
    public boolean verifyEmail(@RequestParam("inputCode") String inputCode, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 세션에 저장해 둔 진짜 인증번호 꺼내기
        String originalCode = (String) session.getAttribute("verificationCode");
        
        // 사용자가 화면에 입력한 값과 진짜 값이 일치하는지 판별 (true / false 반환)
        if (originalCode != null && originalCode.equals(inputCode)) {
            session.removeAttribute("emailCode"); // 인증 성공 시 세션 청소
            return true;
        }
        
        return false;
    }
            // 1. 휴대폰 인증번호 발송 요청 처리
        @Override
        @RequestMapping(value="/sendSms", method = RequestMethod.POST)
        @ResponseBody
        public String sendSms(@RequestParam("phone") String phone, HttpServletRequest request) throws Exception {
            // 가상 시뮬레이터 가동해서 6자리 번호 획득
            String verificationCode = memberService.sendSmsVerification(phone);
            
            // 이메일 때처럼 서버 세션을 열어서 발급된 인증번호를 임시 저장
            HttpSession session = request.getSession();
            session.setAttribute("smsCode", verificationCode);
            
            return "success"; // 프론트 Ajax의 success로 신호 전달
        }

        // 2. 사용자가 입력한 인증번호 검증 처리
        @Override
        @RequestMapping(value="/verifySms", method = RequestMethod.POST)
        @ResponseBody
        public boolean verifySms(@RequestParam("inputCode") String inputCode, HttpServletRequest request) {
            HttpSession session = request.getSession();
            
            // 세션에 저장해 둔 진짜 인증번호 꺼내기
            String originalCode = (String) session.getAttribute("smsCode");
            
            // 사용자가 화면에 입력한 값과 진짜 값이 일치하는지 판별 (true / false 반환)
            if (originalCode != null && originalCode.equals(inputCode)) {
                session.removeAttribute("smsCode"); // 인증 성공 시 세션 청소
                return true;
            }
            
            return false;
        }
        //아이디 중복확인
        @Override
        @RequestMapping(value="/checkId", method = RequestMethod.POST)
        @ResponseBody
        public boolean checkId(@RequestParam("id") String id) throws Exception {
            return memberService.checkId(id);
        }
    // 카카오
    // ... 기존 코드 (login, loginForm 등) ...

    // 카카오 로그인 콜백 (Redirect URI로 설정된 주소)
    @RequestMapping(value="/kakaoCallback.do", method = RequestMethod.GET)
    public ModelAndView kakaoCallback(@RequestParam(value = "code", required = false) String code, 
                                      HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
                                    
        // 1. 인가 코드 누락(사용자가 취소 버튼을 누른 경우 등) 처리
        if (code == null || code.trim().isEmpty()) {
            mav.addObject("message", "카카오 로그인이 취소되었거나 오류가 발생했습니다.");
            mav.setViewName("/member/loginForm");
            return mav;
        }
       
        try {
            // 2. 통합된 MemberService를 통해 카카오 유저 정보 획득
            Map<String, String> userInfo = memberService.getKakaoUserInfo(code);
            String id = userInfo.get("id");
            // 3. 세션 처리
            HttpSession session = request.getSession();
            session.setAttribute("id", id);
            session.setAttribute("member_name", userInfo.get("nickname"));
            if(memberService.checkId(id)){
                mav.setViewName("/member/signup");  
            } else {
                memberVO = memberService.login(userInfo);
                session.setAttribute("memberInfo", memberVO);
                session.setAttribute("isLogOn", true);
                mav.setViewName("redirect:/spendolive/main.do"); // 메인 이동은 redirect 권장
            }
            // TODO: userInfo.get("id") 값을 바탕으로 DB 조회 후
            // 기존 회원이면 로그인 처리, 신규 회원이면 회원가입 페이지 이동 혹은 자동 가입 로직 추가 가능

            // 성공 시 메인 화면으로 이동
            

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("message", "카카오 로그인 연동 중 서버 오류가 발생했습니다.");
            mav.setViewName("/member/loginForm");
        }
        
        return mav;
    }
}
