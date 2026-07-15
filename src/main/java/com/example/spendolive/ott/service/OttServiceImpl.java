package com.example.spendolive.ott.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttChatRoomDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttServiceDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.ott.repository.OttRepository;


// 사용자 OTT 비즈니스 로직 - 입력값과 방·정산 규칙 처리
@Service
public class OttServiceImpl implements OttService {

    private final OttRepository ottRepository;

    public OttServiceImpl(OttRepository ottRepository) {
        this.ottRepository = ottRepository;
    }

    // 1. 화면 조회

    @Override
    public List<OttServiceDTO> getShareableServices() {
        return ottRepository.selectShareableServices();
    }

    @Override
    public List<OttRoomDTO> getRecruitRooms(String loginId) {
        return ottRepository.selectRecruitRooms(loginId);
    }

    @Override
    public List<OttRoomDTO> getRecruitRooms(String loginId, Long ott_service_id, String roomNameKeyword) {
        Long selectedOttServiceId = normalizeOttServiceId(ott_service_id);
        String searchRoomNameKeyword = normalizeKeyword(roomNameKeyword);

        return ottRepository.selectRecruitRooms(loginId, selectedOttServiceId, searchRoomNameKeyword);
    }

    @Override
    public List<OttRoomDTO> getFriendRooms(String loginId) {
        return ottRepository.selectFriendRooms(loginId);
    }

    @Override
    public List<OttRoomDTO> getHostedFriendRooms(String loginId) {
        return ottRepository.selectHostedFriendRooms(loginId);
    }

    @Override
    public List<OttRoomDTO> getHostedRecruitRooms(String loginId) {
        return ottRepository.selectHostedRecruitRooms(loginId);
    }

    @Override
    public List<OttRoomDTO> getJoinedRecruitRooms(String loginId) {
        return ottRepository.selectJoinedRecruitRooms(loginId);
    }

    @Override
    public List<OttRoomDTO> getMyRooms(String loginId) {
        return ottRepository.selectMyRooms(loginId);
    }

    @Override
    public List<OttRoomDTO> getHostedRooms(String loginId) {
        return ottRepository.selectHostedRooms(loginId);
    }

    @Override
    public List<OttRoomMemberDTO> getHostedRoomMembers(String loginId) {
        return ottRepository.selectHostedRoomMembers(loginId);
    }

    @Override
    public List<OttSettlementDTO> getMySettlements(String loginId) {
        return ottRepository.selectMySettlements(loginId);
    }

    @Override
    public List<OttSettlementDTO> getFriendSettlements(String loginId) {
        return ottRepository.selectMySettlements(loginId, "FRIEND");
    }

    @Override
    public List<OttSettlementDTO> getRecruitSettlements(String loginId) {
        return ottRepository.selectMySettlements(loginId, "RECRUIT");
    }

    @Override
    public List<OttSettlementDTO> getHostedSettlementPayments(String loginId, String room_mode) {
        return ottRepository.selectHostedSettlementPayments(loginId, room_mode);
    }

    @Override
    public List<OttChatRoomDTO> getMyChatRooms(String loginId) {
        return ottRepository.selectMyChatRooms(loginId);
    }

    @Override
    public List<OttChatMessageDTO> getChatMessages(Long room_id, String loginId) {
        return ottRepository.selectChatMessages(room_id, loginId);
    }

    @Override
    public OttRoomDTO getChatRoom(Long room_id, String loginId) {
        return ottRepository.selectChatRoom(room_id, loginId);
    }

    @Override
    public int getRecruitRoomCount() {
        return ottRepository.countRecruitRooms();
    }

    @Override
    public int getMyRoomCount(String loginId) {
        return ottRepository.countMyRooms(loginId);
    }

    @Override
    public int getUnreadChatCount(String loginId) {
        return ottRepository.countUnreadChatMessages(loginId);
    }

    // 2. 방 생성 및 참가

    // 가족·지인 방 생성 - 생성 직후 ACTIVE 상태 적용
    @Override
    @Transactional
    public void createFriendRoom(OttRoomDTO roomDTO, String loginId) {
        roomDTO.setRoom_mode("FRIEND");
        prepareRoomDefaultValues(roomDTO);
    
        Long room_id = ottRepository.insertRoom(
                roomDTO,
                loginId,
                "ACTIVE"
        );
    
        ottRepository.createReadySettlement(room_id, loginId);
    }

    // 외부 모집방 생성 - RECRUITING 방과 READY 정산 생성
    @Override
    public void createRecruitRoom(OttRoomDTO roomDTO, String loginId) {
        roomDTO.setRoom_mode("RECRUIT");
        prepareRoomDefaultValues(roomDTO);

            // 1. 모집방 생성
        Long room_id = ottRepository.insertRoom(roomDTO, loginId, "RECRUITING");

        // 2. 결제/정산 연결용 기본 정산 데이터 생성
        ottRepository.createReadySettlement(room_id, loginId);
    }

    // 기존 신청 흐름 호환 처리
    @Override
    public void applyRecruitRoom(Long room_id, String loginId) {
        ottRepository.applyRoom(room_id, loginId);
    }

    // 빠른 참가 방 조회 - 선택 OTT의 가장 오래된 빈 방 조회
    @Override
    public Long findQuickJoinRecruitRoomId(Long ott_service_id, String loginId) {

        // 1. OTT 선택값이 없으면 빠른 참가 불가
        if (ott_service_id == null) {
            return null;
        }

        // 2. 로그인 ID가 없으면 빠른 참가 불가
        if (loginId == null || loginId.isBlank()) {
            return null;
        }

        // 3. Repository에게 참가 가능한 가장 오래된 방 roomId를 찾아달라고 요청
        return ottRepository.selectOldestAvailableRecruitRoomId(ott_service_id, loginId);
    }

    // 초대 URL의 코드로 가족·지인 방 조회
    @Override
    public OttRoomDTO getRoomByInviteCode(String invite_code) {
        return ottRepository.selectRoomByInviteCode(invite_code);
    }

    // 3. 정산 및 방 관리

    // 방장의 정산 생성 요청을 Repository에 전달
    @Override
    public void requestSettlement(Long room_id, String hostId, String settlement_month, String due_date) {
        ottRepository.createSettlement(room_id, hostId, settlement_month, due_date);
    } //

    // 결제 건의 소유자를 확인한 뒤 PAID 상태로 변경
    @Override
    public void markPaymentPaid(Long payment_id, String loginId) {
        ottRepository.markPaymentPaid(payment_id, loginId);
    }

    // 결제 완료 사용자를 ACTIVE 멤버로 확정하는 최종 입장 단계다
    @Override
    public void completePaidRoomEntry(Long room_id, String loginId) {
        ottRepository.completePaidRoomEntry(room_id, loginId);
    } // 결제 완료 후 사용자를 방에 ACTIVE로 넣는 메서드

    // 방장이 요청한 종료 사유와 안내문을 저장하고 종료 예정일을 계산
    @Override
    public void requestRoomClose(Long room_id, String hostId, String close_notice, String close_reason) {
        ottRepository.requestRoomClose(room_id, hostId, close_notice, close_reason);
    }


    // 참여자의 결제 상태에 맞는 탈퇴 예정일을 계산해 예약
    @Override
    public String reserveRoomLeave(Long room_id, String loginId) {
        return ottRepository.reserveRoomLeave(room_id, loginId);
    }

    // 아직 실행되지 않은 탈퇴 예약 취소
    @Override
    public String cancelRoomLeave(Long room_id, String loginId) {
        return ottRepository.cancelRoomLeave(room_id, loginId);
    }

    // 예약 탈퇴, 미결제 만료, 방 종료를 한 번에 처리하는 배치 진입점
    @Override
    public void processScheduledOttJobs() {
        ottRepository.processScheduledOttJobs();
    }

    // 4. 채팅

    // 채팅 메시지 정리 - 공백 제거 후 저장 요청
    @Override
    public void sendChatMessage(Long room_id, String sender_id, String message_content) {
        if (message_content == null || message_content.trim().isEmpty()) {
            return;
        }

        ottRepository.insertChatMessage(room_id, sender_id, message_content.trim());
    }

    @Override
    public void markChatRoomAsRead(Long room_id, String loginId) {
        ottRepository.markChatRoomAsRead(room_id, loginId);
    }


    // 5. 입력값 및 고정 요금 규칙

    // 검색 조건에서 0 이하의 서비스 ID를 미선택 값으로 통일
    private Long normalizeOttServiceId(Long ott_service_id) {
        if (ott_service_id == null || ott_service_id <= 0) {
            return null;
        }

        return ott_service_id;
    }

    // 빈 검색어를 null로 통일하고 실제 검색어의 앞뒤 공백을 제거
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        return keyword.trim();
    }

    // 방 생성 전에 OTT별 고정 요금과 유효한 결제일 기본값 적용
    private void prepareRoomDefaultValues(OttRoomDTO roomDTO) {
        applyFixedOttPlanRule(roomDTO);

        if (roomDTO.getBilling_day() == null || roomDTO.getBilling_day() < 1 || roomDTO.getBilling_day() > 31) {
            roomDTO.setBilling_day(1);
        }
    }

    /**
     * 모집글 작성 화면에서 금액/요금제/최대인원을 직접 입력하지 않도록 바꾸었기 때문에
     * 서버에서도 한 번 더 OTT별 고정 규칙으로 덮어쓴다.
     * 사용자가 개발자도구로 totalPrice나 member_limit 값을 바꿔 보내도 DB에는 고정값만 저장된다.
     */
    private void applyFixedOttPlanRule(OttRoomDTO roomDTO) {
        OttServiceDTO serviceRule = ottRepository.selectOttServiceRule(roomDTO.getOtt_service_id());

        if (serviceRule == null) {
            if (roomDTO.getPlan_name() == null || roomDTO.getPlan_name().isBlank()) {
                roomDTO.setPlan_name("프리미엄");
            }

            if (roomDTO.getTotal_price() == null || roomDTO.getTotal_price() < 0) {
                roomDTO.setTotal_price(0);
            }

            if (roomDTO.getMember_limit() == null || roomDTO.getMember_limit() < 1) {
                roomDTO.setMember_limit(4);
            }
            return;
        }

        roomDTO.setPlan_name(serviceRule.getFixed_plan_name());
        roomDTO.setMember_limit(serviceRule.getMax_member_limit());

        if ("FRIEND".equals(roomDTO.getRoom_mode())) {
            // 가족방: IP 추가요금 제외, 기본요금만 사용
            roomDTO.setTotal_price(serviceRule.getBase_price());
        } else {
            // 외부인방: IP 추가요금 포함 금액 사용
            roomDTO.setTotal_price(serviceRule.getDefault_price());
        }
    }
}
