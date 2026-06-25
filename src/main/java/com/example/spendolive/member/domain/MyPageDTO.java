package com.example.spendolive.member.domain;

import java.util.List;

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
    private int warningCount;
    private int myReportCount;
    private List<MyPageReportDTO> myReportList;
    private List<OttRoomDTO> friendRoomList;
    private List<OttRoomDTO> hostedRecruitRoomList;
    private List<OttRoomDTO> joinedRecruitRoomList;
}
