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
        prepareRoomDefaultValues(roomDTO);
        ottRepository.insertRoom(roomDTO, loginId, "ACTIVE");
    }

    @Override
    public void createRecruitRoom(OttRoomDTO roomDTO, String loginId) {
        prepareRoomDefaultValues(roomDTO);
        ottRepository.insertRoom(roomDTO, loginId, "RECRUITING");
    }

    @Override
    public void applyRecruitRoom(Long roomId, String loginId) {
        ottRepository.applyRoom(roomId, loginId);
    }

    @Override
    public void approveApplication(Long roomMemberId, String hostId) {
        ottRepository.approveApplication(roomMemberId, hostId);
    }

    @Override
    public void rejectApplication(Long roomMemberId, String hostId) {
        ottRepository.rejectApplication(roomMemberId, hostId);
    }

    @Override
    public void requestSettlement(Long roomId, String hostId, String settlementMonth, String dueDate) {
        ottRepository.createSettlement(roomId, hostId, settlementMonth, dueDate);
    }

    @Override
    public void markPaymentPaid(Long paymentId, String loginId) {
        ottRepository.markPaymentPaid(paymentId, loginId);
    }

    @Override
    public void requestRoomClose(Long roomId, String hostId, String closeNotice, String closeReason) {
        ottRepository.requestRoomClose(roomId, hostId, closeNotice, closeReason);
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
        roomDTO.setTotalPrice(serviceRule.getDefaultPrice());
        roomDTO.setMemberLimit(serviceRule.getMaxMemberLimit());
    }
}
