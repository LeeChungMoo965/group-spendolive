package com.example.spendolive.notification.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDTO {
    private int notificationId;
    private String id;
    private String notificationType;
    private String title;
    private String message;
    private String linkUrl;
    private String readYn;
    private String starYn;
    private String created_at;
}