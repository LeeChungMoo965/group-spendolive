/* =========================================================
   00. SpendOlive 전체 객체 삭제 SQL
   =========================================================
   실행 목적:
   - 개발/테스트 중 꼬인 DB 객체를 한 번에 정리하고 처음부터 다시 만들기 위한 파일입니다.
   - 이 파일을 실행하면 SpendOlive 프로젝트에서 사용하는 테이블, 시퀀스, 트리거가 삭제됩니다.
   - 삭제 후에는 아래 순서대로 다시 실행하면 됩니다.

   권장 실행 순서:
   1) 00_reset_all_objects.sql
   2) 01_member_schema.sql
   3) 02_expense_calendar_schema.sql
   4) 03_ott_schema.sql
   5) 04_notice_inquiry_alert_schema.sql
   6) 05_admin_report_warning_schema.sql
   7) 필요한 seed 파일 실행

   주의사항:
   - 현재 접속한 Oracle 계정 안의 SpendOlive 관련 객체만 삭제하도록 이름을 명시했습니다.
   - member_tb도 삭제되므로 회원 데이터까지 모두 사라집니다.
   - 회원 데이터를 보존해야 하는 상황에서는 이 파일을 실행하지 마세요.
   - DROP TABLE은 CASCADE CONSTRAINTS PURGE로 처리해서 외래키 관계와 휴지통 객체를 함께 정리합니다.
   ========================================================= */

SET DEFINE OFF;
SET SERVEROUTPUT ON;

/* =========================================================
   1. 테이블 삭제
   =========================================================
   - 자식 테이블부터 먼저 삭제합니다.
   - CASCADE CONSTRAINTS를 사용하므로 외래키 제약조건 때문에 삭제가 막히지 않습니다.
   - PURGE를 사용하므로 Oracle 휴지통에 남기지 않습니다.
   - 과거에 사용했다가 현재는 제거한 테이블도 같이 넣었습니다.
     예: ott_room_block_tb, escrow_tb, platform_revenue_tb, payout_tb
   ========================================================= */
DECLARE
    PROCEDURE drop_table_if_exists(p_table_name IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE ' || p_table_name || ' CASCADE CONSTRAINTS PURGE';
        DBMS_OUTPUT.PUT_LINE('[DROP TABLE] ' || p_table_name);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -942 THEN
                DBMS_OUTPUT.PUT_LINE('[SKIP TABLE] ' || p_table_name || ' 없음');
            ELSE
                DBMS_OUTPUT.PUT_LINE('[ERROR TABLE] ' || p_table_name || ' / ' || SQLERRM);
            END IF;
    END;
BEGIN
    /* OTT 정산/환불/결제 관련 */
    drop_table_if_exists('settlement_refund_tb');
    drop_table_if_exists('settlement_payment_tb');
    drop_table_if_exists('settlement_tb');

    /* OTT 채팅/읽음/참여자/방 관련 */
    drop_table_if_exists('ott_chat_read_tb');
    drop_table_if_exists('ott_chat_message_tb');
    drop_table_if_exists('ott_room_member_tb');
    drop_table_if_exists('ott_room_block_tb');       -- 과거 버전 제거 대상
    drop_table_if_exists('ott_room_tb');
    drop_table_if_exists('ott_service_tb');

    /* 과거 결제 고도화 테이블: 현재 구조에서는 사용하지 않지만 기존 DB 정리를 위해 포함 */
    drop_table_if_exists('escrow_tb');               -- 과거 버전 제거 대상
    drop_table_if_exists('platform_revenue_tb');     -- 과거 버전 제거 대상
    drop_table_if_exists('payout_tb');               -- 과거 버전 제거 대상

    /* 공지/문의/알림/신고/제재 관련 */
    drop_table_if_exists('notice_bookmark_tb');
    drop_table_if_exists('warning_tb');
    drop_table_if_exists('report_tb');
    drop_table_if_exists('alert_tb');
    drop_table_if_exists('inquiry_answer_tb');
    drop_table_if_exists('inquiry_tb');
    drop_table_if_exists('faq_tb');
    drop_table_if_exists('notice_tb');

    /* 지출/캘린더 관련 */
    drop_table_if_exists('expense_tb');
    drop_table_if_exists('expense_category_tb');

    /* 회원 테이블은 다른 테이블들이 모두 삭제된 후 마지막에 삭제 */
    drop_table_if_exists('member_tb');
END;
/

/* =========================================================
   2. 시퀀스 삭제
   =========================================================
   - 테이블을 삭제해도 시퀀스는 자동 삭제되지 않기 때문에 따로 삭제합니다.
   - 과거 버전에서 사용하던 시퀀스도 같이 정리합니다.
   ========================================================= */
DECLARE
    PROCEDURE drop_sequence_if_exists(p_sequence_name IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || p_sequence_name;
        DBMS_OUTPUT.PUT_LINE('[DROP SEQUENCE] ' || p_sequence_name);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -2289 THEN
                DBMS_OUTPUT.PUT_LINE('[SKIP SEQUENCE] ' || p_sequence_name || ' 없음');
            ELSE
                DBMS_OUTPUT.PUT_LINE('[ERROR SEQUENCE] ' || p_sequence_name || ' / ' || SQLERRM);
            END IF;
    END;
BEGIN
    drop_sequence_if_exists('seq_member');
    drop_sequence_if_exists('seq_expense_category');
    drop_sequence_if_exists('seq_expense');

    drop_sequence_if_exists('seq_ott_service');
    drop_sequence_if_exists('seq_ott_room');
    drop_sequence_if_exists('seq_ott_room_member');
    drop_sequence_if_exists('seq_ott_chat_message');
    drop_sequence_if_exists('seq_ott_room_block');       -- 과거 버전 제거 대상
    drop_sequence_if_exists('seq_settlement');
    drop_sequence_if_exists('seq_settlement_payment');
    drop_sequence_if_exists('seq_settlement_refund');

    drop_sequence_if_exists('seq_escrow');               -- 과거 버전 제거 대상
    drop_sequence_if_exists('seq_platform_revenue');     -- 과거 버전 제거 대상
    drop_sequence_if_exists('seq_payout');               -- 과거 버전 제거 대상

    drop_sequence_if_exists('seq_notice');
    drop_sequence_if_exists('seq_faq');
    drop_sequence_if_exists('seq_inquiry');
    drop_sequence_if_exists('seq_inquiry_answer');
    drop_sequence_if_exists('seq_alert');
    drop_sequence_if_exists('seq_notice_bookmark');
    drop_sequence_if_exists('seq_report');
    drop_sequence_if_exists('seq_warning');
END;
/

/* =========================================================
   3. 트리거 삭제
   =========================================================
   - 테이블 삭제 시 해당 테이블의 트리거는 보통 함께 삭제됩니다.
   - 그래도 기존 DB에 남아 있을 수 있는 트리거를 한 번 더 정리합니다.
   ========================================================= */
DECLARE
    PROCEDURE drop_trigger_if_exists(p_trigger_name IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE 'DROP TRIGGER ' || p_trigger_name;
        DBMS_OUTPUT.PUT_LINE('[DROP TRIGGER] ' || p_trigger_name);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -4080 THEN
                DBMS_OUTPUT.PUT_LINE('[SKIP TRIGGER] ' || p_trigger_name || ' 없음');
            ELSE
                DBMS_OUTPUT.PUT_LINE('[ERROR TRIGGER] ' || p_trigger_name || ' / ' || SQLERRM);
            END IF;
    END;
BEGIN
    drop_trigger_if_exists('trg_member_bi');
    drop_trigger_if_exists('trg_expense_category_bi');
    drop_trigger_if_exists('trg_expense_bi');

    drop_trigger_if_exists('trg_ott_service_bi');
    drop_trigger_if_exists('trg_ott_room_bi');
    drop_trigger_if_exists('trg_ott_room_member_bi');
    drop_trigger_if_exists('trg_ott_chat_message_bi');
    drop_trigger_if_exists('trg_ott_room_block_bi');       -- 과거 버전 제거 대상
    drop_trigger_if_exists('trg_settlement_bi');
    drop_trigger_if_exists('trg_settlement_payment_bi');
    drop_trigger_if_exists('trg_settlement_refund_bi');

    drop_trigger_if_exists('trg_escrow_bi');               -- 과거 버전 제거 대상
    drop_trigger_if_exists('trg_platform_revenue_bi');     -- 과거 버전 제거 대상
    drop_trigger_if_exists('trg_payout_bi');               -- 과거 버전 제거 대상

    drop_trigger_if_exists('trg_notice_bi');
    drop_trigger_if_exists('trg_faq_bi');
    drop_trigger_if_exists('trg_inquiry_bi');
    drop_trigger_if_exists('trg_inquiry_answer_bi');
    drop_trigger_if_exists('trg_alert_bi');
    drop_trigger_if_exists('trg_notice_bookmark_bi');
    drop_trigger_if_exists('trg_report_bi');
    drop_trigger_if_exists('trg_warning_bi');
END;
/

/* =========================================================
   4. 휴지통 정리
   =========================================================
   - PURGE 옵션으로 대부분 정리되지만, 혹시 남아 있는 객체가 있으면 비웁니다.
   ========================================================= */
BEGIN
    EXECUTE IMMEDIATE 'PURGE RECYCLEBIN';
    DBMS_OUTPUT.PUT_LINE('[PURGE RECYCLEBIN] 완료');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('[SKIP PURGE RECYCLEBIN] ' || SQLERRM);
END;
/

COMMIT;

/* =========================================================
   전체 삭제 완료
   ========================================================= */
