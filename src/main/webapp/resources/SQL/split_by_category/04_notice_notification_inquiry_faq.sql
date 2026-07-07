-- ============================================================
-- 대상: 공지사항 / FAQ / 문의하기(+첨부파일) / 알림 / 공지 읽음·찜
-- ============================================================
-- 0. 기존 객체 정리 (없으면 에러 무시하고 통과, 자식 테이블부터 삭제)
-- ============================================================

BEGIN EXECUTE IMMEDIATE 'DROP TABLE inquiry_file_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE inquiry_file_seq';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'DROP TABLE inquiry_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE inquiry_seq';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/

-- 답변 전용 테이블 (04번 원본에만 있던 것, 안 씀)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE inquiry_answer_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_inquiry_answer';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_inquiry'; 
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'DROP TABLE faq_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_faq';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'DROP TABLE notification_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_notification';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'DROP TABLE alert_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_alert';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE notice_bookmark_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_notice_bookmark';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/

-- notice_read_tb/notice_favorite_tb를 먼저 지워야 notice_tb를 지울 때 FK 걸림 없음
BEGIN EXECUTE IMMEDIATE 'DROP TABLE notice_favorite_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE notice_read_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'DROP TABLE notice_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_notice';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/


-- ============================================================
-- 1. 공지사항 (notice_tb) 
-- ============================================================

CREATE TABLE notice_tb (
    notice_id   NUMBER          NOT NULL,
    admin_id    VARCHAR2(20)    NOT NULL,
    title       VARCHAR2(200)   NOT NULL,
    content     CLOB            NOT NULL,
    pinned_yn   CHAR(1)         DEFAULT 'N' NOT NULL,
    created_at  DATE            DEFAULT SYSDATE NOT NULL,
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


-- ============================================================
-- 2. 문의하기 (inquiry_tb) — 지금까지 만든 코드 기준으로 확정
--    InquiryVO / InquiryRepository / InquiryService / InquiryController 전부 이 스키마 기준
-- ============================================================

CREATE SEQUENCE inquiry_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE;

CREATE TABLE inquiry_tb (
    inquiry_id      NUMBER          NOT NULL,
    id              VARCHAR2(50)    NOT NULL,   -- member_tb.id 참조 (작성자)
    category        VARCHAR2(30)    NOT NULL,   -- 계정·로그인 / 지출관리 / OTT관리 / 캘린더 / 공지·알림 / 결제·정산 / 기타
    inquiry_type    VARCHAR2(30)    NOT NULL,   -- 오류/버그 신고 / 기능 개선 제안 / 사용 방법 문의 / 기타 문의
    title           VARCHAR2(50 CHAR)   NOT NULL,
    content         VARCHAR2(1000 CHAR) NOT NULL,
    status          VARCHAR2(10)    DEFAULT 'WAIT' NOT NULL,   -- WAIT / DONE / REVIEW
    reg_date        DATE            DEFAULT SYSDATE NOT NULL,
    reply_content   VARCHAR2(1000 CHAR),
    reply_date      DATE,

    CONSTRAINT pk_inquiry_tb PRIMARY KEY (inquiry_id),
    CONSTRAINT fk_inquiry_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT ck_inquiry_status CHECK (status IN ('WAIT','DONE','REVIEW'))
);

-- 첨부파일 (InquiryFileVO / InquiryFileRepository / FileStorageService 연동)
CREATE SEQUENCE inquiry_file_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE inquiry_file_tb (
    file_id         NUMBER          NOT NULL,
    inquiry_id      NUMBER          NOT NULL,
    origin_name     VARCHAR2(200)   NOT NULL,  -- 업로드 당시 원본 파일명
    saved_name      VARCHAR2(200)   NOT NULL,  -- 서버에 저장된 파일명 (UUID + 확장자)
    file_path       VARCHAR2(500)   NOT NULL,  -- 서버 디스크 실제 경로
    file_size       NUMBER,
    reg_date        DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_inquiry_file_tb PRIMARY KEY (file_id),
    CONSTRAINT fk_inquiry_file_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry_tb(inquiry_id) ON DELETE CASCADE
);


-- ============================================================
-- 3. FAQ (faq_tb) 
-- ============================================================

CREATE SEQUENCE seq_faq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE faq_tb (
    faq_id      NUMBER          NOT NULL,
    category    VARCHAR2(50)    NOT NULL,   -- faqList.jsp 카테고리 버튼과 맞춰서 값 사용 (account/expense/ott/notice/etc 등)
    question    VARCHAR2(300)   NOT NULL,
    answer      CLOB            NOT NULL,
    sort_order  NUMBER          DEFAULT 0 NOT NULL,  -- 관리자가 지정하는 노출 순서
    use_yn      CHAR(1)         DEFAULT 'Y' NOT NULL, -- 노출 여부 (관리자가 숨김 처리 가능)
    created_at  DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_faq PRIMARY KEY (faq_id),
    CONSTRAINT ck_faq_use CHECK (use_yn IN ('Y', 'N'))
);

CREATE OR REPLACE TRIGGER trg_faq_bi
BEFORE INSERT ON faq_tb
FOR EACH ROW
WHEN (NEW.faq_id IS NULL)
BEGIN
    SELECT seq_faq.NEXTVAL INTO :NEW.faq_id FROM dual;
END;
/


-- ============================================================
-- 4. 알림 (notification_tb)
-- ============================================================

CREATE SEQUENCE seq_notification START WITH 1 INCREMENT BY 1;

CREATE TABLE notification_tb (
    notification_id    NUMBER          NOT NULL,
    id                  VARCHAR2(50)    NOT NULL,

    notification_type   VARCHAR2(20)    NOT NULL,  -- HOME / PERSONAL / OTT
    title               VARCHAR2(200)   NOT NULL,
    message             VARCHAR2(1000)  NOT NULL,
    link_url            VARCHAR2(500),

    read_yn             CHAR(1)         DEFAULT 'N' NOT NULL,
    star_yn             CHAR(1)         DEFAULT 'N' NOT NULL,
    created_at          DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notification PRIMARY KEY (notification_id),
    CONSTRAINT fk_notification_member_id FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT chk_notification_type CHECK (notification_type IN ('HOME', 'PERSONAL', 'OTT')),
    CONSTRAINT chk_notification_read CHECK (read_yn IN ('Y', 'N')),
    CONSTRAINT chk_notification_star CHECK (star_yn IN ('Y', 'N'))
);

CREATE OR REPLACE TRIGGER trg_notification_bi
BEFORE INSERT ON notification_tb
FOR EACH ROW
BEGIN
    IF :NEW.notification_id IS NULL THEN
        SELECT seq_notification.NEXTVAL INTO :NEW.notification_id FROM dual;
    END IF;
END;
/

CREATE INDEX idx_notification_id_read ON notification_tb(id, read_yn, created_at DESC);
CREATE INDEX idx_notification_id_type ON notification_tb(id, notification_type, created_at DESC);


-- ============================================================
-- 5. 공지 읽음 / 찜 (notice_tb는 1번 섹션에서 이미 생성됨)
-- ============================================================

CREATE TABLE notice_read_tb (
    notice_id   NUMBER          NOT NULL,
    id          VARCHAR2(50)    NOT NULL,
    read_at     DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notice_read PRIMARY KEY (notice_id, id),
    CONSTRAINT fk_notice_read_notice FOREIGN KEY (notice_id) REFERENCES notice_tb(notice_id),
    CONSTRAINT fk_notice_read_member FOREIGN KEY (id) REFERENCES member_tb(id)
);

CREATE TABLE notice_favorite_tb (
    notice_id   NUMBER          NOT NULL,
    id          VARCHAR2(50)    NOT NULL,
    created_at  DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notice_favorite PRIMARY KEY (notice_id, id),
    CONSTRAINT fk_notice_favorite_notice FOREIGN KEY (notice_id) REFERENCES notice_tb(notice_id),
    CONSTRAINT fk_notice_favorite_member FOREIGN KEY (id) REFERENCES member_tb(id)
);


-- ============================================================
-- 6. 샘플 데이터 (문의하기) — member_tb에 등록된 첫 번째 회원 id로 삽입
-- ============================================================

DECLARE
    v_id member_tb.id%TYPE;
BEGIN
    SELECT id INTO v_id FROM member_tb WHERE ROWNUM = 1;

    INSERT INTO inquiry_tb (inquiry_id, id, category, inquiry_type, title, content, status, reg_date, reply_content, reply_date)
    VALUES (inquiry_seq.NEXTVAL, v_id, '지출관리', '오류/버그 신고',
            '고정지출 등록 후 캘린더에 반영이 안 되어요',
            '고정지출을 새로 등록했는데 캘린더 뷰에서 해당 날짜에 표시가 되지 않습니다. 확인 부탁드립니다.',
            'DONE', TO_DATE('2026.06.20','YYYY.MM.DD'),
            '확인 결과 고정지출 등록 시 캘린더 이벤트 생성 로직에 누락이 있었습니다. 다음 배포에서 수정될 예정입니다. 불편을 드려 죄송합니다.',
            TO_DATE('2026.06.21','YYYY.MM.DD'));

    INSERT INTO inquiry_tb (inquiry_id, id, category, inquiry_type, title, content, status, reg_date)
    VALUES (inquiry_seq.NEXTVAL, v_id, 'OTT관리', '기능 개선 제안',
            'OTT 방장 양도 기능이 필요합니다',
            '방장이 탈퇴하거나 비활성 상태가 될 경우 멤버 중 한 명에게 방장 권한을 넘길 수 있으면 좋겠습니다.',
            'REVIEW', TO_DATE('2026.06.24','YYYY.MM.DD'));

    INSERT INTO inquiry_tb (inquiry_id, id, category, inquiry_type, title, content, status, reg_date)
    VALUES (inquiry_seq.NEXTVAL, v_id, '계정·로그인', '오류/버그 신고',
            '카카오 로그인 연동 후 알림 수신이 안 됩니다',
            '카카오 소셜 로그인으로 전환 후 벨 알림이 전혀 오지 않습니다. 이메일 기반 계정이었을 때는 잘 왔었는데 원인을 알고 싶습니다.',
            'WAIT', TO_DATE('2026.06.27','YYYY.MM.DD'));

    INSERT INTO inquiry_tb (inquiry_id, id, category, inquiry_type, title, content, status, reg_date)
    VALUES (inquiry_seq.NEXTVAL, v_id, '결제·정산', '사용 방법 문의',
            '정산 요청을 취소하고 싶어요',
            '실수로 정산 요청을 보냈는데 취소하는 방법을 모르겠습니다. 상대방이 아직 승인 전인데 취소가 가능한가요?',
            'WAIT', TO_DATE('2026.07.01','YYYY.MM.DD'));

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('member_tb에 회원 데이터가 없어 샘플 문의 데이터는 넣지 않았습니다. 회원가입 후 다시 실행해 주세요.');
END;
/
