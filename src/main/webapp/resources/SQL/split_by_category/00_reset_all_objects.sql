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
            'MONTHLY_BUDGET_TB',
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
            'SEQ_MONTHLY_BUDGET',
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


ALTER TABLE ott_room_member_tb ADD (
    pay_day         NUMBER ,
    pay_late_day    NUMBER DEFAULT 0,
    settlement_status  VARCHAR2(30) DEFAULT 'READY' , -- 방장 정산(송금) 후에 상태
    CONSTRAINT ck_ott_member_settlement_status CHECK (
        settlement_status IN (
            'READY',              -- 정산 안됨
            'DONE'               -- 정산 완료
        )
    )
);


INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('account', '오픈뱅킹 계좌 연동은 꼭 해야 하나요?',
 '오픈뱅킹 계좌 연동은 OTT 공유방 입장, 정산금 이체, 자동결제(빌링) 등 실제 돈이 오가는 기능을 이용하기 위한 안전장치예요. 연동하지 않아도 서비스 둘러보기는 가능하지만, 방 참여나 정산 기능은 계좌 연동 후에 이용할 수 있어요.', 1, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('account', '카카오 로그인과 이메일 로그인을 같이 쓸 수 있나요?',
 '하나의 계정은 하나의 로그인 방식(이메일 또는 카카오)으로 관리돼요. 로그인 방식을 변경하고 싶다면 마이페이지에서 계정 설정을 확인해 주세요.', 2, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('account', '카드 등록은 어떻게 하나요?',
 '마이페이지 > 카드 등록 메뉴에서 진행할 수 있어요. 등록 시 입력하신 카드 정보는 저희 서버가 아니라 Toss Payments 결제창에서 직접 처리되며, 저희는 결제에 필요한 일회성 인증키만 전달받아요.', 3, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('account', '비밀번호를 잊어버렸어요. 어떻게 재설정하나요?',
 '로그인 화면의 비밀번호 찾기 메뉴에서 가입 시 등록한 이메일로 재설정 링크를 받을 수 있어요. 카카오 로그인으로 가입하신 경우 별도 비밀번호가 없으니 카카오 계정 설정에서 확인해 주세요.', 4, 'Y');

-- 지출관리
INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('expense', '고정지출과 변동지출의 차이가 뭔가요?',
 '고정지출은 월세, 통신비처럼 매달 반복되는 지출이고, 변동지출은 식비, 쇼핑처럼 금액과 시기가 달라지는 지출이에요. 고정지출로 등록하면 매달 캘린더에 자동으로 반영돼요.', 1, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('expense', '캘린더에 지출 내역이 안 보여요.',
 '지출 등록 시 날짜가 정확히 입력되었는지 확인해 주세요. 고정지출은 등록한 날짜를 기준으로 매달 같은 날짜에 자동 반영되며, 반영까지 새로고침이 필요할 수 있어요.', 2, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('expense', '지출 카테고리를 직접 추가할 수 있나요?',
 '현재는 기본 제공되는 카테고리(월세, 식비, 교통비 등) 내에서 지출을 등록할 수 있어요. 원하는 카테고리가 없다면 문의하기를 통해 추가 요청을 남겨주세요.', 3, 'Y');

-- OTT관리
INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('ott', '가족 공유방과 외부 모집방은 뭐가 다른가요?',
 '가족 공유방(FRIEND)은 이미 아는 사람끼리 초대해서 만드는 방이고, 외부 모집방(RECRUIT)은 낯선 사람도 참여 신청을 통해 합류할 수 있는 방이에요. 두 모드 모두 정산 방식은 동일해요.', 1, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('ott', '방장이 나가면 방은 어떻게 되나요?',
 '방장이 탈퇴하거나 활동을 중단하면 다른 멤버에게 방장 권한을 넘기는 기능을 준비 중이에요. 현재는 방장 탈퇴 시 고객센터로 문의해 주시면 처리를 도와드려요.', 2, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('ott', '정산 금액은 어떻게 계산되나요?',
 '전체 구독료를 참여 인원수로 나눈 금액(N분의 1)에 플랫폼 이용 수수료 3%를 더해서 각 멤버에게 청구돼요. 정산 내역은 방 상세 화면에서 실시간으로 확인할 수 있어요.', 3, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('ott', '원하는 OTT 서비스가 목록에 없어요.',
 '현재 Netflix, Disney+, TVING, Wavve, Watcha, Laftel, Coupang Play를 지원하고 있어요. 추가를 원하는 서비스가 있다면 문의하기로 알려주세요.', 4, 'Y');

-- 공지·알림
INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('notice', '알림은 어디서 확인하나요?',
 '상단 종 모양 아이콘을 누르면 알림 센터로 이동해요. 결제, 정산, 공지사항 관련 알림을 한 곳에서 확인할 수 있고, 읽지 않은 알림은 빨간 배지로 표시돼요.', 1, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('notice', '공지사항 찜(즐겨찾기) 기능은 어떻게 쓰나요?',
 '공지사항 상세 화면에서 별 아이콘을 누르면 찜 목록에 저장돼요. 나중에 다시 찾아보고 싶은 공지를 모아둘 때 유용해요.', 2, 'Y');

-- 기타
INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('etc', '문의하기와 신고하기는 어떻게 다른가요?',
 '문의하기는 서비스 이용 중 궁금한 점이나 오류를 알리는 용도이고, 신고하기는 다른 회원의 부적절한 행동(정산 미이행, 사기 등)을 알리는 용도예요. 신고 접수 시 별도로 검토 후 조치돼요.', 1, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('etc', '서비스 이용료가 따로 있나요?',
 '가입 및 기본 이용은 무료이며, OTT 공유방 정산 시에만 정산 금액의 3%가 플랫폼 수수료로 부과돼요.', 2, 'Y');

INSERT INTO faq_tb (category, question, answer, sort_order, use_yn)
VALUES ('etc', '회원 탈퇴는 어떻게 하나요?',
 '마이페이지 하단의 회원 탈퇴 메뉴에서 진행할 수 있어요. 진행 중인 정산이나 참여 중인 방이 있다면 정산 완료 후 탈퇴가 가능해요.', 3, 'Y');

commit;

ALTER TABLE member_account_tb ADD (
    to_date VARCHAR2(8) default '20260721',
    from_date VARCHAR2(8) default '20260701',
    to_time VARCHAR2(6) default '235959',
    from_time VARCHAR2(6) default '000000',
    account_name VARCHAR2(20) default '계좌',
     status         VARCHAR2(20) DEFAULT 'NO' NOT NULL,
    CONSTRAINT ck_member_account_status CHECK (status IN ('YES', 'NO'))
);

CREATE TABLE MEMBER_tran_TB (
    MEMBER_tran_IDX NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    ID            VARCHAR2(20) NOT NULL,                           
    ACCOUNT_IDX            NUMBER NOT NULL,                          
    tran_date       VARCHAR2(30) NOT NULL,                           
    inout_type      VARCHAR2(10) NOT NULL,                       
    tran_amt              NUMBER ,                           
    REG_DATE             DATE DEFAULT SYSDATE,              
    
    CONSTRAINT FK_MEMBER_tran_member_id FOREIGN KEY (ID) 
    REFERENCES MEMBER_TB(ID) ON DELETE CASCADE,
    
    CONSTRAINT FK_MEMBER_tran_account_idx FOREIGN KEY (account_idx) 
    REFERENCES MEMBER_ACCOUNT_TB(account_idx) ON DELETE CASCADE
);