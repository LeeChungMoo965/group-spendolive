/* =========================================================
   08. OTT 서비스 기본 데이터
   =========================================================
   실행 안내: 01번과 03번 실행 후 실행. OTT 화면에서 서비스 목록을 보여주기 위한 기본 데이터.

   [피클플러스 방식 반영]
   - 사용자가 구독종류/구독료/최대인원을 직접 고르지 않음.
   - OTT별 최고 멤버십으로 고정.
   - 추가 계정 비용이 있는 OTT는 기본 구독료 + 추가 계정 비용까지 더한 금액을 N분의 1 계산.
   - 서비스 수수료는 3%로 고정.
   ========================================================= */

SET DEFINE OFF;

/* OTT 서비스
   컬럼 순서:
   service_name, default_price, fixed_plan_name, base_price,
   extra_member_fee, extra_member_count, max_member_limit,
   platform_fee_rate, share_yn, risk_level, block_reason
*/
INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    'Netflix', 27000, '프리미엄', 17000,
    5000, 2, 4,
    3, 'Y', 'LOW', NULL
);

INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    'TVING', 22000, '프리미엄', 17000,
    5000, 1, 4,
    3, 'Y', 'LOW', NULL
);

INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    'Wavve', 13900, '프리미엄', 13900,
    0, 0, 4,
    3, 'Y', 'LOW', NULL
);

INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    'Watcha', 12900, '프리미엄', 12900,
    0, 0, 4,
    3, 'Y', 'LOW', NULL
);

INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    'Disney+', 17900, '프리미엄', 13900,
    4000, 1, 4,
    3, 'Y', 'LOW', NULL
);

INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    'Laftel', 14900, '프리미엄', 14900,
    0, 0, 4,
    3, 'Y', 'LOW', NULL
);

INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    '쿠팡플레이', 7890, '단일 멤버십', 7890,
    0, 0, 1,
    3, 'N', 'HIGH', '쿠팡 계정과 연결되어 결제/배송/주문정보 노출 위험'
);

INSERT INTO ott_service_tb(
    service_name, default_price, fixed_plan_name, base_price,
    extra_member_fee, extra_member_count, max_member_limit,
    platform_fee_rate, share_yn, risk_level, block_reason
) VALUES (
    '애플TV+', 6500, '단일 멤버십', 6500,
    0, 0, 1,
    3, 'N', 'HIGH', 'Apple ID 직접 공유 위험, 가족 공유 방식 권장'
);

COMMIT;
