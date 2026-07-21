package com.example.spendolive.notification.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDTO {
    private int notificationId;           // 알림 고유 번호(PK)
    private String id;                    // 수신자 회원 ID(FK)
    private String notificationType;      // 알림 종류 구분값
    private String title;                 // 알림 제목
    private String message;               // 알림 본문 메시지 
    private String linkUrl;               // 클릭 시 이동할 경로
    private String readYn;                // 읽음 Y/N (자체 컬럼)
    private String starYn;                // 찜 Y/N   (자체 컬럼)
    private String createdAt;             // 알림 생성일
}