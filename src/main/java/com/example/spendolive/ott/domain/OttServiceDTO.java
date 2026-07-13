package com.example.spendolive.ott.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OttServiceDTO {
    private Long ottServiceId;
    private String serviceName;

    // defaultPrice는 기존 컬럼명 유지: 피클플러스 방식으로 계산된 최종 분담 대상 금액
    // 예) 넷플릭스 프리미엄 17,000 + 추가회원 5,000 * 2 = 27,000
    private Integer defaultPrice;

    private String shareYn;
    private String riskLevel;
    private String blockReason;

    // OTT별 최고 멤버십 고정 정보
    private String fixedPlanName;
    private Integer basePrice;
    private Integer extraMemberFee;
    private Integer extraMemberCount;
    private Integer maxMemberLimit;
    private Double platformFeeRate;

    // 화면 표시용 계산값
    private Integer shareAmount;
    private Integer feeAmount;
    private Integer perPersonAmount;
}
