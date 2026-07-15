package com.example.spendolive.payment.repository;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.payment.domain.*;
public interface PaymentRepository {
    public List<OttRoomDTO> selectTodaysettlement(int day,String status) throws  Exception;
    public void updateEscrowStatus(int room_id);
    public void updatePaymentStatus(SettlementPaymentVO paymentInfo);
    public SettlementPaymentVO settlement_paymentByroomId(String userId, int room_id) throws DataAccessException;
    public void insertEscrow(EscrowPayoutVO escrow);
    public OttSettlementDTO settlementByroomId (int room_id) throws  Exception;
    public void insertPlatfoem_Revenue(PlatformRevenueVO revenueInfo);
    public void insertSeller(SellerAccountVO sellerInfo);
    public void updatSettlementStatus(int room_id);
    public String roomMemberByroomIdCount (int room_id, String userId) throws DataAccessException;
    public List<OttRoomMemberDTO> selectTodaysettlementMember(int day, String status) throws Exception;
}
