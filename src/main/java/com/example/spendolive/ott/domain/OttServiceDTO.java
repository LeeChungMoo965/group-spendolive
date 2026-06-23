package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OttServiceDTO {
    private Long ottServiceId;
    private String serviceName;
    private Integer defaultPrice;
    private String shareYn;
    private String blockReason;
}
