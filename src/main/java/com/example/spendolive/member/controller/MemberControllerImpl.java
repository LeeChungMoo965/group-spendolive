package com.example.spendolive.member.controller;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.service.MemberService;
@Controller("memberController")
@RequestMapping(value="/member")
public class MemberControllerImpl implements MemberController{
    @Autowired
    private MemberService memberService;
    private MemberVO memberVO;
    @Value("${kakao.client.id}")
    private String kakaoclientId;
    @Value("${kakao.redirect.uri}")
    private String kakaoredirectUri;
    @Value("${openbanking.client-id}")
    private String openbankingclientId;
    @Value("${openbanking.redirect-uri}")
    private String openbankingredirectUri;
    @Value("${openbanking.client-secret}")
    private String openbankingclientSecret;    
    @Override
    
    // 코드리뷰.4
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
            try{String log = (String) session.getAttribute("log");
                if(log.equals("mypage")){mav.setViewName("redirect:/spendolive/mypage.do");}
                else if(log.equals("expense")){mav.setViewName("redirect:/spendolive/expense.do");}
                else if(log.equals("ott")){mav.setViewName("redirect:/spendolive/ott.do");}
                
            }catch(Exception e){   
                mav.setViewName("redirect:/spendolive/main.do");
                if(memberVO.getRole().equals("ADMIN")){mav.setViewName("redirect:/spendolive/admin/main.do");}
            }
              
        }
        //로그인 실피 시 로그인 화면 유지
        else {
            String message = "아이디나 비밀번호가 틀립니다. 다시 로그인해주세요.";
            mav.addObject("message", message);
            
            mav.setViewName("/member/loginForm"); 
        }
        return mav;
    }
    
    // 코드리뷰.3 ->loginform.jsp
    @Override
    @RequestMapping(value="/loginForm.do" , method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView loginForm(@RequestParam(value = "log", required = false) String log, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        HttpSession session = request.getSession();
        session.setAttribute("log", log); // log = 원래 이동하려던 페이지 정보 
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                            + "?client_id=" + kakaoclientId 
                            + "&redirect_uri=" +kakaoredirectUri
                            + "&response_type=code";
        ModelAndView mav = new ModelAndView();
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
        session.removeAttribute("login_type");
        session.removeAttribute("loginId");
        mav.setViewName("redirect:/spendolive/main.do");
        return mav;
    }
    
    
    // 코드리뷰.2
    @Override
    @RequestMapping(value="/addmember.do" ,method = RequestMethod.POST)
    public ModelAndView addMember(@ModelAttribute("memberVO") MemberVO member, HttpServletRequest request, HttpServletResponse response ,RedirectAttributes redirectAttributes)
            throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        HttpSession session = request.getSession();
        session.removeAttribute("id");
        session.removeAttribute("member_name");
        try {
            memberService.addMember(member);
            redirectAttributes.addFlashAttribute("msg", "회원가입에 성공하였습니다 ! ");
            return new ModelAndView("redirect:/member/loginForm.do");
        }catch(Exception e) {
            redirectAttributes.addFlashAttribute("msg", "회원가입에 실패하였습니다 ! ");
            return new ModelAndView("redirect:/member/singup.do");
        }
        
    }
    
    
    // 코드리뷰.1 -> signup.jsp -> js
    //회원가입 페이지 이동 메서드
    @Override
    @RequestMapping(value="/signup.do" , method = RequestMethod.GET)
    public ModelAndView memberForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        

        mav.setViewName("member/signup");
        
    
        
        return mav;
    }
 
 
    // 코드리뷰.2-1
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
            
            return "SUCCESS"; // 프론트 Ajax의 success로 신호 전달
        } catch (Exception e) {
            return "ERROR";
        }
        
    }
 
 
    // 코드리뷰.2-2
    @Override
    @RequestMapping(value="/verifyEmail", method = RequestMethod.POST)
    @ResponseBody
    public boolean verifyEmail(@RequestParam("inputCode") String inputCode, HttpServletRequest request) {
        HttpSession session = request.getSession();
    
        String originalCode = (String) session.getAttribute("verificationCode");
        
        // 사용자가 화면에 입력한 값과 진짜 값이 일치하는지 판별 (true / false 반환)
        if (originalCode != null && originalCode.equals(inputCode)) {
            session.removeAttribute("emailCode"); // 인증 성공 시 세션 청소
            return true;
        }
        
        return false;
    }
    
    
    // 코드리뷰.2-3
            // 1. 휴대폰 인증번호 발송 요청 처리
        @Override
        @RequestMapping(value="/sendSms", method = RequestMethod.POST)
        @ResponseBody
        public String sendSms(@RequestParam("phone") String phone, HttpServletRequest request) throws Exception {
            
            String verificationCode = memberService.sendSmsVerification(phone.replace("-", ""));//인증번호 생성
            HttpSession session = request.getSession();
            session.setAttribute("smsCode", verificationCode);
            
            return "success"; // 프론트 Ajax의 success로 신호 전달
        }
    
    
        // 코드리뷰.2-4
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
        @Override
        @RequestMapping(value="/checkEmail", method = RequestMethod.POST)
        @ResponseBody
        public boolean checkEmail(@RequestParam("email") String email) throws Exception {
            return memberService.checkEmail(email);
        }
        @Override
        @RequestMapping(value="/checkPhone", method = RequestMethod.POST)
        @ResponseBody
        public boolean checkPhone(@RequestParam("phone") String phone) throws Exception {
            return memberService.checkPhone(phone);
        }



// 코드리뷰.4-1 -> signup.jsp
    // 카카오 로그인 콜백 (Redirect URI로 설정된 주소)
    @Override
    @RequestMapping(value="/kakaoCallback.do", method = RequestMethod.GET)
    public ModelAndView kakaoCallback(@RequestParam(value = "code", required = false) String code, 
                                      HttpServletRequest request,RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
                                    
        // 1. 인가 코드 누락(사용자가 취소 버튼을 누른 경우 등) 처리
        if (code == null || code.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "카카오 로그인이 취소되었거나 오류가 발생했습니다."); 
            return layout("/WEB-INF/views/member/loginForm.jsp");

        }
       
        try {
            // 2. 통합된 MemberService를 통해 카카오 유저 정보 획득
            Map<String, String> userInfo = memberService.getKakaoUserInfo(code);
            String id = userInfo.get("id");
            // 3. 세션 처리
            HttpSession session = request.getSession();
            
            if(memberService.checkId(id)){
                session.setAttribute("login_type", "KAKAO");
                session.setAttribute("id", id);
                session.setAttribute("member_name", userInfo.get("nickname")); 
                return layout("/WEB-INF/views/member/signup.jsp");
            } else {
                memberVO = memberService.login(userInfo);
                session.setAttribute("memberInfo", memberVO);
                session.setAttribute("isLogOn", true);
                session.setAttribute("login_type", "KAKAO");
                try{String log = (String) session.getAttribute("log");
                if(log.equals("mypage")){mav.setViewName("redirect:/spendolive/mypage.do");}
                else if(log.equals("expense")){mav.setViewName("redirect:/spendolive/expense.do");}
                else if(log.equals("ott")){mav.setViewName("redirect:/spendolive/ott.do");}  
                }catch(Exception e){mav.setViewName("redirect:/spendolive/main.do");}
                }      
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("msg", "카카오 로그인 연동 중 서버 오류가 발생했습니다."); 
                return layout("/WEB-INF/views/member/loginForm.jsp");
            }
            
        return mav;
    }
   
   
   
    @Override
    @RequestMapping(value="/openBankingAuth.do", method = RequestMethod.GET)
    public String openBankingAuth() throws UnsupportedEncodingException {


        String state = UUID.randomUUID().toString().replace("-", "");
        String encodedRedirectUri = URLEncoder.encode(openbankingredirectUri, StandardCharsets.UTF_8.toString());
        String targetUrl = String.format(
        "https://testapi.openbanking.or.kr/oauth/2.0/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=login+inquiry+transfer&state=%s&auth_type=0",
        openbankingclientId, encodedRedirectUri, state
        );

        // 금결원 페이지로 리다이렉트
        return "redirect:" + targetUrl;
    }
    @Override
    @RequestMapping(value="/openBankingcallback.do", method = RequestMethod.GET)
    public ModelAndView openBankingCallback(
        @RequestParam("code") String code,
        @RequestParam("state") String state,
        HttpServletRequest request, HttpServletResponse response,
        HttpSession session,RedirectAttributes redirectAttributes) throws UnsupportedEncodingException { // 로그인한 회원의 정보를 알기 위해 세션 사용

    // [보안 체크] 내가 보냈던 state 값이 맞는지 검증하는 로직을 넣으면 더 안전합니다.
    
    // 현재 로그인한 사용자의 ID나 고유 번호 가져오기 (세션 등 활용)
    MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
    String userId = memberVO.getId();
    String message = null;
    ResponseEntity resEntity = null;
    HttpHeaders responseHeaders = new HttpHeaders();
    try {
        // 비즈니스 로직 처리를 위해 서비스 호출
        memberService.registerOpenBankingToken(code, userId, responseHeaders, resEntity,memberVO);
        redirectAttributes.addFlashAttribute("msg", "계좌인증을 완료했습니다. 로그인을 다시 해주세요."); 
        return new ModelAndView("redirect:/member/logout.do");
        // 연동 성공 후 완료 페이지나 메인 화면으로 이동
    
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("msg", "계좌 인증에 실패하였습니다. 다시 시도해 주세요."); 
        return new ModelAndView("redirect:/spendolive/main.do");
    }
}

    /* =========================================================
       [추가 기능] 아이디 찾기 - 1단계: 휴대폰 인증번호 발송
       ---------------------------------------------------------
       화면 위치: loginForm.jsp > 아이디 찾기 폼 > "인증번호 받기" 버튼
       호출 JS  : sendFindIdSms()
       URL      : POST /member/findId/sendSms.do
       역할     : 입력한 휴대폰 번호로 가입된 ACTIVE 회원이 있는지 확인한 뒤,
                  인증번호를 발급하고 세션에 임시 저장한다.
       세션 저장: findIdSmsCode = 인증번호, findIdPhone = 숫자만 남긴 휴대폰 번호
       주의     : 현재 sendSmsVerification()은 실제 문자 발송 대신 콘솔 출력 방식일 수 있음.
       ========================================================= */
    @RequestMapping(value = "/findId/sendSms.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> sendFindIdSms(@RequestParam("phone") String phone,
                                             HttpServletRequest request) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            String foundId = memberService.findIdByPhone(phone);
            if (foundId == null || foundId.isBlank()) {
                result.put("success", false);
                result.put("message", "해당 휴대폰 번호로 가입된 계정이 없습니다.");
                return result;
            }

            String normalizedPhone = normalizePhone(phone);
            String verificationCode = memberService.sendSmsVerification(normalizedPhone);

            HttpSession session = request.getSession();
            session.setAttribute("findIdSmsCode", verificationCode);
            session.setAttribute("findIdPhone", normalizedPhone);

            result.put("success", true);
            result.put("message", "인증번호를 발송했습니다. 콘솔에 출력된 인증번호를 입력해주세요.");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "인증번호 발송 중 오류가 발생했습니다.");
            return result;
        }
    }

    /* =========================================================
       [추가 기능] 아이디 찾기 - 2단계: 인증번호 확인 후 아이디 반환
       ---------------------------------------------------------
       화면 위치: loginForm.jsp > 아이디 찾기 폼 > "아이디 찾기" 버튼
       호출 JS  : verifyFindIdSms()
       URL      : POST /member/findId/verify.do
       역할     : 사용자가 입력한 인증번호와 세션의 findIdSmsCode를 비교한다.
                  인증 성공 시 휴대폰 번호로 member_tb.id를 조회해서 화면에 알려준다.
       세션 정리: 성공 시 findIdSmsCode, findIdPhone 제거
       ========================================================= */
    @RequestMapping(value = "/findId/verify.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> verifyFindIdSms(@RequestParam("inputCode") String inputCode,
                                               HttpServletRequest request) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            HttpSession session = request.getSession();
            String originalCode = (String) session.getAttribute("findIdSmsCode");
            String phone = (String) session.getAttribute("findIdPhone");

            if (originalCode == null || phone == null || !originalCode.equals(inputCode)) {
                result.put("success", false);
                result.put("message", "인증번호가 일치하지 않습니다.");
                return result;
            }

            String foundId = memberService.findIdByPhone(phone);
            if (foundId == null || foundId.isBlank()) {
                result.put("success", false);
                result.put("message", "가입된 아이디를 찾을 수 없습니다.");
                return result;
            }

            session.removeAttribute("findIdSmsCode");
            session.removeAttribute("findIdPhone");

            result.put("success", true);
            result.put("id", foundId);
            result.put("message", "가입된 아이디는 [ " + foundId + " ] 입니다.");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "아이디 찾기 중 오류가 발생했습니다.");
            return result;
        }
    }

    /* =========================================================
       [추가 기능] 비밀번호 찾기 - 1단계: 아이디/휴대폰 일치 확인 후 인증번호 발송
       ---------------------------------------------------------
       화면 위치: loginForm.jsp > 비밀번호 찾기 폼 > "인증번호 받기" 버튼
       호출 JS  : sendFindPwSms()
       URL      : POST /member/findPw/sendSms.do
       역할     : 1) 아이디가 ACTIVE 회원인지 확인
                  2) 아이디와 휴대폰 번호가 같은 회원 정보인지 확인
                  3) 맞으면 인증번호를 발급하고 세션에 저장
       세션 저장: findPwSmsCode, findPwId, findPwPhone
       보안 이유: 아이디만 알면 비밀번호를 바꿀 수 없게 휴대폰 번호까지 검증한다.
       ========================================================= */
    @RequestMapping(value = "/findPw/sendSms.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> sendFindPwSms(@RequestParam("id") String id,
                                             @RequestParam("phone") String phone,
                                             HttpServletRequest request) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            if (id == null || id.isBlank()) {
                result.put("success", false);
                result.put("message", "아이디를 입력해주세요.");
                return result;
            }

            if (!memberService.existsActiveId(id)) {
                result.put("success", false);
                result.put("message", "입력한 아이디가 존재하지 않습니다.");
                return result;
            }

            if (!memberService.existsActiveMemberByIdAndPhone(id, phone)) {
                result.put("success", false);
                result.put("message", "아이디와 휴대폰 번호가 일치하지 않습니다.");
                return result;
            }

            String normalizedPhone = normalizePhone(phone);
            String verificationCode = memberService.sendSmsVerification(normalizedPhone);

            HttpSession session = request.getSession();
            session.setAttribute("findPwSmsCode", verificationCode);
            session.setAttribute("findPwId", id);
            session.setAttribute("findPwPhone", normalizedPhone);
            session.removeAttribute("findPwVerifiedId");

            result.put("success", true);
            result.put("message", "인증번호를 발송했습니다. 콘솔에 출력된 인증번호를 입력해주세요.");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "인증번호 발송 중 오류가 발생했습니다.");
            return result;
        }
    }

    /* =========================================================
       [추가 기능] 비밀번호 찾기 - 2단계: 휴대폰 인증 완료 처리
       ---------------------------------------------------------
       화면 위치: loginForm.jsp > 비밀번호 찾기 폼 > "인증 확인" 버튼
       호출 JS  : verifyFindPwSms()
       URL      : POST /member/findPw/verify.do
       역할     : 인증번호가 맞으면 findPwVerifiedId를 세션에 저장한다.
                  이 값이 있어야 다음 단계인 비밀번호 변경이 가능하다.
       세션 저장: findPwVerifiedId = 인증 완료된 회원 아이디
       세션 정리: findPwSmsCode 제거
       ========================================================= */
    @RequestMapping(value = "/findPw/verify.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> verifyFindPwSms(@RequestParam("inputCode") String inputCode,
                                               HttpServletRequest request) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            HttpSession session = request.getSession();
            String originalCode = (String) session.getAttribute("findPwSmsCode");
            String id = (String) session.getAttribute("findPwId");

            if (originalCode == null || id == null || !originalCode.equals(inputCode)) {
                result.put("success", false);
                result.put("message", "인증번호가 일치하지 않습니다.");
                return result;
            }

            session.setAttribute("findPwVerifiedId", id);
            session.removeAttribute("findPwSmsCode");

            result.put("success", true);
            result.put("message", "휴대폰 인증이 완료되었습니다. 새 비밀번호를 입력해주세요.");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "인증 확인 중 오류가 발생했습니다.");
            return result;
        }
    }

    /* =========================================================
       [추가 기능] 비밀번호 찾기 - 3단계: 새 비밀번호 변경
       ---------------------------------------------------------
       화면 위치: loginForm.jsp > 비밀번호 찾기 폼 > 새 비밀번호 입력 영역
       호출 JS  : resetPassword()
       URL      : POST /member/findPw/reset.do
       역할     : 휴대폰 인증이 완료된 회원(findPwVerifiedId)에 한해서
                  새 비밀번호와 비밀번호 확인값을 비교한 뒤 DB 비밀번호를 변경한다.
       세션 조건: findPwVerifiedId가 없으면 "휴대폰 인증 먼저" 메시지 반환
       세션 정리: 성공 시 findPwVerifiedId, findPwId, findPwPhone 제거
       ========================================================= */
    @RequestMapping(value = "/findPw/reset.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestParam("newPassword") String newPassword,
                                             @RequestParam("newPasswordConfirm") String newPasswordConfirm,
                                             HttpServletRequest request) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            HttpSession session = request.getSession();
            String verifiedId = (String) session.getAttribute("findPwVerifiedId");

            if (verifiedId == null || verifiedId.isBlank()) {
                result.put("success", false);
                result.put("message", "휴대폰 인증을 먼저 완료해주세요.");
                return result;
            }

            if (newPassword == null || newPassword.isBlank() || newPasswordConfirm == null || newPasswordConfirm.isBlank()) {
                result.put("success", false);
                result.put("message", "새 비밀번호와 비밀번호 확인을 모두 입력해주세요.");
                return result;
            }

            if (!newPassword.equals(newPasswordConfirm)) {
                result.put("success", false);
                result.put("message", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                return result;
            }

            if (newPassword.length() < 4) {
                result.put("success", false);
                result.put("message", "비밀번호는 최소 4자 이상 입력해주세요.");
                return result;
            }

            memberService.updatePasswordById(verifiedId, newPassword);

            session.removeAttribute("findPwVerifiedId");
            session.removeAttribute("findPwId");
            session.removeAttribute("findPwPhone");

            result.put("success", true);
            result.put("message", "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return result;
        }
    }

    /* =========================================================
       [추가 유틸] 휴대폰 번호 정규화
       ---------------------------------------------------------
       화면에서는 010-1234-5678 또는 01012345678 둘 다 입력될 수 있으므로
       DB 조회 전 숫자만 남겨 같은 형식으로 비교한다.
       예: 010-1234-5678 -> 01012345678
       ========================================================= */
    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9]", "");
    }
    private ModelAndView layout(String bodyPage) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("common/layout");
        mav.addObject("body_page", bodyPage);
        return mav;
    }
}
