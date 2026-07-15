/* =========================================================
   00. SpendOlive 전체 객체 초기화
   ---------------------------------------------------------
   주의: 아래 목록의 프로젝트 테이블과 데이터가 모두 삭제됩니다.
   기존 데이터를 보존해야 하면 이 파일을 실행하지 마세요.
   ========================================================= */

SET DEFINE OFF;

BEGIN
    FOR t IN (
        SELECT table_name
        FROM user_tables
        WHERE table_name IN (
            'WARNING_TB',
            'REPORT_TB',
            'INQUIRY_FILE_TB',
            'INQUIRY_TB',
            'NOTICE_READ_TB',
            'NOTICE_FAVORITE_TB',
            'NOTIFICATION_TB',
            'ALERT_TB',
            'FAQ_TB',
            'NOTICE_TB',
            'PLATFORM_REVENUE_TB',
            'ESCROW_PAYOUT_TB',
            'SELLER_ACCOUNT_TB',
            'SETTLEMENT_REFUND_TB',
            'SETTLEMENT_PAYMENT_TB',
            'SETTLEMENT_TB',
            'OTT_CHAT_READ_TB',
            'OTT_CHAT_MESSAGE_TB',
            'OTT_ROOM_MEMBER_TB',
            'OTT_ROOM_TB',
            'OTT_SERVICE_TB',
            'EXPENSE_TB',
            'EXPENSE_CATEGORY_TB',
            'MEMBER_CARD_TB',
            'MEMBER_ACCOUNT_TB',
            'MEMBER_TB'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
    END LOOP;

    FOR s IN (
        SELECT sequence_name
        FROM user_sequences
        WHERE sequence_name IN (
            'SEQ_MEMBER',
            'SEQ_EXPENSE_CATEGORY',
            'SEQ_EXPENSE',
            'SEQ_OTT_SERVICE',
            'SEQ_OTT_ROOM',
            'SEQ_OTT_ROOM_MEMBER',
            'SEQ_OTT_CHAT_MESSAGE',
            'SEQ_SETTLEMENT',
            'SEQ_SETTLEMENT_PAYMENT',
            'SEQ_SETTLEMENT_REFUND',
            'SEQ_ESCROW_PAYOUT',
            'SEQ_NOTICE',
            'INQUIRY_SEQ',
            'INQUIRY_FILE_SEQ',
            'SEQ_FAQ',
            'SEQ_NOTIFICATION',
            'SEQ_ALERT',
            'SEQ_REPORT'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
    END LOOP;
END;
/

PROMPT SpendOlive 프로젝트 테이블과 시퀀스 초기화 완료
