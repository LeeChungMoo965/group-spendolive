/*
  13_patch_ott_room_mode.sql
  실행 안내:
  - 이미 03_ott_schema.sql을 실행해서 ott_room_tb가 만들어져 있는 경우에만 실행하세요.
  - 이번 수정부터 가족/지인 공유방과 외부 모집글을 DB에서 room_mode로 구분합니다.
  - 새로 DB를 초기화해서 03_ott_schema.sql을 다시 실행하는 경우에는 이 패치가 필요 없습니다.

  수정 내용:
  - Oracle ALTER TABLE ADD 문법 오류 방지: ADD (room_mode ...)
  - PL/SQL 내부 UPDATE 문자열 따옴표 오류 수정
*/

/* 1. room_mode 컬럼 추가 */
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_tab_cols
     WHERE table_name = 'OTT_ROOM_TB'
       AND column_name = 'ROOM_MODE';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ott_room_tb ADD (room_mode VARCHAR2(20) DEFAULT ''RECRUIT'' NOT NULL)';
    END IF;
END;
/

/*
  2. 기존 데이터 1차 분류
  - 기존 데이터에는 가족/지인방과 외부 모집글을 구분하는 컬럼이 없었기 때문에 100% 자동 구분은 불가능합니다.
  - 보통 외부 모집글은 RECRUITING / REPLACE_RECRUITING 상태이므로 RECRUIT로 분류합니다.
  - 나머지는 가족/지인 공유방으로 1차 분류합니다.
*/
UPDATE ott_room_tb
   SET room_mode = CASE
                       WHEN status IN ('RECRUITING', 'REPLACE_RECRUITING') THEN 'RECRUIT'
                       ELSE 'FRIEND'
                   END
 WHERE room_mode IS NULL;

/*
  필요 시 직접 보정하세요.
  예시: 외부 모집글인데 ACTIVE로 전환되어 가족/지인방으로 잡힌 경우
*/
-- UPDATE ott_room_tb SET room_mode = 'RECRUIT' WHERE room_id = 원하는방번호;
-- UPDATE ott_room_tb SET room_mode = 'FRIEND' WHERE room_id = 원하는방번호;

/* 3. room_mode 값 보정 */
UPDATE ott_room_tb
   SET room_mode = 'RECRUIT'
 WHERE room_mode IS NULL
    OR room_mode NOT IN ('FRIEND', 'RECRUIT');

/* 4. CHECK 제약조건 추가 */
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_constraints
     WHERE table_name = 'OTT_ROOM_TB'
       AND constraint_name = 'CK_OTT_ROOM_MODE';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ott_room_tb ADD CONSTRAINT ck_ott_room_mode CHECK (room_mode IN (''FRIEND'', ''RECRUIT''))';
    END IF;
END;
/

/* 5. 조회 성능용 인덱스 추가 */
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_indexes
     WHERE index_name = 'IDX_OTT_ROOM_MODE';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX idx_ott_room_mode ON ott_room_tb(room_mode, status, created_at)';
    END IF;
END;
/

COMMIT;
