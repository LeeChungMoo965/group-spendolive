SELECT * FROM member_tb;
SELECT * FROM expense_category_tb;
SELECT * FROM expense_tb;

/* =========================================================
   SpendOlive Oracle DB Schema - 수정 반영본
   =========================================================
   목차
   1. 회원 테이블
    2. 지출 카테고리 테이블
    3. 지출 내역 테이블
    4. OTT 서비스 테이블
    5. OTT 공유방 테이블
    6. OTT 공유방 참여자 테이블
    7. OTT 공유방 차단 테이블
    8. 월별 정산 테이블
    9. 팀원별 입금 상태 테이블
    10. 홈페이지 보관금 테이블
    11. 플랫폼 수익 테이블
    12. 방장 지급 내역 테이블
    13. 공지사항 테이블
    14. FAQ 테이블
    15. 문의사항 테이블
    16. 문의 답변 테이블
    17. 알림 테이블
    18. 신고 테이블
    19. 경고 테이블
    20. 기본 데이터
    21. 자주 쓰는 SQL
   ========================================================= */


/* =========================================================
   0. 기존 객체 삭제
   =========================================================
   다시 실행할 때만 주석 해제.
*/
/*
DROP TABLE platform_revenue_tb CASCADE CONSTRAINTS;
DROP TABLE warning_tb CASCADE CONSTRAINTS;
DROP TABLE report_tb CASCADE CONSTRAINTS;
DROP TABLE payout_tb CASCADE CONSTRAINTS;
DROP TABLE escrow_tb CASCADE CONSTRAINTS;
DROP TABLE alert_tb CASCADE CONSTRAINTS;
DROP TABLE login_history_tb CASCADE CONSTRAINTS;
DROP TABLE inquiry_answer_tb CASCADE CONSTRAINTS;
DROP TABLE inquiry_tb CASCADE CONSTRAINTS;
DROP TABLE faq_tb CASCADE CONSTRAINTS;
DROP TABLE notice_tb CASCADE CONSTRAINTS;
DROP TABLE settlement_payment_tb CASCADE CONSTRAINTS;
DROP TABLE settlement_tb CASCADE CONSTRAINTS;
DROP TABLE ott_room_member_tb CASCADE CONSTRAINTS;
DROP TABLE ott_room_tb CASCADE CONSTRAINTS;
DROP TABLE ott_service_tb CASCADE CONSTRAINTS;
DROP TABLE expense_tb CASCADE CONSTRAINTS;
DROP TABLE expense_category_tb CASCADE CONSTRAINTS;
DROP TABLE member_tb CASCADE CONSTRAINTS;

DROP SEQUENCE seq_platform_revenue;
DROP SEQUENCE seq_warning;
DROP SEQUENCE seq_report;
DROP SEQUENCE seq_payout;
DROP SEQUENCE seq_escrow;
DROP SEQUENCE seq_alert;
DROP SEQUENCE seq_login_history;
DROP SEQUENCE seq_inquiry_answer;
DROP SEQUENCE seq_inquiry;
DROP SEQUENCE seq_faq;
DROP SEQUENCE seq_notice;
DROP SEQUENCE seq_settlement_payment;
DROP SEQUENCE seq_settlement;
DROP SEQUENCE seq_ott_room_member;
DROP SEQUENCE seq_ott_room;
DROP SEQUENCE seq_ott_service;
DROP SEQUENCE seq_expense;
DROP SEQUENCE seq_expense_category;
DROP SEQUENCE seq_member;
*/


/* =========================================================
    1. 회원 테이블
   =========================================================
   실제 회원 정보를 저장.
   이메일로 로그인하며, 가입 방식은 자체/카카오/구글/네이버로 구분.
*/
CREATE TABLE member_tb (

    /* 회원 번호(PK) */
    member_id NUMBER NOT NULL,

    id VARCHAR2(20) NOT NULL UNIQUE,
    /* 로그인 이메일
       자체 가입 이메일 또는 소셜 계정 이메일
    */
    email VARCHAR2(100) NOT NULL,

    /* 비밀번호
       모든 회원이 우리 사이트에서 직접 설정
    */
    password VARCHAR2(255) NOT NULL,

    /* 회원 이름 */
    member_name VARCHAR2(50) NOT NULL,

    /* 닉네임 */
    nickname VARCHAR2(50),

    /* 휴대폰 번호 */
    phone VARCHAR2(20),

    /* 로그인 방식
       LOCAL  : 자체 회원가입/로그인
       KAKAO  : 카카오 이메일 인증 후 가입
       GOOGLE : 구글 이메일 인증 후 가입
       NAVER  : 네이버 이메일 인증 후 가입
    */
    login_type VARCHAR2(20) DEFAULT 'LOCAL' NOT NULL,

    /* 가입 인증 방식
       EMAIL : 소셜 이메일 인증
       PHONE : 자체 가입 휴대폰 인증
    */
    verify_type VARCHAR2(20) NOT NULL,

    /* 권한
       USER  : 일반 회원
       HOST  : 파티장
       ADMIN : 관리자
    */
    role VARCHAR2(20) DEFAULT 'USER' NOT NULL,

    /* 회원 상태
       ACTIVE     : 정상
       LEAVE      : 탈퇴
       BLOCK      : 기간 정지
       PERM_BLOCK : 영구정지
    */
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,

    /* 정지 해제 예정일 */
    blocked_until DATE,

    /* 경고 누적 횟수 */
    warning_count NUMBER DEFAULT 0 NOT NULL,

    /* 마지막 로그인 일시 */
    last_login_at DATE,

    /* 가입일 */
    created_at DATE DEFAULT SYSDATE NOT NULL,

    /* 수정일 */
    updated_at DATE,

    /* 기본키 */
    CONSTRAINT pk_member PRIMARY KEY (member_id),

    /* 이메일 중복 방지 */
    CONSTRAINT uk_member_email UNIQUE (email),

    /* 로그인 방식 제한 */
    CONSTRAINT ck_member_login_type
        CHECK (login_type IN ('LOCAL', 'KAKAO', 'GOOGLE', 'NAVER')),

    /* 인증 방식 제한 */
    CONSTRAINT ck_member_verify_type
        CHECK (verify_type IN ('EMAIL', 'PHONE')),

    /* 권한 제한 */
    CONSTRAINT ck_member_role
        CHECK (role IN ('USER', 'HOST', 'ADMIN')),

    /* 회원 상태 제한 */
    CONSTRAINT ck_member_status
        CHECK (status IN ('ACTIVE', 'LEAVE', 'BLOCK', 'PERM_BLOCK')),

    /* 경고 횟수 제한 */
    CONSTRAINT ck_member_warning_count
        CHECK (warning_count BETWEEN 0 AND 3)
);


/* 회원 번호 자동 생성 */
CREATE SEQUENCE seq_member
START WITH 1
INCREMENT BY 1
NOCACHE;


CREATE OR REPLACE TRIGGER trg_member_bi
BEFORE INSERT ON member_tb
FOR EACH ROW
WHEN (NEW.member_id IS NULL)
BEGIN
    SELECT seq_member.NEXTVAL
    INTO :NEW.member_id
    FROM dual;
END;
/

/* =========================================================
   이메일/휴대폰 인증 테이블
   =========================================================
   현재 프로젝트에서는 별도 인증 테이블을 만들지 않음.
   이메일 인증은 회원가입 로직에서 인증 성공 후 member_tb에 INSERT.
   즉, 인증 실패/미완료 회원은 DB에 저장하지 않음.
*/
/*
CREATE TABLE email_auth_tb (...);
CREATE TABLE phone_auth_tb (...);
*/




/* =========================================================
    2. 지출 카테고리 테이블
   ========================================================= */
CREATE TABLE expense_category_tb (
    /* 카테고리 번호(PK) */
    category_id    NUMBER          NOT NULL,

    /* 카테고리명 */
    category_name  VARCHAR2(50)    NOT NULL,

    /* 지출 유형: FIXED 고정, VARIABLE 변동, OTT */
    expense_type   VARCHAR2(20)    NOT NULL,

    /* 화면 출력 순서 */
    sort_order     NUMBER          DEFAULT 0 NOT NULL,

    CONSTRAINT pk_expense_category PRIMARY KEY (category_id),
    CONSTRAINT ck_expense_category_type CHECK (expense_type IN ('FIXED', 'VARIABLE', 'OTT'))
);

CREATE SEQUENCE seq_expense_category START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_expense_category_bi
BEFORE INSERT ON expense_category_tb
FOR EACH ROW
WHEN (NEW.category_id IS NULL)
BEGIN
    SELECT seq_expense_category.NEXTVAL INTO :NEW.category_id FROM dual;
END;
/

INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('월세', 'FIXED', 1);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('통신비', 'FIXED', 2);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('보험료', 'FIXED', 3);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('관리비', 'FIXED', 4);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('공과금', 'FIXED', 5);

INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('식비', 'VARIABLE', 1);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('교통비', 'VARIABLE', 2);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('생활비', 'VARIABLE', 3);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('쇼핑', 'VARIABLE', 4);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('문화, 취미생활', 'VARIABLE', 5);

INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('Netflix', 'OTT', 1);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('Disney+', 'OTT', 2);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('TVING', 'OTT', 3);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('Wavve', 'OTT', 4);
INSERT INTO expense_category_tb (category_name, expense_type, sort_order) VALUES ('coupangplay', 'OTT', 5);

COMMIT;

/* =========================================================
    3. 지출 내역 테이블
   ========================================================= */
CREATE TABLE expense_tb (
    /* 지출 번호(PK) */
    expense_id     NUMBER          NOT NULL,

    /* 회원 번호(FK) */
    member_id      NUMBER          NOT NULL,

    /* 카테고리 번호(FK) */
    category_id    NUMBER          NOT NULL,

    /* 지출 제목 */
    expense_title  VARCHAR2(100)   NOT NULL,

    /* 지출 금액 */
    amount         NUMBER          NOT NULL,

    /* 지출 날짜 */
    expense_date   DATE            NOT NULL,

    /* 결제 수단 */
    payment_method VARCHAR2(30),

    /* 메모 */
    memo           VARCHAR2(1000),

    /* 반복 지출 여부: Y 반복, N 일반 */
    repeat_yn      CHAR(1)         DEFAULT 'N' NOT NULL,

    /* 반복 주기: MONTHLY, WEEKLY, YEARLY */
    repeat_cycle   VARCHAR2(20),

    /* 고정 지출 여부: Y 고정, N 변동 */
    fixed_yn       CHAR(1)         DEFAULT 'N' NOT NULL,

    /* 등록일 */
    created_at     DATE            DEFAULT SYSDATE NOT NULL,

    /* 수정일 */
    updated_at     DATE,

    CONSTRAINT pk_expense PRIMARY KEY (expense_id),
    CONSTRAINT fk_expense_member FOREIGN KEY (member_id) REFERENCES member_tb(member_id),
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES expense_category_tb(category_id),
    CONSTRAINT ck_expense_amount CHECK (amount >= 0),
    CONSTRAINT ck_expense_repeat_yn CHECK (repeat_yn IN ('Y', 'N')),
    CONSTRAINT ck_expense_fixed_yn CHECK (fixed_yn IN ('Y', 'N')),
    CONSTRAINT ck_expense_repeat_cycle CHECK (
        repeat_cycle IS NULL
        OR repeat_cycle IN ('MONTHLY', 'WEEKLY', 'YEARLY')
    )
);

CREATE SEQUENCE seq_expense START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_expense_bi
BEFORE INSERT ON expense_tb
FOR EACH ROW
WHEN (NEW.expense_id IS NULL)
BEGIN
    SELECT seq_expense.NEXTVAL INTO :NEW.expense_id FROM dual;
END;
/

CREATE INDEX idx_expense_member_date ON expense_tb(member_id, expense_date);
CREATE INDEX idx_expense_category ON expense_tb(category_id);


/* =========================================================
    4. OTT 서비스 테이블
   =========================================================
   공유 가능/불가능 OTT 목록을 모두 저장.
*/
CREATE TABLE ott_service_tb (
    /* OTT 서비스 번호(PK) */
    ott_service_id NUMBER          NOT NULL,

    /* OTT 서비스명 */
    service_name   VARCHAR2(50)    NOT NULL,

    /* 기본 구독료 */
    default_price  NUMBER          DEFAULT 0 NOT NULL,

    /* 공유 가능 여부: Y 가능, N 불가 */
    share_yn       CHAR(1)         DEFAULT 'Y' NOT NULL,

    /* 공유 불가 사유 */
    block_reason   VARCHAR2(500),

    CONSTRAINT pk_ott_service PRIMARY KEY (ott_service_id),
    CONSTRAINT uk_ott_service_name UNIQUE (service_name),
    CONSTRAINT ck_ott_service_share CHECK (share_yn IN ('Y', 'N')),
   
);

CREATE SEQUENCE seq_ott_service START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_ott_service_bi
BEFORE INSERT ON ott_service_tb
FOR EACH ROW
WHEN (NEW.ott_service_id IS NULL)
BEGIN
    SELECT seq_ott_service.NEXTVAL INTO :NEW.ott_service_id FROM dual;
END;
/


/* =========================================================
    5. OTT 공유방 테이블
   ========================================================= */
CREATE TABLE ott_room_tb (
    /* 공유방 번호(PK) */
    room_id        NUMBER          NOT NULL,

    /* 방장 회원 번호(FK) */
    host_member_id VARCHAR2(20)          NOT NULL,

    /* OTT 서비스 번호(FK) */
    ott_service_id NUMBER          NOT NULL,

    /* 공유방 이름 */
    room_name      VARCHAR2(100)   NOT NULL,

    /* OTT 전체 구독료 */
    total_price    NUMBER          NOT NULL,

    /* 매월 결제일 */
    billing_day    NUMBER          NOT NULL,

    /* 최대 참여 인원 */
    member_limit   NUMBER          DEFAULT 4 NOT NULL,

    /* 방 상태: RECRUITING 모집중, ACTIVE 운영중, END 종료 */
    status         VARCHAR2(20)    DEFAULT 'RECRUITING' NOT NULL,

    /* 초대 코드 */
    invite_code    VARCHAR2(50),

    /* 생성일 */
    created_at     DATE            DEFAULT SYSDATE NOT NULL,

    /* 수정일 */
    updated_at     DATE,

    CONSTRAINT pk_ott_room PRIMARY KEY (room_id),
    CONSTRAINT fk_ott_room_host FOREIGN KEY (host_member_id) REFERENCES member_tb(id),
    CONSTRAINT fk_ott_room_service FOREIGN KEY (ott_service_id) REFERENCES ott_service_tb(ott_service_id),
    CONSTRAINT ck_ott_room_price CHECK (total_price >= 0),
    CONSTRAINT ck_ott_room_billing_day CHECK (billing_day BETWEEN 1 AND 31),
    CONSTRAINT ck_ott_room_status CHECK (status IN ('RECRUITING', 'ACTIVE', 'END')),
    CONSTRAINT uk_ott_room_invite UNIQUE (invite_code)
);

CREATE SEQUENCE seq_ott_room START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_ott_room_bi
BEFORE INSERT ON ott_room_tb
FOR EACH ROW
WHEN (NEW.room_id IS NULL)
BEGIN
    SELECT seq_ott_room.NEXTVAL INTO :NEW.room_id FROM dual;
END;
/

CREATE INDEX idx_ott_room_host ON ott_room_tb(host_member_id);


/* =========================================================
    6. OTT 공유방 참여자 테이블
   ========================================================= */
CREATE TABLE ott_room_member_tb (
    /* 공유방 참여 번호(PK) */
    room_member_id NUMBER          NOT NULL,

    /* 공유방 번호(FK) */
    room_id        NUMBER          NOT NULL,

    /* 참여 회원 번호(FK) */
    member_id      VARCHAR2(20)          NOT NULL,

    /* 방 안에서의 역할: HOST 방장, MEMBER 참여자 */
    member_role    VARCHAR2(20)    DEFAULT 'MEMBER' NOT NULL,

    /* 방장에게 전달될 실제 분담금 */
    share_amount   NUMBER          DEFAULT 0 NOT NULL,

    /* 플랫폼 수수료율: 팀원만 3%, 방장은 0% */
    fee_rate       NUMBER(5,2)     DEFAULT 0 NOT NULL,

    /* 플랫폼 수수료 */
    fee_amount     NUMBER          DEFAULT 0 NOT NULL,

    /* 참여자가 실제 결제할 총액 = share_amount + fee_amount */
    pay_amount     NUMBER          DEFAULT 0 NOT NULL,

    /* 참여일 */
    joined_at      DATE            DEFAULT SYSDATE NOT NULL,

    /* 참여 상태: ACTIVE 참여중, OUT 나감 */
    status         VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL,

    CONSTRAINT pk_ott_room_member PRIMARY KEY (room_member_id),
    CONSTRAINT fk_room_member_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_room_member_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT uk_room_member UNIQUE (room_id, member_id),
    CONSTRAINT ck_room_member_role CHECK (member_role IN ('HOST', 'MEMBER')),
    CONSTRAINT ck_room_member_status CHECK (status IN ('ACTIVE', 'OUT')),
    CONSTRAINT ck_room_member_amount CHECK (share_amount >= 0),
    CONSTRAINT ck_room_member_fee_rate CHECK (fee_rate >= 0),
    CONSTRAINT ck_room_member_fee_amount CHECK (fee_amount >= 0),
    CONSTRAINT ck_room_member_pay_amount CHECK (pay_amount >= 0)
);

CREATE SEQUENCE seq_ott_room_member START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_ott_room_member_bi
BEFORE INSERT ON ott_room_member_tb
FOR EACH ROW
WHEN (NEW.room_member_id IS NULL)
BEGIN
    SELECT seq_ott_room_member.NEXTVAL INTO :NEW.room_member_id FROM dual;
END;
/

/* =========================================================
    7. OTT 공유방 차단 테이블
   =========================================================
   방장이 특정 회원을 공유방에서 차단할 때 사용.
   차단된 회원은 해당 공유방에 다시 참여할 수 없음.
*/
CREATE TABLE ott_room_block_tb (
    /* 공유방 차단 번호(PK) */
    room_block_id NUMBER NOT NULL,

    /* 공유방 번호(FK) */
    room_id NUMBER NOT NULL,

    /* 차단된 회원 번호(FK) */
    id VARCHAR2(20) NOT NULL,

    /* 차단한 사람 번호(FK): 보통 방장 */
    blocked_by VARCHAR2(20) NOT NULL,

    /* 차단 사유 */
    block_reason VARCHAR2(500),

    /* 차단일 */
    blocked_at DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_ott_room_block PRIMARY KEY (room_block_id),

    CONSTRAINT fk_room_block_room
        FOREIGN KEY (room_id)
        REFERENCES ott_room_tb(room_id),

    CONSTRAINT fk_room_block_member
        FOREIGN KEY (id)
        REFERENCES member_tb(id),

    CONSTRAINT fk_room_block_by
        FOREIGN KEY (blocked_by)
        REFERENCES member_tb(id),

    /* 같은 방에서 같은 회원 중복 차단 방지 */
    CONSTRAINT uk_room_block_member
        UNIQUE (room_id, id)
);

/* 공유방 차단 번호 자동 생성 */
CREATE SEQUENCE seq_ott_room_block
START WITH 1
INCREMENT BY 1
NOCACHE;

CREATE OR REPLACE TRIGGER trg_ott_room_block_bi
BEFORE INSERT ON ott_room_block_tb
FOR EACH ROW
WHEN (NEW.room_block_id IS NULL)
BEGIN
    SELECT seq_ott_room_block.NEXTVAL
    INTO :NEW.room_block_id
    FROM dual;
END;
/

/* 공유방별 차단 회원 조회 */
CREATE INDEX idx_room_block_room
ON ott_room_block_tb(room_id);

/* 회원별 차단 여부 조회 */
CREATE INDEX idx_room_block_member
ON ott_room_block_tb(id);

/* =========================================================
   8. 월별 정산 테이블
   ========================================================= */
CREATE TABLE settlement_tb (
    /* 정산 번호(PK) */
    settlement_id   NUMBER          NOT NULL,

    /* 공유방 번호(FK) */
    room_id         NUMBER          NOT NULL,

    /* 정산 월: 예) 2026-06 */
    settlement_month CHAR(7)        NOT NULL,

    /* 방장에게 갈 전체 정산금 */
    total_price     NUMBER          NOT NULL,

    /* 플랫폼 수수료 총액 */
    total_fee       NUMBER          DEFAULT 0 NOT NULL,

    /* 참여자들이 실제 결제할 총액 */
    total_pay_amount NUMBER         DEFAULT 0 NOT NULL,

    /* 입금 마감일 */
    due_date        DATE            NOT NULL,

    /* 정산 상태: READY 생성, REQUESTED 요청, DONE 완료 */
    status          VARCHAR2(20)    DEFAULT 'READY' NOT NULL,

    /* 생성일 */
    created_at      DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_settlement PRIMARY KEY (settlement_id),
    CONSTRAINT fk_settlement_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT uk_settlement_room_month UNIQUE (room_id, settlement_month),
    CONSTRAINT ck_settlement_month CHECK (REGEXP_LIKE(settlement_month, '^\d{4}-\d{2}$')),
    CONSTRAINT ck_settlement_status CHECK (status IN ('READY', 'REQUESTED', 'DONE')),
    CONSTRAINT ck_settlement_total_price CHECK (total_price >= 0),
    CONSTRAINT ck_settlement_total_fee CHECK (total_fee >= 0),
    CONSTRAINT ck_settlement_total_pay CHECK (total_pay_amount >= 0)
);

CREATE SEQUENCE seq_settlement START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_settlement_bi
BEFORE INSERT ON settlement_tb
FOR EACH ROW
WHEN (NEW.settlement_id IS NULL)
BEGIN
    SELECT seq_settlement.NEXTVAL INTO :NEW.settlement_id FROM dual;
END;
/


/* =========================================================
    9. 팀원별 입금 상태 테이블
   =========================================================
   방장 제외 팀원은 3% 수수료를 포함한 금액을 결제.
*/
CREATE TABLE settlement_payment_tb (
    /* 입금 상태 번호(PK) */
    payment_id      NUMBER          NOT NULL,

    /* 정산 번호(FK) */
    settlement_id   NUMBER          NOT NULL,

    /* 입금할 회원 번호(FK) */
    id       VARCHAR2(20)          NOT NULL,

    /* 방장에게 전달될 금액 */
    base_amount     NUMBER          NOT NULL,

    /* 플랫폼 수수료율: 팀원 3% */
    fee_rate        NUMBER(5,2)     DEFAULT 3 NOT NULL,

    /* 플랫폼 수수료 */
    fee_amount      NUMBER          DEFAULT 0 NOT NULL,

    /* 실제 결제 금액 = base_amount + fee_amount */
    total_amount    NUMBER          NOT NULL,

    /* 입금 상태: UNPAID 미입금, PAID 입금완료 */
    payment_status  VARCHAR2(20)    DEFAULT 'UNPAID' NOT NULL,

    /* 입금일 */
    paid_at         DATE,

    /* 메모 */
    memo            VARCHAR2(500),

    CONSTRAINT pk_settlement_payment PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_payment_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT uk_payment_member UNIQUE (settlement_id, id),
    CONSTRAINT ck_payment_base_amount CHECK (base_amount >= 0),
    CONSTRAINT ck_payment_fee_rate CHECK (fee_rate >= 0),
    CONSTRAINT ck_payment_fee_amount CHECK (fee_amount >= 0),
    CONSTRAINT ck_payment_total_amount CHECK (total_amount >= 0),
    CONSTRAINT ck_payment_status CHECK (payment_status IN ('UNPAID', 'PAID'))
);

CREATE SEQUENCE seq_settlement_payment START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_settlement_payment_bi
BEFORE INSERT ON settlement_payment_tb
FOR EACH ROW
WHEN (NEW.payment_id IS NULL)
BEGIN
    SELECT seq_settlement_payment.NEXTVAL INTO :NEW.payment_id FROM dual;
END;
/


/* =========================================================
    10. 홈페이지 보관금 테이블
   =========================================================
   참여자가 낸 돈 중 방장에게 갈 금액을 사이트가 보관.
   수수료는 platform_revenue_tb에 따로 기록.
*/
CREATE TABLE escrow_tb (
    /* 보관금 번호(PK) */
    escrow_id        NUMBER          NOT NULL,

    /* 정산 번호(FK) */
    settlement_id    NUMBER          NOT NULL,

    /* 공유방 번호(FK) */
    room_id          NUMBER          NOT NULL,

    /* 돈을 낸 회원 번호(FK) */
    payer_id  VARCHAR2(20)          NOT NULL,

    /* 돈을 받을 방장 번호(FK) */
    host_id   VARCHAR2(20)          NOT NULL,

    /* 방장에게 전달될 보관 금액 */
    amount           NUMBER          NOT NULL,

    /* 보관 상태: HELD 보관중, RELEASED 지급완료, REFUNDED 환불, CANCELLED 취소 */
    escrow_status    VARCHAR2(20)    DEFAULT 'HELD' NOT NULL,

    /* 참여자가 돈을 낸 날짜 */
    paid_at          DATE            DEFAULT SYSDATE NOT NULL,

    /* 방장에게 지급 가능한 예정일 */
    release_due_date DATE,

    /* 실제 지급일 */
    released_at      DATE,

    /* 환불일 */
    refunded_at      DATE,

    CONSTRAINT pk_escrow PRIMARY KEY (escrow_id),
    CONSTRAINT fk_escrow_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_escrow_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_escrow_payer FOREIGN KEY (payer_id) REFERENCES member_tb(id),
    CONSTRAINT fk_escrow_host FOREIGN KEY (host_id) REFERENCES member_tb(id),
    CONSTRAINT ck_escrow_amount CHECK (amount >= 0),
    CONSTRAINT ck_escrow_status CHECK (escrow_status IN ('HELD', 'RELEASED', 'REFUNDED', 'CANCELLED'))
);

CREATE SEQUENCE seq_escrow START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_escrow_bi
BEFORE INSERT ON escrow_tb
FOR EACH ROW
WHEN (NEW.escrow_id IS NULL)
BEGIN
    SELECT seq_escrow.NEXTVAL INTO :NEW.escrow_id FROM dual;
END;
/

CREATE INDEX idx_escrow_settlement ON escrow_tb(settlement_id);
CREATE INDEX idx_escrow_host_status ON escrow_tb(host_member_id, escrow_status);


/* =========================================================
    11. 플랫폼 수익 테이블
   =========================================================
   방장 제외 팀원에게 받은 3% 수수료를 우리 서비스 수익으로 저장.
*/
CREATE TABLE platform_revenue_tb (
    /* 플랫폼 수익 번호(PK) */
    revenue_id      NUMBER          NOT NULL,

    /* 정산 번호(FK) */
    settlement_id   NUMBER          NOT NULL,

    /* 공유방 번호(FK) */
    room_id         NUMBER          NOT NULL,

    /* 수수료를 낸 회원 번호(FK) */
    payer_id VARCHAR2(20)          NOT NULL,

    /* 수수료 기준 금액 */
    base_amount     NUMBER          NOT NULL,

    /* 수수료율: 기본 3% */
    fee_rate        NUMBER(5,2)     DEFAULT 3 NOT NULL,

    /* 우리 수입으로 들어오는 수수료 */
    fee_amount      NUMBER          NOT NULL,

    /* 수익 상태: EARNED 수익발생, REFUNDED 환불, CANCELLED 취소 */
    revenue_status  VARCHAR2(20)    DEFAULT 'EARNED' NOT NULL,

    /* 수익 발생일 */
    created_at      DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_platform_revenue PRIMARY KEY (revenue_id),
    CONSTRAINT fk_revenue_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_revenue_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_revenue_payer FOREIGN KEY (payer_id) REFERENCES member_tb(id),
    CONSTRAINT ck_revenue_base_amount CHECK (base_amount >= 0),
    CONSTRAINT ck_revenue_fee_rate CHECK (fee_rate >= 0),
    CONSTRAINT ck_revenue_fee_amount CHECK (fee_amount >= 0),
    CONSTRAINT ck_revenue_status CHECK (revenue_status IN ('EARNED', 'REFUNDED', 'CANCELLED'))
);

CREATE SEQUENCE seq_platform_revenue START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_platform_revenue_bi
BEFORE INSERT ON platform_revenue_tb
FOR EACH ROW
WHEN (NEW.revenue_id IS NULL)
BEGIN
    SELECT seq_platform_revenue.NEXTVAL INTO :NEW.revenue_id FROM dual;
END;
/

CREATE INDEX idx_revenue_settlement ON platform_revenue_tb(settlement_id);
CREATE INDEX idx_revenue_created ON platform_revenue_tb(created_at);


/* =========================================================
    12. 방장 지급 내역 테이블
   =========================================================
   수수료를 제외하고 방장에게 지급한 기록.
*/
CREATE TABLE payout_tb (
    /* 지급 번호(PK) */
    payout_id       NUMBER          NOT NULL,

    /* 정산 번호(FK) */
    settlement_id   NUMBER          NOT NULL,

    /* 공유방 번호(FK) */
    room_id         NUMBER          NOT NULL,

    /* 돈을 받을 방장 번호(FK) */
    host_id  VARCHAR2(20)          NOT NULL,

    /* 방장에게 지급할 총액 */
    total_amount    NUMBER          NOT NULL,

    /* 지급 상태: READY 대기, PAID 완료, FAILED 실패, CANCELLED 취소 */
    payout_status   VARCHAR2(20)    DEFAULT 'READY' NOT NULL,

    /* 지급 요청일 */
    requested_at    DATE            DEFAULT SYSDATE NOT NULL,

    /* 실제 지급일 */
    paid_at         DATE,

    /* 메모 */
    memo            VARCHAR2(500),

    CONSTRAINT pk_payout PRIMARY KEY (payout_id),
    CONSTRAINT fk_payout_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_payout_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_payout_host FOREIGN KEY (host_id) REFERENCES member_tb(id),
    CONSTRAINT ck_payout_amount CHECK (total_amount >= 0),
    CONSTRAINT ck_payout_status CHECK (payout_status IN ('READY', 'PAID', 'FAILED', 'CANCELLED'))
);

CREATE SEQUENCE seq_payout START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_payout_bi
BEFORE INSERT ON payout_tb
FOR EACH ROW
WHEN (NEW.payout_id IS NULL)
BEGIN
    SELECT seq_payout.NEXTVAL INTO :NEW.payout_id FROM dual;
END;
/

CREATE INDEX idx_payout_host_status ON payout_tb(host_member_id, payout_status);


/* =========================================================
    13. 공지사항 테이블
   ========================================================= */
CREATE TABLE notice_tb (
    notice_id      NUMBER          NOT NULL,
    admin_id       VARCHAR2(20)          NOT NULL,
    title          VARCHAR2(200)   NOT NULL,
    content        CLOB            NOT NULL,
    pinned_yn      CHAR(1)         DEFAULT 'N' NOT NULL,
    created_at     DATE            DEFAULT SYSDATE NOT NULL,
    updated_at     DATE,

    CONSTRAINT pk_notice PRIMARY KEY (notice_id),
    CONSTRAINT fk_notice_admin FOREIGN KEY (admin_id) REFERENCES member_tb(id),
    CONSTRAINT ck_notice_pinned CHECK (pinned_yn IN ('Y', 'N'))
);

CREATE SEQUENCE seq_notice START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_notice_bi
BEFORE INSERT ON notice_tb
FOR EACH ROW
WHEN (NEW.notice_id IS NULL)
BEGIN
    SELECT seq_notice.NEXTVAL INTO :NEW.notice_id FROM dual;
END;
/


/* =========================================================
    14. FAQ 테이블
   ========================================================= */
CREATE TABLE faq_tb (
    faq_id         NUMBER          NOT NULL,
    category       VARCHAR2(50)    NOT NULL,
    question       VARCHAR2(300)   NOT NULL,
    answer         CLOB            NOT NULL,
    sort_order     NUMBER          DEFAULT 0 NOT NULL,
    use_yn         CHAR(1)         DEFAULT 'Y' NOT NULL,
    created_at     DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_faq PRIMARY KEY (faq_id),
    CONSTRAINT ck_faq_use CHECK (use_yn IN ('Y', 'N'))
);

CREATE SEQUENCE seq_faq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_faq_bi
BEFORE INSERT ON faq_tb
FOR EACH ROW
WHEN (NEW.faq_id IS NULL)
BEGIN
    SELECT seq_faq.NEXTVAL INTO :NEW.faq_id FROM dual;
END;
/


/* =========================================================
    15. 문의사항 테이블
   ========================================================= */
CREATE TABLE inquiry_tb (
    inquiry_id     NUMBER          NOT NULL,
    id      VARCHAR2(20)          NOT NULL,
    title          VARCHAR2(200)   NOT NULL,
    content        CLOB            NOT NULL,
    status         VARCHAR2(20)    DEFAULT 'WAITING' NOT NULL,
    created_at     DATE            DEFAULT SYSDATE NOT NULL,
    updated_at     DATE,

    CONSTRAINT pk_inquiry PRIMARY KEY (inquiry_id),
    CONSTRAINT fk_inquiry_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT ck_inquiry_status CHECK (status IN ('WAITING', 'ANSWERED'))
);

CREATE SEQUENCE seq_inquiry START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_inquiry_bi
BEFORE INSERT ON inquiry_tb
FOR EACH ROW
WHEN (NEW.inquiry_id IS NULL)
BEGIN
    SELECT seq_inquiry.NEXTVAL INTO :NEW.inquiry_id FROM dual;
END;
/


/* =========================================================
    16. 문의 답변 테이블
   ========================================================= */
CREATE TABLE inquiry_answer_tb (
    answer_id      NUMBER          NOT NULL,
    inquiry_id     NUMBER          NOT NULL,
    admin_id       VARCHAR2(20)          NOT NULL,
    content        CLOB            NOT NULL,
    created_at     DATE            DEFAULT SYSDATE NOT NULL,
    updated_at     DATE,

    CONSTRAINT pk_inquiry_answer PRIMARY KEY (answer_id),
    CONSTRAINT fk_answer_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry_tb(inquiry_id),
    CONSTRAINT fk_answer_admin FOREIGN KEY (admin_id) REFERENCES member_tb(id),
    CONSTRAINT uk_answer_inquiry UNIQUE (inquiry_id)
);

CREATE SEQUENCE seq_inquiry_answer START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_inquiry_answer_bi
BEFORE INSERT ON inquiry_answer_tb
FOR EACH ROW
WHEN (NEW.answer_id IS NULL)
BEGIN
    SELECT seq_inquiry_answer.NEXTVAL INTO :NEW.answer_id FROM dual;
END;
/


/* =========================================================
    17. 알림 테이블
   =========================================================
   트리거 없이 사용.
   알림 클릭 시 Service/Mapper에서 read_yn='Y', read_at=SYSDATE로 UPDATE.
*/
CREATE TABLE alert_tb (
    /* 알림 번호(PK) */
    alert_id       NUMBER          NOT NULL,

    /* 알림 받을 회원 번호(FK) */
    id      VARCHAR2(20)          NOT NULL,

    /* 알림 유형: SETTLEMENT, PAYMENT, INQUIRY, NOTICE, REPORT 등 */
    alert_type     VARCHAR2(30)    NOT NULL,

    /* 알림 제목 */
    title          VARCHAR2(200)   NOT NULL,

    /* 알림 내용 */
    content        VARCHAR2(1000),

    /* 클릭 시 이동할 주소 */
    target_url     VARCHAR2(300),

    /* 읽음 여부: Y 읽음, N 안 읽음 */
    read_yn        CHAR(1)         DEFAULT 'N' NOT NULL,

    /* 배너 표시 여부: Y 표시, N 숨김 */
    banner_yn      CHAR(1)         DEFAULT 'Y' NOT NULL,

    /* 알림 생성일 */
    created_at     DATE            DEFAULT SYSDATE NOT NULL,

    /* 읽은 날짜 */
    read_at        DATE,

    CONSTRAINT pk_alert PRIMARY KEY (alert_id),
    CONSTRAINT fk_alert_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT ck_alert_read CHECK (read_yn IN ('Y', 'N')),
    CONSTRAINT ck_alert_banner CHECK (banner_yn IN ('Y', 'N'))
);

CREATE SEQUENCE seq_alert START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_alert_bi
BEFORE INSERT ON alert_tb
FOR EACH ROW
WHEN (NEW.alert_id IS NULL)
BEGIN
    SELECT seq_alert.NEXTVAL INTO :NEW.alert_id FROM dual;
END;
/

/* 알림 이모지 빨간 점 표시용: read_yn='N' 개수 조회 */
CREATE INDEX idx_alert_member_read ON alert_tb(member_id, read_yn, created_at);

/* 배너 알림 조회용 */
CREATE INDEX idx_alert_member_banner ON alert_tb(member_id, banner_yn, read_yn, created_at);


/* =========================================================
    18. 신고 테이블
   ========================================================= */
CREATE TABLE report_tb (
    report_id          NUMBER          NOT NULL,
    /* 신고 한 회원아이디 */
    reporter_id        VARCHAR2(20)          NOT NULL,
    /* 신고 당한 회원아이디 */
    reported_member_id VARCHAR2(20)          NOT NULL,
    room_id            NUMBER,
    report_reason      VARCHAR2(500)   NOT NULL,
    report_status      VARCHAR2(20)    DEFAULT 'WAIT' NOT NULL,
    admin_comment      VARCHAR2(1000),
    created_at         DATE            DEFAULT SYSDATE NOT NULL,
    processed_at       DATE,

    CONSTRAINT pk_report PRIMARY KEY (report_id),
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES member_tb(id),
    CONSTRAINT fk_report_reported FOREIGN KEY (reported_member_id) REFERENCES member_tb(id),
    CONSTRAINT fk_report_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT ck_report_status CHECK (report_status IN ('WAIT', 'PROCESSING', 'COMPLETE', 'REJECT'))
);

CREATE SEQUENCE seq_report START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_report_bi
BEFORE INSERT ON report_tb
FOR EACH ROW
WHEN (NEW.report_id IS NULL)
BEGIN
    SELECT seq_report.NEXTVAL INTO :NEW.report_id FROM dual;
END;
/

CREATE INDEX idx_report_reported ON report_tb(reported_member_id, report_status);


/* =========================================================
    19. 경고 테이블
   =========================================================
   1회: 2주 정지
   2회: 4주 정지
   3회: 영구정지
   실제 회원 상태 변경은 Service에서 처리.
*/
CREATE TABLE warning_tb (

    warning_id     NUMBER          NOT NULL,
    /*경고 받은 회원*/
    member_id      VARCHAR2(20)          NOT NULL,
    report_id      NUMBER,
    warning_reason VARCHAR2(500)   NOT NULL,

    /* 경고 회차: 1, 2, 3 */
    warning_level  NUMBER          NOT NULL,

    /* 정지 기간: 1회 14일, 2회 28일, 3회 영구정지 */
    penalty_days   NUMBER,

    /* 영구정지 여부: Y 영구정지, N 기간정지 */
    permanent_yn   CHAR(1)         DEFAULT 'N' NOT NULL,

    created_at     DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_warning PRIMARY KEY (warning_id),
    CONSTRAINT fk_warning_member FOREIGN KEY (member_id) REFERENCES member_tb(id),
    CONSTRAINT fk_warning_report FOREIGN KEY (report_id) REFERENCES report_tb(report_id),
    CONSTRAINT ck_warning_level CHECK (warning_level IN (1, 2, 3)),
    CONSTRAINT ck_warning_permanent CHECK (permanent_yn IN ('Y', 'N'))
);

CREATE SEQUENCE seq_warning START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_warning_bi
BEFORE INSERT ON warning_tb
FOR EACH ROW
WHEN (NEW.warning_id IS NULL)
BEGIN
    SELECT seq_warning.NEXTVAL INTO :NEW.warning_id FROM dual;
END;
/

CREATE INDEX idx_warning_member ON warning_tb(member_id);


/* =========================================================
    20. 기본 데이터
   ======================================================== */

/* 지출 카테고리 */
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('월세/관리비', 'FIXED', 1);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('교통비', 'VARIABLE', 2);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('식비', 'VARIABLE', 3);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('카드대금', 'FIXED', 4);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('OTT', 'OTT', 5);
INSERT INTO expense_category_tb(category_name, expense_type, sort_order) VALUES ('기타', 'VARIABLE', 99);

/* 공유 가능 OTT */
INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('Netflix', 17000, 'Y', NULL);

INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('TVING', 13900, 'Y',  NULL);

INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('Wavve', 13900, 'Y', ' NULL);

INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('Watcha', 12900, 'Y', ' NULL);

INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('Disney+', 13900, 'Y',  NULL);

INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('Laftel', 14900, 'Y',  NULL);

/* 공유 불가 OTT */
INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('쿠팡플레이', 7890, 'N',  '쿠팡 계정과 연결되어 결제/배송/주문정보 노출 위험');

INSERT INTO ott_service_tb(service_name, default_price, share_yn, risk_level, block_reason)
VALUES ('애플TV+', 6500, 'N', 'Apple ID 직접 공유 위험, 가족 공유 방식 권장');

/* 테스트 회원 */
INSERT INTO member_tb(email, password, member_name, nickname, login_type, role)
VALUES ('admin@spendolive.com', '1234', '관리자', 'admin', 'LOCAL', 'ADMIN');

INSERT INTO member_tb(email, password, member_name, nickname, login_type, role)
VALUES ('host@spendolive.com', '1234', '파티장유저', 'partyhost', 'LOCAL', 'HOST');

INSERT INTO member_tb(email, password, member_name, nickname, login_type, role)
VALUES ('user@spendolive.com', '1234', '일반유저', 'olive', 'LOCAL', 'USER');

INSERT INTO member_tb(email, password, member_name, nickname, login_type, role)
VALUES ('naveruser@spendolive.com', NULL, '네이버유저', 'naverolive', 'NAVER', 'USER');

/* 테스트 지출 */
INSERT INTO expense_tb(member_id, category_id, expense_title, amount, expense_date, payment_method, memo, fixed_yn)
VALUES (3, 1, '관리비', 120000, TRUNC(SYSDATE, 'MM') + 4, 'CARD', '이번 달 관리비', 'Y');

INSERT INTO expense_tb(member_id, category_id, expense_title, amount, expense_date, payment_method, memo, fixed_yn)
VALUES (3, 3, '점심', 9500, TRUNC(SYSDATE), 'CARD', '학원 근처 점심', 'N');

INSERT INTO expense_tb(member_id, category_id, expense_title, amount, expense_date, payment_method, memo, fixed_yn)
VALUES (3, 5, '넷플릭스', 4250, TRUNC(SYSDATE, 'MM') + 14, 'TRANSFER', 'OTT 정산금', 'Y');

/* 테스트 알림 */
INSERT INTO alert_tb(member_id, alert_type, title, content, target_url)
VALUES (3, 'SETTLEMENT', '넷플릭스 정산 요청', '이번 달 넷플릭스 정산금 입금이 필요합니다.', '/settlement/1');

COMMIT;


/* =========================================================
    21. 자주 쓰는 SQL
   ========================================================= */

/* 알림 이모지 빨간 점 표시용: 안 읽은 알림 개수 */
SELECT COUNT(*) AS unread_count
FROM alert_tb
WHERE member_id = :memberId
  AND read_yn = 'N';

/* 배너에 띄울 안 읽은 알림 목록 */
SELECT alert_id, title, content, target_url, created_at
FROM alert_tb
WHERE member_id = :memberId
  AND read_yn = 'N'
  AND banner_yn = 'Y'
ORDER BY created_at DESC;

/* 알림 클릭 시 읽음 처리: 트리거 없이 코드에서 실행 */
UPDATE alert_tb
SET read_yn = 'Y',
    read_at = SYSDATE,
    banner_yn = 'N'
WHERE alert_id = :alertId
  AND member_id = :memberId;

/* 3% 수수료 계산 예시 */
SELECT
    4250 AS base_amount,
    ROUND(4250 * 0.03) AS fee_amount,
    4250 + ROUND(4250 * 0.03) AS total_amount
FROM dual;

/* 월별 플랫폼 수익 합계 */
SELECT
    TO_CHAR(created_at, 'YYYY-MM') AS revenue_month,
    SUM(fee_amount) AS total_revenue
FROM platform_revenue_tb
GROUP BY TO_CHAR(created_at, 'YYYY-MM')
ORDER BY revenue_month;

/* 날짜별 지출 합계: 캘린더 표시용 */
SELECT
    TO_CHAR(e.expense_date, 'YYYY-MM-DD') AS expense_date,
    SUM(e.amount) AS total_amount,
    COUNT(*) AS expense_count
FROM expense_tb e
WHERE e.member_id = :memberId
  AND TO_CHAR(e.expense_date, 'YYYY-MM') = :month
GROUP BY TO_CHAR(e.expense_date, 'YYYY-MM-DD')
ORDER BY expense_date;

/* 특정 날짜 클릭 시 상세 내역 */
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



/* =========================================================
    22. 추가 : 회원별 공지 즐겨찾기(찜)
   ========================================================= */


  CREATE TABLE notice_bookmark_tb (
    bookmark_id NUMBER NOT NULL,
    id VARCHAR2(20) NOT NULL,
    notice_id NUMBER NOT NULL,
    created_at DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notice_bookmark
        PRIMARY KEY (bookmark_id),

    CONSTRAINT fk_notice_bookmark_member
        FOREIGN KEY (id)
        REFERENCES member_tb(id),

    CONSTRAINT fk_notice_bookmark_notice
        FOREIGN KEY (notice_id)
        REFERENCES notice_tb(notice_id),

    CONSTRAINT uk_notice_bookmark
        UNIQUE (id, notice_id)
);

/*시퀀스*/

    CREATE SEQUENCE seq_notice_bookmark
    START WITH 1
    INCREMENT BY 1
    NOCACHE;

/*트리거*/

    
    CREATE OR REPLACE TRIGGER trg_notice_bookmark_bi
    BEFORE INSERT ON notice_bookmark_tb
    FOR EACH ROW
    BEGIN
        IF :NEW.bookmark_id IS NULL THEN
            :NEW.bookmark_id := seq_notice_bookmark.NEXTVAL;
        END IF;
    END;
    /
