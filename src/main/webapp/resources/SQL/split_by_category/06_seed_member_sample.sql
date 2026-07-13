/* =========================================================
   06. 테스트 회원 기본 데이터
   =========================================================
   실행 안내: 01_member_schema.sql 실행 후 실행. 로그인 테스트용 계정 생성.
   ========================================================= */

SET DEFINE OFF;

/* =========================================================
   기본 데이터 분리 기준
   =========================================================
   [팀 원본 사용 + 오류 수정] 기본 데이터에서 기능별로 분리한 파일.
   - 팀 원본의 INSERT 오류 수정 사항을 유지함.
   ========================================================= */

/* 테스트 회원 */
INSERT INTO member_tb(id, email, password, member_name, nickname, login_type, verify_type, role)
VALUES ('admin', 'admin@spendolive.com', '1234', '관리자', 'admin', 'LOCAL', 'PHONE', 'ADMIN');

INSERT INTO member_tb(id, email, password, member_name, nickname, login_type, verify_type, role)
VALUES ('host', 'host@spendolive.com', '1234', '파티장유저', 'partyhost', 'LOCAL', 'PHONE', 'HOST');

INSERT INTO member_tb(id, email, password, member_name, nickname, login_type, verify_type, role)
VALUES ('user', 'user@spendolive.com', '1234', '일반유저', 'olive', 'LOCAL', 'PHONE', 'USER');

INSERT INTO member_tb(id, email, password, member_name, nickname, login_type, verify_type, role)
VALUES ('naveruser', 'naveruser@spendolive.com', 'SOCIAL_LOGIN', '네이버유저', 'naverolive', 'NAVER', 'EMAIL', 'USER');

COMMIT;
