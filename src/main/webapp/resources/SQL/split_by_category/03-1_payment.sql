

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
    card_number     VARCHAR2(50),
    card_company    VARCHAR2(20),
    paid_at         DATE,                     -- 토스 카드 결제 완료 일시
    confirmed_at    DATE,                     -- 정산 확정 일시
    expired_at      DATE,                     -- 미결제 추방 일시
    cancelled_at    DATE,                     -- 환불/취소 일시 (환불 테이블 대체)
    paymentKey      VARCHAR2(100) , 
    orderId         VARCHAR2(100) , 
    memo            VARCHAR2(500),            -- 환불 사유 등 기록 

    CONSTRAINT pk_settlement_payment PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_settlement FOREIGN KEY (settlement_id) REFERENCES settlement_tb(settlement_id),
    CONSTRAINT fk_payment_card_number FOREIGN KEY (card_number) REFERENCES member_card_tb(card_number),
    CONSTRAINT fk_payment_card_company FOREIGN KEY (card_company) REFERENCES member_card_tb(card_company),
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


CREATE TABLE platform_revenue_tb (
    revenue_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 자동 증가 PK (시퀀스 대용)
    settlement_id    NUMBER NOT NULL,                                  -- 월별 정산 고유 ID (FK)
    room_id          NUMBER NOT NULL,                                  -- OTT 매칭방 고유 ID (FK)
    payer_id         VARCHAR2(20) NOT NULL,                            -- 수수료를 지불한 회원 아이디 (FK)
    base_amount      NUMBER NOT NULL,                                  -- 수수료 계산의 기준이 된 원금
    fee_rate         NUMBER(5, 2) DEFAULT 3.00,                        -- 적용 수수료율 (ex: 3.00)
    fee_amount       NUMBER NOT NULL,                                  -- 최종 수수료 수익 금액
    status   VARCHAR2(20) DEFAULT 'EARNED',                            -- 상태 (EARNED, REFUNDED 등)
    created_at       DATE DEFAULT SYSDATE,                             -- 발생 일시 (기본값 현재날짜)

    -- 외래키 제약조건 (실제 존재하는 상대 테이블명/컬럼명에 맞춰 필요시 주석 해제하여 사용)
    CONSTRAINT fk_revenue_room FOREIGN KEY (room_id) REFERENCES ott_room_tb(room_id),
    CONSTRAINT fk_revenue_status CHECK (status IN ('EARNED', 'REFUNDED', 'CANCELLED')),
    CONSTRAINT fk_revenue_payer FOREIGN KEY (payer_id) REFERENCES member_tb(id)
);