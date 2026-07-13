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
    public OttServiceDTO selectOttServiceRule(Long ottServiceId) {
        if (ottServiceId == null) {
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
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapOttService(rs), ottServiceId);
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
    public List<OttRoomDTO> selectRecruitRooms(String loginId, Long ottServiceId, String roomNameKeyword) {
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
                ottServiceId,
                ottServiceId,
                roomNameKeyword,
                roomNameKeyword);
    }

    // 빠른 참가에서 실제로 roomId를 찾는 SQL이다.
    @Override
    public Long selectOldestAvailableRecruitRoomId(Long ottServiceId, String loginId) {
        /*
        * 빠른 참가에서 실제로 roomId를 찾는 SQL이다.
        *
        * 일반 신청하기는 JSP에서 roomId가 바로 넘어오지만,
        * 빠른 참가는 ottServiceId만 넘어오기 때문에
        * 여기서 조건에 맞는 roomId를 직접 찾아야 한다.
        */

        // 1. 값이 없으면 조회할 수 없으므로 null 반환
        if (ottServiceId == null || loginId == null || loginId.isBlank()) {
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
            * 1. ottServiceId → 선택한 OTT
            * 2. loginId      → 내가 만든 방 제외
            * 3. loginId      → 내가 이미 참여한 방 제외
            */
            return jdbcTemplate.queryForObject(sql, Long.class, ottServiceId, loginId, loginId);
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


    private List<OttRoomDTO> selectMyRoomsByMode(String loginId, String roomMode) {
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, false), loginId, roomMode, roomMode, loginId, loginId);
    }

    private List<OttRoomDTO> selectHostedRoomsByMode(String loginId, String roomMode) {
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, false), loginId, roomMode, roomMode);
    }

    private List<OttRoomDTO> selectJoinedRoomsByMode(String loginId, String roomMode) {
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, true), loginId, loginId, roomMode, roomMode);
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
                       rm.member_login_id AS member_id,
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
    public List<OttSettlementDTO> selectMySettlements(String loginId, String roomMode) {
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapMySettlement(rs), loginId, loginId, roomMode, roomMode, loginId, loginId);
    }

    @Override
    public List<OttSettlementDTO> selectHostedSettlementPayments(String loginId, String roomMode) {
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
                       sp.id AS member_id,
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
            settlement.setSettlementId(rs.getLong("settlement_id"));
            settlement.setRoomId(rs.getLong("room_id"));
            settlement.setRoomName(rs.getString("room_name"));
            settlement.setServiceName(rs.getString("service_name"));
            settlement.setSettlementMonth(rs.getString("settlement_month"));
            settlement.setStatus(rs.getString("status"));
            settlement.setPaymentStartDate(rs.getString("payment_start_date"));
            settlement.setPaymentCloseDate(rs.getString("payment_close_date"));
            settlement.setServiceStartDate(rs.getString("service_start_date"));
            settlement.setServiceEndDate(rs.getString("service_end_date"));
            settlement.setPaymentId(getNullableLong(rs, "payment_id"));
            settlement.setMemberId(rs.getString("member_id"));
            settlement.setMemberName(rs.getString("member_name"));
            settlement.setMemberNickname(rs.getString("member_nickname"));
            settlement.setBaseAmount(rs.getInt("base_amount"));
            settlement.setFeeAmount(rs.getInt("fee_amount"));
            settlement.setTotalAmount(rs.getInt("total_amount"));
            settlement.setPaymentStatus(rs.getString("payment_status"));
            settlement.setPaidAt(rs.getString("paid_at"));
            return settlement;
        }, loginId, roomMode, roomMode);
    }

    private OttSettlementDTO mapMySettlement(java.sql.ResultSet rs) throws java.sql.SQLException {
        OttSettlementDTO settlement = new OttSettlementDTO();
        settlement.setSettlementId(rs.getLong("settlement_id"));
        settlement.setRoomId(rs.getLong("room_id"));
        settlement.setRoomName(rs.getString("room_name"));
        settlement.setServiceName(rs.getString("service_name"));
        settlement.setSettlementMonth(rs.getString("settlement_month"));
        settlement.setTotalPrice(rs.getInt("total_price"));
        settlement.setTotalFee(rs.getInt("total_fee"));
        settlement.setTotalPayAmount(rs.getInt("total_pay_amount"));
        settlement.setDueDate(rs.getString("due_date"));
        settlement.setPaymentStartDate(rs.getString("payment_start_date"));
        settlement.setPaymentCloseDate(rs.getString("payment_close_date"));
        settlement.setServiceStartDate(rs.getString("service_start_date"));
        settlement.setServiceEndDate(rs.getString("service_end_date"));
        settlement.setReplaceStartDate(rs.getString("replace_start_date"));
        settlement.setReplaceEndDate(rs.getString("replace_end_date"));
        settlement.setStatus(rs.getString("status"));
        settlement.setCreatedAt(rs.getString("created_at"));
        settlement.setMyRole(rs.getString("my_role"));
        settlement.setPaymentId(getNullableLong(rs, "my_payment_id"));
        settlement.setMyPaymentStatus(rs.getString("my_payment_status"));
        settlement.setMyTotalAmount(rs.getInt("my_total_amount"));
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
            chatRoom.setRoomId(rs.getLong("room_id"));
            chatRoom.setRoomName(rs.getString("room_name"));
            chatRoom.setServiceName(rs.getString("service_name"));
            chatRoom.setUnreadCount(rs.getInt("unread_count"));
            chatRoom.setLastMessage(rs.getString("last_message"));
            return chatRoom;
        }, loginId, loginId, loginId, loginId);
    }

    @Override
    public List<OttChatMessageDTO> selectChatMessages(Long roomId, String loginId) {
        if (!canUseChatRoom(roomId, loginId)) {
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
            message.setMessageId(rs.getLong("message_id"));
            message.setRoomId(rs.getLong("room_id"));
            message.setSenderId(rs.getString("sender_id"));
            message.setSenderName(rs.getString("sender_name"));
            message.setMessageContent(rs.getString("message_content"));
            message.setCreatedAt(rs.getString("created_at"));
            message.setMineYn(rs.getString("mine_yn"));
            message.setSystemYn(rs.getString("system_yn"));
            return message;
        }, loginId, roomId);
    }

    @Override
    public OttRoomDTO selectChatRoom(Long roomId, String loginId) {
        if (!canUseChatRoom(roomId, loginId)) {
            return null;
        }

        return selectRoom(roomId);
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

    @Override
    @Transactional
    public Long insertRoom(OttRoomDTO roomDTO, String loginId, String status) {
        Long roomId = jdbcTemplate.queryForObject("SELECT seq_ott_room.NEXTVAL FROM dual", Long.class);
        String inviteCode = makeInviteCode();
        String roomName = makeRoomName(roomDTO);
        String planName = normalizePlanName(roomDTO.getPlanName());

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
                roomId,
                loginId,
                roomDTO.getOttServiceId(),
                roomName,
                planName,
                roomDTO.getTotalPrice(),
                roomDTO.getBillingDay(),
                roomDTO.getMemberLimit(),
                normalizeRoomMode(roomDTO.getRoomMode()),
                status,
                inviteCode);

        insertHostMember(roomId, loginId);
        insertSystemChatMessage(roomId, loginId, roomName + " 공유방이 만들어졌습니다.");
        return roomId;
    }

    @Override
    public void insertHostMember(Long roomId, String loginId) {
        Long roomMemberId = jdbcTemplate.queryForObject("SELECT seq_ott_room_member.NEXTVAL FROM dual", Long.class);
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
        jdbcTemplate.update(sql, roomMemberId, roomId, loginId);
    }

    @Override
    @Transactional
    public void applyRoom(Long roomId, String loginId) {
        OttRoomDTO room = selectRoom(roomId);

        if (room == null || loginId == null || loginId.equals(room.getHostMemberId())) {
            return;
        }

        if (!"RECRUIT".equals(room.getRoomMode())) {
            return;
        }

        if (!("RECRUITING".equals(room.getStatus()) || "REPLACE_RECRUITING".equals(room.getStatus()))) {
            return;
        }

        if (countActiveRoomMembers(roomId) >= room.getMemberLimit()) {
            jdbcTemplate.update(
                    "UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ? AND status <> 'CLOSE_REQUESTED'",
                    roomId);
        }

        // 신청/승인 시스템 제거 후에는 APPLIED/REJECTED 데이터를 만들지 않는다.
        // 이 버튼은 결제 담당자가 연결할 결제 화면으로 이동시키는 역할만 한다.
        // 결제 성공 이후 ott_room_member_tb ACTIVE 등록은 결제 콜백 쪽에서 처리한다.
    }

    // 방생성하기 누르면 정산 상태를 'READY' 인상태로 데이터 생성
    // 방장이 팀원이 들어오고 나서 정산하기를 누르면 PAYMENT_OPEN로 update를 하고 settlement_payment_tb를 만드는 방식으로 가야함
    @Override
    @Transactional
    public void createReadySettlement(Long roomId, String hostId) {
        OttRoomDTO room = selectRoom(roomId);

        if (room == null || !hostId.equals(room.getHostMemberId())) {
            return;
        }

        YearMonth targetMonth = YearMonth.now().plusMonths(1);
        String targetMonthText = targetMonth.toString();

        if (existsSettlement(roomId, targetMonthText)) {
            return;
        }

        LocalDate serviceStartDate = resolveBillingDate(targetMonth, room.getBillingDay());
        LocalDate serviceEndDate = serviceStartDate.plusMonths(1).minusDays(1);
        LocalDate paymentStartDate = LocalDate.now();
        LocalDate paymentCloseDate = serviceStartDate.minusDays(7);
        LocalDate replaceStartDate = paymentCloseDate;
        LocalDate replaceEndDate = serviceStartDate.minusDays(1);

        Long settlementId = jdbcTemplate.queryForObject("SELECT seq_settlement.NEXTVAL FROM dual", Long.class);

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
                settlementId,
                roomId,
                targetMonthText,
                room.getTotalPrice(),
                Date.valueOf(paymentCloseDate),
                Date.valueOf(paymentStartDate),
                Date.valueOf(paymentCloseDate),
                Date.valueOf(serviceStartDate),
                Date.valueOf(serviceEndDate),
                Date.valueOf(replaceStartDate),
                Date.valueOf(replaceEndDate));
    }


    // =========================================================
    // 7. 정산 요청/결제 처리
    // =========================================================

    @Override
    @Transactional
    public void createSettlement(Long roomId, String hostId, String settlementMonth, String dueDate) {
        OttRoomDTO room = selectRoom(roomId);

        if (room == null || !hostId.equals(room.getHostMemberId())) {
            return;
        }

        if ("CLOSE_REQUESTED".equals(room.getStatus()) || "CLOSED".equals(room.getStatus())) {
            return;
        }

        YearMonth targetMonth = parseSettlementMonth(settlementMonth);
        String targetMonthText = targetMonth.toString();

        if (existsSettlement(roomId, targetMonthText)) {
            return;
        }

        LocalDate serviceStartDate = resolveBillingDate(targetMonth, room.getBillingDay());
        LocalDate serviceEndDate = serviceStartDate.plusMonths(1).minusDays(1);
        LocalDate paymentStartDate = serviceStartDate.minusMonths(1);
        LocalDate paymentCloseDate = serviceStartDate.minusDays(7);
        LocalDate replaceStartDate = paymentCloseDate;
        LocalDate replaceEndDate = serviceStartDate.minusDays(1);

        List<OttRoomMemberDTO> members = selectActiveMembers(roomId);
        int totalPrice = 0;
        int totalFee = 0;
        int totalPayAmount = 0;

        for (OttRoomMemberDTO member : members) {
            if (!"HOST".equals(member.getMemberRole())) {
                totalPrice += nullToZero(member.getShareAmount());
                totalFee += nullToZero(member.getFeeAmount());
                totalPayAmount += nullToZero(member.getPayAmount());
            }
        }

        Long settlementId = jdbcTemplate.queryForObject("SELECT seq_settlement.NEXTVAL FROM dual", Long.class);
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
                settlementId,
                roomId,
                targetMonthText,
                totalPrice,
                totalFee,
                totalPayAmount,
                Date.valueOf(paymentCloseDate),
                Date.valueOf(paymentStartDate),
                Date.valueOf(paymentCloseDate),
                Date.valueOf(serviceStartDate),
                Date.valueOf(serviceEndDate),
                Date.valueOf(replaceStartDate),
                Date.valueOf(replaceEndDate));

        for (OttRoomMemberDTO member : members) {
            if ("HOST".equals(member.getMemberRole())) {
                continue;
            }

            Long paymentId = jdbcTemplate.queryForObject("SELECT seq_settlement_payment.NEXTVAL FROM dual", Long.class);
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
                    paymentId,
                    settlementId,
                    member.getMemberId(),
                    nullToZero(member.getShareAmount()),
                    nullToZero(member.getFeeAmount()),
                    nullToZero(member.getPayAmount()),
                    room.getRoomName() + " " + targetMonthText + " 이용분 정산");

            insertAlert(member.getMemberId(),
                    "SETTLEMENT",
                    "OTT 다음 달 이용분 결제 요청",
                    room.getRoomName() + " " + targetMonthText + " 이용분 " + nullToZero(member.getPayAmount())
                            + "원을 " + paymentCloseDate + "까지 결제해 주세요. 마감 후 미결제자는 자동 추방됩니다.",
                    "/spendolive/ott/recruit.do?tab=settlement&roomId=" + roomId);
        }

        jdbcTemplate.update("UPDATE ott_room_tb SET status = 'PAYMENT_OPEN', updated_at = SYSDATE WHERE room_id = ? AND status <> 'CLOSE_REQUESTED'", roomId);
        insertSystemChatMessage(roomId, hostId, targetMonthText + " 이용분 결제가 열렸습니다. 결제 마감일은 " + paymentCloseDate + "입니다.");
    }

    @Override
    @Transactional
    public void markPaymentPaid(Long paymentId, String loginId) {
        Map<String, Object> data = selectPaymentMap(paymentId);
        if (data == null) {
            return;
        }

        String payerId = (String) data.get("ID");
        if (!loginId.equals(payerId)) {
            return;
        }

        String paymentStatus = (String) data.get("PAYMENT_STATUS");
        String roomStatus = (String) data.get("ROOM_STATUS");
        if (!"UNPAID".equals(paymentStatus) || "CLOSE_REQUESTED".equals(roomStatus) || "CLOSED".equals(roomStatus)) {
            return;
        }

        Long roomId = numberToLong(data.get("ROOM_ID"));
        String hostId = (String) data.get("HOST_MEMBER_ID");
        String roomName = (String) data.get("ROOM_NAME");
        String settlementMonth = (String) data.get("SETTLEMENT_MONTH");
        Integer totalAmount = numberToInt(data.get("TOTAL_AMOUNT"));

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
        int updated = jdbcTemplate.update(sql, paymentId, loginId);

        if (updated > 0) {
            insertAlert(hostId,
                    "PAYMENT_PAID",
                    "OTT 결제 완료 알림",
                    loginId + "님이 " + roomName + " " + settlementMonth + " 이용분 " + totalAmount + "원 결제를 완료했습니다.",
                    "/spendolive/ott/recruit.do?tab=settlement&roomId=" + roomId);
        }
    }

    @Override
    public void completePaidRoomEntry(Long roomId, String loginId) {
        OttRoomDTO room = selectRoom(roomId);

        if (room == null || loginId == null || loginId.equals(room.getHostMemberId())) {
            return;
        }

        if ("CLOSE_REQUESTED".equals(room.getStatus()) || "CLOSED".equals(room.getStatus())) {
            return;
        }

        String currentStatus = selectRoomMemberStatus(roomId, loginId);
        if ("ACTIVE".equals(currentStatus)) {
            return;
        }

        if (countActiveRoomMembers(roomId) >= room.getMemberLimit()) {
            jdbcTemplate.update(
                    "UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ? AND status IN ('RECRUITING', 'REPLACE_RECRUITING')",
                    roomId);
            return;
        }

        int shareAmount = safeDivide(room.getTotalPrice(), room.getMemberLimit());
        double feeRate = 3.0;
        int feeAmount = calculateFeeAmount(shareAmount, feeRate);
        int payAmount = shareAmount + feeAmount;

        if (currentStatus == null) {
            Long roomMemberId = jdbcTemplate.queryForObject("SELECT seq_ott_room_member.NEXTVAL FROM dual", Long.class);
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
                        status
                    ) VALUES (?, ?, ?, 'MEMBER', ?, ?, ?, ?, 'ACTIVE')
                    """;
            jdbcTemplate.update(insertSql, roomMemberId, roomId, loginId, shareAmount, feeRate, feeAmount, payAmount);
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
            jdbcTemplate.update(updateSql, shareAmount, feeRate, feeAmount, payAmount, roomId, loginId);
        }

        if (countActiveRoomMembers(roomId) >= room.getMemberLimit()) {
            jdbcTemplate.update(
                    "UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ? AND status IN ('RECRUITING', 'REPLACE_RECRUITING')",
                    roomId);
        }

        String memberName = selectMemberDisplayName(loginId);
        insertSystemChatMessage(roomId, loginId, memberName + "님이 결제를 완료하고 공유방에 입장했습니다.");
        alertActiveMembers(roomId,
                "ROOM_MEMBER_JOINED",
                "OTT 공유방 입장 알림",
                memberName + "님이 " + room.getRoomName() + " 공유방에 입장했습니다.",
                "/spendolive/ott/chat/room.do?roomId=" + roomId,
                loginId);
    }

    // =========================================================
    // 8. 방 삭제 요청/환불 처리
    // =========================================================

    @Override
    @Transactional
    public void requestRoomClose(Long roomId, String hostId, String closeNotice, String closeReason) {
        OttRoomDTO room = selectRoom(roomId);

        if (room == null || !hostId.equals(room.getHostMemberId())) {
            return;
        }

        if ("CLOSED".equals(room.getStatus()) || "CLOSE_REQUESTED".equals(room.getStatus())) {
            return;
        }

        LocalDate closeEffectiveDate = getNextBillingDate(LocalDate.now(), room.getBillingDay());
        String notice = normalizeCloseNotice(closeNotice);
        String reason = normalizeCloseReason(closeReason);

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
        jdbcTemplate.update(updateRoomSql, Date.valueOf(closeEffectiveDate), reason, notice, roomId, hostId);


        insertRefundsForRoomClose(roomId, closeEffectiveDate);
        cancelUnpaidFuturePayments(roomId, closeEffectiveDate);
        markFutureSettlementsCancelled(roomId, closeEffectiveDate);

        String message = "파티장이 방 삭제를 요청했습니다. 기존 참여자는 " + closeEffectiveDate.minusDays(1)
                + "까지 이용할 수 있으며, 이미 결제된 다음 이용분은 자동 환불 처리됩니다.";
        insertSystemChatMessage(roomId, hostId, message);
        alertActiveMembers(roomId,
                "ROOM_CLOSE_REQUESTED",
                "OTT 공유방 종료 예정",
                room.getRoomName() + " 공유방이 " + closeEffectiveDate + "에 종료될 예정입니다. " + notice,
                "/spendolive/ott/chat/room.do?roomId=" + roomId,
                hostId);
    }


    @Override
    @Transactional
    public String reserveRoomLeave(Long roomId, String loginId) {
        if (roomId == null || loginId == null || loginId.isBlank()) {
            return "나가기 예약을 처리할 수 없습니다.";
        }

        OttRoomDTO room = selectRoom(roomId);
        if (room == null || "CLOSED".equals(room.getStatus()) || "CLOSE_REQUESTED".equals(room.getStatus())) {
            return "이미 종료되었거나 종료 예정인 방입니다.";
        }

        if (loginId.equals(room.getHostMemberId())) {
            return "파티장은 나가기 예약을 할 수 없습니다. 방 삭제 요청 기능을 사용해 주세요.";
        }

        if (!isActiveNormalMember(roomId, loginId)) {
            return "현재 참여 중인 일반 참여자만 나가기 예약을 할 수 있습니다.";
        }

        if (hasReservedLeave(roomId, loginId)) {
            return "이미 나가기 예약이 되어 있습니다.";
        }

        LocalDate today = LocalDate.now();
        LocalDate nextBillingDate = getNextBillingDate(today, room.getBillingDay());
        LocalDate leaveScheduledDate = nextBillingDate.minusDays(7);

        // 선택 A: 결제일 7일 전부터는 이번 회차 나가기 예약 불가
        if (!today.isBefore(leaveScheduledDate)) {
            return "이미 다음 결제 준비 기간이라 이번 회차 나가기 예약은 불가능합니다. 다음 결제일 이후 다시 예약할 수 있습니다.";
        }

        if (hasPaidUpcomingPayment(roomId, loginId, leaveScheduledDate)) {
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
        int updated = jdbcTemplate.update(sql, Date.valueOf(leaveScheduledDate), roomId, loginId);

        if (updated == 0) {
            return "나가기 예약을 처리하지 못했습니다.";
        }

        insertSystemChatMessage(roomId, loginId, loginId + "님이 " + leaveScheduledDate + " 나가기 예약을 했습니다.");
        insertAlert(room.getHostMemberId(),
                "OTT_LEAVE_RESERVED",
                "OTT 참여자 나가기 예약",
                loginId + "님이 " + room.getRoomName() + " 방에서 " + leaveScheduledDate + " 나가기 예약을 했습니다.",
                "/spendolive/ott/chat/room.do?roomId=" + roomId);

        return "나가기 예약이 완료되었습니다. " + leaveScheduledDate + "에 자동으로 방에서 나가집니다.";
    }

    @Override
    @Transactional
    public String cancelRoomLeave(Long roomId, String loginId) {
        if (roomId == null || loginId == null || loginId.isBlank()) {
            return "나가기 예약 취소를 처리할 수 없습니다.";
        }

        OttRoomDTO room = selectRoom(roomId);
        if (room == null || "CLOSED".equals(room.getStatus())) {
            return "이미 종료된 방입니다.";
        }

        if (loginId.equals(room.getHostMemberId())) {
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
        int updated = jdbcTemplate.update(sql, roomId, loginId);

        if (updated == 0) {
            return "취소할 나가기 예약이 없습니다.";
        }

        insertSystemChatMessage(roomId, loginId, loginId + "님이 나가기 예약을 취소했습니다.");
        return "나가기 예약이 취소되었습니다.";
    }

    @Override
    @Transactional
    public void processScheduledOttJobs() {
        processLeaveReservations();
        expireOverduePayments();
        closeEffectiveRooms();
    }

    @Override
    public void insertChatMessage(Long roomId, String senderId, String messageContent) {
        if (!canUseChatRoom(roomId, senderId)) {
            return;
        }

        // 일반 사용자가 [SYSTEM] 접두어로 시스템 알림처럼 위장하지 못하도록 제거한다.
        if (messageContent != null && messageContent.trim().startsWith("[SYSTEM]")) {
            messageContent = messageContent.trim().replaceFirst("^\\[SYSTEM\\]\\s*", "").trim();
        }

        if (messageContent == null || messageContent.isBlank()) {
            return;
        }

        insertChatMessageInternal(roomId, senderId, messageContent);
    }

    @Override
    public void markChatRoomAsRead(Long roomId, String loginId) {
        if (!canUseChatRoom(roomId, loginId)) {
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
        jdbcTemplate.update(sql, roomId, loginId);
    }


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

    private void insertRefundsForRoomClose(Long roomId, LocalDate closeEffectiveDate) {
        String targetMonth = YearMonth.from(closeEffectiveDate).toString();
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
                       sp.id AS member_id,
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
        jdbcTemplate.update(insertRefundSql, roomId, Date.valueOf(closeEffectiveDate), targetMonth);

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
        jdbcTemplate.update(updatePaymentSql, roomId, Date.valueOf(closeEffectiveDate), targetMonth);
    }

    private void cancelUnpaidFuturePayments(Long roomId, LocalDate closeEffectiveDate) {
        String targetMonth = YearMonth.from(closeEffectiveDate).toString();
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
        jdbcTemplate.update(sql, roomId, Date.valueOf(closeEffectiveDate), targetMonth);
    }

    private void markFutureSettlementsCancelled(Long roomId, LocalDate closeEffectiveDate) {
        String targetMonth = YearMonth.from(closeEffectiveDate).toString();
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
        jdbcTemplate.update(sql, roomId, Date.valueOf(closeEffectiveDate), targetMonth);
    }

    private void alertActiveMembers(Long roomId, String alertType, String title, String content, String targetUrl, String exceptMemberId) {
        String sql = """
                SELECT member_login_id
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND status = 'ACTIVE'
                """;
        List<String> members = jdbcTemplate.queryForList(sql, String.class, roomId);
        for (String memberId : members) {
            if (exceptMemberId != null && exceptMemberId.equals(memberId)) {
                continue;
            }
            insertAlert(memberId, alertType, title, content, targetUrl);
        }
    }

    // =========================================================
    // 10. RowMapper / 내부 유틸
    // =========================================================

    private OttServiceDTO mapOttService(java.sql.ResultSet rs) throws java.sql.SQLException {
        OttServiceDTO service = new OttServiceDTO();
        service.setOttServiceId(rs.getLong("ott_service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setDefaultPrice(rs.getInt("default_price"));
        service.setShareYn(rs.getString("share_yn"));
        service.setRiskLevel(rs.getString("risk_level"));
        service.setBlockReason(rs.getString("block_reason"));
        service.setFixedPlanName(rs.getString("fixed_plan_name"));
        service.setBasePrice(rs.getInt("base_price"));
        service.setExtraMemberFee(rs.getInt("extra_member_fee"));
        service.setExtraMemberCount(rs.getInt("extra_member_count"));
        service.setMaxMemberLimit(rs.getInt("max_member_limit"));
        service.setPlatformFeeRate(rs.getDouble("platform_fee_rate"));

        int shareAmount = safeDivide(service.getDefaultPrice(), service.getMaxMemberLimit());
        int feeAmount = calculateFeeAmount(shareAmount, service.getPlatformFeeRate());
        service.setShareAmount(shareAmount);
        service.setFeeAmount(feeAmount);
        service.setPerPersonAmount(shareAmount + feeAmount);
        return service;
    }

    private OttRoomDTO mapRoom(java.sql.ResultSet rs, boolean hasMyStatus) throws java.sql.SQLException {
        OttRoomDTO room = new OttRoomDTO();
        room.setRoomId(rs.getLong("room_id"));
        room.setHostMemberId(rs.getString("host_login_id"));
        room.setHostNickname(rs.getString("host_nickname"));
        room.setOttServiceId(rs.getLong("ott_service_id"));
        room.setServiceName(rs.getString("service_name"));
        room.setRoomName(rs.getString("room_name"));
        room.setPlanName(rs.getString("plan_name"));
        room.setTotalPrice(rs.getInt("total_price"));
        room.setBillingDay(rs.getInt("billing_day"));
        room.setMemberLimit(rs.getInt("member_limit"));
        room.setRoomMode(rs.getString("room_mode"));
        room.setCurrentMemberCount(rs.getInt("current_member_count"));
        room.setStatus(rs.getString("status"));
        room.setInviteCode(rs.getString("invite_code"));
        room.setCloseRequestedAt(rs.getString("close_requested_at"));
        room.setCloseEffectiveDate(rs.getString("close_effective_date"));
        room.setCloseReason(rs.getString("close_reason"));
        room.setCloseNotice(rs.getString("close_notice"));
        room.setClosedAt(rs.getString("closed_at"));
        room.setCreatedAt(rs.getString("created_at"));
        int shareAmount = safeDivide(room.getTotalPrice(), room.getMemberLimit());
        int feeAmount = calculateFeeAmount(shareAmount, 3.0);
        room.setShareAmount(shareAmount);
        room.setPlatformFeeRate(3.0);
        room.setFeeAmount(feeAmount);
        room.setPerPersonAmount(shareAmount + feeAmount);
        if (hasMyStatus) {
            room.setMyApplicationStatus(rs.getString("my_application_status"));
        }
        room.setLeaveReservedYn(getOptionalString(rs, "leave_reserved_yn"));
        room.setLeaveRequestedAt(getOptionalString(rs, "leave_requested_at"));
        room.setLeaveScheduledDate(getOptionalString(rs, "leave_scheduled_date"));
        room.setLeaveCancelledAt(getOptionalString(rs, "leave_cancelled_at"));
        room.setLeaveReason(getOptionalString(rs, "leave_reason"));
        return room;
    }

    private OttRoomMemberDTO mapRoomMember(java.sql.ResultSet rs) throws java.sql.SQLException {
        OttRoomMemberDTO member = new OttRoomMemberDTO();
        member.setRoomMemberId(rs.getLong("room_member_id"));
        member.setRoomId(rs.getLong("room_id"));
        member.setRoomName(rs.getString("room_name"));
        member.setServiceName(rs.getString("service_name"));
        member.setMemberId(rs.getString("member_id"));
        member.setMemberNickname(rs.getString("member_nickname"));
        member.setMemberName(rs.getString("member_name"));
        member.setMemberRole(rs.getString("member_role"));
        member.setShareAmount(rs.getInt("share_amount"));
        member.setFeeRate(rs.getDouble("fee_rate"));
        member.setFeeAmount(rs.getInt("fee_amount"));
        member.setPayAmount(rs.getInt("pay_amount"));
        member.setJoinedAt(rs.getString("joined_at"));
        member.setStatus(rs.getString("status"));
        member.setLeaveReservedYn(getOptionalString(rs, "leave_reserved_yn"));
        member.setLeaveRequestedAt(getOptionalString(rs, "leave_requested_at"));
        member.setLeaveScheduledDate(getOptionalString(rs, "leave_scheduled_date"));
        member.setLeaveCancelledAt(getOptionalString(rs, "leave_cancelled_at"));
        member.setLeaveReason(getOptionalString(rs, "leave_reason"));
        return member;
    }

    @Override
    public OttRoomDTO selectRoomByInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
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
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRoom(rs, false), inviteCode.trim().toUpperCase());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private OttRoomDTO selectRoom(Long roomId) {
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
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRoom(rs, false), roomId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private List<OttRoomMemberDTO> selectActiveMembers(Long roomId) {
        String sql = """
                SELECT rm.room_member_id,
                       rm.room_id,
                       r.room_name,
                       s.service_name,
                       rm.member_login_id AS member_id,
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoomMember(rs), roomId);
    }

    private boolean canUseChatRoom(Long roomId, String loginId) {
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
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId, loginId, loginId);
        return count != null && count > 0;
    }

    private String selectRoomMemberStatus(Long roomId, String loginId) {
        String sql = """
                SELECT status
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND member_login_id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, String.class, roomId, loginId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private int countActiveRoomMembers(Long roomId) {
        String sql = "SELECT COUNT(*) FROM ott_room_member_tb WHERE room_id = ? AND status = 'ACTIVE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId);
        return count == null ? 0 : count;
    }

    private boolean existsSettlement(Long roomId, String settlementMonth) {
        String sql = "SELECT COUNT(*) FROM settlement_tb WHERE room_id = ? AND settlement_month = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId, settlementMonth);
        return count != null && count > 0;
    }

    private Map<String, Object> selectPaymentMap(Long paymentId) {
        String sql = """
                SELECT sp.payment_id,
                       sp.id AS id,
                       sp.payment_status,
                       sp.total_amount,
                       st.settlement_id,
                       st.room_id,
                       st.settlement_month,
                       r.host_login_id AS host_member_id,
                       r.room_name,
                       r.status AS room_status
                FROM settlement_payment_tb sp
                JOIN settlement_tb st ON sp.settlement_id = st.settlement_id
                JOIN ott_room_tb r ON st.room_id = r.room_id
                WHERE sp.payment_id = ?
                """;
        try {
            return jdbcTemplate.queryForMap(sql, paymentId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void insertAlert(String loginId, String alertType, String title, String content, String targetUrl) {
        Long alertId = jdbcTemplate.queryForObject("SELECT seq_alert.NEXTVAL FROM dual", Long.class);
        String sql = """
                INSERT INTO alert_tb (
                    alert_id,
                    id,
                    alert_type,
                    title,
                    content,
                    target_url,
                    read_yn,
                    banner_yn
                ) VALUES (?, ?, ?, ?, ?, ?, 'N', 'Y')
                """;
        jdbcTemplate.update(sql, alertId, loginId, alertType, title, content, targetUrl);
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

    private void insertChatMessageInternal(Long roomId, String senderId, String messageContent) {
        Long messageId = jdbcTemplate.queryForObject("SELECT seq_ott_chat_message.NEXTVAL FROM dual", Long.class);
        String sql = """
                INSERT INTO ott_chat_message_tb (
                    message_id,
                    room_id,
                    sender_id,
                    message_content
                ) VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, messageId, roomId, senderId, messageContent);
    }

    private void insertSystemChatMessage(Long roomId, String senderId, String messageContent) {
        // ott_chat_message_tb.sender_id는 member_tb.id를 참조하는 FK가 있으므로
        // 존재하지 않는 'SYSTEM' 값을 넣으면 ORA-02291 오류가 발생한다.
        // 그래서 실제 회원 id를 sender_id로 저장하고, 내용 접두어로 시스템 메시지를 구분한다.
        insertChatMessageInternal(roomId, senderId, "[SYSTEM] " + messageContent);
    }

    private String makeRoomName(OttRoomDTO roomDTO) {
        if (roomDTO.getRoomName() != null && !roomDTO.getRoomName().isBlank()) {
            return roomDTO.getRoomName().trim();
        }

        String planName = normalizePlanName(roomDTO.getPlanName());
        String serviceName = selectServiceName(roomDTO.getOttServiceId());
        return serviceName + " - " + planName + " - 모집";
    }

    private String selectServiceName(Long ottServiceId) {
        String sql = "SELECT service_name FROM ott_service_tb WHERE ott_service_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, ottServiceId);
        } catch (EmptyResultDataAccessException e) {
            return "OTT";
        }
    }

    private YearMonth parseSettlementMonth(String settlementMonth) {
        if (settlementMonth != null && !settlementMonth.isBlank()) {
            try {
                return YearMonth.parse(settlementMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
            } catch (DateTimeParseException ignored) {
                // 아래 기본값 사용
            }
        }
        return YearMonth.now().plusMonths(1);
    }

    private LocalDate resolveBillingDate(YearMonth month, Integer billingDay) {
        int day = billingDay == null ? 1 : billingDay;
        day = Math.max(1, Math.min(day, month.lengthOfMonth()));
        return month.atDay(day);
    }

    private LocalDate getNextBillingDate(LocalDate today, Integer billingDay) {
        YearMonth month = YearMonth.from(today);
        LocalDate candidate = resolveBillingDate(month, billingDay);
        if (!candidate.isAfter(today)) {
            candidate = resolveBillingDate(month.plusMonths(1), billingDay);
        }
        return candidate;
    }


    private boolean isActiveNormalMember(Long roomId, String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND member_login_id = ?
                  AND member_role = 'MEMBER'
                  AND status = 'ACTIVE'
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId, loginId);
        return count != null && count > 0;
    }

    private boolean hasReservedLeave(Long roomId, String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_room_member_tb
                WHERE room_id = ?
                  AND member_login_id = ?
                  AND member_role = 'MEMBER'
                  AND status = 'ACTIVE'
                  AND leave_reserved_yn = 'Y'
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId, loginId);
        return count != null && count > 0;
    }

    private boolean hasPaidUpcomingPayment(Long roomId, String loginId, LocalDate leaveScheduledDate) {
        String sql = """
                SELECT COUNT(*)
                FROM settlement_payment_tb sp
                JOIN settlement_tb st ON sp.settlement_id = st.settlement_id
                WHERE st.room_id = ?
                  AND sp.id = ?
                  AND sp.payment_status IN ('PAID', 'CONFIRMED')
                  AND st.service_start_date >= ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId, loginId, Date.valueOf(leaveScheduledDate));
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

    private String normalizePlanName(String planName) {
        if (planName == null || planName.isBlank()) {
            return "프리미엄";
        }
        return planName.trim();
    }

    private String normalizeRoomMode(String roomMode) {
        if ("FRIEND".equals(roomMode)) {
            return "FRIEND";
        }
        return "RECRUIT";
    }

    private String normalizeCloseNotice(String closeNotice) {
        if (closeNotice == null || closeNotice.isBlank()) {
            return "파티장 요청으로 이번 이용 기간 종료 후 공유방이 종료됩니다.";
        }
        return closeNotice.trim();
    }

    private String normalizeCloseReason(String closeReason) {
        if (closeReason == null || closeReason.isBlank()) {
            return "파티장 요청";
        }
        return closeReason.trim();
    }

    private int safeDivide(Integer totalPrice, Integer memberLimit) {
        if (totalPrice == null || memberLimit == null || memberLimit <= 0) {
            return 0;
        }
        return (int) Math.ceil(totalPrice / (double) memberLimit);
    }

    private int calculateFeeAmount(Integer shareAmount, Double feeRate) {
        if (shareAmount == null || shareAmount <= 0) {
            return 0;
        }

        double rate = feeRate == null ? 3.0 : feeRate;
        return (int) Math.round(shareAmount * (rate / 100.0));
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
