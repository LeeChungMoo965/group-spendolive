/* =========================================================
   12. 기존 DB에 OTT 피클플러스 방식 규칙만 추가하는 패치 SQL
   =========================================================
   실행 안내:
   - 이미 01_member_schema.sql, 03_ott_schema.sql, 08_seed_ott_services.sql을 실행한 DB에서
     테이블을 지우지 않고 컬럼/기본 데이터를 맞추고 싶을 때 사용.
   - 새로 DB를 만드는 경우에는 이 파일을 실행하지 말고 03번/08번 최신 파일을 실행하면 됨.

   반영 내용:
   - OTT별 최고 멤버십 고정
   - 추가 계정 비용 포함 금액 N분의 1
   - 서비스 수수료 3%
   ========================================================= */

SET DEFINE OFF;

ALTER TABLE ott_service_tb ADD (
    fixed_plan_name    VARCHAR2(50) DEFAULT '프리미엄' NOT NULL,
    base_price         NUMBER DEFAULT 0 NOT NULL,
    extra_member_fee   NUMBER DEFAULT 0 NOT NULL,
    extra_member_count NUMBER DEFAULT 0 NOT NULL,
    max_member_limit   NUMBER DEFAULT 4 NOT NULL,
    platform_fee_rate  NUMBER(5,2) DEFAULT 3 NOT NULL
);

UPDATE ott_service_tb
SET default_price = 27000,
    fixed_plan_name = '프리미엄',
    base_price = 17000,
    extra_member_fee = 5000,
    extra_member_count = 2,
    max_member_limit = 4,
    platform_fee_rate = 3,
    share_yn = 'Y'
WHERE service_name = 'Netflix';

UPDATE ott_service_tb
SET default_price = 22000,
    fixed_plan_name = '프리미엄',
    base_price = 17000,
    extra_member_fee = 5000,
    extra_member_count = 1,
    max_member_limit = 4,
    platform_fee_rate = 3,
    share_yn = 'Y'
WHERE service_name = 'TVING';

UPDATE ott_service_tb
SET default_price = 13900,
    fixed_plan_name = '프리미엄',
    base_price = 13900,
    extra_member_fee = 0,
    extra_member_count = 0,
    max_member_limit = 4,
    platform_fee_rate = 3,
    share_yn = 'Y'
WHERE service_name = 'Wavve';

UPDATE ott_service_tb
SET default_price = 12900,
    fixed_plan_name = '프리미엄',
    base_price = 12900,
    extra_member_fee = 0,
    extra_member_count = 0,
    max_member_limit = 4,
    platform_fee_rate = 3,
    share_yn = 'Y'
WHERE service_name = 'Watcha';

UPDATE ott_service_tb
SET default_price = 17900,
    fixed_plan_name = '프리미엄',
    base_price = 13900,
    extra_member_fee = 4000,
    extra_member_count = 1,
    max_member_limit = 4,
    platform_fee_rate = 3,
    share_yn = 'Y'
WHERE service_name = 'Disney+';

UPDATE ott_service_tb
SET default_price = 14900,
    fixed_plan_name = '프리미엄',
    base_price = 14900,
    extra_member_fee = 0,
    extra_member_count = 0,
    max_member_limit = 4,
    platform_fee_rate = 3,
    share_yn = 'Y'
WHERE service_name = 'Laftel';

UPDATE ott_room_tb r
SET (plan_name, total_price, member_limit) = (
    SELECT s.fixed_plan_name, s.default_price, s.max_member_limit
    FROM ott_service_tb s
    WHERE s.ott_service_id = r.ott_service_id
)
WHERE EXISTS (
    SELECT 1
    FROM ott_service_tb s
    WHERE s.ott_service_id = r.ott_service_id
      AND s.share_yn = 'Y'
);

COMMIT;
