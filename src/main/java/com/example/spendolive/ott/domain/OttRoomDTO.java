package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OttRoomDTO {
    private Long roomId;
    private String hostMemberId;
    private String hostNickname;
    private Long ottServiceId;
    private String serviceName;
    private String roomName;
    private String planName;
    private Integer totalPrice;
    private Integer billingDay;
    private Integer memberLimit;
    private Integer currentMemberCount;
    private String status;
    private String inviteCode;
    private String closeRequestedAt;
    private String closeEffectiveDate;
    private String closeReason;
    private String closeNotice;
    private String closedAt;
    private String createdAt;
    private Integer shareAmount;
    private Integer feeAmount;
    private Integer perPersonAmount;
    private Double platformFeeRate;
    private Integer basePrice;
    private Integer extraMemberFee;
    private Integer extraMemberCount;
    private String myApplicationStatus;
    private String leaveReservedYn;
    private String leaveRequestedAt;
    private String leaveScheduledDate;
    private String leaveCancelledAt;
    private String leaveReason;

    // 화면에서만 사용하는 값
    private String roomMode;
}
