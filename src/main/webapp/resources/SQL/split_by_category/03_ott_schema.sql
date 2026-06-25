/* =========================================================
   03. OTT 공유방/채팅/정산/환불 SQL
   =========================================================
   실행 안내: 01_member_schema.sql 실행 후 실행. OTT 신청/정산/방삭제 알림까지 테스트하려면 04_notice_inquiry_alert_schema.sql도 실행.
   ========================================================= */

SET DEFINE OFF;

/* =========================================================
   4. [팀 원본 사용 + 팀 원본 오류 수정] OTT 서비스 테이블
   =========================================================
   [팀 원본 오류 수정]
   - 마지막 CONSTRAINT 뒤에 있던 쉼표 제거
   - 기본 데이터 INSERT에서 사용하던 risk_level 컬럼 추가
*/
CREATE TABLE ott_service_tb (
    ott_service_id     NUMBER NOT NULL,
    service_name       VARCHAR2(50) NOT NULL,

    /*
       [방금 만든 OTT SQL 반영]
       default_price는 화면에서 N분의 1을 할 최종 금액입니다.
       예) 넷플릭스 프리미엄 17,000 + 추가 계정 5,000 * 2 = 27,000
    */
    default_price      NUMBER DEFAULT 0 NOT NULL,

    /* [피클플러스 방식 반영] OTT별 최고 멤버십 고정 정보 */
    fixed_plan_name    VARCHAR2(50) DEFAULT '프리미엄' NOT NULL,
    base_price         NUMBER DEFAULT 0 NOT NULL,
    extra_member_fee   NUMBER DEFAULT 0 NOT NULL,
    extra_member_count NUMBER DEFAULT 0 NOT NULL,
    max_member_limit   NUMBER DEFAULT 4 NOT NULL,
    platform_fee_rate  NUMBER(5,2) DEFAULT 3 NOT NULL,

    share_yn           CHAR(1) DEFAULT 'Y' NOT NULL,
    risk_level         VARCHAR2(20),
    block_reason       VARCHAR2(500),

    CONSTRAINT pk_ott_service PRIMARY KEY (ott_service_id),
    CONSTRAINT uk_ott_service_name UNIQUE (service_name),
    CONSTRAINT ck_ott_service_share CHECK (share_yn IN ('Y', 'N')),
    CONSTRAINT ck_ott_service_risk CHECK (risk_level IS NULL OR risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT ck_ott_service_price CHECK (default_price >= 0 AND base_price >= 0),
    CONSTRAINT ck_ott_service_extra CHECK (extra_member_fee >= 0 AND extra_member_count >= 0),
    CONSTRAINT ck_ott_service_member_limit CHECK (max_member_limit BETWEEN 1 AND 6),
    CONSTRAINT ck_ott_service_fee_rate CHECK (platform_fee_rate >= 0)
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
   5. [팀 원본 사용 + 방금 만든 OTT SQL 반영] OTT 공유방 테이블
   =========================================================
   [팀 원본 사용]
   - room_id, host_member_id, ott_service_id, room_name, total_price,
     billing_day, member_limit, status, invite_code, created_at, updated_at 유지

   [방금 만든 OTT SQL 반영]
   - plan_name: 요금제명
   - CLOSE_REQUESTED: 파티장 방 삭제 요청 상태
   - close_requested_at / close_effective_date / close_reason / close_notice / closed_at
   - PAYMENT_OPEN, REPLACE_RECRUITING, CLOSED 상태 추가
*/
CREATE TABLE ott_room_tb (
    room_id              NUMBER NOT NULL,
    host_member_id       VARCHAR2(20) NOT NULL,
    ott_service_id       NUMBER NOT NULL,
    room_name            VARCHAR2(100) NOT NULL,
    plan_name            VARCHAR2(50) DEFAULT '기본' NOT NULL,
    total_price          NUMBER NOT NULL,
    billing_day          NUMBER NOT NULL,
    member_limit         NUMBER DEFAULT 4 NOT NULL,
    room_mode            VARCHAR2(20) DEFAULT 'RECRUIT' NOT NULL,
    status               VARCHAR2(30) DEFAULT 'RECRUITING' NOT NULL,
    invite_code          VARCHAR2(50),
    close_requested_at   DATE,
    close_effective_date DATE,
    close_reason         VARCHAR2(500),
    close_notice         VARCHAR2(1000),
    closed_at            DATE,
    created_at           DATE DEFAULT SYSDATE NOT NULL,
    updated_at           DATE,

    CONSTRAINT pk_ott_room PRIMARY KEY (room_id),
    CONSTRAINT fk_ott_room_host FOREIGN KEY (host_member_id) REFERENCES member_tb(id),
    CONSTRAINT fk_ott_room_service FOREIGN KEY (ott_service_id) REFERENCES ott_service_tb(ott_service_id),
    CONSTRAINT ck_ott_room_price CHECK (total_price >= 0),
    CONSTRAINT ck_ott_room_billing_day CHECK (billing_day BETWEEN 1 AND 31),
    CONSTRAINT ck_ott_room_member_limit CHECK (member_limit BETWEEN 1 AND 6),
    CONSTRAINT ck_ott_room_mode CHECK (room_mode IN ('FRIEND', 'RECRUIT')),
    CONSTRAINT ck_ott_room_status CHECK (
        status IN (
            'RECRUITING',
            'ACTIVE',
            'PAYMENT_OPEN',
            'REPLACE_RECRUITING',
            'CLOSE_REQUESTED',
            'CLOSED',
            'END'
        )
    ),
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
CREATE INDEX idx_ott_room_mode ON ott_room_tb(room_mode, status, created_at);
CREATE INDEX idx_ott_room_status ON ott_room_tb(status, created_at);
CREATE INDEX idx_ott_room_close ON ott_room_tb(status, close_effective_date);

/* =========================================================
   6. [팀 원본 사용 + 팀 원본 오류 수정 + 방금 만든 OTT SQL 반영] OTT 공유방 참여자 테이블
   =========================================================
   [팀 원본 오류 수정]
   - member_id 컬럼을 만들고 FK는 id로 걸려 있던 오류 수정
     기존 오류: FOREIGN KEY (id) REFERENCES member_tb(id)
     수정 후: FOREIGN KEY (member_id) REFERENCES member_tb(id)

   [방금 만든 OTT SQL 반영]
   - APPLIED, REJECTED, KICKED 상태 추가
   - kicked_at, kicked_reason, left_at 추가
*/
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
    kicked_at      DATE,
    kicked_reason  VARCHAR2(500),
    left_at        DATE,

    CONSTRAINT pk_ott_room_member PRIMARY KEY (room_member_id),
    CONSTRAINT fk_room_member_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_room_member_member FOREIGN KEY (member_id) REFERENCES member_tb(id),
    CONSTRAINT uk_room_member UNIQUE (room_id, member_id),
    CONSTRAINT ck_room_member_role CHECK (member_role IN ('HOST', 'MEMBER')),
    CONSTRAINT ck_room_member_status CHECK (status IN ('APPLIED', 'ACTIVE', 'REJECTED', 'OUT', 'KICKED')),
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

CREATE INDEX idx_room_member_room ON ott_room_member_tb(room_id, status);
CREATE INDEX idx_room_member_member ON ott_room_member_tb(member_id, status);

/* =========================================================
   7. [방금 만든 OTT SQL 반영 - 신규 추가] OTT 공유방 채팅 메시지 테이블
   =========================================================
   팀 최종 SQL에는 없었지만, 이전에 정리한 OTT 공유방 구조에 있던 채팅 기능 테이블.
   필요 없으면 이 테이블과 아래 ott_chat_read_tb를 빼도 됨.
*/
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
    SELECT seq_ott_chat_message.NEXTVAL INTO :NEW.message_id FROM dual;
END;
/

CREATE INDEX idx_chat_message_room ON ott_chat_message_tb(room_id, created_at, message_id);
CREATE INDEX idx_chat_message_sender ON ott_chat_message_tb(sender_id, created_at);

/* =========================================================
   8. [방금 만든 OTT SQL 반영 - 신규 추가] OTT 공유방 채팅 읽음 기준 테이블
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
   9. [팀 원본 사용] OTT 공유방 차단 테이블
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
    SELECT seq_ott_room_block.NEXTVAL INTO :NEW.room_block_id FROM dual;
END;
/

CREATE INDEX idx_room_block_room ON ott_room_block_tb(room_id);
CREATE INDEX idx_room_block_member ON ott_room_block_tb(id);

/* =========================================================
   10. [팀 원본 사용 + 방금 만든 OTT SQL 반영] 월별 정산 테이블
   =========================================================
   [팀 원본 사용]
   - settlement_id, room_id, settlement_month, total_price, total_fee,
     total_pay_amount, due_date, status, created_at 유지

   [방금 만든 OTT SQL 반영]
   - payment_start_date: 결제 가능 시작일. 예) 6월 결제일
   - payment_close_date: 결제 마감일. 예) 7월 결제일 5일 전
   - service_start_date: 해당 이용분 시작일. 예) 7월 결제일
   - service_end_date: 해당 이용분 종료일
   - replace_start_date / replace_end_date: 미결제자 추방 후 대체 모집 기간
   - status에 PAYMENT_OPEN, REPLACE_RECRUITING, CONFIRMED, CANCELLED, CLOSED 추가
*/
CREATE TABLE settlement_tb (
    settlement_id      NUMBER NOT NULL,
    room_id            NUMBER NOT NULL,
    settlement_month   CHAR(7) NOT NULL,
    total_price        NUMBER NOT NULL,
    total_fee          NUMBER DEFAULT 0 NOT NULL,
    total_pay_amount   NUMBER DEFAULT 0 NOT NULL,
    due_date           DATE NOT NULL,
    payment_start_date DATE,
    payment_close_date DATE,
    service_start_date DATE,
    service_end_date   DATE,
    replace_start_date DATE,
    replace_end_date   DATE,
    closed_at          DATE,
    status             VARCHAR2(30) DEFAULT 'READY' NOT NULL,
    created_at         DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_settlement PRIMARY KEY (settlement_id),
    CONSTRAINT fk_settlement_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT uk_settlement_room_month UNIQUE (room_id, settlement_month),
    CONSTRAINT ck_settlement_month CHECK (REGEXP_LIKE(settlement_month, '^[0-9]{4}-[0-9]{2}$')),
    CONSTRAINT ck_settlement_status CHECK (
        status IN (
            'READY',
            'REQUESTED',
            'DONE',
            'PAYMENT_OPEN',
            'REPLACE_RECRUITING',
            'CONFIRMED',
            'CANCELLED',
            'CLOSED'
        )
    ),
    CONSTRAINT ck_settlement_total_price CHECK (total_price >= 0),
    CONSTRAINT ck_settlement_total_fee CHECK (total_fee >= 0),
    CONSTRAINT ck_settlement_total_pay CHECK (total_pay_amount >= 0),
    CONSTRAINT ck_settlement_pay_period CHECK (
        payment_start_date IS NULL
        OR payment_close_date IS NULL
        OR payment_start_date <= payment_close_date
    ),
    CONSTRAINT ck_settlement_service_period CHECK (
        service_start_date IS NULL
        OR service_end_date IS NULL
        OR service_start_date <= service_end_date
    )
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

CREATE INDEX idx_settlement_room ON settlement_tb(room_id, settlement_month);
CREATE INDEX idx_settlement_status_close ON settlement_tb(status, payment_close_date);
CREATE INDEX idx_settlement_service ON settlement_tb(status, service_start_date, service_end_date);

/* =========================================================
   11. [팀 원본 사용 + 방금 만든 OTT SQL 반영] 팀원별 입금 상태 테이블
   =========================================================
   [방금 만든 OTT SQL 반영]
   - EXPIRED: 결제 마감일까지 결제하지 않아 만료
   - REFUND_REQUESTED / REFUNDED: 방 삭제 요청 등으로 환불 처리
   - confirmed_at, expired_at, cancelled_at 추가
*/
CREATE TABLE settlement_payment_tb (
    payment_id      NUMBER NOT NULL,
    settlement_id   NUMBER NOT NULL,
    id              VARCHAR2(20) NOT NULL,
    base_amount     NUMBER NOT NULL,
    fee_rate        NUMBER(5,2) DEFAULT 3 NOT NULL,
    fee_amount      NUMBER DEFAULT 0 NOT NULL,
    total_amount    NUMBER NOT NULL,
    payment_status  VARCHAR2(30) DEFAULT 'UNPAID' NOT NULL,
    paid_at         DATE,
    confirmed_at    DATE,
    expired_at      DATE,
    cancelled_at    DATE,
    memo            VARCHAR2(500),

    CONSTRAINT pk_settlement_payment PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_payment_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT uk_payment_member UNIQUE (settlement_id, id),
    CONSTRAINT ck_payment_base_amount CHECK (base_amount >= 0),
    CONSTRAINT ck_payment_fee_rate CHECK (fee_rate >= 0),
    CONSTRAINT ck_payment_fee_amount CHECK (fee_amount >= 0),
    CONSTRAINT ck_payment_total_amount CHECK (total_amount >= 0),
    CONSTRAINT ck_payment_status CHECK (
        payment_status IN (
            'UNPAID',
            'PAID',
            'CONFIRMED',
            'EXPIRED',
            'CANCELLED',
            'REFUND_REQUESTED',
            'REFUNDED'
        )
    )
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

CREATE INDEX idx_payment_settlement ON settlement_payment_tb(settlement_id);
CREATE INDEX idx_payment_member ON settlement_payment_tb(id, payment_status);
CREATE INDEX idx_payment_status ON settlement_payment_tb(payment_status, paid_at);

/* =========================================================
   12. [방금 만든 OTT SQL 반영 - 신규 추가] 정산 환불 테이블
   =========================================================
   파티장이 방 삭제 요청을 했을 때 다음 달 이용분을 이미 결제한 참여자의 환불 기록을 저장.
*/
CREATE TABLE settlement_refund_tb (
    refund_id       NUMBER NOT NULL,
    payment_id      NUMBER NOT NULL,
    settlement_id   NUMBER NOT NULL,
    room_id         NUMBER NOT NULL,
    id              VARCHAR2(20) NOT NULL,
    refund_amount   NUMBER NOT NULL,
    refund_reason   VARCHAR2(30) DEFAULT 'ROOM_CLOSE' NOT NULL,
    refund_status   VARCHAR2(30) DEFAULT 'REQUESTED' NOT NULL,
    requested_at    DATE DEFAULT SYSDATE NOT NULL,
    completed_at    DATE,
    memo            VARCHAR2(500),

    CONSTRAINT pk_settlement_refund PRIMARY KEY (refund_id),
    CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES settlement_payment_tb(payment_id),
    CONSTRAINT fk_refund_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_refund_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_refund_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT uk_refund_payment UNIQUE (payment_id),
    CONSTRAINT ck_refund_amount CHECK (refund_amount >= 0),
    CONSTRAINT ck_refund_reason CHECK (refund_reason IN ('ROOM_CLOSE', 'PAYMENT_CANCEL', 'ADMIN_CANCEL', 'ETC')),
    CONSTRAINT ck_refund_status CHECK (refund_status IN ('REQUESTED', 'COMPLETED', 'FAILED'))
);

CREATE SEQUENCE seq_settlement_refund START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_settlement_refund_bi
BEFORE INSERT ON settlement_refund_tb
FOR EACH ROW
WHEN (NEW.refund_id IS NULL)
BEGIN
    SELECT seq_settlement_refund.NEXTVAL INTO :NEW.refund_id FROM dual;
END;
/

CREATE INDEX idx_refund_payment ON settlement_refund_tb(payment_id);
CREATE INDEX idx_refund_member ON settlement_refund_tb(id, refund_status);
CREATE INDEX idx_refund_room ON settlement_refund_tb(room_id, refund_status);

/* =========================================================
   13. [팀 원본 사용] 홈페이지 보관금 테이블
   ========================================================= */
CREATE TABLE escrow_tb (
    escrow_id        NUMBER NOT NULL,
    settlement_id    NUMBER NOT NULL,
    room_id          NUMBER NOT NULL,
    payer_id         VARCHAR2(20) NOT NULL,
    host_id          VARCHAR2(20) NOT NULL,
    amount           NUMBER NOT NULL,
    escrow_status    VARCHAR2(20) DEFAULT 'HELD' NOT NULL,
    paid_at          DATE DEFAULT SYSDATE NOT NULL,
    release_due_date DATE,
    released_at      DATE,
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
/* [팀 원본 오류 수정] host_member_id 컬럼은 없으므로 host_id로 수정 */
CREATE INDEX idx_escrow_host_status ON escrow_tb(host_id, escrow_status);

/* =========================================================
   14. [팀 원본 사용] 플랫폼 수익 테이블
   ========================================================= */
CREATE TABLE platform_revenue_tb (
    revenue_id      NUMBER NOT NULL,
    settlement_id   NUMBER NOT NULL,
    room_id         NUMBER NOT NULL,
    payer_id        VARCHAR2(20) NOT NULL,
    base_amount     NUMBER NOT NULL,
    fee_rate        NUMBER(5,2) DEFAULT 3 NOT NULL,
    fee_amount      NUMBER NOT NULL,
    revenue_status  VARCHAR2(20) DEFAULT 'EARNED' NOT NULL,
    created_at      DATE DEFAULT SYSDATE NOT NULL,

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
   15. [팀 원본 사용] 방장 지급 내역 테이블
   ========================================================= */
CREATE TABLE payout_tb (
    payout_id       NUMBER NOT NULL,
    settlement_id   NUMBER NOT NULL,
    room_id         NUMBER NOT NULL,
    host_id         VARCHAR2(20) NOT NULL,
    total_amount    NUMBER NOT NULL,
    payout_status   VARCHAR2(20) DEFAULT 'READY' NOT NULL,
    requested_at    DATE DEFAULT SYSDATE NOT NULL,
    paid_at         DATE,
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

/* [팀 원본 오류 수정] host_member_id 컬럼은 없으므로 host_id로 수정 */
CREATE INDEX idx_payout_host_status ON payout_tb(host_id, payout_status);
