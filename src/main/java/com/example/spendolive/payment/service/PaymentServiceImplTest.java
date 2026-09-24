package com.example.spendolive.payment.service;

import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.ott.repository.OttRepository;
import com.example.spendolive.payment.domain.PaymentAmountDTO;
import com.example.spendolive.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OttRepository ottRepository;

    @Test
    @DisplayName("공유 결제 금액이 소수점일 경우 원 단위 올림 처리 및 수수료 계산이 정확해야 한다.")
    void getPaymentAmount_success() throws Exception {
        //Given
        int roomId = 1;
        
        OttSettlementDTO mockSettlement = new OttSettlementDTO();
        mockSettlement.setSettlement_id(100L);
        mockSettlement.setMember_limit(4); 
        mockSettlement.setTotal_price(17000);
        mockSettlement.setHost_login_id("user");

        OttRoomDTO mockRoom = new OttRoomDTO();
        mockRoom.setRoom_name("넷플릭스 파티");
        mockRoom.setStatus("OPEN");
        mockRoom.setBilling_day(5); // 매월 5일 결제

        
        given(paymentRepository.settlementByroomId(roomId)).willReturn(mockSettlement);
        given(ottRepository.selectRoom((long) roomId)).willReturn(mockRoom);
        
        // When
        PaymentAmountDTO result = paymentService.getPaymentAmount(roomId);

        // Then
        assertThat(result.getBaseAmount()).isEqualTo(4250);
        assertThat(result.getFeeAmount()).isEqualTo(128);
        assertThat(result.getTotalAmount()).isEqualTo(4378);
        assertThat(result.getAutomaticPaymentDay()).isEqualTo(25);
    }
   

    @Test
    @DisplayName("자동결제 보안 검증: 토스에서 승인된 금액이 요청 금액과 다르면 결제가 즉시 취소되고 예외가 발생한다.")
    void executeAutomaticPayment_Fail_AmountMismatch() throws Exception {
        // Given 
        String userId = "testUser";
        int requestedAmount = 5000;
        
        // 유저의 주 결제 카드가 있다고 설정
        MemberCardVO mockCard = new MemberCardVO();
        mockCard.setStatus("YES");
        mockCard.setBilling_key("test_billing_key_123");
        given(memberRepository.selectCardById(userId)).willReturn(List.of(mockCard));

        // 실제 결제 금액 다르게 설정
        String mockTossResponse = """
            {
                "paymentKey": "test_payment_key_999",
                "status": "DONE",
                "orderId": "SPENDOLIVE_ABC",
                "totalAmount": 6000, 
                "approvedAt": "2026-09-25T12:00:00+09:00",
                "card": { "number": "1234-****", "issuerCode": "00" }
            }
            """;
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockTossResponse, HttpStatus.OK);
        
        given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .willReturn(responseEntity);
            
        // 취소 API 승인 MOCK DATA
        String mockCancelResponse = "{ \"approvedAt\": \"2026-09-25T12:00:05+09:00\" }";
        ResponseEntity<String> cancelResponseEntity = new ResponseEntity<>(mockCancelResponse, HttpStatus.OK);
        given(restTemplate.postForEntity(contains("/cancel"), any(HttpEntity.class), eq(String.class)))
            .willReturn(cancelResponseEntity);

        // When & Then
        // 금액이 다르므로 PAYMENT_AMOUNT_MISMATCH 
        PaymentProcessException exception = assertThrows(PaymentProcessException.class, () -> {
            paymentService.executeAutomaticPayment(userId, requestedAmount, 1, 150, 4850, 100, "hostId");
        });

        assertThat(exception.getErrorCode()).isEqualTo("PAYMENT_AMOUNT_MISMATCH");
    }

    @Test
    @DisplayName("자동결제 정상 승인: 금액이 일치하고 토스 승인이 완료되면 DB 저장 로직(savePaymentAll)이 호출된다.")
    void executeAutomaticPayment_Success() throws Exception {
        // Given
        String userId = "testUser";
        int requestedAmount = 5000;
        
        MemberCardVO mockCard = new MemberCardVO();
        mockCard.setStatus("YES");
        mockCard.setBilling_key("test_billing_key_123");
        given(memberRepository.selectCardById(userId)).willReturn(List.of(mockCard));

        // 정상 상황: 요청 금액(5000원)과 똑같이 결제 성공 응답이 옴
        String mockTossResponse = """
            {
                "paymentKey": "test_payment_key_999",
                "status": "DONE",
                "orderId": "SPENDOLIVE_ABC",
                "totalAmount": 5000, 
                "approvedAt": "2026-09-25T12:00:00+09:00",
                "card": { "number": "1234-****", "issuerCode": "00" }
            }
            """;
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockTossResponse, HttpStatus.OK);
        
        given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .willReturn(responseEntity);

        // When
        // 에러 없이 정상적으로 결제 메서드가 끝까지 실행되어야 함
        paymentService.executeAutomaticPayment(userId, requestedAmount, 1, 150, 4850, 100, "hostId");

        // Then
        // 토스 승인 후 결제 장부를 DB에 기록하는 savePaymentAll() 메서드가 정확히 1번 호출되었는지 검증
        verify(paymentStoreService, times(1)).savePaymentAll(
            any(SettlementPaymentVO.class), 
            any(EscrowPayoutVO.class), 
            any(PlatformRevenueVO.class), 
            eq(1), 
            eq(userId)
        );
    }
}
