package com.example.spendolive.payment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.payment.domain.*;
import com.example.spendolive.payment.repository.PaymentRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class PaymentServiceImpl implements PaymentService{
    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${openbanking.useorg-code}")
    private String useorgCode;

    @Value("${openbanking.cntr-account-num}")
    private String cntrAccountNum;

    @Value("${openbanking.cntr-bank-code}")
    private String cntrBankCode;

    @Value("${openbanking.cntr-account-holder}")
    private String cntrAccountHolder;

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
                paymentInfo.setPaid_at(LocalDateTime.now());
                paymentRepository.updatePaymentStatus(paymentInfo); // 레포지토리에 반영

                // 🚀 [성공] 2단계: 먹튀 방지를 위해 에스크로(Escrow) 금고 테이블에 돈 묶어두기
                EscrowVO escrow = new EscrowVO();
                escrow.setSettlement_id(paymentInfo.getSettlement_id());
                // 방의 룸 ID와 방장 ID는 원래 룸 정보에서 꺼내와야 하므로 데이터 바인딩 필요
                escrow.setRoom_id(1); // 예시 ID
                escrow.setPayer_id(paymentInfo.getId());
                escrow.setHost_id("방장ID_조회필요");
                escrow.setAmount(paymentInfo.getBase_amount()); // 수수료 뺀 원금 보관
                escrow.setEscrow_status("HELD"); // 보관 상태로 지정
                
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
}

