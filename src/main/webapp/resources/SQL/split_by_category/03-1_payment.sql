/* =========================================================
   1. [신규 추가] 파티원 카드 빌링 정보 테이블
   ========================================================= */
CREATE TABLE member_billing_tb (
    billing_id       NUMBER NOT NULL,
    id               VARCHAR2(20) NOT NULL,    -- MEMBER_TB의 PK (파티원 ID)
    customer_key     VARCHAR2(100) NOT NULL,   -- 토스에 보낸 유저 고유 ID (ex: member_123)
    billing_key      VARCHAR2(255) NOT NULL,   -- 토스가 준 진짜 빌링키 (출금 치트키)
    card_company     VARCHAR2(50),             -- 카드사 이름 (신한, 현대 등)
    card_number      VARCHAR2(50),             -- 마스킹된 카드번호
    card_type        VARCHAR2(20),             -- 신용 / 체크 구분
    is_active        CHAR(1) DEFAULT 'Y' NOT NULL, -- 현재 주 결제 카드로 사용 중인지 여부 (Y/N)
    created_at       DATE DEFAULT SYSDATE NOT NULL,
    
    CONSTRAINT pk_member_billing PRIMARY KEY (billing_id),
    CONSTRAINT fk_billing_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT ck_billing_active CHECK (is_active IN ('Y', 'N'))
);

CREATE SEQUENCE seq_member_billing START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_member_billing_bi
BEFORE INSERT ON member_billing_tb
FOR EACH ROW
WHEN (NEW.billing_id IS NULL)
BEGIN
    SELECT seq_member_billing.NEXTVAL INTO :NEW.billing_id FROM dual;
END;
/


/* =========================================================
   2. [다이어트 통합] 월별 정산 마스터 테이블
   ========================================================= */
CREATE TABLE settlement_tb (
    settlement_id      NUMBER NOT NULL,
    room_id            NUMBER NOT NULL,
    settlement_month   CHAR(7) NOT NULL,       -- YYYY-MM
    total_price        NUMBER NOT NULL,        -- 방 전체 가격
    total_fee          NUMBER DEFAULT 0 NOT NULL,
    total_pay_amount   NUMBER DEFAULT 0 NOT NULL,
    due_date           DATE NOT NULL,          -- 최종 마감일
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
        status IN ('READY','REQUESTED','DONE','PAYMENT_OPEN','REPLACE_RECRUITING','CONFIRMED','CANCELLED','CLOSED')
    ),
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

CREATE INDEX idx_settlement_room ON settlement_tb(room_id, settlement_month);


/* =========================================================
   3. [다이어트 통합] 팀원별 입금 및 취소/환불 상태 테이블
   ========================================================= */
CREATE TABLE settlement_payment_tb (
    payment_id      NUMBER NOT NULL,
    settlement_id   NUMBER NOT NULL,
    id              VARCHAR2(20) NOT NULL,    -- 파티원 ID
    base_amount     NUMBER NOT NULL,          -- 순수 분담금
    fee_rate        NUMBER(5,2) DEFAULT 3 NOT NULL, -- 수수료율
    fee_amount      NUMBER DEFAULT 0 NOT NULL,      -- 수수료 금액 (플랫폼 수익 테이블 대체)
    total_amount    NUMBER NOT NULL,          -- 최종 결제 금액 (분담금 + 수수료)
    payment_status  VARCHAR2(30) DEFAULT 'UNPAID' NOT NULL,
    paid_at         DATE,                     -- 토스 카드 결제 완료 일시
    confirmed_at    DATE,                     -- 정산 확정 일시
    expired_at      DATE,                     -- 미결제 추방 일시
    cancelled_at    DATE,                     -- 환불/취소 일시 (환불 테이블 대체)
    memo            VARCHAR2(500),            -- 환불 사유 등 기록

    CONSTRAINT pk_settlement_payment PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_payment_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT uk_payment_member UNIQUE (settlement_id, id),
    CONSTRAINT ck_payment_base_amount CHECK (base_amount >= 0),
    CONSTRAINT ck_payment_fee_rate CHECK (fee_rate >= 0),
    CONSTRAINT ck_payment_fee_amount CHECK (fee_amount >= 0),
    CONSTRAINT ck_payment_total_amount CHECK (total_amount >= 0),
    CONSTRAINT ck_payment_status CHECK (
        payment_status IN ('UNPAID','PAID','CONFIRMED','EXPIRED','CANCELLED','REFUND_REQUESTED','REFUNDED')
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


/* =========================================================
   4. [다이어트 통합] 통합 정산 금고 테이블 (에스크로 + 방장지급)
   ========================================================= */
CREATE TABLE escrow_payout_tb (
    escrow_payout_id NUMBER NOT NULL,
    settlement_id    NUMBER NOT NULL,
    room_id          NUMBER NOT NULL,
    payer_id         VARCHAR2(20) NOT NULL,   -- 돈 낸 파티원
    host_id          VARCHAR2(20) NOT NULL,   -- 돈 받을 방장
    amount           NUMBER NOT NULL,         -- 방장에게 갈 순수 정산금
    status           VARCHAR2(30) DEFAULT 'HELD' NOT NULL, -- HELD(보관), RELEASED(방장지급완료), REFUNDED(파티원환불), CANCELLED(취소)
    created_at       DATE DEFAULT SYSDATE NOT NULL,        -- 금고 입고 시점
    payout_due_date  DATE,                    -- 방장 정산 예정일
    payout_at        DATE,                    -- 방장 지급 완료 시점

    CONSTRAINT pk_escrow_payout PRIMARY KEY (escrow_payout_id),
    CONSTRAINT fk_ep_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_ep_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_ep_payer FOREIGN KEY (payer_id) REFERENCES member_tb(id),
    CONSTRAINT fk_ep_host FOREIGN KEY (host_id) REFERENCES member_tb(id),
    CONSTRAINT ck_ep_amount CHECK (amount >= 0),
    CONSTRAINT ck_ep_status CHECK (status IN ('HELD', 'RELEASED', 'REFUNDED', 'CANCELLED'))
);

CREATE SEQUENCE seq_escrow_payout START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_escrow_payout_bi
BEFORE INSERT ON escrow_payout_tb
FOR EACH ROW
WHEN (NEW.escrow_payout_id IS NULL)
BEGIN
    SELECT seq_escrow_payout.NEXTVAL INTO :NEW.escrow_payout_id FROM dual;
END;
/

CREATE INDEX idx_ep_settlement ON escrow_payout_tb(settlement_id);
CREATE INDEX idx_ep_host_status ON escrow_payout_tb(host_id, status);