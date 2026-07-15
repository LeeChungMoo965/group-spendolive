package com.example.spendolive.ott.repository;

import java.util.List;

import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttChatRoomDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttServiceDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;

// 사용자 OTT Repository 인터페이스 - 방, 정산, 채팅 DB 기능 정의
public interface OttRepository {
    
    // 공유 가능한 OTT와 요금 규칙 조회
    List<OttServiceDTO> selectShareableServices();

    // 특정 OTT의 고정 요금제/인원/수수료 규칙 조회
    OttServiceDTO selectOttServiceRule(Long ott_service_id);

    // 외부 모집방 전체를 로그인 사용자 상태와 함께 조회
    List<OttRoomDTO> selectRecruitRooms(String loginId);

    // 선택 OTT에서 자리가 남은 가장 오래된 모집방 하나 조회
    Long selectOldestAvailableRecruitRoomId(Long ott_service_id, String loginId);

    // OTT 종류와 제목 검색 조건을 적용해 모집방 조회
    List<OttRoomDTO> selectRecruitRooms(String loginId, Long ott_service_id, String roomNameKeyword);

    // 사용자가 속한 가족·지인 방 조회
    List<OttRoomDTO> selectFriendRooms(String loginId);

    // 사용자가 방장인 가족·지인 방 조회
    List<OttRoomDTO> selectHostedFriendRooms(String loginId);

    // 사용자가 방장인 외부 모집방 조회
    List<OttRoomDTO> selectHostedRecruitRooms(String loginId);

    // 사용자가 일반 멤버로 참여 중인 외부 모집방 조회
    List<OttRoomDTO> selectJoinedRecruitRooms(String loginId);

    // 초대 코드에 해당하는 가족·지인 방 조회
    OttRoomDTO selectRoomByInviteCode(String invite_code);

    // 사용자가 ACTIVE 상태로 속한 모든 방 조회
    List<OttRoomDTO> selectMyRooms(String loginId);

    // 사용자가 생성한 모든 방 조회
    List<OttRoomDTO> selectHostedRooms(String loginId);

    // 방장이 관리할 참여자와 탈퇴 예약 정보 조회
    List<OttRoomMemberDTO> selectHostedRoomMembers(String loginId);

    // 사용자 관련 전체 정산 조회
    List<OttSettlementDTO> selectMySettlements(String loginId);

    // 방 모드 기준으로 사용자 정산 조회
    List<OttSettlementDTO> selectMySettlements(String loginId, String room_mode);

    // 방장이 확인할 참여자별 결제 내역 조회
    List<OttSettlementDTO> selectHostedSettlementPayments(String loginId, String room_mode);

    // 접근 가능한 채팅방과 최근 메시지/미읽음 수 조회
    List<OttChatRoomDTO> selectMyChatRooms(String loginId);

    // 채팅 권한을 확인하고 방 메시지 조회
    List<OttChatMessageDTO> selectChatMessages(Long room_id, String loginId);

    // 채팅 권한이 있는 경우 방 정보 반환
    OttRoomDTO selectChatRoom(Long room_id, String loginId);

    // 종료되지 않은 외부 모집방 수 조회
    int countRecruitRooms();

    // 사용자가 참여 중인 방 수 조회
    int countMyRooms(String loginId);

    // 사용자의 읽지 않은 채팅 수 조회
    int countUnreadChatMessages(String loginId);

    // 방을 생성하고 생성된 room_id 반환
    Long insertRoom(OttRoomDTO roomDTO, String loginId, String status);

    // 방 생성자를 HOST/ACTIVE 멤버로 등록
    void insertHostMember(Long room_id, String loginId);

    // 기존 신청 흐름용 참여 데이터 저장
    void applyRoom(Long room_id, String loginId);

    // 방장 권한을 확인하고 정산 및 멤버별 결제 건 생성
    void createSettlement(Long room_id, String hostId, String settlement_month, String due_date);

    // 사용자의 결제 건을 PAID로 변경하고 결제 시각 기록
    void markPaymentPaid(Long payment_id, String loginId);

    // 결제 완료 사용자를 방의 ACTIVE 멤버로 확정
    void completePaidRoomEntry(Long room_id, String loginId);

    // 방 종료 예정일과 사유를 저장하고 멤버에게 알린다
    void requestRoomClose(Long room_id, String hostId, String close_notice, String close_reason);

    // 참여자의 탈퇴 가능일을 계산해 예약하고 안내 문구 반환
    String reserveRoomLeave(Long room_id, String loginId);

    // 탈퇴 예약을 취소하고 안내 문구 반환
    String cancelRoomLeave(Long room_id, String loginId);

    // 예약 탈퇴, 미결제 만료, 방 종료 작업을 실행
    void processScheduledOttJobs();

    // 채팅 권한을 확인하고 일반 메시지 저장
    void insertChatMessage(Long room_id, String sender_id, String message_content);

    // 채팅방 마지막 읽은 시각 갱신
    void markChatRoomAsRead(Long room_id, String loginId);


    OttRoomDTO selectRoom(Long room_id); 

    // 모집방 생성 직후 결제 연동용 READY 정산 생성
    void createReadySettlement(Long room_id, String hostId);

}
