package com.example.spendolive.ott.repository;

import java.util.List;

import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttChatRoomDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttServiceDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;

public interface OttRepository {
    List<OttServiceDTO> selectShareableServices();

    OttServiceDTO selectOttServiceRule(Long ottServiceId);

    List<OttRoomDTO> selectRecruitRooms(String loginId);

    List<OttRoomDTO> selectFriendRooms(String loginId);

    List<OttRoomDTO> selectHostedFriendRooms(String loginId);

    List<OttRoomDTO> selectHostedRecruitRooms(String loginId);

    List<OttRoomDTO> selectJoinedRecruitRooms(String loginId);

    List<OttRoomDTO> selectMyRooms(String loginId);

    List<OttRoomDTO> selectHostedRooms(String loginId);

    List<OttRoomMemberDTO> selectHostedRoomMembers(String loginId);

    List<OttSettlementDTO> selectMySettlements(String loginId);

    List<OttSettlementDTO> selectMySettlements(String loginId, String roomMode);

    List<OttSettlementDTO> selectHostedSettlementPayments(String loginId, String roomMode);

    List<OttChatRoomDTO> selectMyChatRooms(String loginId);

    List<OttChatMessageDTO> selectChatMessages(Long roomId, String loginId);

    OttRoomDTO selectChatRoom(Long roomId, String loginId);

    int countRecruitRooms();

    int countMyRooms(String loginId);

    int countUnreadChatMessages(String loginId);

    Long insertRoom(OttRoomDTO roomDTO, String loginId, String status);

    void insertHostMember(Long roomId, String loginId);

    void applyRoom(Long roomId, String loginId);

    void approveApplication(Long roomMemberId, String hostId);

    void rejectApplication(Long roomMemberId, String hostId);

    void createSettlement(Long roomId, String hostId, String settlementMonth, String dueDate);

    void markPaymentPaid(Long paymentId, String loginId);

    void requestRoomClose(Long roomId, String hostId, String closeNotice, String closeReason);

    void processScheduledOttJobs();

    void insertChatMessage(Long roomId, String senderId, String messageContent);

    void markChatRoomAsRead(Long roomId, String loginId);
}
