/* =========================================================
   04. 공지/FAQ/문의/알림 SQL
   =========================================================
   실행 안내: 01_member_schema.sql 실행 후 실행. 공지사항, 고객센터, 알림 기능에 필요.
   ========================================================= */

SET DEFINE OFF;

/* =========================================================
   16. [팀 원본 사용] 공지사항 테이블
   ========================================================= */
CREATE TABLE notice_tb (
    notice_id   NUMBER NOT NULL,
    admin_id    VARCHAR2(20) NOT NULL,
    title       VARCHAR2(200) NOT NULL,
    content     CLOB NOT NULL,
    pinned_yn   CHAR(1) DEFAULT 'N' NOT NULL,
    created_at  DATE DEFAULT SYSDATE NOT NULL,
    updated_at  DATE,

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
   17. [팀 원본 사용] FAQ 테이블
   ========================================================= */
CREATE TABLE faq_tb (
    faq_id      NUMBER NOT NULL,
    category    VARCHAR2(50) NOT NULL,
    question    VARCHAR2(300) NOT NULL,
    answer      CLOB NOT NULL,
    sort_order  NUMBER DEFAULT 0 NOT NULL,
    use_yn      CHAR(1) DEFAULT 'Y' NOT NULL,
    created_at  DATE DEFAULT SYSDATE NOT NULL,

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
   18. [팀 원본 사용] 문의사항 테이블
   ========================================================= */
CREATE TABLE inquiry_tb (
    inquiry_id NUMBER NOT NULL,
    id         VARCHAR2(20) NOT NULL,
    title      VARCHAR2(200) NOT NULL,
    content    CLOB NOT NULL,
    status     VARCHAR2(20) DEFAULT 'WAITING' NOT NULL,
    created_at DATE DEFAULT SYSDATE NOT NULL,
    updated_at DATE,

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
   19. [팀 원본 사용] 문의 답변 테이블
   ========================================================= */
CREATE TABLE inquiry_answer_tb (
    answer_id  NUMBER NOT NULL,
    inquiry_id NUMBER NOT NULL,
    admin_id   VARCHAR2(20) NOT NULL,
    content    CLOB NOT NULL,
    created_at DATE DEFAULT SYSDATE NOT NULL,
    updated_at DATE,

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
   20. [팀 원본 사용 + 팀 원본 오류 수정] 알림 테이블
   =========================================================
   [팀 원본 오류 수정]
   - 테이블 컬럼명은 id인데 인덱스/INSERT/예시 SQL에서 member_id를 사용하던 부분 수정
*/
CREATE TABLE alert_tb (
    alert_id   NUMBER NOT NULL,
    id         VARCHAR2(20) NOT NULL,
    alert_type VARCHAR2(30) NOT NULL,
    title      VARCHAR2(200) NOT NULL,
    content    VARCHAR2(1000),
    target_url VARCHAR2(300),
    read_yn    CHAR(1) DEFAULT 'N' NOT NULL,
    banner_yn  CHAR(1) DEFAULT 'Y' NOT NULL,
    created_at DATE DEFAULT SYSDATE NOT NULL,
    read_at    DATE,

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

CREATE INDEX idx_alert_member_read ON alert_tb(id, read_yn, created_at);
CREATE INDEX idx_alert_member_banner ON alert_tb(id, banner_yn, read_yn, created_at);

/* =========================================================
   23. [팀 원본 사용] 회원별 공지 즐겨찾기 테이블
   ========================================================= */
CREATE TABLE notice_bookmark_tb (
    bookmark_id NUMBER NOT NULL,
    id          VARCHAR2(20) NOT NULL,
    notice_id   NUMBER NOT NULL,
    created_at  DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notice_bookmark PRIMARY KEY (bookmark_id),
    CONSTRAINT fk_notice_bookmark_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT fk_notice_bookmark_notice FOREIGN KEY (notice_id) REFERENCES notice_tb(notice_id),
    CONSTRAINT uk_notice_bookmark UNIQUE (id, notice_id)
);

CREATE SEQUENCE seq_notice_bookmark START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER trg_notice_bookmark_bi
BEFORE INSERT ON notice_bookmark_tb
FOR EACH ROW
WHEN (NEW.bookmark_id IS NULL)
BEGIN
    SELECT seq_notice_bookmark.NEXTVAL INTO :NEW.bookmark_id FROM dual;
END;
/
