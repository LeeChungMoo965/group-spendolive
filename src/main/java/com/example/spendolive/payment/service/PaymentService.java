package com.example.spendolive.payment.service;

import java.util.List;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttRoomMemberDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.payment.domain.*;

public interface PaymentService {
  //  public boolean processWithdraw(SettlementPaymentVO paymentInfo,  MemberVO memberInfo) throws Exception;
    public List<OttRoomDTO> selectTodaysettlement(String status) throws  Exception;
    public void issueAndSaveBillingKey(String customerKey, String authKey, String userId) throws Exception;
    public void executeAutomaticPayment(String userId, int total, int room_id, int fee, int base, int settlement_id, String host_id) throws Exception;
    public SettlementPaymentVO getSettlement_PaymentByRoomId(String userId, int roomId) throws Exception;
    public OttSettlementDTO selectMySettlements(int roomId)  throws Exception;
    public void registerSubMall(String userId, String bankCode, String accNum, String holderName,MemberVO memberVO) throws Exception;
    public String updateExcrow(int roomId) throws Exception;
    public String roomMemberByroomIdCount(int roomId, String userId) throws Exception;
    public List<OttRoomMemberDTO> selectTodaysettlementmember(String status) throws Exception;
    public boolean processWithdraw(SettlementPaymentVO paymentInfo, MemberVO memberInfo) throws Exception;// 금결원 출금이체 프로세스 (테스트 권환 문제로 보류)
    public void updateTodaysettlementroommemberlate(int roomId,String userId,int late_day) throws Exception;
    public OttRoomDTO selectRoomByRoomId(int roomId) throws Exception;
    
  }
