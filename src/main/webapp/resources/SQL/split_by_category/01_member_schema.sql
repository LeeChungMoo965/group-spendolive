/* =========================================================
   01. 회원/로그인/회원가입 SQL
   =========================================================
   실행 안내: 가장 먼저 실행. 다른 대부분의 테이블이 member_tb를 참조함.
   ========================================================= */

SET DEFINE OFF;

/* =========================================================
   1. [팀 원본 사용] 회원 테이블
   ========================================================= */
CREATE TABLE member_tb (
    member_id      NUMBER NOT NULL,
    id             VARCHAR2(20) NOT NULL,
    email          VARCHAR2(100) NOT NULL,
    password       VARCHAR2(255) NOT NULL,
    member_name    VARCHAR2(50) NOT NULL,
    nickname       VARCHAR2(50),
    phone          VARCHAR2(20),
    login_type     VARCHAR2(20) DEFAULT 'LOCAL' NOT NULL,
    verify_type    VARCHAR2(20) NOT NULL,
    role           VARCHAR2(20) DEFAULT 'USER' NOT NULL,
    status         VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    blocked_until  DATE,
    warning_count  NUMBER DEFAULT 0 NOT NULL,
    last_login_at  DATE,
    created_at     DATE DEFAULT SYSDATE NOT NULL,
    updated_at     DATE,

    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uk_member_id UNIQUE (id),
    CONSTRAINT uk_member_email UNIQUE (email),
    CONSTRAINT ck_member_login_type CHECK (login_type IN ('LOCAL', 'KAKAO', 'GOOGLE', 'NAVER')),
    CONSTRAINT ck_member_verify_type CHECK (verify_type IN ('EMAIL', 'PHONE')),
    CONSTRAINT ck_member_role CHECK (role IN ('USER', 'HOST', 'ADMIN')),
    CONSTRAINT ck_member_status CHECK (status IN ('ACTIVE', 'LEAVE', 'BLOCK', 'PERM_BLOCK')),
    CONSTRAINT ck_member_warning_count CHECK (warning_count BETWEEN 0 AND 3)
);

CREATE SEQUENCE seq_member START WITH 1 INCREMENT BY 1 NOCACHE;

/* =========================================================
   2. 마이페이지/OTT 연동 상태 컬럼
      - 실제 오픈뱅킹 값은 member_account_tb에 저장
      - 실제 카드 빌링키는 member_card_tb에 저장
   ========================================================= */
ALTER TABLE member_tb ADD (
    account_status  VARCHAR2(4) DEFAULT 'NO' NOT NULL,
    card_status     VARCHAR2(4) DEFAULT 'NO' NOT NULL,
    CONSTRAINT ck_member_account_status CHECK (account_status IN ('YES', 'NO')),
    CONSTRAINT ck_member_card_link_status CHECK (card_status IN ('YES', 'NO'))
);

alter table member_tb add(
warninged_at date);

CREATE OR REPLACE TRIGGER trg_member_bi
BEFORE INSERT ON member_tb
FOR EACH ROW
WHEN (NEW.member_id IS NULL)
BEGIN
    SELECT seq_member.NEXTVAL INTO :NEW.member_id FROM dual;
END;
/

CREATE TABLE MEMBER_ACCOUNT_TB (
    ACCOUNT_IDX          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 고유 번호
    ID            VARCHAR2(20) NOT NULL,                           -- 회원 ID (MEMBER_TB 외래키)
    BANK_CODE            VARCHAR2(50) NOT NULL,                           -- 은행명 (ex: 신한은행, 국민은행)
    ACCOUNT_NUMBER       VARCHAR2(30) NOT NULL,                           -- 마스킹된 계좌번호 (ex: 110-***-1234)
    FINTECH_USE_NUM      VARCHAR2(50) NOT NULL,                           -- 금결원 핵심 키 (핀테크이용번호 💥)
    BALANCE              NUMBER DEFAULT 0,                                -- 계좌 잔액 (실시간 동기화용 💰)
    OPEN_BANK_TOKEN      VARCHAR2(255) NOT NULL,                          -- 금결원 사용자 토큰
    OPEN_BANK_USER_SEQ   VARCHAR2(50) NOT NULL,                           -- 금결원 사용자 일련번호
    ACCOUNT_HOLDER_NAM   VARCHAR2(50),
    REG_DATE             DATE DEFAULT SYSDATE,                            -- 연동 일자
    account_name VARCHAR2(20) default '계좌',
     to_date VARCHAR2(8) default '20260721',
    from_date VARCHAR2(8) default '20260701',
    to_time VARCHAR2(6) default '235959',
    from_time VARCHAR2(6) default '000000',
    
    -- 회원 테이블과의 연관 관계 (회원 탈퇴 시 계좌도 같이 자동 삭제)

    CONSTRAINT FK_ACCOUNT_member_id FOREIGN KEY (ID) 

    REFERENCES MEMBER_TB(ID) ON DELETE CASCADE
);
ALTER TABLE member_account_tb ADD (
    to_date VARCHAR2(8) default '20260721',
    from_date VARCHAR2(8) default '20260701',
    to_time VARCHAR2(6) default '235959',
    from_time VARCHAR2(6) default '000000',
    account_name VARCHAR2(20) default '계좌'
);

CREATE TABLE MEMBER_CARD_TB (
    CARD_IDX        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 고유 번호
    ID              VARCHAR2(20) NOT NULL,                           -- 회원 ID (FK)
    BILLING_KEY     VARCHAR2(100) NOT NULL,                          -- 토스 빌링키 
    CARD_COMPANY    VARCHAR2(50),                                    -- 카드사 이름 (ex: 신한, 현대)
    CARD_NUMBER     VARCHAR2(20),                                    -- 마스킹된 카드번호 (ex: 433012******1234)
    REG_DATE        DATE DEFAULT SYSDATE,                            -- 등록일
    
    -- 회원 테이블과의 연관 관계 설정 (회원 탈퇴 시 카드 정보도 삭제되게)


    CONSTRAINT FK_CARD_MEMBER_ID FOREIGN KEY (ID) 
    REFERENCES MEMBER_TB(ID) ON DELETE CASCADE
);

ALTER TABLE member_card_tb ADD (
    status         VARCHAR2(20) DEFAULT 'NO' NOT NULL,
    CONSTRAINT ck_member_card_status CHECK (status IN ('YES', 'NO'))
);

CREATE TABLE MEMBER_tran_TB (
    MEMBER_tran_IDX NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 고유 번호
    ID            VARCHAR2(20) NOT NULL,                           -- 회원 ID (MEMBER_TB 외래키)
    ACCOUNT_IDX            NUMBER NOT NULL,                           -- 은행명 (ex: 신한은행, 국민은행)
    tran_date       VARCHAR2(30) NOT NULL,                           -- 마스킹된 계좌번호 (ex: 110-***-1234)
    inout_type      VARCHAR2(10) NOT NULL,                           -- 금결원 핵심 키 (핀테크이용번호 💥)
    tran_amt              NUMBER ,                                -- 계좌 잔액 (실시간 동기화용 💰)
    REG_DATE             DATE DEFAULT SYSDATE,                            -- 연동 일자
    
    -- 회원 테이블과의 연관 관계 (회원 탈퇴 시 계좌도 같이 자동 삭제)

    CONSTRAINT FK_MEMBER_tran_member_id FOREIGN KEY (ID) 
    REFERENCES MEMBER_TB(ID) ON DELETE CASCADE,
    
    CONSTRAINT FK_MEMBER_tran_account_idx FOREIGN KEY (account_idx) 
    REFERENCES MEMBER_TB(account_idx) ON DELETE CASCADE
);
