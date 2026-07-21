package com.example.spendolive.payment.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.spendolive.member.domain.MemberCardVO;
import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.ott.repository.OttRepository;
import com.example.spendolive.payment.domain.*;
import com.example.spendolive.payment.repository.PaymentRepository;
@Service
public class PaymentServiceImpl implements PaymentService{
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OttRepository ottRepository;
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
    

    @Override
    @Transactional(rollbackFor = Exception.class) //금결원 출금이체 프로세스 (권한 문제로 홀딩)
    public boolean processWithdraw(SettlementPaymentVO paymentInfo, MemberVO memberInfo) throws Exception {
        /*
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
                paymentInfo.setPaid_at(LocalDateTime.now());
                paymentRepository.updatePaymentStatus(paymentInfo); // 레포지토리에 반영

                // 🚀 [성공] 2단계: 먹튀 방지를 위해 에스크로(Escrow) 금고 테이블에 돈 묶어두기
                EscrowPayoutVO escrow = new EscrowPayoutVO();
                escrow.setSettlement_id(paymentInfo.getSettlement_id());
                // 방의 룸 ID와 방장 ID는 원래 룸 정보에서 꺼내와야 하므로 데이터 바인딩 필요
                escrow.setRoom_id(1); // 예시 ID
                escrow.setPayerId(paymentInfo.getId());
                escrow.setHostId("방장ID_조회필요");
                escrow.setAmount(paymentInfo.getBase_amount()); // 수수료 뺀 원금 보관
                escrow.setStatus("HELD"); // 보관 상태로 지정
                
                paymentRepository.insertEscrow(escrow); // 에스크로 인서트
                
                System.out.println("출금이체 및 장부 업데이트 완료! 파티원: " + paymentInfo.getId());
                return true;
            } else {
                System.out.println("금결원 거절 사유: " + resultMap.get("rsp_message"));
                return false;
            }
        }
     */   
        return false;
    }
      // 카드 등록 프로세스
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueAndSaveBillingKey(String customerKey, String authKey, String userId) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        
        // 1. 최신 2024-06-01 버전 규격 엔드포인트 주소
        String url = "https://api.tosspayments.com/v1/billing/authorizations/issue";
        
        // 토스 규격대로 뒤에 콜론(:)을 붙이고 Base64로 인코딩
        String rawKey = secretKey.trim() + ":";
        String encodedSecretKey = Base64.getEncoder().encodeToString(rawKey.getBytes());

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedSecretKey); 
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 바디 설정
        Map<String, String> body = new HashMap<>();
        body.put("customerKey", customerKey);
        body.put("authKey", authKey);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("[토스 응답 바디] : " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
                Map<String, Object> resBody = mapper.readValue(response.getBody(), Map.class);
                String billingKey = (String) resBody.get("billingKey"); 
                Map<String, Object> cardInfo = (Map<String, Object>) resBody.get("card");
                String card_num = null;
                String card_company = null;
                if (cardInfo != null) {
                    card_num = (String) cardInfo.get("number"); 
                    card_company = (String) cardInfo.get("issuerCode"); 
                }
                memberRepository.updateTossInfo(userId, card_num, card_company, billingKey);
                memberRepository.updateMember_card_status(userId);
            }
        } catch (Exception e) {
            System.out.println("[최종 에러 디버깅] : " + e.getMessage());
            throw new RuntimeException("토스 통신 실패: " + e.getMessage());
        }
    }
    // 결제 프로세스
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeAutomaticPayment(String userId, int amount, int room_id, int fee, int base, int settlement_id, String host_id) throws Exception {
    RestTemplate restTemplate = new RestTemplate();
    

    restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));
    
    List<MemberCardVO> cardVoList = memberRepository.selectCardById(userId);
    String billingKey = "";
    if (cardVoList == null) {
        throw new RuntimeException("등록된 결제 카드가 없습니다.");
    }
    for(MemberCardVO card : cardVoList){
        if(card.getStatus().equals("YES")){
            billingKey = card.getBilling_key();
        }
    }
    
    String url = "https://api.tosspayments.com/v1/billing/" + billingKey;

    String myRealSecretKey = secretKey; 
    String rawKey = myRealSecretKey.trim() + ":";
    String encodedSecretKey = Base64.getEncoder().encodeToString(rawKey.getBytes());

    // 헤더 설정
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Basic " + encodedSecretKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    // 주문번호(orderId) 생성
    String orderId = "SPENDOLIVE_" + java.util.UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    // 바디 설정
    Map<String, Object> body = new HashMap<>();
    body.put("customerKey", userId); 
    body.put("amount", amount);                // 결제 금액
    body.put("orderId", orderId);              // 주문 번호
    body.put("orderName", "spendOlive OTT 정산");          // 주문명
    tools.jackson.databind.ObjectMapper jsonMapper = new tools.jackson.databind.ObjectMapper();
    String jsonBody = jsonMapper.writeValueAsString(body);
    HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

    try {
        
        // 토스 서버로 결제 승인 요청 (POST)
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        //200코드 응답
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            Map<String, Object> resBody = mapper.readValue(response.getBody(), Map.class);
            Map<String, Object> cardInfo = (Map<String, Object>) resBody.get("card");
            // 토스 응답에서 결제 고유 번호(paymentKey) 
            String paymentKey = (String) resBody.get("paymentKey");
            String status = (String) resBody.get("status"); 
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
            paymentInfo.setBase_amount(base);
            paymentInfo.setFee_amount(fee);
            paymentInfo.setFee_rate(3D);
            paymentInfo.setMemo("OTT 사용료");   
            paymentInfo.setSettlement_id(settlement_id);
            EscrowPayoutVO escrowInfo = new EscrowPayoutVO();

            escrowInfo.setAmount(base);
            escrowInfo.setCreated_at(approved_at);
            escrowInfo.setHost_id(host_id);
            escrowInfo.setPayer_id(userId);
            escrowInfo.setRoom_id(room_id);
            escrowInfo.setSettlement_id(settlement_id);
            escrowInfo.setStatus("HELD");
            
            PlatformRevenueVO revenueInfo = new PlatformRevenueVO();
            revenueInfo.setBase_amount(base * 4);
            revenueInfo.setCreated_at(approved_at);
            revenueInfo.setFee_amount(fee);
            revenueInfo.setFee_rate(3D);
            revenueInfo.setPayer_id(userId);
            revenueInfo.setRoom_id(room_id);
            revenueInfo.setSettlement_id(settlement_id);
            revenueInfo.setStatus("EARNED");
            

            System.out.println("결제 성공 확인 paymentKey: " + paymentKey + " | 상태: " + status);

            if ("DONE".equals(status)) {
                try{
                    if(totalamount != amount){throw new RuntimeException("결제 금액 미부합 ");}
                    paymentRepository.updatePaymentStatus(paymentInfo);
                    paymentRepository.insertEscrow(escrowInfo);
                    paymentRepository.insertPlatfoem_Revenue(revenueInfo);
                    paymentRepository.updatSettlementroommemberStatus(room_id, userId);
                    
                }catch(Exception e){
                    //취소 api 요청
                    //헤더는 위에 것 그대로 사용 
                    String cancelUrl = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
                    //바디 설정
                    Map<String, Object> cancelBody = new HashMap<>();
                    cancelBody.put("cancelReason", "서버 오류로 인한 취소");//취소 이유
                    String cancelJson = jsonMapper.writeValueAsString(cancelBody);
                    HttpEntity<String> cancelEntity = new HttpEntity<>(cancelJson, headers);
                    try{
                        ResponseEntity<String> cancelResponse = restTemplate.postForEntity(cancelUrl, cancelEntity, String.class);
                        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                            // 추후 취소 내역 테이블 생성후 처리
                            mapper = new tools.jackson.databind.ObjectMapper();
                            Map<String, Object> resBodys = mapper.readValue(cancelResponse.getBody(), Map.class);
                            String canceledAtStr = (String) resBodys.get("approvedAt");
                        }
                    }catch(Exception a){
                        throw new RuntimeException("결제 취소 중 오류 발생 다시 시도 하겠습니다: " + status);

                    }
                    throw new RuntimeException("결제 완료 후 데이터 저장 중 오류 발생   결제를 취소하겠습니다: " + status);
                }

            } else {
                throw new RuntimeException("결제가 완료되지 않은 상태입니다: " + status);
            }
        }
    } catch (Exception e) {
        // 타임아웃, 한도초과, 잔액부족, 카드정지 등 외부 에러
        System.out.println("자동결제 승인 실패 에러  : " + e.getMessage());
        System.out.println(userId + amount + orderId);
        throw new RuntimeException("자동결제 시스템 오류로 승인이 실패했습니다: " + e.getMessage());
    }
}
    @Override
    public SettlementPaymentVO getSettlement_PaymentByRoomId(String userId, int room_id) throws Exception {
        return paymentRepository.settlement_paymentByroomId(userId, room_id);
    }
    @Override
    public OttSettlementDTO selectMySettlements(int room_id)  throws Exception{
        return paymentRepository.settlementByroomId(room_id);
    }
    //토스 정산금 보낼 셀러 등록 프로세스(권한 문제로 보류)
    @Override
    @Transactional
    public void registerSubMall(String userId, String bankCode, String accNum, String holderName, MemberVO memberVO) {
    
    // 1. v1 정산 API 주소
    String TOSS_API_URL = "https://api.tosspayments.com/v1/payouts/sub-malls"; 
    
    try {
        
        RestTemplate restTemplate = new RestTemplate();
        String name = memberVO.getMember_name();
        
        // v1은 계좌 실시간 조회를 안 하므로 마스킹이 섞여도 포맷만 맞으면 무조건 패스합니다.
        String cleanNum = accNum.replace("***", "000").replace("-", ""); 

        // 🌟 2. 별도 DTO 없이 Map 구조로 v1 스펙에 맞게 데이터 세팅
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("subMallId", "SELLER_" + userId);   // 고유 식별자
        requestBody.put("companyName", name);               // 상호명
        requestBody.put("representativeName", name);        // 대표자명
        requestBody.put("identityNumber", "0001013111111"); // 예시: 주민번호 앞자리6자리 + 뒷자리7자리 총 13자리
        requestBody.put("type", "INDIVIDUAL");
        // 정산 계좌 객체 조립 (v1 필드명: bank, accountNumber, holderName)
        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put("bank", "국민"); 
        accountInfo.put("accountNumber", cleanNum);
        accountInfo.put("holderName", name);
        requestBody.put("account", accountInfo);

        // 🌟 3. HTTP 헤더 세팅 (순수 JSON 통신 설정)
        HttpHeaders headers = new HttpHeaders();
        String rawKey = secretKey.trim() + ":";
        String encodedSecretKey = Base64.getEncoder().encodeToString(rawKey.getBytes());
        
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON); // text/plain 대신 무조건 JSON!

        // Map 객체와 헤더를 바인딩
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 🌟 4. 토스 API 호출
        ResponseEntity<String> response = restTemplate.exchange(
            TOSS_API_URL,
            HttpMethod.POST,
            entity,
            String.class
        );
        
        
        if (response.getStatusCode() == HttpStatus.OK) {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            Map<String, Object> resBody = mapper.readValue(response.getBody(), Map.class);
            
            // 🌟 5. v1 응답 데이터 구조에 맞춰 파싱 및 DB 저장

            SellerAccountVO seller = SellerAccountVO.builder()
                    .member_id(userId)
                    .bank_name((String) resBody.get("bank"))
                    .account_number((String) resBody.get("accountNumber"))
                    .traceId(UUID.randomUUID().toString()) // v1은 traceId를 안 주므로 내부용 임의 생성
                    .build();
                    
            try{
            String traceId ="1123412312321413243142sadsadadsdsadasd";        
            SellerAccountVO sellerInfo = SellerAccountVO.builder()
            .member_id(userId)
            .bank_name(bankCode)
            .account_number(accNum)
            .traceId(traceId) // v1은 traceId를 안 주므로 내부용 임의 생성
            .build();
            paymentRepository.insertSeller(seller);
            
            System.out.println("🎉 [토스 셀러 등록 성공] subMallId : SELLER_" + userId);
            }catch(Exception e){
                //취소 api요청
                throw new RuntimeException("서버 오류 로 송금을 취소합니다");
            }
        }

    } catch (HttpClientErrorException e) {
        System.err.println("🚨 [토스 API 리턴 에러]: " + e.getResponseBodyAsString());
        throw new RuntimeException("토스 서브몰 등록 중 API 검증 오류 발생");
    } catch (Exception e) {
        System.err.println("🚨 [시스템 에러]: " + e.getMessage());
        throw new RuntimeException("토스 서브몰 등록 중 시스템 오류 발생");
    }
}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OttRoomDTO> selectTodaysettlement(String status) throws Exception {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        int endday = YearMonth.from(today).lengthOfMonth();
        if(day == endday && endday<31){
            endday =31;
      
        }else{
            endday = day;
            
        }
        paymentRepository.updatSettlementStatusYET(endday); // 빌링데이가 지난 데이터는 다시 상태를YET으로 
        paymentRepository.updateReadyfromYet(day,endday); //오늘 정산금 송금 할 내역들은 YET에서 READY로
 
            
        return paymentRepository.selectTodaysettlement(day,endday,status);
       
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OttRoomMemberDTO> selectTodaysettlementmember(String status) throws Exception {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        int endday = YearMonth.from(today).lengthOfMonth();
       if(day == endday && endday<31){
            endday =31;
      
        }else{
            endday = day;
            
        }

            paymentRepository.updatSettlementStatusYETroommember(day); // 페이데이가 지난 데이터는 다시 상태를YET으로 
            paymentRepository.updateReadyfromYettoroommember(day,endday); //오늘 정산금 정산 할 내역들은 YET에서 READY로
            return paymentRepository.selectTodaysettlementMember(day,endday,status);
        }
    
    //정산금 송금 후 상태값 변경 프로세스
    @Override
    @Transactional
    public String updateExcrow(int room_id) throws Exception {


        try{
        //추후 사업자 등록 후 토스지급대행 , 금결원 출금이체 api 사용 메서드      
        }catch(Exception e){

            //api사용중 오류 시 바로 예외처리 db저장 x
            
            return "송금중 문제가 생겼습니다. ";
            }
        try{
            paymentRepository.updateEscrowStatus(room_id);
            paymentRepository.updatSettlementStatus(room_id);
        }catch(Exception e){
            throw new RuntimeException("DB 업데이트 실패: 송금 완료 후 서버 쪽에서 오류가 생겼습니다.");
        }
        return "송금을 정상적으로 완료 하였습니다.";
    }
    
    @Override
    public String roomMemberByroomIdCount(int room_id, String userId) throws Exception {
        return paymentRepository.roomMemberByroomIdCount(room_id, userId);
    }
    @Override
    public void updateTodaysettlementroommemberlate(int roomId,String userId,int late_day) throws Exception {
        paymentRepository.updateTodaysettlementroommemberlate(roomId, userId,late_day);
    }
    @Override
    public OttRoomDTO selectRoomByRoomId(int roomId) throws Exception {
        String roomidStr = String.valueOf(roomId);
        Long roomid = Long.parseLong(roomidStr);
        return ottRepository.selectRoom(roomid);
    }
}    

