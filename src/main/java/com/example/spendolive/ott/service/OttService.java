package com.example.spendolive.ott.service;

import java.util.List;

import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttChatRoomDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttServiceDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;

public interface OttService {
    List<OttServiceDTO> getShareableServices();

    List<OttRoomDTO> getRecruitRooms(String loginId);

    List<OttRoomDTO> getRecruitRooms(String loginId, Long ottServiceId, String roomNameKeyword);

    List<OttRoomDTO> getFriendRooms(String loginId);

    List<OttRoomDTO> getHostedFriendRooms(String loginId);

    List<OttRoomDTO> getHostedRecruitRooms(String loginId);

    List<OttRoomDTO> getJoinedRecruitRooms(String loginId);

    List<OttRoomDTO> getMyRooms(String loginId);

    List<OttRoomDTO> getHostedRooms(String loginId);

    List<OttRoomMemberDTO> getHostedRoomMembers(String loginId);

    List<OttSettlementDTO> getMySettlements(String loginId);

    List<OttSettlementDTO> getFriendSettlements(String loginId);

    List<OttSettlementDTO> getRecruitSettlements(String loginId);

    List<OttSettlementDTO> getHostedSettlementPayments(String loginId, String roomMode);

    List<OttChatRoomDTO> getMyChatRooms(String loginId);

    List<OttChatMessageDTO> getChatMessages(Long roomId, String loginId);

    OttRoomDTO getChatRoom(Long roomId, String loginId);

    int getRecruitRoomCount();

    int getMyRoomCount(String loginId);

    int getUnreadChatCount(String loginId);

    void createFriendRoom(OttRoomDTO roomDTO, String loginId);

    void createRecruitRoom(OttRoomDTO roomDTO, String loginId);

    void applyRecruitRoom(Long roomId, String loginId);

    void approveApplication(Long roomMemberId, String hostId);

    void rejectApplication(Long roomMemberId, String hostId);

    void requestSettlement(Long roomId, String hostId, String settlementMonth, String dueDate);

    void markPaymentPaid(Long paymentId, String loginId);

    void requestRoomClose(Long roomId, String hostId, String closeNotice, String closeReason);

    void processScheduledOttJobs();

    void sendChatMessage(Long roomId, String senderId, String messageContent);

    void markChatRoomAsRead(Long roomId, String loginId);
}
