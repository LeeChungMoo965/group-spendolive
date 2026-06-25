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
ALTER TABLE member_tb ADD (
    open_bank_user_seq_no VARCHAR2(50),
    open_bank_token       VARCHAR2(500),
    fintech_use_num VARCHAR2(24)
);
CREATE OR REPLACE TRIGGER trg_member_bi
BEFORE INSERT ON member_tb
FOR EACH ROW
WHEN (NEW.member_id IS NULL)
BEGIN
    SELECT seq_member.NEXTVAL INTO :NEW.member_id FROM dual;
END;
/
