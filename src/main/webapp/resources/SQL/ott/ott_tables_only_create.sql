/* =========================================================
   SpendOlive OTT 테이블 생성 SQL
   - 회원정보(member_tb)는 생성/수정/삭제하지 않습니다.
   - 테스트 데이터 INSERT도 없습니다.
   - member_tb가 먼저 생성되어 있어야 합니다.
   ========================================================= */

SET DEFINE OFF;

/* =========================================================
   1. OTT 서비스 테이블
   ========================================================= */
CREATE TABLE ott_service_tb (
    ott_service_id NUMBER NOT NULL,
    service_name   VARCHAR2(50) NOT NULL,
    default_price  NUMBER DEFAULT 0 NOT NULL,
    share_yn       CHAR(1) DEFAULT 'Y' NOT NULL,
    block_reason   VARCHAR2(500),

    CONSTRAINT pk_ott_service PRIMARY KEY (ott_service_id),
    CONSTRAINT uk_ott_service_name UNIQUE (service_name),
    CONSTRAINT ck_ott_service_share CHECK (share_yn IN ('Y', 'N'))
);

CREATE SEQUENCE seq_ott_service START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_ott_service_bi
BEFORE INSERT ON ott_service_tb
FOR EACH ROW
WHEN (NEW.ott_service_id IS NULL)
BEGIN
    SELECT seq_ott_service.NEXTVAL
    INTO :NEW.ott_service_id
    FROM dual;
END;
/

/* =========================================================
   2. OTT 공유방 / 모집글 테이블
   ========================================================= */
CREATE TABLE ott_room_tb (
    room_id        NUMBER NOT NULL,
    host_member_id VARCHAR2(20) NOT NULL,
    ott_service_id NUMBER NOT NULL,
    room_name      VARCHAR2(100) NOT NULL,
    total_price    NUMBER NOT NULL,
    billing_day    NUMBER NOT NULL,
    member_limit   NUMBER DEFAULT 4 NOT NULL,
    status         VARCHAR2(20) DEFAULT 'RECRUITING' NOT NULL,
    invite_code    VARCHAR2(50),
    created_at     DATE DEFAULT SYSDATE NOT NULL,
    updated_at     DATE,

    CONSTRAINT pk_ott_room PRIMARY KEY (room_id),
    CONSTRAINT fk_ott_room_host FOREIGN KEY (host_member_id) REFERENCES member_tb(id),
    CONSTRAINT fk_ott_room_service FOREIGN KEY (ott_service_id) REFERENCES ott_service_tb(ott_service_id),
    CONSTRAINT ck_ott_room_price CHECK (total_price >= 0),
    CONSTRAINT ck_ott_room_billing_day CHECK (billing_day BETWEEN 1 AND 31),
    CONSTRAINT ck_ott_room_member_limit CHECK (member_limit BETWEEN 2 AND 6),
    CONSTRAINT ck_ott_room_status CHECK (status IN ('RECRUITING', 'ACTIVE', 'END')),
    CONSTRAINT uk_ott_room_invite UNIQUE (invite_code)
);

CREATE SEQUENCE seq_ott_room START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_ott_room_bi
BEFORE INSERT ON ott_room_tb
FOR EACH ROW
WHEN (NEW.room_id IS NULL)
BEGIN
    SELECT seq_ott_room.NEXTVAL
    INTO :NEW.room_id
    FROM dual;
END;
/

CREATE INDEX idx_ott_room_host ON ott_room_tb(host_member_id);
CREATE INDEX idx_ott_room_status ON ott_room_tb(status, created_at);

/* =========================================================
   3. OTT 공유방 참여자 / 신청자 테이블
   - APPLIED: 신청대기
   - ACTIVE: 수락되어 참여중
   - REJECTED: 거절
   - OUT: 나감
   ========================================================= */
CREATE TABLE ott_room_member_tb (
    room_member_id NUMBER NOT NULL,
    room_id        NUMBER NOT NULL,
    member_id      VARCHAR2(20) NOT NULL,
    member_role    VARCHAR2(20) DEFAULT 'MEMBER' NOT NULL,
    share_amount   NUMBER DEFAULT 0 NOT NULL,
    fee_rate       NUMBER(5,2) DEFAULT 0 NOT NULL,
    fee_amount     NUMBER DEFAULT 0 NOT NULL,
    pay_amount     NUMBER DEFAULT 0 NOT NULL,
    joined_at      DATE DEFAULT SYSDATE NOT NULL,
    status         VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,

    CONSTRAINT pk_ott_room_member PRIMARY KEY (room_member_id),
    CONSTRAINT fk_room_member_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_room_member_member FOREIGN KEY (member_id) REFERENCES member_tb(id),
    CONSTRAINT uk_room_member UNIQUE (room_id, member_id),
    CONSTRAINT ck_room_member_role CHECK (member_role IN ('HOST', 'MEMBER')),
    CONSTRAINT ck_room_member_status CHECK (status IN ('ACTIVE', 'OUT', 'APPLIED', 'REJECTED')),
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
    SELECT seq_ott_room_member.NEXTVAL
    INTO :NEW.room_member_id
    FROM dual;
END;
/

CREATE INDEX idx_room_member_room ON ott_room_member_tb(room_id, status);
CREATE INDEX idx_room_member_member ON ott_room_member_tb(member_id, status);

/* =========================================================
   4. OTT 공유방 채팅 메시지 테이블
   ========================================================= */
CREATE TABLE ott_chat_message_tb (
    message_id      NUMBER NOT NULL,
    room_id         NUMBER NOT NULL,
    sender_id       VARCHAR2(20) NOT NULL,
    message_content VARCHAR2(1000) NOT NULL,
    created_at      DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_ott_chat_message PRIMARY KEY (message_id),
    CONSTRAINT fk_chat_message_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES member_tb(id)
);

CREATE SEQUENCE seq_ott_chat_message START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_ott_chat_message_bi
BEFORE INSERT ON ott_chat_message_tb
FOR EACH ROW
WHEN (NEW.message_id IS NULL)
BEGIN
    SELECT seq_ott_chat_message.NEXTVAL
    INTO :NEW.message_id
    FROM dual;
END;
/

CREATE INDEX idx_chat_message_room ON ott_chat_message_tb(room_id, created_at, message_id);
CREATE INDEX idx_chat_message_sender ON ott_chat_message_tb(sender_id, created_at);

/* =========================================================
   5. OTT 공유방 채팅 읽음 기준 테이블
   ========================================================= */
CREATE TABLE ott_chat_read_tb (
    room_id      NUMBER NOT NULL,
    member_id    VARCHAR2(20) NOT NULL,
    last_read_at DATE,

    CONSTRAINT pk_ott_chat_read PRIMARY KEY (room_id, member_id),
    CONSTRAINT fk_chat_read_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_chat_read_member FOREIGN KEY (member_id) REFERENCES member_tb(id)
);

/* =========================================================
   6. OTT 공유방 차단 테이블
   ========================================================= */
CREATE TABLE ott_room_block_tb (
    room_block_id NUMBER NOT NULL,
    room_id       NUMBER NOT NULL,
    id            VARCHAR2(20) NOT NULL,
    blocked_by    VARCHAR2(20) NOT NULL,
    block_reason  VARCHAR2(500),
    blocked_at    DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_ott_room_block PRIMARY KEY (room_block_id),
    CONSTRAINT fk_room_block_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_room_block_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT fk_room_block_by FOREIGN KEY (blocked_by) REFERENCES member_tb(id),
    CONSTRAINT uk_room_block_member UNIQUE (room_id, id)
);

CREATE SEQUENCE seq_ott_room_block START WITH 1 INCREMENT BY 1 NOCACHE;

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

CREATE INDEX idx_room_block_room ON ott_room_block_tb(room_id);
CREATE INDEX idx_room_block_member ON ott_room_block_tb(id);

/* =========================================================
   7. 월별 정산 테이블
   ========================================================= */
CREATE TABLE settlement_tb (
    settlement_id    NUMBER NOT NULL,
    room_id          NUMBER NOT NULL,
    settlement_month CHAR(7) NOT NULL,
    total_price      NUMBER NOT NULL,
    total_fee        NUMBER DEFAULT 0 NOT NULL,
    total_pay_amount NUMBER DEFAULT 0 NOT NULL,
    due_date         DATE NOT NULL,
    status           VARCHAR2(20) DEFAULT 'READY' NOT NULL,
    created_at       DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_settlement PRIMARY KEY (settlement_id),
    CONSTRAINT fk_settlement_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT uk_settlement_room_month UNIQUE (room_id, settlement_month),
    CONSTRAINT ck_settlement_month CHECK (REGEXP_LIKE(settlement_month, '^[0-9]{4}-[0-9]{2}$')),
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
    SELECT seq_settlement.NEXTVAL
    INTO :NEW.settlement_id
    FROM dual;
END;
/

CREATE INDEX idx_settlement_room ON settlement_tb(room_id, settlement_month);

/* =========================================================
   8. 팀원별 입금 상태 테이블
   ========================================================= */
CREATE TABLE settlement_payment_tb (
    payment_id      NUMBER NOT NULL,
    settlement_id   NUMBER NOT NULL,
    id              VARCHAR2(20) NOT NULL,
    base_amount     NUMBER NOT NULL,
    fee_rate        NUMBER(5,2) DEFAULT 3 NOT NULL,
    fee_amount      NUMBER DEFAULT 0 NOT NULL,
    total_amount    NUMBER NOT NULL,
    payment_status  VARCHAR2(20) DEFAULT 'UNPAID' NOT NULL,
    paid_at         DATE,
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
    SELECT seq_settlement_payment.NEXTVAL
    INTO :NEW.payment_id
    FROM dual;
END;
/

CREATE INDEX idx_payment_settlement ON settlement_payment_tb(settlement_id);
CREATE INDEX idx_payment_member ON settlement_payment_tb(id, payment_status);

/* =========================================================
   9. 알림 테이블
   ========================================================= */
CREATE TABLE alert_tb (
    alert_id       NUMBER NOT NULL,
    id             VARCHAR2(20) NOT NULL,
    alert_type     VARCHAR2(30) NOT NULL,
    title          VARCHAR2(200) NOT NULL,
    content        VARCHAR2(1000),
    target_url     VARCHAR2(300),
    read_yn        CHAR(1) DEFAULT 'N' NOT NULL,
    banner_yn      CHAR(1) DEFAULT 'Y' NOT NULL,
    created_at     DATE DEFAULT SYSDATE NOT NULL,
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
    SELECT seq_alert.NEXTVAL
    INTO :NEW.alert_id
    FROM dual;
END;
/

CREATE INDEX idx_alert_member_read ON alert_tb(id, read_yn, created_at);
CREATE INDEX idx_alert_member_banner ON alert_tb(id, banner_yn, read_yn, created_at);

COMMIT;
