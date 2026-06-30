package com.example.spendolive.payment.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.spendolive.member.domain.MemberCardVO;
import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;
import com.example.spendolive.payment.domain.*;
import com.example.spendolive.payment.repository.PaymentRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class PaymentServiceImpl implements PaymentService{
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Value("${openbanking.useorg-code}")
    private String useorgCode;

    @Value("${openbanking.cntr-account-num}")
    private String cntrAccountNum;

    @Value("${openbanking.cntr-bank-code}")
    private String cntrBankCode;

    @Value("${openbanking.cntr-account-holder}")
    private String cntrAccountHolder;
    @Value("${toss.secret-key}")
    private String secretKey;
/*
    @Override
    @Transactional(rollbackFor = Exception.class) // 💥 돈 관련 로직이므로 에러 나면 무조건 DB 롤백!
    public boolean processWithdraw(SettlementPaymentVO paymentInfo, MemberVO memberInfo) throws Exception {
        
        // 1. 금결원 출금이체 API 주소
        String apiUrl = "https://testapi.openbanking.or.kr/v2.0/transfer/withdraw/fintech_use_num";

        // 2. RestTemplate 및 헤더 세팅 (Bearer 토큰 필수)
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // 출금이체는 JSON 바디로 요청
        headers.set("Authorization", "Bearer " + memberInfo.getOpen_bank_token());

        // 3. 거래고유번호(bank_tran_id) 생성 -> 이용기관코드(10) + U + 난수(9) = 총 20자리
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
        String bankTranId = useorgCode + "U" + uniqueId;

        // 4. 요청 시간 생성 (YYYYMMDDHHMMSS)
        String tranDtime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 5. 금결원 규격에 맞는 JSON 바디 조립 (Map 활용)
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("bank_tran_id", bankTranId);
        bodyMap.put("cntr_account_type", "C");                  // N: 계좌 형식
        bodyMap.put("cntr_account_num", cntrAccountNum);        // 우리 수납 계좌
        bodyMap.put("dps_print_text", "SpendOlive입금");        // 우리 통장에 찍힐 문구
        
        // 중요: 돈을 빼올 파티원의 핀테크이용번호 (이건 이전에 계좌조회로 얻어와서 세션이나 매개변수로 넘겨받아야 함)
        // 여기선 셈플로 파티원의 메모 필드나 가상 필드에 들고 있다고 가정합니다.
        bodyMap.put("fintech_use_num", "199000000000000000000001"); 
        
        bodyMap.put("wd_print_text", "스펜드올리브출금");          // 파티원 통장에 찍힐 문구
        bodyMap.put("tran_amt", paymentInfo.getTotal_amount()); // 출금할 금액 (이용료 + 수수료)
        bodyMap.put("tran_dtime", tranDtime);
        bodyMap.put("req_client_name", "홍길동");             // 파티원 이름
        bodyMap.put("req_client_num", memberInfo.getId());      // 파티원 ID
        bodyMap.put("req_client_fintech_use_num", "199000000000000000000001");
        bodyMap.put("recv_client_name", cntrAccountHolder);     // 수취인 성명 (나)
        bodyMap.put("recv_client_bank_code", cntrBankCode);     // 수취인 은행 코드 (내 은행)
        bodyMap.put("recv_client_account_num", cntrAccountNum); // 수취인 계좌 (내 계좌)

        // 6. API 요청 날리기
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> resultMap = mapper.readValue(response.getBody(), Map.class);
            
            // 금결원 응답 코드 응답 (기본적으로 "A0000" 이 성공입니다)
            String rspCode = (String) resultMap.get("rsp_code");
            
            if ("A0000".equals(rspCode)) {
                // 🚀 [성공] 1단계: 팀원별 입금 장부(SettlementPayment) 상태를 PAID로 변경
                paymentInfo.setPayment_status("PAID");
                paymentInfo.setPaidAt(LocalDateTime.now());
                paymentRepository.updatePaymentStatus(paymentInfo); // 레포지토리에 반영

                // 🚀 [성공] 2단계: 먹튀 방지를 위해 에스크로(Escrow) 금고 테이블에 돈 묶어두기
                EscrowPayoutVO escrow = new EscrowPayoutVO();
                escrow.setSettlementId(paymentInfo.getSettlementId());
                // 방의 룸 ID와 방장 ID는 원래 룸 정보에서 꺼내와야 하므로 데이터 바인딩 필요
                escrow.setRoomId(1); // 예시 ID
                escrow.setPayerId(paymentInfo.getId());
                escrow.setHostId("방장ID_조회필요");
                escrow.setAmount(paymentInfo.getBaseAmount()); // 수수료 뺀 원금 보관
                escrow.setStatus("HELD"); // 보관 상태로 지정
                
                paymentRepository.insertEscrow(escrow); // 에스크로 인서트
                
                System.out.println("출금이체 및 장부 업데이트 완료! 파티원: " + paymentInfo.getId());
                return true;
            } else {
                System.out.println("금결원 거절 사유: " + resultMap.get("rsp_message"));
                return false;
            }
        }
        
        return false;
    }
     */
    @Override
    public void issueAndSaveBillingKey(String customerKey, String authKey, String userId) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        
        // 1. 최신 2024-06-01 버전 규격 엔드포인트 주소
        String url = "https://api.tosspayments.com/v1/billing/authorizations/issue";

        // 💥 [임시 조치]properties에서 읽어오는 게 문제일 수 있으니, 대시보드에 있는 진짜 test_sk_... 값을 여기에 생으로 넣어버려 형!
        String myRealSecretKey = secretKey; 
        
        // 토스 규격대로 뒤에 콜론(:)을 붙이고 Base64로 인코딩
        String rawKey = myRealSecretKey.trim() + ":";
        String encodedSecretKey = Base64.getEncoder().encodeToString(rawKey.getBytes());

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedSecretKey); // Basic 뒤에 한 칸 공백 필수
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 바디 설정
        Map<String, String> body = new HashMap<>();
        body.put("customerKey", customerKey);
        body.put("authKey", authKey);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            // String으로 생으로 받아서 꼬임 방지
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            System.out.println("👉 [토스 응답 바디] : " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
                Map<String, Object> resBody = mapper.readValue(response.getBody(), Map.class);
                String billingKey = (String) resBody.get("billingKey"); 
                Map<String, Object> cardInfo = (Map<String, Object>) resBody.get("card");
                
                String card_num = null;
                String card_company = null;
                if (cardInfo != null) {
                    card_num = (String) cardInfo.get("number"); 
                    card_company = (String) cardInfo.get("issuerCode"); // "신한카드" 형태로 나옴 (만약 안 나오면 "company"로 테스트)
                }
               
               
                memberRepository.updateTossInfo(userId, card_num, card_company, billingKey);
                
            }
        } catch (Exception e) {
            System.out.println("❌ [최종 에러 디버깅] : " + e.getMessage());
            throw new RuntimeException("토스 통신 실패: " + e.getMessage());
        }
    }
    @Override
public void executeAutomaticPayment(String userId, int amount, int room_id) throws Exception {
    RestTemplate restTemplate = new RestTemplate();
    
    // 💥 한글 깨짐 방지 처리 (주문명 한글 깨짐 방지)
    restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("🚨 [서비스 입구] 컨트롤러가 나한테 넘겨준 userId 값 : [" + userId + "]");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    // 1. DB에서 해당 유저의 빌링키와 카드 정보 조회해오기
    // 형이 만든 MEMBER_CARD_TB에서 가져오는 레포지토리 메서드가 있다고 가정할게!
    MemberCardVO cardVo = memberRepository.getCardInfoByUserId(userId);
    if (cardVo == null || cardVo.getBillingKey() == null) {
        throw new RuntimeException("등록된 결제 카드가 없습니다.");
    }
    
    String billingKey = cardVo.getBillingKey();

    // 2. 토스 자동결제 엔드포인트 URL (패스 배리어블에 빌링키 꽂기!)
    String url = "https://api.tosspayments.com/v1/billing/" + billingKey;

    // 3. 인증 헤더 세팅 (Basic Auth)
    String myRealSecretKey = secretKey; 
    String rawKey = myRealSecretKey.trim() + ":";
    String encodedSecretKey = Base64.getEncoder().encodeToString(rawKey.getBytes());

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Basic " + encodedSecretKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    // 4. 매 결제마다 고유해야 하는 주문번호(orderId) 생성 (UUID 기반)
    String orderId = "SPENDOLIVE_" + java.util.UUID.randomUUID().toString().substring(0, 12).toUpperCase();

    // 5. 토스 규격 필수 바디 파라미터 조립
    Map<String, Object> body = new HashMap<>();
    body.put("customerKey", userId); // 토스 구분을 위한 고객 고유키
    body.put("amount", amount);                // 결제 금액
    body.put("orderId", orderId);              // 주문 번호
    body.put("orderName", "spendOlive OTT 정산");          // 주문명 (ex: 정기 구독권)

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

    try {
        System.out.println("💳 [자동결제 요청 시작] 유저: " + userId + " | 금액: " + amount + "원");
        
        // 토스 서버로 결제 승인 요청 (POST)
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        System.out.println("👉 [토스 결제 응답 바디] : " + response.getBody());

        // 6. 🔥 철칙 준수: 토스 응답이 확실하게 200 OK일 때만 내부 비즈니스 로직 진행!
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            Map<String, Object> resBody = mapper.readValue(response.getBody(), Map.class);
            Map<String, Object> cardInfo = (Map<String, Object>) resBody.get("card");
            // 토스 응답에서 결제 고유 번호(paymentKey) 추출 (나중에 혹시 취소/환불할 때 무조건 필요함!)
            String paymentKey = (String) resBody.get("paymentKey");
            String status = (String) resBody.get("status"); // DONE 이면 결제 완료
            String orderid = (String) resBody.get("orderId");
            int totalamount = (int) resBody.get("totalAmount");
            String approvedAtStr = (String) resBody.get("approvedAt");
            LocalDateTime approved_at = OffsetDateTime.parse(approvedAtStr).toLocalDateTime();
            String cardnumber = (String) cardInfo.get("number");
            String cardcompany = (String) cardInfo.get("issuerCode");
            
            SettlementPaymentVO paymentInfo = new SettlementPaymentVO();
            paymentInfo.setId(userId);
            paymentInfo.setOrderId(orderid);
            paymentInfo.setPaymentKey(paymentKey);
            paymentInfo.setTotal_amount(totalamount);
            paymentInfo.setCard_number(cardnumber);
            paymentInfo.setCard_company(cardcompany);
            paymentInfo.setPaid_at(approved_at);
            paymentInfo.setPayment_status("PAID");
            
            System.out.println("✅ [결제 성공 확인] paymentKey: " + paymentKey + " | 상태: " + status);

            if ("DONE".equals(status)) {
                paymentRepository.updatePaymentStatus(paymentInfo);
               
                System.out.println("🎉 회원 [" + userId + "] DB 비즈니스 로직 반영 완료!");
            } else {
                throw new RuntimeException("결제가 완료되지 않은 상태입니다: " + status);
            }
        }
    } catch (Exception e) {
        // 7. 🚨 타임아웃, 한도초과, 잔액부족, 카드정지 등 외부 에러 발생 시 잡아내기
        System.out.println("❌ [자동결제 승인 실패 에러 대피소] : " + e.getMessage());
        
        // 형이 말했던 예외 처리 로직 작동 구역
        // 여기서는 우리 DB에 아무 작업도 안 가했기 때문에 데이터 정합성이 깨질 일이 없어 형! (안전)
        throw new RuntimeException("자동결제 시스템 오류로 승인이 실패했습니다: " + e.getMessage());
    }
}
    @Override
    public SettlementPaymentVO getSettlement_PaymentByRoomId(String userId, int roomId) throws Exception {
        return paymentRepository.settlement_paymentByroomId(userId, roomId);
    }
}

