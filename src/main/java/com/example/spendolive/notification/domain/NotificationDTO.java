package com.example.spendolive.notification.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDTO {
    private int notification_id;
    private String id;
    private String notification_type;
    private String title;
    private String message;

    private String link_url;
    private String read_yn;
    private String star_yn;

    private String created_at;
}