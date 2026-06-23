package com.example.spendolive.alert.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Data
public class AlertDTO {

    private int alertId;
    private String id;
    private String alertType;
    private String title;
    private String content;
    private String targetUrl;
    private String readYn;
    private String bannerYn;
    private String createdAt;
    private String readAt;

}