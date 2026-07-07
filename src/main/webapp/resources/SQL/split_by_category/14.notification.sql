/* =========================================================
   1. 알림 테이블
   ========================================================= */

DROP TABLE notification_tb CASCADE CONSTRAINTS;
DROP SEQUENCE seq_notification;

CREATE TABLE notification_tb (
    notification_id NUMBER NOT NULL,
    id VARCHAR2(50) NOT NULL,

    notification_type VARCHAR2(20) NOT NULL,
    title VARCHAR2(200) NOT NULL,
    message VARCHAR2(1000) NOT NULL,
    link_url VARCHAR2(500),

    read_yn CHAR(1) DEFAULT 'N' NOT NULL,
    star_yn CHAR(1) DEFAULT 'N' NOT NULL,
    created_at DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notification PRIMARY KEY (notification_id),

    CONSTRAINT fk_notification_member_id
        FOREIGN KEY (id)
        REFERENCES member_tb(id),

    CONSTRAINT chk_notification_type
        CHECK (notification_type IN ('HOME', 'PERSONAL', 'OTT')),

    CONSTRAINT chk_notification_read
        CHECK (read_yn IN ('Y', 'N')),

    CONSTRAINT chk_notification_star
        CHECK (star_yn IN ('Y', 'N'))
);

CREATE SEQUENCE seq_notification
START WITH 1
INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_notification_bi
BEFORE INSERT ON notification_tb
FOR EACH ROW
BEGIN
    IF :NEW.notification_id IS NULL THEN
        SELECT seq_notification.NEXTVAL
        INTO :NEW.notification_id
        FROM dual;
    END IF;
END;
/

CREATE INDEX idx_notification_id_read
ON notification_tb(id, read_yn, created_at DESC);

CREATE INDEX idx_notification_id_type
ON notification_tb(id, notification_type, created_at DESC);



/* =========================================================
   2. 공지 읽음 저장용 
   ========================================================= */


   CREATE TABLE notice_read_tb (
    notice_id NUMBER NOT NULL,
    id VARCHAR2(50) NOT NULL,
    read_at DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notice_read
        PRIMARY KEY (notice_id, id),

    CONSTRAINT fk_notice_read_notice
        FOREIGN KEY (notice_id)
        REFERENCES notice_tb(notice_id),

    CONSTRAINT fk_notice_read_member
        FOREIGN KEY (id)
        REFERENCES member_tb(id)
);


/* =========================================================
   3. 공지 찜
   ========================================================= */


   CREATE TABLE notice_favorite_tb (
    notice_id NUMBER NOT NULL,
    id VARCHAR2(50) NOT NULL,
    created_at DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_notice_favorite
        PRIMARY KEY (notice_id, id),

    CONSTRAINT fk_notice_favorite_notice
        FOREIGN KEY (notice_id)
        REFERENCES notice_tb(notice_id),

    CONSTRAINT fk_notice_favorite_member
        FOREIGN KEY (id)
        REFERENCES member_tb(id)
);