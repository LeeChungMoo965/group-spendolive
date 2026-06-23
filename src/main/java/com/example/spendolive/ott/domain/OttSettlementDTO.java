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
    private String status;
    private String createdAt;
    private String myRole;
    private String myPaymentStatus;
    private Integer myTotalAmount;
}
