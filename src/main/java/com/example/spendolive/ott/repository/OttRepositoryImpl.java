package com.example.spendolive.ott.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttChatRoomDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttServiceDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;

// 사용자 OTT DB 처리 - JdbcTemplate으로 방, 정산, 채팅 기능 수행
@Repository
public class OttRepositoryImpl implements OttRepository {

    private final JdbcTemplate jdbcTemplate;

    public OttRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================
    // 1. OTT 서비스/요금제 기준 조회
    // =========================================================

    @Override
    public List<OttServiceDTO> selectShareableServices() {
        String sql = """
                SELECT ott_service_id,
                       service_name,
                       default_price,
                       share_yn,
                       risk_level,
                       block_reason,
                       fixed_plan_name,
                       base_price,
                       extra_member_fee,
                       extra_member_count,
                       max_member_limit,
                       platform_fee_rate
                FROM ott_service_tb
                WHERE share_yn = 'Y'
                ORDER BY service_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapOttService(rs));
    }

    @Override
    public OttServiceDTO selectOttServiceRule(Long ott_service_id) {
        if (ott_service_id == null) {
            return null;
        }

        String sql = """
                SELECT ott_service_id,
                       service_name,
                       default_price,
                       share_yn,
                       risk_level,
                       block_reason,
                       fixed_plan_name,
                       base_price,
                       extra_member_fee,
                       extra_member_count,
                       max_member_limit,
                       platform_fee_rate
                FROM ott_service_tb
                WHERE ott_service_id = ?
                  AND share_yn = 'Y'
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapOttService(rs), ott_service_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // =========================================================
    // 2. 공유방/모집방 조회
    // =========================================================

    @Override
    public List<OttRoomDTO> selectRecruitRooms(String loginId) {
        return selectRecruitRooms(loginId, null, null);
    }

    @Override
    public List<OttRoomDTO> selectRecruitRooms(String loginId, Long ott_service_id, String roomNameKeyword) {
        String sql = """
                SELECT r.room_id,
                       r.host_login_id,
                       NVL(m.nickname, r.host_login_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.plan_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       NVL(r.room_mode, 'RECRUIT') AS room_mode,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.close_requested_at, 'YYYY-MM-DD') AS close_requested_at,
                       TO_CHAR(r.close_effective_date, 'YYYY-MM-DD') AS close_effective_date,
                       r.close_reason,
                       r.close_notice,
                       TO_CHAR(r.closed_at, 'YYYY-MM-DD') AS closed_at,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL(COUNT(CASE WHEN rm.status = 'ACTIVE' THEN 1 END), 0) AS current_member_count,
                       NVL((
                            SELECT MAX(mine.status)
                            FROM ott_room_member_tb mine
                            WHERE mine.room_id = r.room_id
                              AND mine.member_login_id = ?
                       ), 'NONE') AS my_application_status
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_login_id = m.id
                LEFT JOIN ott_room_member_tb rm ON r.room_id = rm.room_id
                WHERE NVL(r.room_mode, 'RECRUIT') = 'RECRUIT'
                  AND r.status <> 'CLOSED'
                  AND (? IS NULL OR r.ott_service_id = ?)
                  AND (? IS NULL OR LOWER(r.room_name) LIKE '%' || LOWER(?) || '%')
                GROUP BY r.room_id,
                         r.host_login_id,
                         NVL(m.nickname, r.host_login_id),
                         r.ott_service_id,
                         s.service_name,
                         r.room_name,
                         r.plan_name,
                         r.total_price,
                         r.billing_day,
                         r.member_limit,
                         NVL(r.room_mode, 'RECRUIT'),
                         r.status,
                         r.invite_code,
                         TO_CHAR(r.close_requested_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.close_effective_date, 'YYYY-MM-DD'),
                         r.close_reason,
                         r.close_notice,
                         TO_CHAR(r.closed_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.created_at, 'YYYY-MM-DD')
                ORDER BY r.room_id DESC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> mapRoom(rs, true),
                loginId,
                ott_service_id,
                ott_service_id,
                roomNameKeyword,
                roomNameKeyword);
    }

    // 빠른 참가에서 실제로 roomId를 찾는 SQL이다.
    @Override
    public Long selectOldestAvailableRecruitRoomId(Long ott_service_id, String loginId) {
        /*
        * 빠른 참가에서 실제로 roomId를 찾는 SQL이다.
        *
        * 일반 신청하기는 JSP에서 roomId가 바로 넘어오지만,
        * 빠른 참가는 ottServiceId만 넘어오기 때문에
        * 여기서 조건에 맞는 roomId를 직접 찾아야 한다.
        */

        // 1. 값이 없으면 조회할 수 없으므로 null 반환
        if (ott_service_id == null || loginId == null || loginId.isBlank()) {
            return null;
        }

        /*
        * SQL 설명:
        *
        * FROM ott_room_tb r
        * - 모집방 정보가 들어있는 테이블에서 찾는다.
        *
        * NVL(r.room_mode, 'RECRUIT') = 'RECRUIT'
        * - 가족방이 아니라 외부인 모집방만 찾는다.
        *
        * r.ott_service_id = ?
        * - 사용자가 선택한 OTT 종류와 같은 방만 찾는다.
        *
        * r.status IN ('RECRUITING', 'REPLACE_RECRUITING')
        * - 현재 모집중인 방만 찾는다.
        *
        * r.host_login_id <> ?
        * - 내가 만든 방에는 빠른 참가하지 못하게 한다.
        *
        * NOT EXISTS (...)
        * - 내가 이미 ACTIVE 상태로 참여 중인 방은 제외한다.
        *
        * ACTIVE 인원 수 < r.member_limit
        * - 아직 자리가 남아있는 방만 찾는다.
        *
        * ORDER BY r.created_at ASC, r.room_id ASC
        * - 가장 오래된 방부터 채우기 위해 생성일 오래된 순으로 정렬한다.
        * - 생성일이 같으면 room_id가 낮은 방을 먼저 선택한다.
        *
        * WHERE ROWNUM = 1
        * - 조건에 맞는 방 중 가장 첫 번째 방 하나만 가져온다.
        */
        String sql = """
                SELECT room_id
                FROM (
                    SELECT r.room_id
                    FROM ott_room_tb r
                    WHERE NVL(r.room_mode, 'RECRUIT') = 'RECRUIT'
                    AND r.ott_service_id = ?
                    AND r.status IN ('RECRUITING', 'REPLACE_RECRUITING')
                    AND r.host_login_id <> ?
                    AND NOT EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb mine
                            WHERE mine.room_id = r.room_id
                            AND mine.member_login_id = ?
                            AND mine.status = 'ACTIVE'
                    )
                    AND (
                            SELECT COUNT(*)
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                            AND rm.status = 'ACTIVE'
                    ) < r.member_limit
                    ORDER BY r.created_at ASC, r.room_id ASC
                )
                WHERE ROWNUM = 1
                """;

        try {
            /*
            * 파라미터 순서:
            * 1. ott_service_id → 선택한 OTT
            * 2. loginId      → 내가 만든 방 제외
            * 3. loginId      → 내가 이미 참여한 방 제외
            */
            return jdbcTemplate.queryForObject(sql, Long.class, ott_service_id, loginId, loginId);
        } catch (EmptyResultDataAccessException e) {
            /*
            * 조건에 맞는 방이 하나도 없으면 queryForObject가 예외를 던진다.
            * 이 경우 빠른 참가 실패로 보고 null을 반환한다.
            */
            return null;
        }
    }

    @Override
    public List<OttRoomDTO> selectFriendRooms(String loginId) {
        return selectMyRoomsByMode(loginId, "FRIEND");
    }

    @Override
    public List<OttRoomDTO> selectHostedFriendRooms(String loginId) {
        return selectHostedRoomsByMode(loginId, "FRIEND");
    }

    @Override
    public List<OttRoomDTO> selectHostedRecruitRooms(String loginId) {
        return selectHostedRoomsByMode(loginId, "RECRUIT");
    }

    @Override
    public List<OttRoomDTO> selectJoinedRecruitRooms(String loginId) {
        return selectJoinedRoomsByMode(loginId, "RECRUIT");
    }

    @Override
    public List<OttRoomDTO> selectMyRooms(String loginId) {
        return selectMyRoomsByMode(loginId, null);
    }

    @Override
    public List<OttRoomDTO> selectHostedRooms(String loginId) {
        return selectHostedRoomsByMode(loginId, null);
    }


    private List<OttRoomDTO> selectMyRoomsByMode(String loginId, String room_mode) {
        String sql = """
                SELECT r.room_id,
                       r.host_login_id,
                       NVL(m.nickname, r.host_login_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.plan_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       NVL(r.room_mode, 'RECRUIT') AS room_mode,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.close_requested_at, 'YYYY-MM-DD') AS close_requested_at,
                       TO_CHAR(r.close_effective_date, 'YYYY-MM-DD') AS close_effective_date,
                       r.close_reason,
                       r.close_notice,
                       TO_CHAR(r.closed_at, 'YYYY-MM-DD') AS closed_at,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL(COUNT(CASE WHEN rm_all.status = 'ACTIVE' THEN 1 END), 0) AS current_member_count,
                       NVL(MAX(rm_mine.leave_reserved_yn), 'N') AS leave_reserved_yn,
                       TO_CHAR(MAX(rm_mine.leave_requested_at), 'YYYY-MM-DD') AS leave_requested_at,
                       TO_CHAR(MAX(rm_mine.leave_scheduled_date), 'YYYY-MM-DD') AS leave_scheduled_date,
                       TO_CHAR(MAX(rm_mine.leave_cancelled_at), 'YYYY-MM-DD') AS leave_cancelled_at,
                       MAX(rm_mine.leave_reason) AS leave_reason
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_login_id = m.id
                LEFT JOIN ott_room_member_tb rm_all ON r.room_id = rm_all.room_id
                LEFT JOIN ott_room_member_tb rm_mine ON r.room_id = rm_mine.room_id AND rm_mine.member_login_id = ?
                WHERE r.status <> 'CLOSED'
                  AND (? IS NULL OR NVL(r.room_mode, 'RECRUIT') = ?)
                  AND (
                       r.host_login_id = ?
                       OR EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb mine
                            WHERE mine.room_id = r.room_id
                              AND mine.member_login_id = ?
                              AND mine.status = 'ACTIVE'
                       )
                  )
                GROUP BY r.room_id,
                         r.host_login_id,
                         NVL(m.nickname, r.host_login_id),
                         r.ott_service_id,
                         s.service_name,
                         r.room_name,
                         r.plan_name,
                         r.total_price,
                         r.billing_day,
                         r.member_limit,
                         NVL(r.room_mode, 'RECRUIT'),
                         r.status,
                         r.invite_code,
                         TO_CHAR(r.close_requested_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.close_effective_date, 'YYYY-MM-DD'),
                         r.close_reason,
                         r.close_notice,
                         TO_CHAR(r.closed_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.created_at, 'YYYY-MM-DD')
                ORDER BY r.room_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, false), loginId, room_mode, room_mode, loginId, loginId);
    }

    private List<OttRoomDTO> selectHostedRoomsByMode(String loginId, String room_mode) {
        String sql = """
                SELECT r.room_id,
                       r.host_login_id,
                       NVL(m.nickname, r.host_login_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.plan_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       NVL(r.room_mode, 'RECRUIT') AS room_mode,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.close_requested_at, 'YYYY-MM-DD') AS close_requested_at,
                       TO_CHAR(r.close_effective_date, 'YYYY-MM-DD') AS close_effective_date,
                       r.close_reason,
                       r.close_notice,
                       TO_CHAR(r.closed_at, 'YYYY-MM-DD') AS closed_at,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL(COUNT(CASE WHEN rm.status = 'ACTIVE' THEN 1 END), 0) AS current_member_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_login_id = m.id
                LEFT JOIN ott_room_member_tb rm ON r.room_id = rm.room_id
                WHERE r.host_login_id = ?
                  AND r.status <> 'CLOSED'
                  AND (? IS NULL OR NVL(r.room_mode, 'RECRUIT') = ?)
                GROUP BY r.room_id,
                         r.host_login_id,
                         NVL(m.nickname, r.host_login_id),
                         r.ott_service_id,
                         s.service_name,
                         r.room_name,
                         r.plan_name,
                         r.total_price,
                         r.billing_day,
                         r.member_limit,
                         NVL(r.room_mode, 'RECRUIT'),
                         r.status,
                         r.invite_code,
                         TO_CHAR(r.close_requested_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.close_effective_date, 'YYYY-MM-DD'),
                         r.close_reason,
                         r.close_notice,
                         TO_CHAR(r.closed_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.created_at, 'YYYY-MM-DD')
                ORDER BY r.room_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, false), loginId, room_mode, room_mode);
    }

    private List<OttRoomDTO> selectJoinedRoomsByMode(String loginId, String room_mode) {
        String sql = """
                SELECT r.room_id,
                       r.host_login_id,
                       NVL(m.nickname, r.host_login_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.plan_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       NVL(r.room_mode, 'RECRUIT') AS room_mode,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.close_requested_at, 'YYYY-MM-DD') AS close_requested_at,
                       TO_CHAR(r.close_effective_date, 'YYYY-MM-DD') AS close_effective_date,
                       r.close_reason,
                       r.close_notice,
                       TO_CHAR(r.closed_at, 'YYYY-MM-DD') AS closed_at,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL(COUNT(CASE WHEN rm_all.status = 'ACTIVE' THEN 1 END), 0) AS current_member_count,
                       mine.status AS my_application_status,
                       NVL(mine.leave_reserved_yn, 'N') AS leave_reserved_yn,
                       TO_CHAR(mine.leave_requested_at, 'YYYY-MM-DD') AS leave_requested_at,
                       TO_CHAR(mine.leave_scheduled_date, 'YYYY-MM-DD') AS leave_scheduled_date,
                       TO_CHAR(mine.leave_cancelled_at, 'YYYY-MM-DD') AS leave_cancelled_at,
                       mine.leave_reason
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                JOIN ott_room_member_tb mine
                  ON mine.room_id = r.room_id
                 AND mine.member_login_id = ?
                 AND mine.member_role = 'MEMBER'
                 AND mine.status = 'ACTIVE'
                LEFT JOIN member_tb m ON r.host_login_id = m.id
                LEFT JOIN ott_room_member_tb rm_all ON r.room_id = rm_all.room_id
                WHERE r.status <> 'CLOSED'
                  AND r.host_login_id <> ?
                  AND (? IS NULL OR NVL(r.room_mode, 'RECRUIT') = ?)
                GROUP BY r.room_id,
                         r.host_login_id,
                         NVL(m.nickname, r.host_login_id),
                         r.ott_service_id,
                         s.service_name,
                         r.room_name,
                         r.plan_name,
                         r.total_price,
                         r.billing_day,
                         r.member_limit,
                         NVL(r.room_mode, 'RECRUIT'),
                         r.status,
                         r.invite_code,
                         TO_CHAR(r.close_requested_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.close_effective_date, 'YYYY-MM-DD'),
                         r.close_reason,
                         r.close_notice,
                         TO_CHAR(r.closed_at, 'YYYY-MM-DD'),
                         TO_CHAR(r.created_at, 'YYYY-MM-DD'),
                         mine.status,
                         NVL(mine.leave_reserved_yn, 'N'),
                         TO_CHAR(mine.leave_requested_at, 'YYYY-MM-DD'),
                         TO_CHAR(mine.leave_scheduled_date, 'YYYY-MM-DD'),
                         TO_CHAR(mine.leave_cancelled_at, 'YYYY-MM-DD'),
                         mine.leave_reason
                ORDER BY r.room_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, true), loginId, loginId, room_mode, room_mode);
    }

    // =========================================================
    // 3. 참여자 조회
    // =========================================================

    @Override
    public List<OttRoomMemberDTO> selectHostedRoomMembers(String loginId) {
        String sql = """
                SELECT rm.room_member_id,
                       rm.room_id,
                       r.room_name,
                       s.service_name,
                       rm.member_login_id AS member_login_id,
                       NVL(m.nickname, rm.member_login_id) AS member_nickname,
                       NVL(m.member_name, rm.member_login_id) AS member_name,
                       rm.member_role,
                       rm.share_amount,
                       rm.fee_rate,
                       rm.fee_amount,
                       rm.pay_amount,
                       TO_CHAR(rm.joined_at, 'YYYY-MM-DD') AS joined_at,
                       rm.status,
                       NVL(rm.leave_reserved_yn, 'N') AS leave_reserved_yn,
                       TO_CHAR(rm.leave_requested_at, 'YYYY-MM-DD') AS leave_requested_at,
                       TO_CHAR(rm.leave_scheduled_date, 'YYYY-MM-DD') AS leave_scheduled_date,
                       TO_CHAR(rm.leave_cancelled_at, 'YYYY-MM-DD') AS leave_cancelled_at,
                       rm.leave_reason
                FROM ott_room_member_tb rm
                JOIN ott_room_tb r ON rm.room_id = r.room_id
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON rm.member_login_id = m.id
                WHERE r.host_login_id = ?
                  AND NVL(r.room_mode, 'RECRUIT') = 'RECRUIT'
                  AND rm.member_role = 'MEMBER'
                  AND rm.status = 'ACTIVE'
                  AND r.status <> 'CLOSED'
                ORDER BY r.room_id DESC,
                         rm.joined_at DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoomMember(rs), loginId);
    }

    // =========================================================
    // 4. 정산/결제 조회
    // =========================================================

    @Override
    public List<OttSettlementDTO> selectMySettlements(String loginId) {
        return selectMySettlements(loginId, null);
    }

    @Override
    public List<OttSettlementDTO> selectMySettlements(String loginId, String room_mode) {
        String sql = """
            SELECT st.settlement_id,
                   st.room_id,
                   r.room_name,
                   s.service_name,
                   st.settlement_month,
                   st.total_price,
                   st.total_fee,
                   st.total_pay_amount,
                   TO_CHAR(st.due_date, 'YYYY-MM-DD') AS due_date,
                   TO_CHAR(st.payment_start_date, 'YYYY-MM-DD') AS payment_start_date,
                   TO_CHAR(st.payment_close_date, 'YYYY-MM-DD') AS payment_close_date,
                   TO_CHAR(st.service_start_date, 'YYYY-MM-DD') AS service_start_date,
                   TO_CHAR(st.service_end_date, 'YYYY-MM-DD') AS service_end_date,
                   TO_CHAR(st.replace_start_date, 'YYYY-MM-DD') AS replace_start_date,
                   TO_CHAR(st.replace_end_date, 'YYYY-MM-DD') AS replace_end_date,
                   st.status,
                   TO_CHAR(st.created_at, 'YYYY-MM-DD') AS created_at,
                   CASE WHEN r.host_login_id = ? THEN 'HOST' ELSE 'MEMBER' END AS my_role,
                   sp.payment_id AS my_payment_id,
                   sp.payment_status AS my_payment_status,
                   sp.total_amount AS my_total_amount
            FROM settlement_tb st
            JOIN ott_room_tb r ON st.room_id = r.room_id
            JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
            LEFT JOIN settlement_payment_tb sp
                   ON st.settlement_id = sp.settlement_id
                  AND sp.id = ?
            WHERE (? IS NULL OR NVL(r.room_mode, 'RECRUIT') = ?)
              AND (
                   r.host_login_id = ?
                   OR EXISTS (
                        SELECT 1
                        FROM ott_room_member_tb rm
                        WHERE rm.room_id = r.room_id
                          AND rm.member_login_id = ?
                   )
              )
            ORDER BY st.settlement_month DESC, st.settlement_id DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapMySettlement(rs), loginId, loginId, room_mode, room_mode, loginId, loginId);
    }

    @Override
    public List<OttSettlementDTO> selectHostedSettlementPayments(String loginId, String room_mode) {
        String sql = """
                SELECT st.settlement_id,
                       st.room_id,
                       r.room_name,
                       s.service_name,
                       st.settlement_month,
                       st.status,
                       TO_CHAR(st.payment_start_date, 'YYYY-MM-DD') AS payment_start_date,
                       TO_CHAR(st.payment_close_date, 'YYYY-MM-DD') AS payment_close_date,
                       TO_CHAR(st.service_start_date, 'YYYY-MM-DD') AS service_start_date,
                       TO_CHAR(st.service_end_date, 'YYYY-MM-DD') AS service_end_date,
                       sp.payment_id,
                       sp.id AS member_login_id,
                       NVL(m.member_name, sp.id) AS member_name,
                       NVL(m.nickname, sp.id) AS member_nickname,
                       sp.base_amount,
                       sp.fee_amount,
                       sp.total_amount,
                       sp.payment_status,
                       TO_CHAR(sp.paid_at, 'YYYY-MM-DD') AS paid_at
                FROM settlement_tb st
                JOIN ott_room_tb r ON st.room_id = r.room_id
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                JOIN settlement_payment_tb sp ON st.settlement_id = sp.settlement_id
                LEFT JOIN member_tb m ON sp.id = m.id
                WHERE r.host_login_id = ?
                  AND (? IS NULL OR NVL(r.room_mode, 'RECRUIT') = ?)
                ORDER BY st.settlement_month DESC,
                         st.settlement_id DESC,
                         r.room_id DESC,
                         sp.payment_status,
                         sp.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OttSettlementDTO settlement = new OttSettlementDTO();
            settlement.setSettlement_id(rs.getLong("settlement_id"));
            settlement.setRoom_id(rs.getLong("room_id"));
            settlement.setRoom_name(rs.getString("room_name"));
            settlement.setService_name(rs.getString("service_name"));
            settlement.setSettlement_month(rs.getString("settlement_month"));
            settlement.setStatus(rs.getString("status"));
            settlement.setPayment_start_date(rs.getString("payment_start_date"));
            settlement.setPayment_close_date(rs.getString("payment_close_date"));
            settlement.setService_start_date(rs.getString("service_start_date"));
            settlement.setService_end_date(rs.getString("service_end_date"));
            settlement.setPayment_id(getNullableLong(rs, "payment_id"));
            settlement.setMember_login_id(rs.getString("member_login_id"));
            settlement.setMember_name(rs.getString("member_name"));
            settlement.setMember_nickname(rs.getString("member_nickname"));
            settlement.setBase_amount(rs.getInt("base_amount"));
            settlement.setFee_amount(rs.getInt("fee_amount"));
            settlement.setTotal_amount(rs.getInt("total_amount"));
            settlement.setPayment_status(rs.getString("payment_status"));
            settlement.setPaid_at(rs.getString("paid_at"));
            return settlement;
        }, loginId, room_mode, room_mode);
    }

    private OttSettlementDTO mapMySettlement(java.sql.ResultSet rs) throws java.sql.SQLException {
        OttSettlementDTO settlement = new OttSettlementDTO();
        settlement.setSettlement_id(rs.getLong("settlement_id"));
        settlement.setRoom_id(rs.getLong("room_id"));
        settlement.setRoom_name(rs.getString("room_name"));
        settlement.setService_name(rs.getString("service_name"));
        settlement.setSettlement_month(rs.getString("settlement_month"));
        settlement.setTotal_price(rs.getInt("total_price"));
        settlement.setTotal_fee(rs.getInt("total_fee"));
        settlement.setTotal_pay_amount(rs.getInt("total_pay_amount"));
        settlement.setDue_date(rs.getString("due_date"));
        settlement.setPayment_start_date(rs.getString("payment_start_date"));
        settlement.setPayment_close_date(rs.getString("payment_close_date"));
        settlement.setService_start_date(rs.getString("service_start_date"));
        settlement.setService_end_date(rs.getString("service_end_date"));
        settlement.setReplace_start_date(rs.getString("replace_start_date"));
        settlement.setReplace_end_date(rs.getString("replace_end_date"));
        settlement.setStatus(rs.getString("status"));
        settlement.setCreated_at(rs.getString("created_at"));
        settlement.setMy_role(rs.getString("my_role"));
        settlement.setPayment_id(getNullableLong(rs, "my_payment_id"));
        settlement.setMy_payment_status(rs.getString("my_payment_status"));
        settlement.setMy_total_amount(rs.getInt("my_total_amount"));
        return settlement;
    }

    // =========================================================
    // 5. 채팅 조회
    // =========================================================

    @Override
    public List<OttChatRoomDTO> selectMyChatRooms(String loginId) {
        String sql = """
                SELECT r.room_id,
                       r.room_name,
                       s.service_name,
                       NVL((
                            SELECT MAX(cm.message_content) KEEP (DENSE_RANK LAST ORDER BY cm.created_at, cm.message_id)
                            FROM ott_chat_message_tb cm
                            WHERE cm.room_id = r.room_id
                       ), '아직 메시지가 없습니다.') AS last_message,
                       NVL((
                            SELECT COUNT(*)
                            FROM ott_chat_message_tb cm
                            WHERE cm.room_id = r.room_id
                              AND cm.sender_id <> ?
                              AND cm.created_at > NVL((
                                    SELECT cr.last_read_at
                                    FROM ott_chat_read_tb cr
                                    WHERE cr.room_id = r.room_id
                                      AND cr.member_login_id = ?
                              ), TO_DATE('1900-01-01', 'YYYY-MM-DD'))
                       ), 0) AS unread_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                WHERE r.status <> 'CLOSED'
                  AND (
                       r.host_login_id = ?
                       OR EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.member_login_id = ?
                              AND rm.status = 'ACTIVE'
                       )
                  )
                ORDER BY r.room_id DESC
                FETCH FIRST 8 ROWS ONLY
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OttChatRoomDTO chatRoom = new OttChatRoomDTO();
            chatRoom.setRoom_id(rs.getLong("room_id"));
            chatRoom.setRoom_name(rs.getString("room_name"));
            chatRoom.setService_name(rs.getString("service_name"));
            chatRoom.setUnread_count(rs.getInt("unread_count"));
            chatRoom.setLast_message(rs.getString("last_message"));
            return chatRoom;
        }, loginId, loginId, loginId, loginId);
    }

    @Override
    public List<OttChatMessageDTO> selectChatMessages(Long room_id, String loginId) {
        if (!canUseChatRoom(room_id, loginId)) {
            return Collections.emptyList();
        }

        String sql = """
                SELECT *
                FROM (
                    SELECT latest.*
                    FROM (
                        SELECT cm.message_id,
                               cm.room_id,
                               cm.sender_id,
                               CASE
                                   WHEN cm.message_content LIKE '[SYSTEM] %'
                                        OR cm.message_content LIKE '%공유방에 참여했습니다.%'
                                   THEN '시스템 알림'
                                   ELSE NVL(m.member_name, cm.sender_id)
                               END AS sender_name,
                               CASE
                                   WHEN cm.message_content LIKE '[SYSTEM] %' THEN SUBSTR(cm.message_content, 10)
                                   ELSE cm.message_content
                               END AS message_content,
                               TO_CHAR(cm.created_at, 'YYYY-MM-DD HH24:MI') AS created_at,
                               CASE
                                   WHEN cm.message_content LIKE '[SYSTEM] %'
                                        OR cm.message_content LIKE '%공유방에 참여했습니다.%'
                                   THEN 'N'
                                   WHEN cm.sender_id = ? THEN 'Y'
                                   ELSE 'N'
                               END AS mine_yn,
                               CASE
                                   WHEN cm.message_content LIKE '[SYSTEM] %'
                                        OR cm.message_content LIKE '%공유방에 참여했습니다.%'
                                   THEN 'Y'
                                   ELSE 'N'
                               END AS system_yn
                        FROM ott_chat_message_tb cm
                        LEFT JOIN member_tb m ON cm.sender_id = m.id
                        WHERE cm.room_id = ?
                        ORDER BY cm.created_at DESC, cm.message_id DESC
                    ) latest
                    WHERE ROWNUM <= 100
                )
                ORDER BY message_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OttChatMessageDTO message = new OttChatMessageDTO();
            message.setMessage_id(rs.getLong("message_id"));
            message.setRoom_id(rs.getLong("room_id"));
            message.setSender_id(rs.getString("sender_id"));
            message.setSender_name(rs.getString("sender_name"));
            message.setMessage_content(rs.getString("message_content"));
            message.setCreated_at(rs.getString("created_at"));
            message.setMine_yn(rs.getString("mine_yn"));
            message.setSystem_yn(rs.getString("system_yn"));
            return message;
        }, loginId, room_id);
    }

    @Override
    public OttRoomDTO selectChatRoom(Long room_id, String loginId) {
        if (!canUseChatRoom(room_id, loginId)) {
            return null;
        }

        return selectRoom(room_id);
    }

    @Override
    public int countRecruitRooms() {
        String sql = "SELECT COUNT(*) FROM ott_room_tb WHERE NVL(room_mode, 'RECRUIT') = 'RECRUIT' AND status <> 'CLOSED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public int countMyRooms(String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_room_tb r
                WHERE r.status <> 'CLOSED'
                  AND (
                       r.host_login_id = ?
                       OR EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.member_login_id = ?
                              AND rm.status = 'ACTIVE'
                       )
                  )
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginId, loginId);
        return count == null ? 0 : count;
    }

    @Override
    public int countUnreadChatMessages(String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_chat_message_tb cm
                JOIN ott_room_tb r ON cm.room_id = r.room_id
                WHERE cm.sender_id <> ?
                  AND r.status <> 'CLOSED'
                  AND (
                        r.host_login_id = ?
                        OR EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.member_login_id = ?
                              AND rm.status = 'ACTIVE'
                        )
                  )
                  AND cm.created_at > NVL((
                        SELECT cr.last_read_at
                        FROM ott_chat_read_tb cr
                        WHERE cr.room_id = cm.room_id
                          AND cr.member_login_id = ?
                  ), TO_DATE('1900-01-01', 'YYYY-MM-DD'))
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginId, loginId, loginId, loginId);
        return count == null ? 0 : count;
    }

    // =========================================================
    // 6. 공유방 생성/결제 연결
    // =========================================================

    // 방 생성 처리 - 방 정보 저장 후 생성자를 HOST로 등록
    @Override
    @Transactional
    public Long insertRoom(OttRoomDTO roomDTO, String loginId, String status) {
        Long room_id = jdbcTemplate.queryForObject("SELECT seq_ott_room.NEXTVAL FROM dual", Long.class);
        String invite_code = makeInviteCode();
        String room_name = makeRoomName(roomDTO);
        String plan_name = normalizePlanName(roomDTO.getPlan_name());

        String sql = """
                INSERT INTO ott_room_tb (
                    room_id,
                    host_login_id,
                    ott_service_id,
                    room_name,
                    plan_name,
                    total_price,
                    billing_day,
                    member_limit,
                    room_mode,
                    status,
                    invite_code
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                room_id,
                loginId,
                roomDTO.getOtt_service_id(),
                room_name,
                plan_name,
                roomDTO.getTotal_price(),
                roomDTO.getBilling_day(),
                roomDTO.getMember_limit(),
                normalizeRoomMode(roomDTO.getRoom_mode()),
                status,
                invite_code);

        insertHostMember(room_id, loginId);
        insertSystemChatMessage(room_id, loginId, room_name + " 공유방이 만들어졌습니다.");
        return room_id;
    }

    @Override
    public void insertHostMember(Long room_id, String loginId) {
        Long room_member_id = jdbcTemplate.queryForObject("SELECT seq_ott_room_member.NEXTVAL FROM dual", Long.class);
        String sql = """
                INSERT INTO ott_room_member_tb (
                    room_member_id,
                    room_id,
                    member_login_id,
                    member_role,
                    share_amount,
                    fee_rate,
                    fee_amount,
                    pay_amount,
                    status
                ) VALUES (?, ?, ?, 'HOST', 0, 0, 0, 0, 'ACTIVE')
                """;
        jdbcTemplate.update(sql, room_member_id, room_id, loginId);
    }

    @Override
    @Transactional
    public void applyRoom(Long room_id, String loginId) {
        OttRoomDTO room = selectRoom(room_id);

        if (room == null || loginId == null || loginId.equals(room.getHost_login_id())) {
            return;
        }

        if (!"RECRUIT".equals(room.getRoom_mode())) {
            return;
        }

        if (!("RECRUITING".equals(room.getStatus()) || "REPLACE_RECRUITING".equals(room.getStatus()))) {
            return;
        }

        if (countActiveRoomMembers(room_id) >= room.getMember_limit()) {
            jdbcTemplate.update(
                    "UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ? AND status <> 'CLOSE_REQUESTED'",
                    room_id);
        }

        // 신청/승인 시스템 제거 후에는 APPLIED/REJECTED 데이터를 만들지 않는다.
        // 이 버튼은 결제 담당자가 연결할 결제 화면으로 이동시키는 역할만 한다.
        // 결제 성공 이후 ott_room_member_tb ACTIVE 등록은 결제 콜백 쪽에서 처리한다.
    }

    // 방생성하기 누르면 정산 상태를 'READY' 인상태로 데이터 생성
    // 방장이 팀원이 들어오고 나서 정산하기를 누르면 PAYMENT_OPEN로 update를 하고 settlement_payment_tb를 만드는 방식으로 가야함
    // 초기 정산 생성 - 외부 모집방의 READY 정산 데이터 생성
    @Override
    @Transactional
    public void createReadySettlement(Long room_id, String hostId) {
        OttRoomDTO room = selectRoom(room_id);

        if (room == null || !hostId.equals(room.getHost_login_id())) {
            return;
        }

        YearMonth targetMonth = YearMonth.now().plusMonths(1);
        String targetMonthText = targetMonth.toString();

        if (existsSettlement(room_id, targetMonthText)) {
            return;
        }

        LocalDate service_start_date = resolveBillingDate(targetMonth, room.getBilling_day());
        LocalDate service_end_date = service_start_date.plusMonths(1).minusDays(1);
        LocalDate payment_start_date = LocalDate.now();
        LocalDate payment_close_date = service_start_date.minusDays(7);
        LocalDate replace_start_date = payment_close_date;
        LocalDate replace_end_date = service_start_date.minusDays(1);

        Long settlement_id = jdbcTemplate.queryForObject("SELECT seq_settlement.NEXTVAL FROM dual", Long.class);

        String sql = """
                INSERT INTO settlement_tb (
                    settlement_id,
                    room_id,
                    settlement_month,
                    total_price,
                    total_fee,
                    total_pay_amount,
                    due_date,
                    payment_start_date,
                    payment_close_date,
                    service_start_date,
                    service_end_date,
                    replace_start_date,
                    replace_end_date,
                    status
                ) VALUES (?, ?, ?, ?, 0, 0, ?, ?, ?, ?, ?, ?, ?, 'READY')
                """;

        jdbcTemplate.update(sql,
                settlement_id,
                room_id,
                targetMonthText,
                room.getTotal_price(),
                Date.valueOf(payment_close_date),
                Date.valueOf(payment_start_date),
                Date.valueOf(payment_close_date),
                Date.valueOf(service_start_date),
                Date.valueOf(service_end_date),
                Date.valueOf(replace_start_date),
                Date.valueOf(replace_end_date));
    }


    // =========================================================
    // 7. 정산 요청/결제 처리
    // =========================================================

    // 정산 회차 생성 - 정산 정보와 멤버별 결제 건 생성
    @Override
    @Transactional
    public void createSettlement(Long room_id, String hostId, String settlement_month, String due_date) {
        OttRoomDTO room = selectRoom(room_id);

        if (room == null || !hostId.equals(room.getHost_login_id())) {
            return;
        }

        if ("CLOSE_REQUESTED".equals(room.getStatus()) || "CLOSED".equals(room.getStatus())) {
            return;
        }

        YearMonth targetMonth = parseSettlementMonth(settlement_month);
        String targetMonthText = targetMonth.toString();

        if (existsSettlement(room_id, targetMonthText)) {
            return;
        }

        LocalDate service_start_date = resolveBillingDate(targetMonth, room.getBilling_day());
        LocalDate service_end_date = service_start_date.plusMonths(1).minusDays(1);
        LocalDate payment_start_date = service_start_date.minusMonths(1);
        LocalDate payment_close_date = service_start_date.minusDays(7);
        LocalDate replace_start_date = payment_close_date;
        LocalDate replace_end_date = service_start_date.minusDays(1);

        List<OttRoomMemberDTO> members = selectActiveMembers(room_id);
        int total_price = 0;
        int total_fee = 0;
        int total_pay_amount = 0;

        for (OttRoomMemberDTO member : members) {
            if (!"HOST".equals(member.getMember_role())) {
                total_price += nullToZero(member.getShare_amount());
                total_fee += nullToZero(member.getFee_amount());
                total_pay_amount += nullToZero(member.getPay_amount());
            }
        }

        Long settlement_id = jdbcTemplate.queryForObject("SELECT seq_settlement.NEXTVAL FROM dual", Long.class);
        String settlementSql = """
                INSERT INTO settlement_tb (
                    settlement_id,
                    room_id,
                    settlement_month,
                    total_price,
                    total_fee,
                    total_pay_amount,
                    due_date,
                    payment_start_date,
                    payment_close_date,
                    service_start_date,
                    service_end_date,
                    replace_start_date,
                    replace_end_date,
                    status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PAYMENT_OPEN')
                """;

        jdbcTemplate.update(settlementSql,
                settlement_id,
                room_id,
                targetMonthText,
                total_price,
                total_fee,
                total_pay_amount,
                Date.valueOf(payment_close_date),
                Date.valueOf(payment_start_date),
                Date.valueOf(payment_close_date),
                Date.valueOf(service_start_date),
                Date.valueOf(service_end_date),
                Date.valueOf(replace_start_date),
                Date.valueOf(replace_end_date));

        for (OttRoomMemberDTO member : members) {
            if ("HOST".equals(member.getMember_role())) {
                continue;
            }

            Long payment_id = jdbcTemplate.queryForObject("SELECT seq_settlement_payment.NEXTVAL FROM dual", Long.class);
            String paymentSql = """
                    INSERT INTO settlement_payment_tb (
                        payment_id,
                        settlement_id,
                        id,
                        base_amount,
                        fee_rate,
                        fee_amount,
                        total_amount,
                        payment_status,
                        memo
                    ) VALUES (?, ?, ?, ?, 3, ?, ?, 'UNPAID', ?)
                    """;

            jdbcTemplate.update(paymentSql,
                    payment_id,
                    settlement_id,
                    member.getMember_login_id(),
                    nullToZero(member.getShare_amount()),
                    nullToZero(member.getFee_amount()),
                    nullToZero(member.getPay_amount()),
                    room.getRoom_name() + " " + targetMonthText + " 이용분 정산");

            insertOttNotification(member.getMember_login_id(),
                    "OTT 다음 달 이용분 결제 요청",
                    room.getRoom_name() + " " + targetMonthText + " 이용분 " + nullToZero(member.getPay_amount())
                            + "원을 " + payment_close_date + "까지 결제해 주세요. 마감 후 미결제자는 자동 추방됩니다.",
                    "/spendolive/ott/recruit.do?tab=settlement&room_id=" + room_id);
        }

        jdbcTemplate.update("UPDATE ott_room_tb SET status = 'PAYMENT_OPEN', updated_at = SYSDATE WHERE room_id = ? AND status <> 'CLOSE_REQUESTED'", room_id);
        insertSystemChatMessage(room_id, hostId, targetMonthText + " 이용분 결제가 열렸습니다. 결제 마감일은 " + payment_close_date + "입니다.");
    }

    // 정산 결제 완료 - 본인의 미결제 건만 PAID로 변경
    @Override
    @Transactional
    public void markPaymentPaid(Long payment_id, String loginId) {
        Map<String, Object> data = selectPaymentMap(payment_id);
        if (data == null) {
            return;
        }

        String payerId = (String) data.get("ID");
        if (!loginId.equals(payerId)) {
            return;
        }

        String payment_status = (String) data.get("PAYMENT_STATUS");
        String roomStatus = (String) data.get("ROOM_STATUS");
        if (!"UNPAID".equals(payment_status) || "CLOSE_REQUESTED".equals(roomStatus) || "CLOSED".equals(roomStatus)) {
            return;
        }

        Long room_id = numberToLong(data.get("ROOM_ID"));
        String hostId = (String) data.get("HOST_LOGIN_ID");
        String room_name = (String) data.get("ROOM_NAME");
        String settlement_month = (String) data.get("SETTLEMENT_MONTH");
        Integer total_amount = numberToInt(data.get("TOTAL_AMOUNT"));

        String sql = """
                UPDATE settlement_payment_tb sp
                SET payment_status = 'PAID',
                    paid_at = SYSDATE,
                    memo = NVL(sp.memo, '') || ' / 사용자 결제완료 처리'
                WHERE sp.payment_id = ?
                  AND sp.id = ?
                  AND sp.payment_status = 'UNPAID'
                  AND EXISTS (
                        SELECT 1
                        FROM settlement_tb st
                        JOIN ott_room_tb r ON st.room_id = r.room_id
                        WHERE st.settlement_id = sp.settlement_id
                          AND r.status NOT IN ('CLOSE_REQUESTED', 'CLOSED')
                          AND st.status IN ('PAYMENT_OPEN', 'REQUESTED', 'READY')
                          AND (st.payment_start_date IS NULL OR TRUNC(SYSDATE) >= st.payment_start_date)
                          AND (st.payment_close_date IS NULL OR TRUNC(SYSDATE) <= st.payment_close_date)
                  )
                """;
        int updated = jdbcTemplate.update(sql, payment_id, loginId);

        if (updated > 0) {
            insertOttNotification(hostId,
                    "OTT 결제 완료 알림",
                    loginId + "님이 " + room_name + " " + settlement_month + " 이용분 " + total_amount + "원 결제를 완료했습니다.",
                    "/spendolive/ott/recruit.do?tab=settlement&room_id=" + room_id);
        }
    }

    // 결제 완료 입장 - 사용자를 ACTIVE 멤버로 등록
    @Override
    public void completePaidRoomEntry(Long room_id, String loginId) {
        OttRoomDTO room = selectRoom(room_id);

        if (room == null || loginId == null || loginId.equals(room.getHost_login_id())) {
            return;
        }

        if ("CLOSE_REQUESTED".equals(room.getStatus()) || "CLOSED".equals(room.getStatus())) {
            return;
        }

        String currentStatus = selectRoomMemberStatus(room_id, loginId);
        if ("ACTIVE".equals(currentStatus)) {
            return;
        }

        if (countActiveRoomMembers(room_id) >= room.getMember_limit()) {
            jdbcTemplate.update(
                    "UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ? AND status IN ('RECRUITING', 'REPLACE_RECRUITING')",
                    room_id);
            return;
        }

        int share_amount = safeDivide(room.getTotal_price(), room.getMember_limit());
        double fee_rate = 3.0;
        int fee_amount = calculateFeeAmount(share_amount, fee_rate);
        int pay_amount = share_amount + fee_amount;

        if (currentStatus == null) {
            Long room_member_id = jdbcTemplate.queryForObject("SELECT seq_ott_room_member.NEXTVAL FROM dual", Long.class);
            String insertSql = """
                    INSERT INTO ott_room_member_tb (
                        room_member_id,
                        room_id,
                        member_login_id,
                        member_role,
                        share_amount,
                        fee_rate,
                        fee_amount,
                        pay_amount,
                        status,
                        pay_day
                    ) VALUES (?, ?, ?, 'MEMBER', ?, ?, ?, ?, 'ACTIVE', ?)
                    """;
            LocalDate today = LocalDate.now();
            int day = today.getDayOfMonth();

            jdbcTemplate.update(insertSql, room_member_id, room_id, loginId, share_amount, fee_rate, fee_amount, pay_amount,day);
        } else {
            String updateSql = """
                    UPDATE ott_room_member_tb
                    SET member_role = 'MEMBER',
                        share_amount = ?,
                        fee_rate = ?,
                        fee_amount = ?,
                        pay_amount = ?,
                        status = 'ACTIVE',
                        joined_at = SYSDATE,
                        kicked_at = NULL,
                        kicked_reason = NULL,
                        left_at = NULL
                    WHERE room_id = ?
                      AND member_login_id = ?
                    """;
            jdbcTemplate.update(updateSql, share_amount, fee_rate, fee_amount, pay_amount, room_id, loginId);
        }

        if (countActiveRoomMembers(room_id) >= room.getMember_limit()) {
            jdbcTemplate.update(
                    "UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ? AND status IN ('RECRUITING', 'REPLACE_RECRUITING')",
                    room_id);
        }

        String member_name = selectMemberDisplayName(loginId);
        insertSystemChatMessage(room_id, loginId, member_name + "님이 결제를 완료하고 공유방에 입장했습니다.");
        notifyActiveRoomMembers(room_id,
                "OTT 공유방 입장 알림",
                member_name + "님이 " + room.getRoom_name() + " 공유방에 입장했습니다.",
                "/spendolive/ott/chat/room.do?room_id=" + room_id,
                loginId);
    }

    // =========================================================
    // 8. 방 삭제 요청/환불 처리
    // =========================================================

    // 방 종료 예약 - 종료일 저장 및 이후 결제·정산 취소
    @Override
    @Transactional
    public void requestRoomClose(Long room_id, String hostId, String close_notice, String close_reason) {
        OttRoomDTO room = selectRoom(room_id);

        if (room == null || !hostId.equals(room.getHost_login_id())) {
            return;
        }

        if ("CLOSED".equals(room.getStatus()) || "CLOSE_REQUESTED".equals(room.getStatus())) {
            return;
        }

        LocalDate close_effective_date = getNextBillingDate(LocalDate.now(), room.getBilling_day());
        String notice = normalizeCloseNotice(close_notice);
        String reason = normalizeCloseReason(close_reason);

        String updateRoomSql = """
                UPDATE ott_room_tb
                SET status = 'CLOSE_REQUESTED',
                    close_requested_at = SYSDATE,
                    close_effective_date = ?,
                    close_reason = ?,
                    close_notice = ?,
                    updated_at = SYSDATE
                WHERE room_id = ?
                  AND host_login_id = ?
                """;
        jdbcTemplate.update(updateRoomSql, Date.valueOf(close_effective_date), reason, notice, room_id, hostId);


        insertRefundsForRoomClose(room_id, close_effective_date);
        cancelUnpaidFuturePayments(room_id, close_effective_date);
        markFutureSettlementsCancelled(room_id, close_effective_date);

        String message = "파티장이 방 삭제를 요청했습니다. 기존 참여자는 " + close_effective_date.minusDays(1)
                + "까지 이용할 수 있으며, 이미 결제된 다음 이용분은 자동 환불 처리됩니다.";
        insertSystemChatMessage(room_id, hostId, message);
        notifyActiveRoomMembers(room_id,
                "OTT 공유방 종료 예정",
                room.getRoom_name() + " 공유방이 " + close_effective_date + "에 종료될 예정입니다. " + notice,
                "/spendolive/ott/chat/room.do?room_id=" + room_id,
                hostId);
    }


    // 탈퇴 예약 검증 - 결제 상태에 따라 예약 가능일 계산
    @Override
    @Transactional
    public String reserveRoomLeave(Long room_id, String loginId) {
        if (room_id == null || loginId == null || loginId.isBlank()) {
            return "나가기 예약을 처리할 수 없습니다.";
        }

        OttRoomDTO room = selectRoom(room_id);
        if (room == null || "CLOSED".equals(room.getStatus()) || "CLOSE_REQUESTED".equals(room.getStatus())) {
            return "이미 종료되었거나 종료 예정인 방입니다.";
        }

        if (loginId.equals(room.getHost_login_id())) {
            return "파티장은 나가기 예약을 할 수 없습니다. 방 삭제 요청 기능을 사용해 주세요.";
        }

        if (!isActiveNormalMember(room_id, loginId)) {
            return "현재 참여 중인 일반 참여자만 나가기 예약을 할 수 있습니다.";
        }

        if (hasReservedLeave(room_id, loginId)) {
            return "이미 나가기 예약이 되어 있습니다.";
        }

        LocalDate today = LocalDate.now();
        LocalDate nextBillingDate = getNextBillingDate(today, room.getBilling_day());
        LocalDate leave_scheduled_date = nextBillingDate.minusDays(7);

        // 선택 A: 결제일 7일 전부터는 이번 회차 나가기 예약 불가
        if (!today.isBefore(leave_scheduled_date)) {
            return "이미 다음 결제 준비 기간이라 이번 회차 나가기 예약은 불가능합니다. 다음 결제일 이후 다시 예약할 수 있습니다.";
        }

        if (hasPaidUpcomingPayment(room_id, loginId, leave_scheduled_date)) {
            return "이미 다음 이용분 결제가 완료되어 이번 회차 나가기 예약은 불가능합니다.";
        }

        String sql = """
                UPDATE ott_room_member_tb
                SET leave_reserved_yn = 'Y',
                    leave_requested_at = SYSDATE,
                    leave_scheduled_date = ?,
                    leave_cancelled_at = NULL,
                    leave_reason = '다음 결제일 전 나가기 예약'
                WHERE room_id = ?
                  AND member_login_id = ?
                  AND member_role = 'MEMBER'
                  AND status = 'ACTIVE'
                """;
        int updated = jdbcTemplate.update(sql, Date.valueOf(leave_scheduled_date), room_id, loginId);

        if (updated == 0) {
            return "나가기 예약을 처리하지 못했습니다.";
        }

        insertSystemChatMessage(room_id, loginId, loginId + "님이 " + leave_scheduled_date + " 나가기 예약을 했습니다.");
        insertOttNotification(room.getHost_login_id(),
                "OTT 참여자 나가기 예약",
                loginId + "님이 " + room.getRoom_name() + " 방에서 " + leave_scheduled_date + " 나가기 예약을 했습니다.",
                "/spendolive/ott/chat/room.do?room_id=" + room_id);

        return "나가기 예약이 완료되었습니다. " + leave_scheduled_date + "에 자동으로 방에서 나가집니다.";
    }

    // 활성 상태로 남아 있는 일반 멤버의 나가기 예약만 취소
    @Override
    @Transactional
    public String cancelRoomLeave(Long room_id, String loginId) {
        if (room_id == null || loginId == null || loginId.isBlank()) {
            return "나가기 예약 취소를 처리할 수 없습니다.";
        }

        OttRoomDTO room = selectRoom(room_id);
        if (room == null || "CLOSED".equals(room.getStatus())) {
            return "이미 종료된 방입니다.";
        }

        if (loginId.equals(room.getHost_login_id())) {
            return "파티장은 나가기 예약 취소 대상이 아닙니다.";
        }

        String sql = """
                UPDATE ott_room_member_tb
                SET leave_reserved_yn = 'N',
                    leave_cancelled_at = SYSDATE,
                    leave_reason = NVL(leave_reason, '') || ' / 예약 취소'
                WHERE room_id = ?
                  AND member_login_id = ?
                  AND member_role = 'MEMBER'
                  AND status = 'ACTIVE'
                  AND leave_reserved_yn = 'Y'
                """;
        int updated = jdbcTemplate.update(sql, room_id, loginId);

        if (updated == 0) {
            return "취소할 나가기 예약이 없습니다.";
        }

        insertSystemChatMessage(room_id, loginId, loginId + "님이 나가기 예약을 취소했습니다.");
        return "나가기 예약이 취소되었습니다.";
    }

    // 9. 예약 작업 및 채팅 상태

    // 예약 작업 처리 - 탈퇴, 미결제 만료, 방 종료 순서로 실행
    @Override
    @Transactional
    public void processScheduledOttJobs() {
        processLeaveReservations();
        expireOverduePayments();
        closeEffectiveRooms();
    }

    // 채팅 메시지 저장 - 참여 권한 확인 후 일반 메시지로 저장
    @Override
    public void insertChatMessage(Long room_id, String sender_id, String message_content) {
        if (!canUseChatRoom(room_id, sender_id)) {
            return;
        }

        // 일반 사용자가 [SYSTEM] 접두어로 시스템 알림처럼 위장하지 못하도록 제거한다.
        if (message_content != null && message_content.trim().startsWith("[SYSTEM]")) {
            message_content = message_content.trim().replaceFirst("^\\[SYSTEM\\]\\s*", "").trim();
        }

        if (message_content == null || message_content.isBlank()) {
            return;
        }

        insertChatMessageInternal(room_id, sender_id, message_content);
    }

    // 채팅 읽음 처리 - 마지막 읽은 시각 저장 또는 갱신
    @Override
    public void markChatRoomAsRead(Long room_id, String loginId) {
        if (!canUseChatRoom(room_id, loginId)) {
            return;
        }

        String sql = """
                MERGE INTO ott_chat_read_tb cr
                USING (
                    SELECT ? AS room_id,
                           ? AS member_login_id
                    FROM dual
                ) src
                ON (cr.room_id = src.room_id AND cr.member_login_id = src.member_login_id)
                WHEN MATCHED THEN UPDATE SET cr.last_read_at = SYSDATE
                WHEN NOT MATCHED THEN INSERT (room_id, member_login_id, last_read_at)
                VALUES (src.room_id, src.member_login_id, SYSDATE)
                """;
        jdbcTemplate.update(sql, room_id, loginId);
    }


    // 예약 탈퇴 처리 - 멤버 OUT 처리 후 방 상태 갱신
    private void processLeaveReservations() {
        String cancelUnpaidPaymentSql = """
                UPDATE settlement_payment_tb sp
                SET payment_status = 'CANCELLED',
                    cancelled_at = SYSDATE,
                    memo = NVL(sp.memo, '') || ' / 나가기 예약으로 다음 이용분 결제 제외'
                WHERE sp.payment_status = 'UNPAID'
                  AND EXISTS (
                        SELECT 1
                        FROM settlement_tb st
                        JOIN ott_room_member_tb rm ON rm.room_id = st.room_id
                        WHERE st.settlement_id = sp.settlement_id
                          AND st.room_id = rm.room_id
                          AND sp.id = rm.member_login_id
                          AND rm.member_role = 'MEMBER'
                          AND rm.status = 'ACTIVE'
                          AND rm.leave_reserved_yn = 'Y'
                          AND rm.leave_scheduled_date <= TRUNC(SYSDATE)
                          AND st.service_start_date >= TRUNC(SYSDATE)
                  )
                """;
        jdbcTemplate.update(cancelUnpaidPaymentSql);

        String updateRecruitRoomSql = """
                UPDATE ott_room_tb r
                SET status = 'REPLACE_RECRUITING',
                    updated_at = SYSDATE
                WHERE NVL(r.room_mode, 'RECRUIT') = 'RECRUIT'
                  AND r.status NOT IN ('CLOSED', 'CLOSE_REQUESTED')
                  AND EXISTS (
                        SELECT 1
                        FROM ott_room_member_tb rm
                        WHERE rm.room_id = r.room_id
                          AND rm.member_role = 'MEMBER'
                          AND rm.status = 'ACTIVE'
                          AND rm.leave_reserved_yn = 'Y'
                          AND rm.leave_scheduled_date <= TRUNC(SYSDATE)
                  )
                """;
        jdbcTemplate.update(updateRecruitRoomSql);

        String leaveMemberSql = """
                UPDATE ott_room_member_tb rm
                SET status = 'OUT',
                    left_at = SYSDATE,
                    leave_reserved_yn = 'N',
                    leave_reason = NVL(rm.leave_reason, '') || ' / 예약일 자동 퇴장 완료'
                WHERE rm.member_role = 'MEMBER'
                  AND rm.status = 'ACTIVE'
                  AND rm.leave_reserved_yn = 'Y'
                  AND rm.leave_scheduled_date <= TRUNC(SYSDATE)
                """;
        jdbcTemplate.update(leaveMemberSql);
    }

    // 미결제 만료 처리 - UNPAID 결제 만료 후 자동 퇴장
    private void expireOverduePayments() {
        String expirePaymentsSql = """
                UPDATE settlement_payment_tb sp
                SET payment_status = 'EXPIRED',
                    expired_at = SYSDATE,
                    memo = NVL(sp.memo, '') || ' / 결제 마감일 초과'
                WHERE sp.payment_status = 'UNPAID'
                  AND EXISTS (
                        SELECT 1
                        FROM settlement_tb st
                        WHERE st.settlement_id = sp.settlement_id
                          AND st.status IN ('PAYMENT_OPEN', 'REQUESTED', 'READY')
                          AND st.payment_close_date < TRUNC(SYSDATE)
                  )
                """;
        jdbcTemplate.update(expirePaymentsSql);

        String kickMembersSql = """
                UPDATE ott_room_member_tb rm
                SET status = 'KICKED',
                    kicked_at = SYSDATE,
                    kicked_reason = '다음 이용분 결제 마감일까지 결제하지 않아 자동 추방되었습니다.'
                WHERE rm.member_role = 'MEMBER'
                AND rm.status = 'ACTIVE'
                AND EXISTS (
                        SELECT 1
                        FROM settlement_payment_tb sp
                        JOIN settlement_tb st ON sp.settlement_id = st.settlement_id
                        WHERE st.room_id = rm.room_id
                        AND sp.id = rm.member_login_id
                        AND sp.payment_status = 'EXPIRED'
                        AND st.payment_close_date < TRUNC(SYSDATE)
                )
                """;
        jdbcTemplate.update(kickMembersSql);

        String updateSettlementSql = """
                UPDATE settlement_tb st
                SET status = 'REPLACE_RECRUITING'
                WHERE st.status IN ('PAYMENT_OPEN', 'REQUESTED', 'READY')
                  AND st.payment_close_date < TRUNC(SYSDATE)
                  AND EXISTS (
                        SELECT 1
                        FROM settlement_payment_tb sp
                        WHERE sp.settlement_id = st.settlement_id
                          AND sp.payment_status = 'EXPIRED'
                  )
                """;
        jdbcTemplate.update(updateSettlementSql);

        String updateRoomSql = """
                UPDATE ott_room_tb r
                SET status = 'REPLACE_RECRUITING',
                    updated_at = SYSDATE
                WHERE r.status IN ('ACTIVE', 'PAYMENT_OPEN', 'RECRUITING')
                  AND EXISTS (
                        SELECT 1
                        FROM settlement_tb st
                        WHERE st.room_id = r.room_id
                          AND st.status = 'REPLACE_RECRUITING'
                          AND st.replace_end_date >= TRUNC(SYSDATE)
                  )
                """;
        jdbcTemplate.update(updateRoomSql);
    }

    // 종료 예정일이 도래한 방의 ACTIVE 멤버와 방 상태를 최종 종료 처리
    private void closeEffectiveRooms() {
        String closeMemberSql = """
                UPDATE ott_room_member_tb rm
                SET status = 'OUT',
                    left_at = SYSDATE
                WHERE rm.status = 'ACTIVE'
                  AND EXISTS (
                        SELECT 1
                        FROM ott_room_tb r
                        WHERE r.room_id = rm.room_id
                          AND r.status = 'CLOSE_REQUESTED'
                          AND r.close_effective_date <= TRUNC(SYSDATE)
                  )
                """;
        jdbcTemplate.update(closeMemberSql);

        String closeRoomSql = """
                UPDATE ott_room_tb
                SET status = 'CLOSED',
                    closed_at = SYSDATE,
                    updated_at = SYSDATE
                WHERE status = 'CLOSE_REQUESTED'
                  AND close_effective_date <= TRUNC(SYSDATE)
                """;
        jdbcTemplate.update(closeRoomSql);
    }

    // 방 종료일 이후 이용분의 결제 완료 건을 환불 이력으로 남기고 REFUNDED 처리
    private void insertRefundsForRoomClose(Long room_id, LocalDate close_effective_date) {
        String targetMonth = YearMonth.from(close_effective_date).toString();
        String insertRefundSql = """
                INSERT INTO settlement_refund_tb (
                    refund_id,
                    payment_id,
                    settlement_id,
                    room_id,
                    member_login_id,
                    refund_amount,
                    refund_reason,
                    refund_status,
                    completed_at,
                    memo
                )
                SELECT seq_settlement_refund.NEXTVAL,
                       sp.payment_id,
                       st.settlement_id,
                       st.room_id,
                       sp.id AS member_login_id,
                       sp.total_amount,
                       'ROOM_CLOSE',
                       'COMPLETED',
                       SYSDATE,
                       '방 삭제 요청으로 자동 환불 처리'
                FROM settlement_payment_tb sp
                JOIN settlement_tb st ON sp.settlement_id = st.settlement_id
                WHERE st.room_id = ?
                  AND (
                        st.service_start_date >= ?
                        OR (st.service_start_date IS NULL AND st.settlement_month >= ?)
                  )
                  AND sp.payment_status IN ('PAID', 'CONFIRMED')
                  AND NOT EXISTS (
                        SELECT 1
                        FROM settlement_refund_tb rf
                        WHERE rf.payment_id = sp.payment_id
                  )
                """;
        jdbcTemplate.update(insertRefundSql, room_id, Date.valueOf(close_effective_date), targetMonth);

        String updatePaymentSql = """
                UPDATE settlement_payment_tb sp
                SET payment_status = 'REFUNDED',
                    cancelled_at = SYSDATE,
                    memo = NVL(sp.memo, '') || ' / 방 삭제 요청으로 자동 환불 완료'
                WHERE sp.payment_status IN ('PAID', 'CONFIRMED')
                  AND EXISTS (
                        SELECT 1
                        FROM settlement_tb st
                        WHERE st.settlement_id = sp.settlement_id
                          AND st.room_id = ?
                          AND (
                                st.service_start_date >= ?
                                OR (st.service_start_date IS NULL AND st.settlement_month >= ?)
                          )
                  )
                """;
        jdbcTemplate.update(updatePaymentSql, room_id, Date.valueOf(close_effective_date), targetMonth);
    }

    // 방 종료일 이후의 미결제 건을 더 이상 결제되지 않도록 취소
    private void cancelUnpaidFuturePayments(Long room_id, LocalDate close_effective_date) {
        String targetMonth = YearMonth.from(close_effective_date).toString();
        String sql = """
                UPDATE settlement_payment_tb sp
                SET payment_status = 'CANCELLED',
                    cancelled_at = SYSDATE,
                    memo = NVL(sp.memo, '') || ' / 방 삭제 요청으로 결제 취소'
                WHERE sp.payment_status = 'UNPAID'
                  AND EXISTS (
                        SELECT 1
                        FROM settlement_tb st
                        WHERE st.settlement_id = sp.settlement_id
                          AND st.room_id = ?
                          AND (
                                st.service_start_date >= ?
                                OR (st.service_start_date IS NULL AND st.settlement_month >= ?)
                          )
                  )
                """;
        jdbcTemplate.update(sql, room_id, Date.valueOf(close_effective_date), targetMonth);
    }

    // 방 종료일 이후에 해당하는 미래 정산 회차를 CANCELLED로 변경
    private void markFutureSettlementsCancelled(Long room_id, LocalDate close_effective_date) {
        String targetMonth = YearMonth.from(close_effective_date).toString();
        String sql = """
                UPDATE settlement_tb
                SET status = 'CANCELLED',
                    closed_at = SYSDATE
                WHERE room_id = ?
                  AND status IN ('PAYMENT_OPEN', 'REQUESTED', 'READY', 'REPLACE_RECRUITING')
                  AND (
                        service_start_date >= ?
                        OR (service_start_date IS NULL AND settlement_month >= ?)
                  )
                """;
        jdbcTemplate.update(sql, room_id, Date.valueOf(close_effective_date), targetMonth);
    }

    // 특정 사용자를 제외한 ACTIVE 멤버 전체에게 동일한 OTT 알림 생성
    private void notifyActiveRoomMembers(Long room_id, String title, String message, String link_url, String except_member_login_id) {
        String sql = """
                SELECT member_login_id
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND status = 'ACTIVE'
                """;
        List<String> members = jdbcTemplate.queryForList(sql, String.class, room_id);
        for (String member_login_id : members) {
            if (except_member_login_id != null && except_member_login_id.equals(member_login_id)) {
                continue;
            }
            insertOttNotification(member_login_id, title, message, link_url);
        }
    }

    // =========================================================
    // 10. RowMapper / 내부 유틸
    // =========================================================

    private OttServiceDTO mapOttService(java.sql.ResultSet rs) throws java.sql.SQLException {
        OttServiceDTO service = new OttServiceDTO();
        service.setOtt_service_id(rs.getLong("ott_service_id"));
        service.setService_name(rs.getString("service_name"));
        service.setDefault_price(rs.getInt("default_price"));
        service.setShare_yn(rs.getString("share_yn"));
        service.setRisk_level(rs.getString("risk_level"));
        service.setBlock_reason(rs.getString("block_reason"));
        service.setFixed_plan_name(rs.getString("fixed_plan_name"));
        service.setBase_price(rs.getInt("base_price"));
        service.setExtra_member_fee(rs.getInt("extra_member_fee"));
        service.setExtra_member_count(rs.getInt("extra_member_count"));
        service.setMax_member_limit(rs.getInt("max_member_limit"));
        service.setPlatform_fee_rate(rs.getDouble("platform_fee_rate"));

        int share_amount = safeDivide(service.getDefault_price(), service.getMax_member_limit());
        int fee_amount = calculateFeeAmount(share_amount, service.getPlatform_fee_rate());
        service.setShare_amount(share_amount);
        service.setFee_amount(fee_amount);
        service.setPer_person_amount(share_amount + fee_amount);
        return service;
    }

    private OttRoomDTO mapRoom(java.sql.ResultSet rs, boolean hasMyStatus) throws java.sql.SQLException {
        OttRoomDTO room = new OttRoomDTO();
        room.setRoom_id(rs.getLong("room_id"));
        room.setHost_login_id(rs.getString("host_login_id"));
        room.setHost_nickname(rs.getString("host_nickname"));
        room.setOtt_service_id(rs.getLong("ott_service_id"));
        room.setService_name(rs.getString("service_name"));
        room.setRoom_name(rs.getString("room_name"));
        room.setPlan_name(rs.getString("plan_name"));
        room.setTotal_price(rs.getInt("total_price"));
        room.setBilling_day(rs.getInt("billing_day"));
        room.setMember_limit(rs.getInt("member_limit"));
        room.setRoom_mode(rs.getString("room_mode"));
        room.setCurrent_member_count(rs.getInt("current_member_count"));
        room.setStatus(rs.getString("status"));
        room.setInvite_code(rs.getString("invite_code"));
        room.setClose_requested_at(rs.getString("close_requested_at"));
        room.setClose_effective_date(rs.getString("close_effective_date"));
        room.setClose_reason(rs.getString("close_reason"));
        room.setClose_notice(rs.getString("close_notice"));
        room.setClosed_at(rs.getString("closed_at"));
        room.setCreated_at(rs.getString("created_at"));
        int share_amount = safeDivide(room.getTotal_price(), room.getMember_limit());
        int fee_amount = calculateFeeAmount(share_amount, 3.0);
        room.setShare_amount(share_amount);
        room.setPlatform_fee_rate(3.0);
        room.setFee_amount(fee_amount);
        room.setPer_person_amount(share_amount + fee_amount);
        if (hasMyStatus) {
            room.setMy_application_status(rs.getString("my_application_status"));
        }
        room.setLeave_reserved_yn(getOptionalString(rs, "leave_reserved_yn"));
        room.setLeave_requested_at(getOptionalString(rs, "leave_requested_at"));
        room.setLeave_scheduled_date(getOptionalString(rs, "leave_scheduled_date"));
        room.setLeave_cancelled_at(getOptionalString(rs, "leave_cancelled_at"));
        room.setLeave_reason(getOptionalString(rs, "leave_reason"));
        return room;
    }

    private OttRoomMemberDTO mapRoomMember(java.sql.ResultSet rs) throws java.sql.SQLException {
        OttRoomMemberDTO member = new OttRoomMemberDTO();
        member.setRoom_member_id(rs.getLong("room_member_id"));
        member.setRoom_id(rs.getLong("room_id"));
        member.setRoom_name(rs.getString("room_name"));
        member.setService_name(rs.getString("service_name"));
        member.setMember_login_id(rs.getString("member_login_id"));
        member.setMember_nickname(rs.getString("member_nickname"));
        member.setMember_name(rs.getString("member_name"));
        member.setMember_role(rs.getString("member_role"));
        member.setShare_amount(rs.getInt("share_amount"));
        member.setFee_rate(rs.getDouble("fee_rate"));
        member.setFee_amount(rs.getInt("fee_amount"));
        member.setPay_amount(rs.getInt("pay_amount"));
        member.setJoined_at(rs.getString("joined_at"));
        member.setStatus(rs.getString("status"));
        member.setLeave_reserved_yn(getOptionalString(rs, "leave_reserved_yn"));
        member.setLeave_requested_at(getOptionalString(rs, "leave_requested_at"));
        member.setLeave_scheduled_date(getOptionalString(rs, "leave_scheduled_date"));
        member.setLeave_cancelled_at(getOptionalString(rs, "leave_cancelled_at"));
        member.setLeave_reason(getOptionalString(rs, "leave_reason"));
        return member;
    }

    @Override
    public OttRoomDTO selectRoomByInviteCode(String invite_code) {
        if (invite_code == null || invite_code.isBlank()) {
            return null;
        }

        String sql = """
                SELECT r.room_id,
                       r.host_login_id,
                       NVL(m.nickname, r.host_login_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.plan_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       NVL(r.room_mode, 'RECRUIT') AS room_mode,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.close_requested_at, 'YYYY-MM-DD') AS close_requested_at,
                       TO_CHAR(r.close_effective_date, 'YYYY-MM-DD') AS close_effective_date,
                       r.close_reason,
                       r.close_notice,
                       TO_CHAR(r.closed_at, 'YYYY-MM-DD') AS closed_at,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL((
                            SELECT COUNT(*)
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.status = 'ACTIVE'
                       ), 0) AS current_member_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_login_id = m.id
                WHERE r.invite_code = ?
                  AND r.status <> 'CLOSED'
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRoom(rs, false), invite_code.trim().toUpperCase());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private OttRoomDTO selectRoom(Long room_id) {
        String sql = """
                SELECT r.room_id,
                       r.host_login_id,
                       NVL(m.nickname, r.host_login_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.plan_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       NVL(r.room_mode, 'RECRUIT') AS room_mode,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.close_requested_at, 'YYYY-MM-DD') AS close_requested_at,
                       TO_CHAR(r.close_effective_date, 'YYYY-MM-DD') AS close_effective_date,
                       r.close_reason,
                       r.close_notice,
                       TO_CHAR(r.closed_at, 'YYYY-MM-DD') AS closed_at,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL((
                            SELECT COUNT(*)
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.status = 'ACTIVE'
                       ), 0) AS current_member_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_login_id = m.id
                WHERE r.room_id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRoom(rs, false), room_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private List<OttRoomMemberDTO> selectActiveMembers(Long room_id) {
        String sql = """
                SELECT rm.room_member_id,
                       rm.room_id,
                       r.room_name,
                       s.service_name,
                       rm.member_login_id AS member_login_id,
                       NVL(m.nickname, rm.member_login_id) AS member_nickname,
                       NVL(m.member_name, rm.member_login_id) AS member_name,
                       rm.member_role,
                       rm.share_amount,
                       rm.fee_rate,
                       rm.fee_amount,
                       rm.pay_amount,
                       TO_CHAR(rm.joined_at, 'YYYY-MM-DD') AS joined_at,
                       rm.status,
                       NVL(rm.leave_reserved_yn, 'N') AS leave_reserved_yn,
                       TO_CHAR(rm.leave_requested_at, 'YYYY-MM-DD') AS leave_requested_at,
                       TO_CHAR(rm.leave_scheduled_date, 'YYYY-MM-DD') AS leave_scheduled_date,
                       TO_CHAR(rm.leave_cancelled_at, 'YYYY-MM-DD') AS leave_cancelled_at,
                       rm.leave_reason
                FROM ott_room_member_tb rm
                JOIN ott_room_tb r ON rm.room_id = r.room_id
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON rm.member_login_id = m.id
                WHERE rm.room_id = ?
                  AND rm.status = 'ACTIVE'
                ORDER BY rm.member_role DESC, rm.joined_at
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoomMember(rs), room_id);
    }

    private boolean canUseChatRoom(Long room_id, String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_room_tb r
                WHERE r.room_id = ?
                  AND r.status <> 'CLOSED'
                  AND (
                        r.host_login_id = ?
                        OR EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.member_login_id = ?
                              AND rm.status = 'ACTIVE'
                        )
                  )
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, room_id, loginId, loginId);
        return count != null && count > 0;
    }

    private String selectRoomMemberStatus(Long room_id, String loginId) {
        String sql = """
                SELECT status
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND member_login_id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, String.class, room_id, loginId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private int countActiveRoomMembers(Long room_id) {
        String sql = "SELECT COUNT(*) FROM ott_room_member_tb WHERE room_id = ? AND status = 'ACTIVE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, room_id);
        return count == null ? 0 : count;
    }

    private boolean existsSettlement(Long room_id, String settlement_month) {
        String sql = "SELECT COUNT(*) FROM settlement_tb WHERE room_id = ? AND settlement_month = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, room_id, settlement_month);
        return count != null && count > 0;
    }

    private Map<String, Object> selectPaymentMap(Long payment_id) {
        String sql = """
                SELECT sp.payment_id,
                       sp.id AS id,
                       sp.payment_status,
                       sp.total_amount,
                       st.settlement_id,
                       st.room_id,
                       st.settlement_month,
                       r.host_login_id AS host_login_id,
                       r.room_name,
                       r.status AS room_status
                FROM settlement_payment_tb sp
                JOIN settlement_tb st ON sp.settlement_id = st.settlement_id
                JOIN ott_room_tb r ON st.room_id = r.room_id
                WHERE sp.payment_id = ?
                """;
        try {
            return jdbcTemplate.queryForMap(sql, payment_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void insertOttNotification(String member_login_id, String title, String message, String link_url) {
        String sql = """
                INSERT INTO notification_tb (
                    id,
                    notification_type,
                    title,
                    message,
                    link_url,
                    read_yn,
                    star_yn
                ) VALUES (?, 'OTT', ?, ?, ?, 'N', 'N')
                """;
        jdbcTemplate.update(sql, member_login_id, title, message, link_url);
    }

    private String selectMemberDisplayName(String loginId) {
        String sql = """
                SELECT NVL(member_name, NVL(nickname, id))
                FROM member_tb
                WHERE id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, String.class, loginId);
        } catch (EmptyResultDataAccessException e) {
            return loginId;
        }
    }

    private void insertChatMessageInternal(Long room_id, String sender_id, String message_content) {
        Long message_id = jdbcTemplate.queryForObject("SELECT seq_ott_chat_message.NEXTVAL FROM dual", Long.class);
        String sql = """
                INSERT INTO ott_chat_message_tb (
                    message_id,
                    room_id,
                    sender_id,
                    message_content
                ) VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, message_id, room_id, sender_id, message_content);
    }

    private void insertSystemChatMessage(Long room_id, String sender_id, String message_content) {
        // ott_chat_message_tb.sender_id는 member_tb.id를 참조하는 FK가 있으므로
        // 존재하지 않는 'SYSTEM' 값을 넣으면 ORA-02291 오류가 발생한다.
        // 그래서 실제 회원 id를 sender_id로 저장하고, 내용 접두어로 시스템 메시지를 구분한다.
        insertChatMessageInternal(room_id, sender_id, "[SYSTEM] " + message_content);
    }

    private String makeRoomName(OttRoomDTO roomDTO) {
        if (roomDTO.getRoom_name() != null && !roomDTO.getRoom_name().isBlank()) {
            return roomDTO.getRoom_name().trim();
        }

        String plan_name = normalizePlanName(roomDTO.getPlan_name());
        String service_name = selectServiceName(roomDTO.getOtt_service_id());
        return service_name + " - " + plan_name + " - 모집";
    }

    private String selectServiceName(Long ott_service_id) {
        String sql = "SELECT service_name FROM ott_service_tb WHERE ott_service_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, ott_service_id);
        } catch (EmptyResultDataAccessException e) {
            return "OTT";
        }
    }

    private YearMonth parseSettlementMonth(String settlement_month) {
        if (settlement_month != null && !settlement_month.isBlank()) {
            try {
                return YearMonth.parse(settlement_month, DateTimeFormatter.ofPattern("yyyy-MM"));
            } catch (DateTimeParseException ignored) {
                // 아래 기본값 사용
            }
        }
        return YearMonth.now().plusMonths(1);
    }

    private LocalDate resolveBillingDate(YearMonth month, Integer billing_day) {
        int day = billing_day == null ? 1 : billing_day;
        day = Math.max(1, Math.min(day, month.lengthOfMonth()));
        return month.atDay(day);
    }

    private LocalDate getNextBillingDate(LocalDate today, Integer billing_day) {
        YearMonth month = YearMonth.from(today);
        LocalDate candidate = resolveBillingDate(month, billing_day);
        if (!candidate.isAfter(today)) {
            candidate = resolveBillingDate(month.plusMonths(1), billing_day);
        }
        return candidate;
    }


    private boolean isActiveNormalMember(Long room_id, String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND member_login_id = ?
                  AND member_role = 'MEMBER'
                  AND status = 'ACTIVE'
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, room_id, loginId);
        return count != null && count > 0;
    }

    private boolean hasReservedLeave(Long room_id, String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND member_login_id = ?
                  AND member_role = 'MEMBER'
                  AND status = 'ACTIVE'
                  AND leave_reserved_yn = 'Y'
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, room_id, loginId);
        return count != null && count > 0;
    }

    private boolean hasPaidUpcomingPayment(Long room_id, String loginId, LocalDate leave_scheduled_date) {
        String sql = """
                SELECT COUNT(*)
                FROM settlement_payment_tb sp
                JOIN settlement_tb st ON sp.settlement_id = st.settlement_id
                WHERE st.room_id = ?
                  AND sp.id = ?
                  AND sp.payment_status IN ('PAID', 'CONFIRMED')
                  AND st.service_start_date >= ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, room_id, loginId, Date.valueOf(leave_scheduled_date));
        return count != null && count > 0;
    }

    private String getOptionalString(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        java.sql.ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return rs.getString(columnName);
            }
        }
        return null;
    }

    private String makeInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String normalizePlanName(String plan_name) {
        if (plan_name == null || plan_name.isBlank()) {
            return "프리미엄";
        }
        return plan_name.trim();
    }

    private String normalizeRoomMode(String room_mode) {
        if ("FRIEND".equals(room_mode)) {
            return "FRIEND";
        }
        return "RECRUIT";
    }

    private String normalizeCloseNotice(String close_notice) {
        if (close_notice == null || close_notice.isBlank()) {
            return "파티장 요청으로 이번 이용 기간 종료 후 공유방이 종료됩니다.";
        }
        return close_notice.trim();
    }

    private String normalizeCloseReason(String close_reason) {
        if (close_reason == null || close_reason.isBlank()) {
            return "파티장 요청";
        }
        return close_reason.trim();
    }

    private int safeDivide(Integer total_price, Integer member_limit) {
        if (total_price == null || member_limit == null || member_limit <= 0) {
            return 0;
        }
        return (int) Math.ceil(total_price / (double) member_limit);
    }

    private int calculateFeeAmount(Integer share_amount, Double fee_rate) {
        if (share_amount == null || share_amount <= 0) {
            return 0;
        }

        double rate = fee_rate == null ? 3.0 : fee_rate;
        return (int) Math.round(share_amount * (rate / 100.0));
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Long numberToLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer numberToInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private Long getNullableLong(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }
}
