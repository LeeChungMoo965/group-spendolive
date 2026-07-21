package com.example.spendolive.notification.domain;

/**
 * 알림 종류 코드 모음.
 * notification_tb.notification_type 컬럼에 그대로 들어간다.
 * (DB CHECK 제약: 04_notice_notification_inquiry_faq.sql / 08_notification_type_expand.sql 참고)
 *
 * 새 알림을 보낼 땐 이 클래스의 상수 + NotificationService.createNotification(...)
 * 한 줄이면 된다. 예)
 *
 *   notificationService.createNotification(
 *       memberId,
 *       NotificationType.PAYMENT_FAIL,
 *       "결제에 실패했어요",
 *       "카드 결제가 실패했습니다. 카드 정보를 확인해 주세요.",
 *       "/payment/detail.do?id=" + paymentId
 *   );
 */
public final class NotificationType {

    private NotificationType() {}

    /** 공지사항 등록 (기존부터 쓰던 값, NoticeRepository.insertNoticeAlertForAll 참고) */
    public static final String NOTICE = "HOME";

    /** 채팅방에 새 메시지 도착 */
    public static final String CHAT = "CHAT";

    /** 정기결제(빌링) 실패 */
    public static final String PAYMENT_FAIL = "PAYMENT_FAIL";

    /** 결제 예정일 임박 (OTT 구독료 등) */
    public static final String PAYMENT_DUE = "PAYMENT_DUE";

    /** 회원가입 축하 */
    public static final String SIGNUP = "SIGNUP";

    /** 정산 요청 도착 */
    public static final String SETTLEMENT_REQUEST = "SETTLEMENT_REQUEST";

    /** 정산 완료 */
    public static final String SETTLEMENT_DONE = "SETTLEMENT_DONE";

    /** OTT 공유방 모집 완료 (정원 참) */
    public static final String ROOM_FULL = "ROOM_FULL";

    /** OTT 공유방 나가기 / 강퇴 */
    public static final String ROOM_LEAVE_KICK = "ROOM_LEAVE_KICK";

    /** 등록 카드(빌링키) 만료 예정 */
    public static final String CARD_EXPIRING = "CARD_EXPIRING";

    /** 환불 / 정산 취소 완료 */
    public static final String REFUND_DONE = "REFUND_DONE";

    /** 내 문의에 답변 완료 */
    public static final String INQUIRY_REPLY = "INQUIRY_REPLY";

    /** (캘린더) 지출 결제일 임박 */
    public static final String EXPENSE_DUE = "EXPENSE_DUE";
}
