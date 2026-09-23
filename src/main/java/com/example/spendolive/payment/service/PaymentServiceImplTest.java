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

        PaymentAmountDTO result = paymentService.getPaymentAmount(roomId);

        assertThat(result.getBaseAmount()).isEqualTo(4250);
        assertThat(result.getFeeAmount()).isEqualTo(128);
        assertThat(result.getTotalAmount()).isEqualTo(4378);
        assertThat(result.getAutomaticPaymentDay()).isEqualTo(25);
    }
}
