package com.example.spendolive.payment.domain;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                // Getter, Setter, toString, hashCode, equals 자동 생성
@NoArgsConstructor   // 기본 생성자 생성
@AllArgsConstructor  // 모든 필드를 포함한 생성자 생성
@Builder             // 빌더 패턴 지원 (토스 API나 서비스단에서 객체 생성할 때 개편함)
public class SellerAccountVO {

    private Long sellerIdx;         // SELLER_IDX (NUMBER -> 자바에서는 Long이 정석!)
    private String member_id;        // member_id (VARCHAR2)
    private String bankName;        // BANK_NAME (VARCHAR2)
    private String accountNumber;   // ACCOUNT_NUMBER (VARCHAR2)
    private String traceId;         // traceId (VARCHAR2) - 금결원 거래고유번호나 토스 서브몰 ID 킵하는 용도
    private Date regDate;           // REG_DATE (DATE -> java.util.Date)

}