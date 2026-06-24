/* =========================================================
   07. 지출 카테고리/지출 샘플 데이터
   =========================================================
   실행 안내: 01, 02, 06번 실행 후 실행. 지출 카테고리와 테스트 지출 데이터를 넣음.
   ========================================================= */

SET DEFINE OFF;

/* =========================================================
   기본 데이터 분리 기준
   =========================================================
   [팀 원본 사용 + 오류 수정] 기본 데이터에서 기능별로 분리한 파일.
   - 팀 원본의 INSERT 오류 수정 사항을 유지함.
   ========================================================= */

/* 지출 카테고리 */
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('월세', 'FIXED', 1);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('통신비', 'FIXED', 2);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('보험료', 'FIXED', 3);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('관리비', 'FIXED', 4);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('공과금', 'FIXED', 5);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('식비', 'VARIABLE', 1);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('교통비', 'VARIABLE', 2);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('생활비', 'VARIABLE', 3);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('쇼핑', 'VARIABLE', 4);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('문화, 취미생활', 'VARIABLE', 5);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('기타', 'VARIABLE', 99);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('Netflix', 'OTT', 1);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('Disney+', 'OTT', 2);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('TVING', 'OTT', 3);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('Wavve', 'OTT', 4);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('Watcha', 'OTT', 5);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('Laftel', 'OTT', 6);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('Coupang Play', 'OTT', 7);

/* 테스트 지출: member_id는 seq_member 기준으로 user가 3번이라고 가정 */
INSERT INTO expense_tb(member_id, category_id, expense_title, amount, expense_date, payment_method, memo, fixed_yn)
VALUES (3, 4, '관리비', 120000, TRUNC(SYSDATE, 'MM') + 4, 'CARD', '이번 달 관리비', 'Y');

INSERT INTO expense_tb(member_id, category_id, expense_title, amount, expense_date, payment_method, memo, fixed_yn)
VALUES (3, 6, '점심', 9500, TRUNC(SYSDATE), 'CARD', '학원 근처 점심', 'N');

INSERT INTO expense_tb(member_id, category_id, expense_title, amount, expense_date, payment_method, memo, fixed_yn)
VALUES (3, 12, '넷플릭스', 4250, TRUNC(SYSDATE, 'MM') + 14, 'TRANSFER', 'OTT 정산금', 'Y');

COMMIT;
