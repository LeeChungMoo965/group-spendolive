package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OttRoomMemberDTO {
    private Long roomMemberId;
    private Long roomId;
    private String roomName;
    private String serviceName;
    private String memberId;
    private String memberNickname;
    private String memberName;
    private String memberRole;
    private Integer shareAmount;
    private Double feeRate;
    private Integer feeAmount;
    private Integer payAmount;
    private String joinedAt;
    private String status;
}
