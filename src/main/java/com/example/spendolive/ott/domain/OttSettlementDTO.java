package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OttSettlementDTO {
    private Long settlementId;
    private Long roomId;
    private String roomName;
    private String serviceName;
    private String settlementMonth;
    private Integer totalPrice;
    private Integer totalFee;
    private Integer totalPayAmount;
    private String dueDate;
    private String paymentStartDate;
    private String paymentCloseDate;
    private String serviceStartDate;
    private String serviceEndDate;
    private String replaceStartDate;
    private String replaceEndDate;
    private String status;
    private String createdAt;
    private String myRole;
    private int member_limit;
    private Long paymentId;
    private String myPaymentStatus;
    private Integer myTotalAmount;

    // 방장이 보는 팀원별 정산 상태 표시용
    private String memberId;
    private String memberName;
    private String memberNickname;
    private Integer baseAmount;
    private Integer feeAmount;
    private Integer totalAmount;
    private String paymentStatus;
    private String paidAt;
    //방장 아이디
    private String host_id;
}
