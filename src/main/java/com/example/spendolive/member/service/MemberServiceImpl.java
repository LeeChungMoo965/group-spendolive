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
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Propagation;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.util.LinkedMultiValueMap;
    import org.springframework.util.MultiValueMap;
    import org.springframework.web.client.RestTemplate;

    import com.example.spendolive.member.domain.MemberVO;
    import com.example.spendolive.member.repository.MemberRepository;
    import com.google.gson.JsonElement;
    import com.google.gson.JsonObject;
    import com.google.gson.JsonParser;
    import jakarta.mail.internet.MimeMessage;
import tools.jackson.databind.ObjectMapper;
    @Service
    public class MemberServiceImpl implements MemberService {

        @Autowired
        private MemberRepository memberRepository;
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
            String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));
            
            String title = "[SpendOlive] 회원가입 이메일 인증번호입니다.";
            String content = "<h3>안녕하세요. SpendOlive입니다.</h3>"
                        + "<p>회원가입 화면에서 아래의 인증번호를 입력해 주세요.</p>"
                        + "<h2 style='color: #4CAF50;'>" + verificationCode + "</h2>"
                        + "<p>감사합니다.</p>";

            // 포털 사이트의 기기 보안 차단을 원천 봉쇄하는 개발자 전용 발송 서버 세팅
            org.springframework.mail.javamail.JavaMailSenderImpl customSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
            customSender.setHost("sandbox.smtp.mailtrap.io"); // ◀ 메일트랩 호스트
            customSender.setPort(2525); // ◀ 보안 충돌 없는 포트
            customSender.setUsername("e05077ac67ba67"); // ◀ 임시 공용 테스트 아이디
            customSender.setPassword("a9a2d54c235102"); // ◀ 임시 공용 테스트 비밀번호

            java.util.Properties props = customSender.getJavaMailProperties();
    // ◀ [이 줄을 반드시 추가!] 윈도우 한글 이름으로 인한 501 에러를 원천 차단합니다.
            props.put("mail.smtp.localhost", "127.0.0.1"); 
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true"); 
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            try {
                MimeMessage message = customSender.createMimeMessage(); 
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("admin@spendolive.com");
                helper.setTo(toEmail);
                helper.setSubject(title);
                helper.setText(content, true);

                // 실제 발송 전송 로직 작동
                customSender.send(message); 
                System.out.println("★ SMTP 이메일 발송 최종 성공! 인증번호: " + verificationCode);
                
                return verificationCode;
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.");
            }
        }
    //추후에 시연 할 때 실계정 전환 후 시연 할 예정  ()
        @Override
        public String sendSmsVerification(String toNumber) throws Exception {
            // 1. 진짜 통신사 망을 탈 때와 똑같이 6자리 랜덤 인증번호 생성
            String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));

            // 2. 외부 쿨에스엠에스 서버를 거치지 않고, 성공 패킷을 백엔드가 직접 조립
            try {
                System.out.println("=========================================");
                System.out.println("★ [CoolSMS 가상 시뮬레이터 작동 중]");
                System.out.println("★ 수신자 번호: " + toNumber);
                System.out.println("★ 발송 메세지: [SpendOlive] 가입 인증번호는 [" + verificationCode + "] 입니다.");
                System.out.println("★ 상태 코드: 200 (Success) - 가상 발송 완료");
                System.out.println("=========================================");
                
                // 3. 화면(프론트엔드)과 세션 검증 로직으로 인증번호를 그대로 리턴
                return verificationCode;
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("문자 가상 전송 중 오류 발생");
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
        public void registerOpenBankingToken(String code, String userId,HttpHeaders headers,ResponseEntity<Map> response) throws Exception {
        
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
                if(resList != null && !resList.isEmpty()) {
                // 💥 첫 번째 계좌의 24자리 핀테크이용번호를 쏙 뽑아옴!
                String fintechUseNum = (String) resList.get(0).get("fintech_use_num");

                // 이 24자리 값을 DB의 OPEN_BANK_USER_SEQ_NO 컬럼에 업데이트 하거나 별도로 저장해서 출금할 때 써야 합니다!
                System.out.println("👉 진짜 24자리 번호 획득: " + fintechUseNum);
                memberRepository.updateOpenBankingInfo(userId, accessToken, userSeqNo, fintechUseNum);
                }
                }
                // 6. DB에 저장 (내 서비스 기획에 맞게 마이바티스나 JPA로 쿼리 실행)
                
                
            } else {
                throw new RuntimeException("금융결제원 토큰 발급 실패");
            }
        }
    }


