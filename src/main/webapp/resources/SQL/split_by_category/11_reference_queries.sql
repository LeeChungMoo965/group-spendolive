/* =========================================================
   11. 참고용 조회 SQL
   =========================================================
   실행 안내: 실행 필수 아님. 개발하면서 복사해서 쓰는 참고 SQL.
   ========================================================= */

SET DEFINE OFF;

/* =========================================================
   25. 자주 쓰는 SQL 예시
   =========================================================
   아래 SQL은 참고용입니다.
   전체 스키마 실행 시 바인드 변수(:memberId 등) 때문에 멈출 수 있으므로 주석 처리했습니다.

-- 알림 이모지 빨간 점 표시용: 안 읽은 알림 개수
SELECT COUNT(*) AS unread_count
FROM alert_tb
WHERE id = :memberId
  AND read_yn = 'N';

-- 배너에 띄울 안 읽은 알림 목록
SELECT alert_id, title, content, target_url, created_at
FROM alert_tb
WHERE id = :memberId
  AND read_yn = 'N'
  AND banner_yn = 'Y'
ORDER BY created_at DESC;

-- 알림 클릭 시 읽음 처리
UPDATE alert_tb
SET read_yn = 'Y',
    read_at = SYSDATE,
    banner_yn = 'N'
WHERE alert_id = :alertId
  AND id = :memberId;

-- 3% 수수료 계산 예시
SELECT
    4250 AS base_amount,
    ROUND(4250 * 0.03) AS fee_amount,
    4250 + ROUND(4250 * 0.03) AS total_amount
FROM dual;

-- 월별 플랫폼 수수료 합계
-- platform_revenue_tb는 최소 스키마에서 제거했고, 수수료는 결제 테이블 fee_amount 합계로 계산합니다.
SELECT
    st.settlement_month AS revenue_month,
    SUM(sp.fee_amount) AS total_fee_revenue
FROM settlement_payment_tb sp
JOIN settlement_tb st ON sp.settlement_id = st.settlement_id
WHERE sp.payment_status IN ('PAID', 'CONFIRMED')
GROUP BY st.settlement_month
ORDER BY revenue_month;

-- 날짜별 지출 합계: 캘린더 표시용
SELECT
    TO_CHAR(e.expense_date, 'YYYY-MM-DD') AS expense_date,
    SUM(e.amount) AS total_amount,
    COUNT(*) AS expense_count
FROM expense_tb e
WHERE e.member_id = :memberId
  AND TO_CHAR(e.expense_date, 'YYYY-MM') = :month
GROUP BY TO_CHAR(e.expense_date, 'YYYY-MM-DD')
ORDER BY expense_date;

-- 특정 날짜 클릭 시 상세 내역
SELECT
    e.expense_id,
    e.expense_title,
    e.amount,
    TO_CHAR(e.expense_date, 'YYYY-MM-DD') AS expense_date,
    e.payment_method,
    e.memo,
    e.fixed_yn,
    c.category_name,
    c.expense_type
FROM expense_tb e
JOIN expense_category_tb c ON e.category_id = c.category_id
WHERE e.member_id = :memberId
  AND TRUNC(e.expense_date) = TO_DATE(:expenseDate, 'YYYY-MM-DD')
ORDER BY e.expense_id DESC;

-- 결제일이 15일인 방의 2026년 7월 이용분 예시
-- payment_start_date = 2026-06-15
-- payment_close_date = 2026-07-10
-- service_start_date = 2026-07-15
-- service_end_date   = 2026-08-14
========================================================= */
