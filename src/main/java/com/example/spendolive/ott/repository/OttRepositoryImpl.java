package com.example.spendolive.ott.repository;

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

    @Override
    public List<OttServiceDTO> selectShareableServices() {
        String sql = """
                SELECT ott_service_id,
                       service_name,
                       default_price,
                       share_yn,
                       block_reason
                FROM ott_service_tb
                WHERE share_yn = 'Y'
                ORDER BY service_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OttServiceDTO service = new OttServiceDTO();
            service.setOttServiceId(rs.getLong("ott_service_id"));
            service.setServiceName(rs.getString("service_name"));
            service.setDefaultPrice(rs.getInt("default_price"));
            service.setShareYn(rs.getString("share_yn"));
            service.setBlockReason(rs.getString("block_reason"));
            return service;
        });
    }

    @Override
    public List<OttRoomDTO> selectRecruitRooms(String loginId) {
        String sql = """
                SELECT r.room_id,
                       r.host_member_id,
                       NVL(m.nickname, r.host_member_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL(COUNT(CASE WHEN rm.status = 'ACTIVE' THEN 1 END), 0) AS current_member_count,
                       NVL((
                            SELECT MAX(mine.status)
                            FROM ott_room_member_tb mine
                            WHERE mine.room_id = r.room_id
                              AND mine.member_id = ?
                       ), 'NONE') AS my_application_status
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_member_id = m.id
                LEFT JOIN ott_room_member_tb rm ON r.room_id = rm.room_id
                WHERE r.status = 'RECRUITING'
                GROUP BY r.room_id,
                         r.host_member_id,
                         NVL(m.nickname, r.host_member_id),
                         r.ott_service_id,
                         s.service_name,
                         r.room_name,
                         r.total_price,
                         r.billing_day,
                         r.member_limit,
                         r.status,
                         r.invite_code,
                         TO_CHAR(r.created_at, 'YYYY-MM-DD')
                ORDER BY r.room_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, true), loginId);
    }

    @Override
    public List<OttRoomDTO> selectMyRooms(String loginId) {
        String sql = """
                SELECT r.room_id,
                       r.host_member_id,
                       NVL(m.nickname, r.host_member_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL(COUNT(CASE WHEN rm_all.status = 'ACTIVE' THEN 1 END), 0) AS current_member_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_member_id = m.id
                LEFT JOIN ott_room_member_tb rm_all ON r.room_id = rm_all.room_id
                WHERE r.host_member_id = ?
                   OR EXISTS (
                        SELECT 1
                        FROM ott_room_member_tb mine
                        WHERE mine.room_id = r.room_id
                          AND mine.member_id = ?
                          AND mine.status = 'ACTIVE'
                   )
                GROUP BY r.room_id,
                         r.host_member_id,
                         NVL(m.nickname, r.host_member_id),
                         r.ott_service_id,
                         s.service_name,
                         r.room_name,
                         r.total_price,
                         r.billing_day,
                         r.member_limit,
                         r.status,
                         r.invite_code,
                         TO_CHAR(r.created_at, 'YYYY-MM-DD')
                ORDER BY r.room_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, false), loginId, loginId);
    }

    @Override
    public List<OttRoomDTO> selectHostedRooms(String loginId) {
        String sql = """
                SELECT r.room_id,
                       r.host_member_id,
                       NVL(m.nickname, r.host_member_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL(COUNT(CASE WHEN rm.status = 'ACTIVE' THEN 1 END), 0) AS current_member_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_member_id = m.id
                LEFT JOIN ott_room_member_tb rm ON r.room_id = rm.room_id
                WHERE r.host_member_id = ?
                GROUP BY r.room_id,
                         r.host_member_id,
                         NVL(m.nickname, r.host_member_id),
                         r.ott_service_id,
                         s.service_name,
                         r.room_name,
                         r.total_price,
                         r.billing_day,
                         r.member_limit,
                         r.status,
                         r.invite_code,
                         TO_CHAR(r.created_at, 'YYYY-MM-DD')
                ORDER BY r.room_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoom(rs, false), loginId);
    }

    @Override
    public List<OttRoomMemberDTO> selectHostedRoomMembers(String loginId) {
        String sql = """
                SELECT rm.room_member_id,
                       rm.room_id,
                       r.room_name,
                       s.service_name,
                       rm.member_id,
                       NVL(m.nickname, rm.member_id) AS member_nickname,
                       NVL(m.member_name, rm.member_id) AS member_name,
                       rm.member_role,
                       rm.share_amount,
                       rm.fee_rate,
                       rm.fee_amount,
                       rm.pay_amount,
                       TO_CHAR(rm.joined_at, 'YYYY-MM-DD') AS joined_at,
                       rm.status
                FROM ott_room_member_tb rm
                JOIN ott_room_tb r ON rm.room_id = r.room_id
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON rm.member_id = m.id
                WHERE r.host_member_id = ?
                  AND rm.member_role = 'MEMBER'
                  AND rm.status IN ('APPLIED', 'ACTIVE', 'REJECTED')
                ORDER BY CASE rm.status
                            WHEN 'APPLIED' THEN 1
                            WHEN 'ACTIVE' THEN 2
                            WHEN 'REJECTED' THEN 3
                            ELSE 4
                         END,
                         r.room_id DESC,
                         rm.joined_at DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRoomMember(rs), loginId);
    }

    @Override
    public List<OttSettlementDTO> selectMySettlements(String loginId) {
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
                       st.status,
                       TO_CHAR(st.created_at, 'YYYY-MM-DD') AS created_at,
                       CASE WHEN r.host_member_id = ? THEN 'HOST' ELSE 'MEMBER' END AS my_role,
                       sp.payment_status AS my_payment_status,
                       sp.total_amount AS my_total_amount
                FROM settlement_tb st
                JOIN ott_room_tb r ON st.room_id = r.room_id
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN settlement_payment_tb sp
                       ON st.settlement_id = sp.settlement_id
                      AND sp.id = ?
                WHERE r.host_member_id = ?
                   OR EXISTS (
                        SELECT 1
                        FROM ott_room_member_tb rm
                        WHERE rm.room_id = r.room_id
                          AND rm.member_id = ?
                          AND rm.status = 'ACTIVE'
                   )
                ORDER BY st.settlement_month DESC, st.settlement_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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
            settlement.setStatus(rs.getString("status"));
            settlement.setCreatedAt(rs.getString("created_at"));
            settlement.setMyRole(rs.getString("my_role"));
            settlement.setMyPaymentStatus(rs.getString("my_payment_status"));
            settlement.setMyTotalAmount(rs.getInt("my_total_amount"));
            return settlement;
        }, loginId, loginId, loginId, loginId);
    }

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
                                      AND cr.member_id = ?
                              ), TO_DATE('1900-01-01', 'YYYY-MM-DD'))
                       ), 0) AS unread_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                WHERE r.host_member_id = ?
                   OR EXISTS (
                        SELECT 1
                        FROM ott_room_member_tb rm
                        WHERE rm.room_id = r.room_id
                          AND rm.member_id = ?
                          AND rm.status = 'ACTIVE'
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
                               NVL(m.member_name, cm.sender_id) AS sender_name,
                               cm.message_content,
                               TO_CHAR(cm.created_at, 'YYYY-MM-DD HH24:MI') AS created_at,
                               CASE WHEN cm.sender_id = ? THEN 'Y' ELSE 'N' END AS mine_yn
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
        String sql = "SELECT COUNT(*) FROM ott_room_tb WHERE status = 'RECRUITING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public int countMyRooms(String loginId) {
        String sql = """
                SELECT COUNT(*)
                FROM ott_room_tb r
                WHERE r.host_member_id = ?
                   OR EXISTS (
                        SELECT 1
                        FROM ott_room_member_tb rm
                        WHERE rm.room_id = r.room_id
                          AND rm.member_id = ?
                          AND rm.status = 'ACTIVE'
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
                  AND (
                        r.host_member_id = ?
                        OR EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.member_id = ?
                              AND rm.status = 'ACTIVE'
                        )
                  )
                  AND cm.created_at > NVL((
                        SELECT cr.last_read_at
                        FROM ott_chat_read_tb cr
                        WHERE cr.room_id = cm.room_id
                          AND cr.member_id = ?
                  ), TO_DATE('1900-01-01', 'YYYY-MM-DD'))
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginId, loginId, loginId, loginId);
        return count == null ? 0 : count;
    }

    @Override
    @Transactional
    public Long insertRoom(OttRoomDTO roomDTO, String loginId, String status) {
        Long roomId = jdbcTemplate.queryForObject("SELECT seq_ott_room.NEXTVAL FROM dual", Long.class);
        String inviteCode = makeInviteCode();
        String roomName = makeRoomName(roomDTO);

        String sql = """
                INSERT INTO ott_room_tb (
                    room_id,
                    host_member_id,
                    ott_service_id,
                    room_name,
                    total_price,
                    billing_day,
                    member_limit,
                    status,
                    invite_code
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                roomId,
                loginId,
                roomDTO.getOttServiceId(),
                roomName,
                roomDTO.getTotalPrice(),
                roomDTO.getBillingDay(),
                roomDTO.getMemberLimit(),
                status,
                inviteCode);

        insertHostMember(roomId, loginId);
        insertChatMessageInternal(roomId, loginId, roomName + " 공유방이 만들어졌습니다.");
        return roomId;
    }

    @Override
    public void insertHostMember(Long roomId, String loginId) {
        Long roomMemberId = jdbcTemplate.queryForObject("SELECT seq_ott_room_member.NEXTVAL FROM dual", Long.class);
        String sql = """
                INSERT INTO ott_room_member_tb (
                    room_member_id,
                    room_id,
                    member_id,
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

        if (room == null || !"RECRUITING".equals(room.getStatus())) {
            return;
        }

        if (loginId.equals(room.getHostMemberId())) {
            return;
        }

        if (countActiveRoomMembers(roomId) >= room.getMemberLimit()) {
            jdbcTemplate.update("UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ?", roomId);
            return;
        }

        String existingStatus = selectRoomMemberStatus(roomId, loginId);
        if ("ACTIVE".equals(existingStatus) || "APPLIED".equals(existingStatus)) {
            return;
        }

        if ("REJECTED".equals(existingStatus) || "OUT".equals(existingStatus)) {
            String updateSql = """
                    UPDATE ott_room_member_tb
                    SET member_role = 'MEMBER',
                        share_amount = 0,
                        fee_rate = 0,
                        fee_amount = 0,
                        pay_amount = 0,
                        status = 'APPLIED',
                        joined_at = SYSDATE
                    WHERE room_id = ?
                      AND member_id = ?
                    """;
            jdbcTemplate.update(updateSql, roomId, loginId);
        } else {
            Long roomMemberId = jdbcTemplate.queryForObject("SELECT seq_ott_room_member.NEXTVAL FROM dual", Long.class);
            String insertSql = """
                    INSERT INTO ott_room_member_tb (
                        room_member_id,
                        room_id,
                        member_id,
                        member_role,
                        share_amount,
                        fee_rate,
                        fee_amount,
                        pay_amount,
                        status
                    ) VALUES (?, ?, ?, 'MEMBER', 0, 0, 0, 0, 'APPLIED')
                    """;
            jdbcTemplate.update(insertSql, roomMemberId, roomId, loginId);
        }

        insertAlert(room.getHostMemberId(),
                "ROOM_APPLY",
                "새로운 OTT 모집 신청",
                loginId + "님이 " + room.getRoomName() + "에 신청했습니다. 신청관리에서 수락 또는 거절해 주세요.",
                "/spendolive/ott/recruit.do?tab=apply&roomId=" + roomId);
    }

    @Override
    @Transactional
    public void approveApplication(Long roomMemberId, String hostId) {
        Map<String, Object> data = selectApplicationMap(roomMemberId);
        if (data == null) {
            return;
        }

        String roomHostId = (String) data.get("HOST_MEMBER_ID");
        if (!hostId.equals(roomHostId)) {
            return;
        }

        Long roomId = numberToLong(data.get("ROOM_ID"));
        String memberId = (String) data.get("MEMBER_ID");
        Integer totalPrice = numberToInt(data.get("TOTAL_PRICE"));
        Integer memberLimit = numberToInt(data.get("MEMBER_LIMIT"));
        String roomName = (String) data.get("ROOM_NAME");

        if (countActiveRoomMembers(roomId) >= memberLimit) {
            jdbcTemplate.update("UPDATE ott_room_member_tb SET status = 'REJECTED' WHERE room_member_id = ?", roomMemberId);
            insertAlert(memberId,
                    "ROOM_REJECTED",
                    "OTT 모집 신청 거절",
                    roomName + " 모집 인원이 이미 마감되어 신청이 거절되었습니다.",
                    "/spendolive/ott/recruit.do");
            return;
        }

        int baseAmount = safeDivide(totalPrice, memberLimit);
        int feeAmount = (int) Math.round(baseAmount * 0.03);
        int payAmount = baseAmount + feeAmount;

        String updateSql = """
                UPDATE ott_room_member_tb
                SET status = 'ACTIVE',
                    share_amount = ?,
                    fee_rate = 3,
                    fee_amount = ?,
                    pay_amount = ?,
                    joined_at = SYSDATE
                WHERE room_member_id = ?
                  AND status = 'APPLIED'
                """;
        jdbcTemplate.update(updateSql, baseAmount, feeAmount, payAmount, roomMemberId);

        if (countActiveRoomMembers(roomId) >= memberLimit) {
            jdbcTemplate.update("UPDATE ott_room_tb SET status = 'ACTIVE', updated_at = SYSDATE WHERE room_id = ?", roomId);
        }

        insertAlert(memberId,
                "ROOM_APPROVED",
                "OTT 모집 신청 수락",
                roomName + " 신청이 수락되었습니다. 이제 공유방 대화에 참여할 수 있습니다.",
                "/spendolive/ott/chat/room.do?roomId=" + roomId);
        insertChatMessageInternal(roomId, hostId, memberId + "님이 공유방에 참여했습니다.");
    }

    @Override
    @Transactional
    public void rejectApplication(Long roomMemberId, String hostId) {
        Map<String, Object> data = selectApplicationMap(roomMemberId);
        if (data == null) {
            return;
        }

        String roomHostId = (String) data.get("HOST_MEMBER_ID");
        if (!hostId.equals(roomHostId)) {
            return;
        }

        Long roomId = numberToLong(data.get("ROOM_ID"));
        String memberId = (String) data.get("MEMBER_ID");
        String roomName = (String) data.get("ROOM_NAME");

        jdbcTemplate.update("UPDATE ott_room_member_tb SET status = 'REJECTED' WHERE room_member_id = ? AND status = 'APPLIED'", roomMemberId);

        insertAlert(memberId,
                "ROOM_REJECTED",
                "OTT 모집 신청 거절",
                roomName + " 신청이 거절되었습니다.",
                "/spendolive/ott/recruit.do");
    }

    @Override
    @Transactional
    public void createSettlement(Long roomId, String hostId, String settlementMonth, String dueDate) {
        OttRoomDTO room = selectRoom(roomId);

        if (room == null || !hostId.equals(room.getHostMemberId())) {
            return;
        }

        if (existsSettlement(roomId, settlementMonth)) {
            return;
        }

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
                    status
                ) VALUES (?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), 'REQUESTED')
                """;

        jdbcTemplate.update(settlementSql, settlementId, roomId, settlementMonth, totalPrice, totalFee, totalPayAmount, dueDate);

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
                    room.getRoomName() + " " + settlementMonth + " 정산");

            insertAlert(member.getMemberId(),
                    "SETTLEMENT",
                    "OTT 정산 요청",
                    room.getRoomName() + " " + settlementMonth + " 정산금 " + nullToZero(member.getPayAmount()) + "원이 요청되었습니다.",
                    "/spendolive/ott/recruit.do?tab=settlement&roomId=" + roomId);
        }
    }

    @Override
    public void insertChatMessage(Long roomId, String senderId, String messageContent) {
        if (!canUseChatRoom(roomId, senderId)) {
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
                           ? AS member_id
                    FROM dual
                ) src
                ON (cr.room_id = src.room_id AND cr.member_id = src.member_id)
                WHEN MATCHED THEN UPDATE SET cr.last_read_at = SYSDATE
                WHEN NOT MATCHED THEN INSERT (room_id, member_id, last_read_at)
                VALUES (src.room_id, src.member_id, SYSDATE)
                """;
        jdbcTemplate.update(sql, roomId, loginId);
    }

    private OttRoomDTO mapRoom(java.sql.ResultSet rs, boolean hasMyStatus) throws java.sql.SQLException {
        OttRoomDTO room = new OttRoomDTO();
        room.setRoomId(rs.getLong("room_id"));
        room.setHostMemberId(rs.getString("host_member_id"));
        room.setHostNickname(rs.getString("host_nickname"));
        room.setOttServiceId(rs.getLong("ott_service_id"));
        room.setServiceName(rs.getString("service_name"));
        room.setRoomName(rs.getString("room_name"));
        room.setTotalPrice(rs.getInt("total_price"));
        room.setBillingDay(rs.getInt("billing_day"));
        room.setMemberLimit(rs.getInt("member_limit"));
        room.setCurrentMemberCount(rs.getInt("current_member_count"));
        room.setStatus(rs.getString("status"));
        room.setInviteCode(rs.getString("invite_code"));
        room.setCreatedAt(rs.getString("created_at"));
        room.setPerPersonAmount(safeDivide(room.getTotalPrice(), room.getMemberLimit()));
        if (hasMyStatus) {
            room.setMyApplicationStatus(rs.getString("my_application_status"));
        }
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
        return member;
    }

    private OttRoomDTO selectRoom(Long roomId) {
        String sql = """
                SELECT r.room_id,
                       r.host_member_id,
                       NVL(m.nickname, r.host_member_id) AS host_nickname,
                       r.ott_service_id,
                       s.service_name,
                       r.room_name,
                       r.total_price,
                       r.billing_day,
                       r.member_limit,
                       r.status,
                       r.invite_code,
                       TO_CHAR(r.created_at, 'YYYY-MM-DD') AS created_at,
                       NVL((
                            SELECT COUNT(*)
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.status = 'ACTIVE'
                       ), 0) AS current_member_count
                FROM ott_room_tb r
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON r.host_member_id = m.id
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
                       rm.member_id,
                       NVL(m.nickname, rm.member_id) AS member_nickname,
                       NVL(m.member_name, rm.member_id) AS member_name,
                       rm.member_role,
                       rm.share_amount,
                       rm.fee_rate,
                       rm.fee_amount,
                       rm.pay_amount,
                       TO_CHAR(rm.joined_at, 'YYYY-MM-DD') AS joined_at,
                       rm.status
                FROM ott_room_member_tb rm
                JOIN ott_room_tb r ON rm.room_id = r.room_id
                JOIN ott_service_tb s ON r.ott_service_id = s.ott_service_id
                LEFT JOIN member_tb m ON rm.member_id = m.id
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
                  AND (
                        r.host_member_id = ?
                        OR EXISTS (
                            SELECT 1
                            FROM ott_room_member_tb rm
                            WHERE rm.room_id = r.room_id
                              AND rm.member_id = ?
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
                  AND member_id = ?
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

    private Map<String, Object> selectApplicationMap(Long roomMemberId) {
        String sql = """
                SELECT rm.room_member_id,
                       rm.room_id,
                       rm.member_id,
                       r.host_member_id,
                       r.room_name,
                       r.total_price,
                       r.member_limit
                FROM ott_room_member_tb rm
                JOIN ott_room_tb r ON rm.room_id = r.room_id
                WHERE rm.room_member_id = ?
                  AND rm.status = 'APPLIED'
                """;
        try {
            return jdbcTemplate.queryForMap(sql, roomMemberId);
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

    private String makeRoomName(OttRoomDTO roomDTO) {
        if (roomDTO.getRoomName() != null && !roomDTO.getRoomName().isBlank()) {
            return roomDTO.getRoomName().trim();
        }

        String planName = roomDTO.getPlanName();
        if (planName == null || planName.isBlank()) {
            planName = "프리미엄";
        }

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

    private String makeInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private int safeDivide(Integer totalPrice, Integer memberLimit) {
        if (totalPrice == null || memberLimit == null || memberLimit <= 0) {
            return 0;
        }
        return (int) Math.ceil(totalPrice / (double) memberLimit);
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
}
