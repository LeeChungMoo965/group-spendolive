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

    // 빠른 참가용 방 찾기
    Long findQuickJoinRecruitRoomId(Long ottServiceId, String loginId);

    OttRoomDTO getRoomByInviteCode(String inviteCode);


    void requestSettlement(Long roomId, String hostId, String settlementMonth, String dueDate); // 정산 요청 생성용 서비스 메서드

    void markPaymentPaid(Long paymentId, String loginId);

    void completePaidRoomEntry(Long roomId, String loginId); // 결제 완료 후 방 입장

    void requestRoomClose(Long roomId, String hostId, String closeNotice, String closeReason);

    String reserveRoomLeave(Long roomId, String loginId);

    String cancelRoomLeave(Long roomId, String loginId);

    void processScheduledOttJobs();

    void sendChatMessage(Long roomId, String senderId, String messageContent);

    void markChatRoomAsRead(Long roomId, String loginId);
}
