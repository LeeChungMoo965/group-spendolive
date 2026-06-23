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
    private Integer totalPrice;
    private Integer billingDay;
    private Integer memberLimit;
    private Integer currentMemberCount;
    private String status;
    private String inviteCode;
    private String createdAt;
    private Integer perPersonAmount;
    private String myApplicationStatus;

    // 화면에서만 사용하는 값. DB 컬럼은 없기 때문에 roomName에 합쳐서 저장한다.
    private String planName;
    private String roomMode;
}
