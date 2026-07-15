package com.example.spendolive.mypage.domain;

import java.util.List;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttRoomDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPageDTO {

    private MemberVO memberInfo;
    private String profileInitial;
    private int thisMonthExpenseTotal;

    private boolean accountConnected;
    private String openBankUserSeq;
    
    private int warning_count;
    private int myReportCount;
    private List<MyPageReportDTO> myReportList;
    private List<OttRoomDTO> friendRoomList;
    private List<OttRoomDTO> hostedRecruitRoomList;
    private List<OttRoomDTO> joinedRecruitRoomList;
}
