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

