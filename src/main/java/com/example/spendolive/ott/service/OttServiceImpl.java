package com.example.spendolive.ott.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttChatRoomDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttServiceDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.ott.repository.OttRepository;

@Service
public class OttServiceImpl implements OttService {

    private final OttRepository ottRepository;

    public OttServiceImpl(OttRepository ottRepository) {
        this.ottRepository = ottRepository;
    }

    @Override
    public List<OttServiceDTO> getShareableServices() {
        return ottRepository.selectShareableServices();
    }

    @Override
    public List<OttRoomDTO> getRecruitRooms(String loginId) {
        return ottRepository.selectRecruitRooms(loginId);
    }

    @Override
    public List<OttRoomDTO> getRecruitRooms(String loginId, Long ottServiceId, String roomNameKeyword) {
        Long selectedOttServiceId = normalizeOttServiceId(ottServiceId);
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
    public List<OttSettlementDTO> getHostedSettlementPayments(String loginId, String roomMode) {
        return ottRepository.selectHostedSettlementPayments(loginId, roomMode);
    }

    @Override
    public List<OttChatRoomDTO> getMyChatRooms(String loginId) {
        return ottRepository.selectMyChatRooms(loginId);
    }

    @Override
    public List<OttChatMessageDTO> getChatMessages(Long roomId, String loginId) {
        return ottRepository.selectChatMessages(roomId, loginId);
    }

    @Override
    public OttRoomDTO getChatRoom(Long roomId, String loginId) {
        return ottRepository.selectChatRoom(roomId, loginId);
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

    @Override
    public void createFriendRoom(OttRoomDTO roomDTO, String loginId) {
        roomDTO.setRoomMode("FRIEND");
        prepareRoomDefaultValues(roomDTO);
        ottRepository.insertRoom(roomDTO, loginId, "ACTIVE");
    }

    @Override
    public void createRecruitRoom(OttRoomDTO roomDTO, String loginId) {
        roomDTO.setRoomMode("RECRUIT");
        prepareRoomDefaultValues(roomDTO);

            // 1. 모집방 생성
        Long roomId = ottRepository.insertRoom(roomDTO, loginId, "RECRUITING");

        // 2. 결제/정산 연결용 기본 정산 데이터 생성
        ottRepository.createReadySettlement(roomId, loginId);
    }

    @Override
    public void applyRecruitRoom(Long roomId, String loginId) {
        ottRepository.applyRoom(roomId, loginId);
    }

    @Override
    public Long findQuickJoinRecruitRoomId(Long ottServiceId, String loginId) {

        // 1. OTT 선택값이 없으면 빠른 참가 불가
        if (ottServiceId == null) {
            return null;
        }

        // 2. 로그인 ID가 없으면 빠른 참가 불가
        if (loginId == null || loginId.isBlank()) {
            return null;
        }

        // 3. Repository에게 참가 가능한 가장 오래된 방 roomId를 찾아달라고 요청
        return ottRepository.selectOldestAvailableRecruitRoomId(ottServiceId, loginId);
    }

    @Override
    public OttRoomDTO getRoomByInviteCode(String inviteCode) {
        return ottRepository.selectRoomByInviteCode(inviteCode);
    }

    @Override
    public void requestSettlement(Long roomId, String hostId, String settlementMonth, String dueDate) {
        ottRepository.createSettlement(roomId, hostId, settlementMonth, dueDate);
    } //

    @Override
    public void markPaymentPaid(Long paymentId, String loginId) {
        ottRepository.markPaymentPaid(paymentId, loginId);
    }

    @Override
    public void completePaidRoomEntry(Long roomId, String loginId) {
        ottRepository.completePaidRoomEntry(roomId, loginId);
    } // 결제 완료 후 사용자를 방에 ACTIVE로 넣는 메서드

    @Override
    public void requestRoomClose(Long roomId, String hostId, String closeNotice, String closeReason) {
        ottRepository.requestRoomClose(roomId, hostId, closeNotice, closeReason);
    }


    @Override
    public String reserveRoomLeave(Long roomId, String loginId) {
        return ottRepository.reserveRoomLeave(roomId, loginId);
    }

    @Override
    public String cancelRoomLeave(Long roomId, String loginId) {
        return ottRepository.cancelRoomLeave(roomId, loginId);
    }

    @Override
    public void processScheduledOttJobs() {
        ottRepository.processScheduledOttJobs();
    }

    @Override
    public void sendChatMessage(Long roomId, String senderId, String messageContent) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            return;
        }

        ottRepository.insertChatMessage(roomId, senderId, messageContent.trim());
    }

    @Override
    public void markChatRoomAsRead(Long roomId, String loginId) {
        ottRepository.markChatRoomAsRead(roomId, loginId);
    }


    private Long normalizeOttServiceId(Long ottServiceId) {
        if (ottServiceId == null || ottServiceId <= 0) {
            return null;
        }

        return ottServiceId;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        return keyword.trim();
    }

    private void prepareRoomDefaultValues(OttRoomDTO roomDTO) {
        applyFixedOttPlanRule(roomDTO);

        if (roomDTO.getBillingDay() == null || roomDTO.getBillingDay() < 1 || roomDTO.getBillingDay() > 31) {
            roomDTO.setBillingDay(1);
        }
    }

    /**
     * 모집글 작성 화면에서 금액/요금제/최대인원을 직접 입력하지 않도록 바꾸었기 때문에
     * 서버에서도 한 번 더 OTT별 고정 규칙으로 덮어쓴다.
     * 사용자가 개발자도구로 totalPrice나 memberLimit 값을 바꿔 보내도 DB에는 고정값만 저장된다.
     */
    private void applyFixedOttPlanRule(OttRoomDTO roomDTO) {
        OttServiceDTO serviceRule = ottRepository.selectOttServiceRule(roomDTO.getOttServiceId());

        if (serviceRule == null) {
            if (roomDTO.getPlanName() == null || roomDTO.getPlanName().isBlank()) {
                roomDTO.setPlanName("프리미엄");
            }

            if (roomDTO.getTotalPrice() == null || roomDTO.getTotalPrice() < 0) {
                roomDTO.setTotalPrice(0);
            }

            if (roomDTO.getMemberLimit() == null || roomDTO.getMemberLimit() < 1) {
                roomDTO.setMemberLimit(4);
            }
            return;
        }

        roomDTO.setPlanName(serviceRule.getFixedPlanName());
        roomDTO.setMemberLimit(serviceRule.getMaxMemberLimit());

        if ("FRIEND".equals(roomDTO.getRoomMode())) {
            // 가족방: IP 추가요금 제외, 기본요금만 사용
            roomDTO.setTotalPrice(serviceRule.getBasePrice());
        } else {
            // 외부인방: IP 추가요금 포함 금액 사용
            roomDTO.setTotalPrice(serviceRule.getDefaultPrice());
        }
    }
}
