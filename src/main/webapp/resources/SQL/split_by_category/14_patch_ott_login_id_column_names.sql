/*
  14_patch_ott_login_id_column_names.sql
  실행 안내:
  - 기존 DB를 초기화하지 않고, 예전 OTT 테이블을 계속 사용할 때만 실행하세요.
  - 새로 03_ott_schema.sql을 실행해서 DB를 만드는 경우에는 이 패치가 필요 없습니다.

  수정 목적:
  - OTT 테이블의 member_id는 실제로 member_tb.member_id(숫자 PK)가 아니라 member_tb.id(로그인 ID)를 참조하고 있었습니다.
  - 팀원들이 헷갈리지 않도록 아래 컬럼명을 로그인 ID 기준으로 변경합니다.

  변경 내용:
  - ott_room_tb.host_member_id              -> host_login_id
  - ott_room_member_tb.member_id            -> member_login_id
  - ott_chat_read_tb.member_id              -> member_login_id
  - settlement_payment_tb.id                -> member_login_id
  - settlement_refund_tb.id                 -> member_login_id
*/

SET DEFINE OFF;

DECLARE
    v_old NUMBER;
    v_new NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_old FROM user_tab_cols WHERE table_name = 'OTT_ROOM_TB' AND column_name = 'HOST_MEMBER_ID';
    SELECT COUNT(*) INTO v_new FROM user_tab_cols WHERE table_name = 'OTT_ROOM_TB' AND column_name = 'HOST_LOGIN_ID';
    IF v_old = 1 AND v_new = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ott_room_tb RENAME COLUMN host_member_id TO host_login_id';
    END IF;
END;
/

DECLARE
    v_old NUMBER;
    v_new NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_old FROM user_tab_cols WHERE table_name = 'OTT_ROOM_MEMBER_TB' AND column_name = 'MEMBER_ID';
    SELECT COUNT(*) INTO v_new FROM user_tab_cols WHERE table_name = 'OTT_ROOM_MEMBER_TB' AND column_name = 'MEMBER_LOGIN_ID';
    IF v_old = 1 AND v_new = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ott_room_member_tb RENAME COLUMN member_id TO member_login_id';
    END IF;
END;
/

DECLARE
    v_old NUMBER;
    v_new NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_old FROM user_tab_cols WHERE table_name = 'OTT_CHAT_READ_TB' AND column_name = 'MEMBER_ID';
    SELECT COUNT(*) INTO v_new FROM user_tab_cols WHERE table_name = 'OTT_CHAT_READ_TB' AND column_name = 'MEMBER_LOGIN_ID';
    IF v_old = 1 AND v_new = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ott_chat_read_tb RENAME COLUMN member_id TO member_login_id';
    END IF;
END;
/

DECLARE
    v_old NUMBER;
    v_new NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_old FROM user_tab_cols WHERE table_name = 'SETTLEMENT_PAYMENT_TB' AND column_name = 'ID';
    SELECT COUNT(*) INTO v_new FROM user_tab_cols WHERE table_name = 'SETTLEMENT_PAYMENT_TB' AND column_name = 'MEMBER_LOGIN_ID';
    IF v_old = 1 AND v_new = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE settlement_payment_tb RENAME COLUMN id TO member_login_id';
    END IF;
END;
/

DECLARE
    v_old NUMBER;
    v_new NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_old FROM user_tab_cols WHERE table_name = 'SETTLEMENT_REFUND_TB' AND column_name = 'ID';
    SELECT COUNT(*) INTO v_new FROM user_tab_cols WHERE table_name = 'SETTLEMENT_REFUND_TB' AND column_name = 'MEMBER_LOGIN_ID';
    IF v_old = 1 AND v_new = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE settlement_refund_tb RENAME COLUMN id TO member_login_id';
    END IF;
END;
/

COMMIT;
