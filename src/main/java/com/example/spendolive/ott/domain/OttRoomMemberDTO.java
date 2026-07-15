package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

// OTT 방 멤버 DTO - 참여 상태와 개인 정산 정보 전달
@Getter
@Setter
public class OttRoomMemberDTO {
    // 멤버 및 방 식별 정보
    private Long room_member_id;
    private Long room_id;
    private String room_name;
    private String service_name;
    private String member_login_id;
    private String member_nickname;
    private String member_name;
    private String member_role;

    // 개인별 정산 계산값
    private Integer share_amount;
    private Double fee_rate;
    private Integer fee_amount;
    private Integer pay_amount;

    // 참여 및 탈퇴 상태
    private String joined_at;
    private String status;
    private String leave_reserved_yn;
    private String leave_requested_at;
    private String leave_scheduled_date;
    private String leave_cancelled_at;
    private String leave_reason;
    private String settlement_status;
    private int pay_day;            //자동 결제일
    private int pay_late_day;       //자동 결제일 연체 일 수
}
