package com.example.spendolive.member.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;
import com.example.spendolive.payment.service.PaymentService;
import com.example.spendolive.payment.service.PaymentServiceImpl;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

import jakarta.mail.internet.MimeMessage;
import tools.jackson.databind.ObjectMapper;
@Service
public class MemberServiceImpl implements MemberService {

<<<<<<< HEAD
        @Autowired
        private MemberRepository memberRepository;
        @Autowired
        private PaymentService paymentService;
        @Autowired
        private JavaMailSender mailSender;
        @Value("${kakao.client.id}")
        private String restApiKey;
        @Value("${kakao.redirect.uri}")
        private String redirectUri;
        @Value("${openbanking.client-id}")
        private String openbankingclientId;
        @Value("${openbanking.redirect-uri}")
        private String openbankingredirectUri;
        @Value("${openbanking.client-secret}")
        private String openbankingclientSecret;  
        @Value("${solapi.api-key}")
        private String solapiapikey;
        @Value("${solapi.secret-key}")
        private String solapisecretkey;  
        @Override
        public MemberVO login(Map loginMap) throws Exception {
            
            return memberRepository.login(loginMap);
        }

        @Override
        public void addMember(MemberVO memberVO) throws Exception {
            memberRepository.insertNewMember(memberVO);
        }

        @Override
        public String overlapped(String id) throws Exception {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'overlapped'");
        }

        @Override
        public String sendVerificationEmail(String toEmail) throws Exception {
           
            // 윈도우 한글 이름으로 인해 구글이 EOF 뱉는 현상을 방어하기 위해 로컬호스트 강제 지정
            
            if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl) {
                java.util.Properties props = ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getJavaMailProperties();
                props.put("mail.smtp.localhost", "127.0.0.1");
            }
            String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("chung100302@gmail.com");
            message.setTo(toEmail); // 받는 사람 이메일
            message.setSubject("[SpendOlive] 회원가입 인증번호 안내"); // 이메일 제목
            message.setText("안녕하세요. SpendOlive입니다.\n\n" +
                "회원가입을 완료하기 위한 인증번호는 [ " + verificationCode + " ] 입니다.\n" +
                "타인에게 노출되지 않도록 주의해 주세요."); // 이메일 본문
            
            try{
                mailSender.send(message);
                return verificationCode;
            } catch (Exception e){
                System.out.println("이메일 발송 에러: " + e.getMessage());
            throw new RuntimeException("이메일 전송 중 에러 발생");
            }
            
      
        

        }

        @Override
        public String sendSmsVerification(String toNumber) throws Exception {
            // 1. 진짜 통신사 망을 탈 때와 똑같이 6자리 랜덤 인증번호 생성
            String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));

              DefaultMessageService messageService =  SolapiClient.INSTANCE.createInstance(solapiapikey, solapisecretkey);
            // Message 패키지가 중복될 경우 com.solapi.sdk.message.model.Message로 치환하여 주세요
            Message message = new Message();
            message.setFrom("01024414631");
            message.setTo(toNumber);
            message.setText("★ 발송 메세지: [SpendOlive] 가입 인증번호는 [" + verificationCode + "] 입니다.");

            try {
            // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
            messageService.send(message);
            return verificationCode;
            } catch (SolapiMessageNotReceivedException exception) {
            // 발송에 실패한 메시지 목록을 확인할 수 있습니다!
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
            throw new RuntimeException("문자 전송 중 오류 발생");
            } catch (Exception exception) { 
            System.out.println(exception.getMessage());
            throw new RuntimeException("문자 전송 중 오류 발생");
            }

       
        }
        // 아이디 중복확인 메서드
        @Override
        public boolean checkId(String id){
            return memberRepository.checkId(id);
        }
        @Override
        public boolean checkEmail(String email){
            return memberRepository.checkEmail(email);
        }
        @Override
        public boolean checkPhone(String phone){
            return memberRepository.checkPhone(phone);
        }
        // 카카오
        @Override
        public Map<String, String> getKakaoUserInfo(String code) throws Exception {
            // 1. 코드로 토큰을 받고
            String accessToken = getAccessToken(code);
            // 2. 토큰으로 유저 정보를 가져와서 반환
            return getUserInfo(accessToken);
        }
        private String getAccessToken(String code) throws Exception {
            String accessToken = "";
            URL url = new URL("https://kauth.kakao.com/oauth/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true);

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                sb.append("grant_type=authorization_code");
                sb.append("&client_id=").append(restApiKey);
                sb.append("&redirect_uri=").append(redirectUri);
                sb.append("&code=").append(code);
                bw.write(sb.toString());
                bw.flush();
            }

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }
                    JsonElement element = JsonParser.parseString(result.toString());
                    accessToken = element.getAsJsonObject().get("access_token").getAsString();
                }
            } else {
                throw new Exception("카카오 토큰 발급 실패: 상태 코드 " + conn.getResponseCode());
            }
            return accessToken;
        }
        private Map<String, String> getUserInfo(String accessToken) throws Exception {
            Map<String, String> userInfoMap = new HashMap<>();
            URL url = new URL("https://kapi.kakao.com/v2/user/me");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    JsonObject userInfo = JsonParser.parseString(result.toString()).getAsJsonObject();
                    String id = userInfo.get("id").getAsString();
                    
                    JsonObject properties = userInfo.getAsJsonObject("properties");
                    String nickname = (properties != null && properties.get("nickname") != null) 
                                    ? properties.get("nickname").getAsString() : "카카오회원";

                    userInfoMap.put("id", id);
                    userInfoMap.put("nickname", nickname);
                    userInfoMap.put("password", "KAKAO");
                }
            } else {
                throw new Exception("카카오 유저 정보 요청 실패: 상태 코드 " + conn.getResponseCode());
            }
            return userInfoMap;
        }
        //오픈뱅킹

        @Override
        public MemberVO getMemberById(String id) throws Exception {
            return memberRepository.selectMemberById(id);
        }

        @Override
        public void updateMyInfo(MemberVO memberVO, String newPassword) throws Exception {
            memberRepository.updateMyInfo(memberVO, newPassword);
        }
        @Override
        @Transactional
        public void registerOpenBankingToken(String code, String userId,HttpHeaders headers,ResponseEntity<Map> response, MemberVO memberVO) throws Exception {
        
            // 1. 금결원 토큰 발급 요청 주소 (테스트베드 환경이므로 testapi 사용!)
            String tokenUrl = "https://testapi.openbanking.or.kr/oauth/2.0/token";
    
            // 2. RestTemplate을 이용한 HTTP 통신 준비
            RestTemplate restTemplate = new RestTemplate();
            
            // 헤더 설정 (Form 데이터 형식)
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
            // 3. 금결원 규격에 맞는 필수 파라미터 셋팅 (명세서에 나온 필수값들)
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", openbankingclientId);
            params.add("client_secret", openbankingclientSecret);
            params.add("redirect_uri", openbankingredirectUri);
            params.add("grant_type", "authorization_code"); // 고정값
    
            // 4. 요청 보내기 (POST)
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            final ResponseEntity<String> response1 = restTemplate.postForEntity(tokenUrl, request, String.class);
    
            // 5. 결과 받아오기 (JSON 형태의 문자열)
            if (response1.getStatusCode() == HttpStatus.OK) {
                String responseBody = response1.getBody();
                
                // Jackson ObjectMapper로 JSON 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> resultMap = objectMapper.readValue(responseBody, Map.class);
                
                // 🔥 우리가 그토록 원하던 핵심 데이터 추출!
                String accessToken = (String) resultMap.get("access_token");
                String userSeqNo = (String) resultMap.get("user_seq_no"); // 고객 고유 번호
    
                System.out.println("발급된 Access Token: " + accessToken);
                System.out.println("발급된 사용자 일련번호(user_seq_no): " + userSeqNo);
                // 1. 등록계좌조회 API URL (10자리 주면 24자리 계좌번호들 뱉는 곳)
                String accountUrl = "https://testapi.openbanking.or.kr/v2.0/account/list?user_seq_no=" 
                + userSeqNo 
                + "&include_account_num=Y";

                headers.set("Authorization", "Bearer " + accessToken);

                HttpEntity<String> entity = new HttpEntity<>(headers);
                response = restTemplate.exchange(accountUrl, HttpMethod.GET, entity, Map.class);

                if(response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, Object>> resList = (List<Map<String, Object>>) response.getBody().get("res_list");
                Map<String, Object> firstAccount = resList.get(0);
                if(resList != null && !resList.isEmpty()) {
                // 💥 첫 번째 계좌의 24자리 핀테크이용번호를 쏙 뽑아옴!
                String fintechUseNum = (String) firstAccount.get("fintech_use_num");
                String accountNum = (String) firstAccount.get("account_num_masked");
                String bankCode = (String) firstAccount.get("bank_code_std");
                String account_holder_name = (String) firstAccount.get("account_holder_name");
                // 이 24자리 값을 DB의 OPEN_BANK_USER_SEQ_NO 컬럼에 업데이트 하거나 별도로 저장해서 출금할 때 써야 합니다!
                System.out.println("👉 진짜 24자리 번호 획득: " + fintechUseNum);
                System.out.println("👉 진짜 24자리 번호 획득: " + bankCode);
                System.out.println("👉 진짜 24자리 번호 획득: " + accountNum);
                // 🕵️‍♂️ 1. 잔액조회 API URL 생성 (GET 방식이므로 파라미터를 뒤에 주렁주렁 붙임)
                // 현재 요청하는 일시(종료시각 포함)를 생성 (예: 20260629094000) -> 금융 데이터 필수 규격
                String tranDtime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                
                String balanceUrl = "https://testapi.openbanking.or.kr/v2.0/account/balance/fintech_use_num"
                        + "?fintech_use_num=" + fintechUseNum
                        + "&tran_dtime=" + tranDtime;

                // 🕵️‍♂️ 2. 헤더는 기존 Authorization 토큰이 들어있는 headers를 그대로 재사용
                HttpEntity<String> balanceEntity = new HttpEntity<>(headers);
                
                // 🕵️‍♂️ 3. API 전송 및 결과 처리
                ResponseEntity<Map> balanceResponse = restTemplate.exchange(balanceUrl, HttpMethod.GET, balanceEntity, Map.class);
                
                int balance = 0; // 기본값 세팅
                
                if (balanceResponse.getStatusCode() == HttpStatus.OK && balanceResponse.getBody() != null) {
                    Map<String, Object> balanceBody = balanceResponse.getBody();
                    
                    // 금결원 명세서상 잔액 필드명은 "balance_amt" 임! (문자열로 오므로 숫자로 파싱)
                    String balanceAmtStr = (String) balanceBody.get("balance_amt");
                    if (balanceAmtStr != null) {
                        balance = Integer.parseInt(balanceAmtStr);
                    }
                    System.out.println("💰 실시간 계좌 잔액 확인 완료: " + balance + "원");
                }
                memberRepository.updateOpenBankingInfo(userId, accessToken, userSeqNo, fintechUseNum, bankCode, accountNum, balance, account_holder_name);
                paymentService.registerSubMall(userId, bankCode,accountNum, account_holder_name,memberVO);//토스 지급대행을 보안키 지원 안해줘서 api요청은 pass 
                }
                }
                // 6. DB에 저장 (내 서비스 기획에 맞게 마이바티스나 JPA로 쿼리 실행)
                
                
            } else {
                throw new RuntimeException("금융결제원 토큰 발급 실패");
            }
        }
=======
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${kakao.client.id}")
    private String restApiKey;
    @Value("${kakao.redirect.uri}")
    private String redirectUri;
    @Value("${openbanking.client-id}")
    private String openbankingclientId;
    @Value("${openbanking.redirect-uri}")
    private String openbankingredirectUri;
    @Value("${openbanking.client-secret}")
    private String openbankingclientSecret;  
    @Value("${solapi.api-key}")
    private String solapiapikey;
    @Value("${solapi.secret-key}")
    private String solapisecretkey;  
    @Override
    public MemberVO login(Map loginMap) throws Exception {
>>>>>>> origin/develop
        
        return memberRepository.login(loginMap);
    }

    @Override
    public void addMember(MemberVO memberVO) throws Exception {
        memberRepository.insertNewMember(memberVO);
    }

    @Override
    public String overlapped(String id) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'overlapped'");
    }

    @Override
    public String sendVerificationEmail(String toEmail) throws Exception {
        String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));
        // 윈도우 한글 이름으로 인해 구글이 EOF 뱉는 현상을 방어하기 위해 로컬호스트 강제 지정
        /*
        if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl) {
            java.util.Properties props = ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getJavaMailProperties();
            props.put("mail.smtp.localhost", "127.0.0.1");
        }
        String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("chung100302@gmail.com");
        message.setTo(toEmail); // 받는 사람 이메일
        message.setSubject("[SpendOlive] 회원가입 인증번호 안내"); // 이메일 제목
        message.setText("안녕하세요. SpendOlive입니다.\n\n" +
            "회원가입을 완료하기 위한 인증번호는 [ " + verificationCode + " ] 입니다.\n" +
            "타인에게 노출되지 않도록 주의해 주세요."); // 이메일 본문
        
        try{
            mailSender.send(message);
            return verificationCode;
        } catch (Exception e){
            System.out.println("이메일 발송 에러: " + e.getMessage());
        throw new RuntimeException("이메일 전송 중 에러 발생");
        }
   */
        System.out.println("인증번호:"+verificationCode);
        return verificationCode;
    }

    @Override
    public String sendSmsVerification(String toNumber) throws Exception {
        // 1. 진짜 통신사 망을 탈 때와 똑같이 6자리 랜덤 인증번호 생성
        String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));
/*
        DefaultMessageService messageService =  SolapiClient.INSTANCE.createInstance(solapiapikey, solapisecretkey);
        // Message 패키지가 중복될 경우 com.solapi.sdk.message.model.Message로 치환하여 주세요
        Message message = new Message();
        message.setFrom("01024414631");
        message.setTo(toNumber);
        message.setText("★ 발송 메세지: [SpendOlive] 가입 인증번호는 [" + verificationCode + "] 입니다.");

        try {
        // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
        messageService.send(message);
        return verificationCode;
        } catch (SolapiMessageNotReceivedException exception) {
        // 발송에 실패한 메시지 목록을 확인할 수 있습니다!
        System.out.println(exception.getFailedMessageList());
        System.out.println(exception.getMessage());
        throw new RuntimeException("문자 전송 중 오류 발생");
        } catch (Exception exception) { 
        System.out.println(exception.getMessage());
        throw new RuntimeException("문자 전송 중 오류 발생");
        } */
        System.out.println("인증번호:"+verificationCode);
        return verificationCode;
    }
    // 아이디 중복확인 메서드
    @Override
    public boolean checkId(String id){
        return memberRepository.checkId(id);
    }
    @Override
    public boolean checkEmail(String email){
        return memberRepository.checkEmail(email);
    }
    @Override
    public boolean checkPhone(String phone){
        return memberRepository.checkPhone(phone);
    }
    // 카카오
    @Override
    public Map<String, String> getKakaoUserInfo(String code) throws Exception {
        // 1. 코드로 토큰을 받고
        String accessToken = getAccessToken(code);
        // 2. 토큰으로 유저 정보를 가져와서 반환
        return getUserInfo(accessToken);
    }
    private String getAccessToken(String code) throws Exception {
        String accessToken = "";
        URL url = new URL("https://kauth.kakao.com/oauth/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        conn.setDoOutput(true);

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=").append(restApiKey);
            sb.append("&redirect_uri=").append(redirectUri);
            sb.append("&code=").append(code);
            bw.write(sb.toString());
            bw.flush();
        }

        if (conn.getResponseCode() == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
                JsonElement element = JsonParser.parseString(result.toString());
                accessToken = element.getAsJsonObject().get("access_token").getAsString();
            }
        } else {
            throw new Exception("카카오 토큰 발급 실패: 상태 코드 " + conn.getResponseCode());
        }
        return accessToken;
    }
    private Map<String, String> getUserInfo(String accessToken) throws Exception {
        Map<String, String> userInfoMap = new HashMap<>();
        URL url = new URL("https://kapi.kakao.com/v2/user/me");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        if (conn.getResponseCode() == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                JsonObject userInfo = JsonParser.parseString(result.toString()).getAsJsonObject();
                String id = userInfo.get("id").getAsString();
                
                JsonObject properties = userInfo.getAsJsonObject("properties");
                String nickname = (properties != null && properties.get("nickname") != null) 
                                ? properties.get("nickname").getAsString() : "카카오회원";

                userInfoMap.put("id", id);
                userInfoMap.put("nickname", nickname);
                userInfoMap.put("password", "KAKAO");
            }
        } else {
            throw new Exception("카카오 유저 정보 요청 실패: 상태 코드 " + conn.getResponseCode());
        }
        return userInfoMap;
    }
    //오픈뱅킹

    @Override
    public MemberVO getMemberById(String id) throws Exception {
        return memberRepository.selectMemberById(id);
    }

    @Override
    public void updateMyInfo(MemberVO memberVO, String newPassword) throws Exception {
        memberRepository.updateMyInfo(memberVO, newPassword);
    }
    @Override
    @Transactional
    public void registerOpenBankingToken(String code, String userId,HttpHeaders headers,ResponseEntity<Map> response, MemberVO memberVO) throws Exception {
    
        // 1. 금결원 토큰 발급 요청 주소 (테스트베드 환경이므로 testapi 사용!)
        String tokenUrl = "https://testapi.openbanking.or.kr/oauth/2.0/token";

        // 2. RestTemplate을 이용한 HTTP 통신 준비
        RestTemplate restTemplate = new RestTemplate();
        
        // 헤더 설정 (Form 데이터 형식)
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3. 금결원 규격에 맞는 필수 파라미터 셋팅 (명세서에 나온 필수값들)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", openbankingclientId);
        params.add("client_secret", openbankingclientSecret);
        params.add("redirect_uri", openbankingredirectUri);
        params.add("grant_type", "authorization_code"); // 고정값

        // 4. 요청 보내기 (POST)
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        final ResponseEntity<String> response1 = restTemplate.postForEntity(tokenUrl, request, String.class);

        // 5. 결과 받아오기 (JSON 형태의 문자열)
        if (response1.getStatusCode() == HttpStatus.OK) {
            String responseBody = response1.getBody();
            
            // Jackson ObjectMapper로 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> resultMap = objectMapper.readValue(responseBody, Map.class);
            
            // 🔥 우리가 그토록 원하던 핵심 데이터 추출!
            String accessToken = (String) resultMap.get("access_token");
            String userSeqNo = (String) resultMap.get("user_seq_no"); // 고객 고유 번호

            System.out.println("발급된 Access Token: " + accessToken);
            System.out.println("발급된 사용자 일련번호(user_seq_no): " + userSeqNo);
            // 1. 등록계좌조회 API URL (10자리 주면 24자리 계좌번호들 뱉는 곳)
            String accountUrl = "https://testapi.openbanking.or.kr/v2.0/account/list?user_seq_no=" 
            + userSeqNo 
            + "&include_account_num=Y";

            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(accountUrl, HttpMethod.GET, entity, Map.class);

            if(response.getStatusCode() == HttpStatus.OK) {
            List<Map<String, Object>> resList = (List<Map<String, Object>>) response.getBody().get("res_list");
            Map<String, Object> firstAccount = resList.get(0);
            if(resList != null && !resList.isEmpty()) {
            // 💥 첫 번째 계좌의 24자리 핀테크이용번호를 쏙 뽑아옴!
            String fintechUseNum = (String) firstAccount.get("fintech_use_num");
            String accountNum = (String) firstAccount.get("account_num_masked");
            String bankCode = (String) firstAccount.get("bank_code_std");
            String account_holder_name = (String) firstAccount.get("account_holder_name");
            // 이 24자리 값을 DB의 OPEN_BANK_USER_SEQ_NO 컬럼에 업데이트 하거나 별도로 저장해서 출금할 때 써야 합니다!
            System.out.println("👉 진짜 24자리 번호 획득: " + fintechUseNum);
            System.out.println("👉 진짜 24자리 번호 획득: " + bankCode);
            System.out.println("👉 진짜 24자리 번호 획득: " + accountNum);
            // 🕵️‍♂️ 1. 잔액조회 API URL 생성 (GET 방식이므로 파라미터를 뒤에 주렁주렁 붙임)
            // 현재 요청하는 일시(종료시각 포함)를 생성 (예: 20260629094000) -> 금융 데이터 필수 규격
            String tranDtime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            String balanceUrl = "https://testapi.openbanking.or.kr/v2.0/account/balance/fintech_use_num"
                    + "?fintech_use_num=" + fintechUseNum
                    + "&tran_dtime=" + tranDtime;

            // 🕵️‍♂️ 2. 헤더는 기존 Authorization 토큰이 들어있는 headers를 그대로 재사용
            HttpEntity<String> balanceEntity = new HttpEntity<>(headers);
            
            // 🕵️‍♂️ 3. API 전송 및 결과 처리
            ResponseEntity<Map> balanceResponse = restTemplate.exchange(balanceUrl, HttpMethod.GET, balanceEntity, Map.class);
            
            int balance = 0; // 기본값 세팅
            
            if (balanceResponse.getStatusCode() == HttpStatus.OK && balanceResponse.getBody() != null) {
                Map<String, Object> balanceBody = balanceResponse.getBody();
                
                // 금결원 명세서상 잔액 필드명은 "balance_amt" 임! (문자열로 오므로 숫자로 파싱)
                String balanceAmtStr = (String) balanceBody.get("balance_amt");
                if (balanceAmtStr != null) {
                    balance = Integer.parseInt(balanceAmtStr);
                }
                System.out.println("💰 실시간 계좌 잔액 확인 완료: " + balance + "원");
            }
            memberRepository.updateOpenBankingInfo(userId, accessToken, userSeqNo, fintechUseNum, bankCode, accountNum, balance, account_holder_name);
            memberRepository.updateMember_account_status(userId);
            paymentService.registerSubMall(userId, bankCode,accountNum, account_holder_name,memberVO);//토스 지급대행을 보안키 지원 안해줘서 api요청은 pass 
               
            }
            }
            // 6. DB에 저장 (내 서비스 기획에 맞게 마이바티스나 JPA로 쿼리 실행)
            
            
        } else {
            throw new RuntimeException("금융결제원 토큰 발급 실패");
        }
    }
    

    /* =========================================================
       [추가 기능 구현] 아이디/비밀번호 찾기 Service 구현부
       ---------------------------------------------------------
       현재 ServiceImpl에서는 별도 복잡한 비즈니스 로직을 추가하지 않고,
       Controller에서 검증한 요청을 Repository로 전달하는 역할을 한다.
       실제 SQL 조회/수정은 MemberRepositoryImpl에서 처리한다.
       ========================================================= */
    @Override
    public String findIdByPhone(String phone) throws Exception {
        // 아이디 찾기: 휴대폰 번호로 ACTIVE 회원의 id를 조회한다.
        return memberRepository.findIdByPhone(phone);
    }

    @Override
    public boolean existsActiveId(String id) throws Exception {
        // 비밀번호 찾기 1차 검증: 입력한 아이디가 존재하는지 확인한다.
        return memberRepository.existsActiveId(id);
    }

    @Override
    public boolean existsActiveMemberByIdAndPhone(String id, String phone) throws Exception {
        // 비밀번호 찾기 2차 검증: 아이디와 휴대폰 번호가 같은 회원 정보인지 확인한다.
        return memberRepository.existsActiveMemberByIdAndPhone(id, phone);
    }

    @Override
    public void updatePasswordById(String id, String newPassword) throws Exception {
        // 휴대폰 인증이 끝난 회원의 비밀번호를 새 값으로 변경한다.
        memberRepository.updatePasswordById(id, newPassword);
    }

}


