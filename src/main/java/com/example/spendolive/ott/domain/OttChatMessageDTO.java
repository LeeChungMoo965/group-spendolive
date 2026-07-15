package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

// OTT 채팅 메시지 DTO - 일반 메시지와 시스템 메시지 정보 전달
@Getter
@Setter
public class OttChatMessageDTO {

    // 메시지 PK
    private Long message_id;
    // 메시지가 속한 공유방 PK
    private Long room_id;
    // 실제 발신자 로그인 ID
    private String sender_id;
    // 화면에 표시할 닉네임 또는 이름
    private String sender_name;
    // 사용자가 입력했거나 시스템이 생성한 메시지 본문
    private String message_content;
    // 화면 표시용 작성 시각 문자열
    private String created_at;
    // 현재 로그인 사용자가 보낸 메시지인지 여부(Y/N)
    private String mine_yn;
    // 입장·퇴장·정산 등의 시스템 메시지 여부(Y/N)
    private String system_yn;

}
