package com.example.spendolive.ott.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttChatRoomDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttServiceDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;

// 사용자 OTT Repository 인터페이스 - SQL 조회와 저장 기능만 정의
public interface OttRepository {

    // 조회 기능
    // 공유 가능한 OTT 서비스와 고정 요금 규칙 조회
    List<OttServiceDTO> selectShareableServices();
    // 선택한 OTT 서비스의 요금 및 공유 규칙 조회
    OttServiceDTO selectOttServiceRule(Long ott_service_id);
    // 외부인 모집방 목록 조회
    List<OttRoomDTO> selectRecruitRooms(String loginId);
    // 빠른 참가가 가능한 가장 오래된 모집방 ID 조회
    Long selectOldestAvailableRecruitRoomId(Long ott_service_id, String loginId);
    // 외부인 모집방 목록 조회
    List<OttRoomDTO> selectRecruitRooms(String loginId, Long ott_service_id, String roomNameKeyword);
    // 내가 참여 중인 가족·지인 공유방 목록 조회
    List<OttRoomDTO> selectFriendRooms(String loginId);
    // 내가 방장인 가족·지인 공유방 목록 조회
    List<OttRoomDTO> selectHostedFriendRooms(String loginId);
    // 내가 방장인 외부인 모집방 목록 조회
    List<OttRoomDTO> selectHostedRecruitRooms(String loginId);
    // 내가 일반 멤버로 참여 중인 외부인 모집방 목록 조회
    List<OttRoomDTO> selectJoinedRecruitRooms(String loginId);
    // 초대 코드에 해당하는 가족·지인 공유방 조회
    OttRoomDTO selectRoomByInviteCode(String invite_code);
    // 내가 참여 중인 전체 OTT 방 목록 조회
    List<OttRoomDTO> selectMyRooms(String loginId);
    // 내가 방장인 전체 OTT 방 목록 조회
    List<OttRoomDTO> selectHostedRooms(String loginId);
    // 내가 방장인 방의 참여자 목록 조회
    List<OttRoomMemberDTO> selectHostedRoomMembers(String loginId);
    // 내 OTT 정산 내역 조회
    List<OttSettlementDTO> selectMySettlements(String loginId);
    // 내 OTT 정산 내역 조회
    List<OttSettlementDTO> selectMySettlements(String loginId, String room_mode);
    // 내가 방장인 방의 멤버별 정산 결제 내역 조회
    List<OttSettlementDTO> selectHostedSettlementPayments(String loginId, String room_mode);
    // 내가 참여 중인 OTT 채팅방 목록 조회
    List<OttChatRoomDTO> selectMyChatRooms(String loginId);
    // 선택한 OTT 채팅방의 메시지 목록 조회
    List<OttChatMessageDTO> selectChatMessages(Long room_id, String loginId);
    // 채팅방 입장에 필요한 OTT 방 정보 조회
    OttRoomDTO selectChatRoom(Long room_id, String loginId);
    // 현재 모집 중인 외부인 방 개수 조회
    int countRecruitRooms();
    // 내가 참여 중인 OTT 방 개수 조회
    int countMyRooms(String loginId);
    // 내가 읽지 않은 OTT 채팅 메시지 개수 조회
    int countUnreadChatMessages(String loginId);
    // 선택 월에 사용자가 관련된 실제 OTT 정산 회차 수 조회
    int countMySettlements(String loginId, String settlement_month);

    // Service의 판단에 필요한 단순 조회
    // 방 ID에 해당하는 OTT 방 한 건 조회
    OttRoomDTO selectRoom(Long room_id);
    // 선택한 방의 ACTIVE 멤버 목록 조회
    List<OttRoomMemberDTO> selectActiveMembers(Long room_id);
    // 로그인 사용자의 채팅방 사용 권한 확인
    boolean canUseChatRoom(Long room_id, String loginId);
    // 선택한 방에서 로그인 사용자의 멤버 상태 조회
    String selectRoomMemberStatus(Long room_id, String loginId);
    // 선택한 방의 현재 ACTIVE 인원 수 조회
    int countActiveRoomMembers(Long room_id);
    // 선택한 방과 정산 월의 정산 존재 여부 확인
    boolean existsSettlement(Long room_id, String settlement_month);
    // 선택한 방과 정산 월의 정산 ID 조회
    Long selectSettlementId(Long room_id, String settlement_month);
    // 결제 처리에 필요한 정산 결제 정보 조회
    Map<String, Object> selectPaymentMap(Long payment_id);
    // 회원 ID에 해당하는 화면 표시 이름 조회
    String selectMemberDisplayName(String loginId);
    // OTT 서비스 ID에 해당하는 서비스명 조회
    String selectServiceName(Long ott_service_id);
    // 사용자가 해당 방의 ACTIVE 일반 멤버인지 확인
    boolean isActiveNormalMember(Long room_id, String loginId);
    // 사용자의 미처리 나가기 예약 존재 여부 확인
    boolean hasReservedLeave(Long room_id, String loginId);
    // 나가기 예정일까지 결제 완료된 이용 회차 존재 여부 확인
    boolean hasPaidUpcomingPayment(Long room_id, String loginId, LocalDate leave_scheduled_date);
    // 선택한 방의 ACTIVE 멤버 ID 목록 조회
    List<String> selectActiveRoomMemberIds(Long room_id);

    // 방 저장 기능
    // OTT 방 기본 정보 등록
    Long insertRoom(OttRoomDTO roomDTO, String loginId, String status);
    // 방 생성자를 HOST이자 ACTIVE 멤버로 등록
    void insertHostMember(Long room_id, String loginId);
    // OTT 방의 현재 상태 변경
    void updateRoomStatus(Long room_id, String status);
    // 방 종료 요청 정보와 종료 예정일 저장
    int updateRoomCloseRequest(Long room_id, String hostId, LocalDate close_effective_date,
            String close_reason, String close_notice);

    // 정산과 결제 저장 기능
    // OTT 정산 회차 기본 정보 등록
    Long insertSettlement(OttSettlementDTO settlementDTO);
    // 기존 OTT 정산 회차 정보와 상태 변경
    void updateSettlement(OttSettlementDTO settlementDTO);
    // 정산 멤버별 결제 건을 중복 없이 등록
    void insertSettlementPaymentIfAbsent(Long settlement_id, OttRoomMemberDTO member, String memo);
    // 로그인 사용자의 정산 결제 건을 PAID 상태로 변경
    int updatePaymentPaid(Long payment_id, String loginId);

    // 방 멤버 저장 기능
    // 결제 완료 사용자를 ACTIVE 방 멤버로 신규 등록
    void insertActiveRoomMember(Long room_id, String loginId, int share_amount,
            double fee_rate, int fee_amount, int pay_amount, int pay_day);
    // 기존 방 멤버를 ACTIVE 상태로 재입장 처리
    void reactivateRoomMember(Long room_id, String loginId, int share_amount,
            double fee_rate, int fee_amount, int pay_amount, int pay_day);
    // 해당 정산 회차의 실제 결제 마감일에 맞춰 ACTIVE 멤버의 결제일 갱신
    void updateActiveMemberPayDay(Long room_id, int pay_day);
    // 일반 멤버의 방 나가기 예약 정보 저장
    int reserveRoomLeave(Long room_id, String loginId, LocalDate leave_scheduled_date);
    // 아직 처리되지 않은 방 나가기 예약 취소
    int cancelRoomLeave(Long room_id, String loginId);

    // 방 종료·예약 배치 SQL
    // 방 종료일 이후 이용분의 환불 내역 등록
    void insertRefundsForRoomClose(Long room_id, LocalDate close_effective_date, String targetMonth);
    // 방 종료일 이후 미결제 결제 건 취소
    void cancelUnpaidFuturePayments(Long room_id, LocalDate close_effective_date, String targetMonth);
    // 방 종료일 이후 미래 정산 회차 취소
    void markFutureSettlementsCancelled(Long room_id, LocalDate close_effective_date, String targetMonth);
    // 예약일이 도래한 방 멤버를 OUT 상태로 변경
    void processLeaveReservations();
    // 결제 기한이 지난 미결제 건 만료 처리
    void expireOverduePayments();
    // 종료 예정일이 도래한 방과 멤버를 최종 종료 처리
    void closeEffectiveRooms();

    // 알림과 채팅 저장 기능
    // OTT 관련 사용자 알림 등록
    void insertOttNotification(String member_login_id, String title, String message, String link_url);
    // OTT 채팅 메시지 등록
    void insertChatMessage(Long room_id, String sender_id, String message_content);
    // 채팅방의 마지막 읽은 시각 저장 또는 갱신
    void markChatRoomAsRead(Long room_id, String loginId);
}
