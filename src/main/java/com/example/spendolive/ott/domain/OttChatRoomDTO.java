package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OttChatRoomDTO {
    private Long roomId;
    private String roomName;
    private String serviceName;
    private Integer unreadCount;
    private String lastMessage;
}
