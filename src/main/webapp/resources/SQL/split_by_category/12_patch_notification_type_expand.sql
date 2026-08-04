/* =========================================================
   12. 알림 타입 확장 패치
   =========================================================
   [추가 안내 - 알림 타입 확장 패치]
   - 기존 DB를 삭제하지 않고, notification_tb.notification_type의 CHECK 제약만
     넓혀서 새 알림 타입(채팅/결제/정산/문의답변/캘린더 등)을 쓸 수 있게 한다.
   - 04_notice_notification_inquiry_faq.sql을 처음부터 다시 실행(=DROP 후 재생성)
     하는 경우에는 이미 확장된 CHECK 제약이 반영되어 있으므로 12번은 실행하지 않아도 됨.
   - 기존 알림 데이터를 보존한 채로 타입만 늘리고 싶을 때만 이 패치를 실행.
   ========================================================= */

SET DEFINE OFF;

ALTER TABLE notification_tb DROP CONSTRAINT chk_notification_type;

ALTER TABLE notification_tb ADD CONSTRAINT chk_notification_type CHECK (notification_type IN (
    'HOME', 'PERSONAL', 'OTT',
    'CHAT', 'PAYMENT_FAIL', 'PAYMENT_DUE', 'SIGNUP',
    'SETTLEMENT_REQUEST', 'SETTLEMENT_DONE',
    'ROOM_FULL', 'ROOM_LEAVE_KICK', 'CARD_EXPIRING',
    'REFUND_DONE', 'INQUIRY_REPLY', 'EXPENSE_DUE'
));
