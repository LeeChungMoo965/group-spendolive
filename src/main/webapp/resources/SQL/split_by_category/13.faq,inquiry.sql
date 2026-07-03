-- ============================================================
-- 문의하기(inquiry_tb) 테이블 생성 + 샘플 데이터
-- 파일명 규칙: 기존 스키마 파일 번호 이어서 예) 07_inquiry.sql
--   (팀에서 쓰는 numbered schema 파일 순서에 맞춰 이름만 바꿔서 실행)
-- ============================================================

-- 1) 기존 객체 안전하게 삭제 (없으면 에러 무시하고 통과)
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE inquiry_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN -- ORA-00942: table or view does not exist
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE inquiry_seq';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2289 THEN -- ORA-02289: sequence does not exist
            RAISE;
        END IF;
END;
/

-- 2) 시퀀스 생성
CREATE SEQUENCE inquiry_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE;

-- 3) 테이블 생성
CREATE TABLE inquiry_tb (
    inquiry_id      NUMBER          NOT NULL,
    id              VARCHAR2(50)    NOT NULL,   -- member_tb.id 참조 (작성자)
    category        VARCHAR2(30)    NOT NULL,   -- 계정·로그인 / 지출관리 / OTT관리 / 캘린더 / 공지·알림 / 결제·정산 / 기타
    inquiry_type    VARCHAR2(30)    NOT NULL,   -- 오류/버그 신고 / 기능 개선 제안 / 사용 방법 문의 / 기타 문의
    title           VARCHAR2(50)    NOT NULL,
    content         VARCHAR2(1000)  NOT NULL,
    status          VARCHAR2(10)    DEFAULT 'WAIT' NOT NULL,
    reg_date        DATE            DEFAULT SYSDATE NOT NULL,
    reply_content   VARCHAR2(1000),
    reply_date      DATE,
    CONSTRAINT pk_inquiry_tb PRIMARY KEY (inquiry_id),
    CONSTRAINT fk_inquiry_member FOREIGN KEY (id) REFERENCES member_tb(id),
    CONSTRAINT ck_inquiry_status CHECK (status IN ('WAIT','DONE','REVIEW'))
);

-- 4) 첨부파일 (선택 · 지금 컨트롤러에서는 저장 로직 미구현 상태, 스키마만 미리 준비)
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE inquiry_file_tb CASCADE CONSTRAINTS PURGE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE inquiry_file_seq';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2289 THEN
            RAISE;
        END IF;
END;
/

CREATE SEQUENCE inquiry_file_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE inquiry_file_tb (
    file_id         NUMBER          NOT NULL,
    inquiry_id      NUMBER          NOT NULL,
    origin_name     VARCHAR2(200)   NOT NULL,  -- 업로드 당시 원본 파일명
    saved_name      VARCHAR2(200)   NOT NULL,  -- 서버에 저장된 파일명(UUID 등)
    file_path       VARCHAR2(500)   NOT NULL,
    file_size       NUMBER,
    reg_date        DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_inquiry_file_tb PRIMARY KEY (file_id),
    CONSTRAINT fk_inquiry_file_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry_tb(inquiry_id) ON DELETE CASCADE
);



ALTER TABLE inquiry_tb MODIFY (title VARCHAR2(50 CHAR));
ALTER TABLE inquiry_tb MODIFY (content VARCHAR2(1000 CHAR));
ALTER TABLE inquiry_tb MODIFY (reply_content VARCHAR2(1000 CHAR));




-- 5) 샘플 데이터 삽입 (member_tb에 등록된 첫 번째 회원 id를 그대로 사용)
--    member_tb에 데이터가 하나도 없으면 그냥 건너뜀 (FK 위반 방지)
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
