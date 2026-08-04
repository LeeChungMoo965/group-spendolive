package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

// OTT 채팅방 요약 DTO
@Getter
@Setter
public class OttChatRoomDTO {
    // 공유방 PK
    private Long room_id;
    // 방 제목
    private String room_name;
    // Netflix, TVING 등 OTT 서비스명
    private String service_name;
    // 마지막 읽은 시각 이후의 미읽음 메시지 수
    private Integer unread_count;
    // 채팅방 목록에 표시할 최근 메시지
    private String last_message;
}
