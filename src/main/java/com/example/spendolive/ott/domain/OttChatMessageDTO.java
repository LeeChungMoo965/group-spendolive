package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OttChatMessageDTO {
    private Long messageId;
    private Long roomId;
    private String senderId;
    private String senderName;
    private String messageContent;
    private String created_at;
    private String mineYn;
    private String systemYn;
}
