package com.example.spendolive.mypage.domain;

import java.util.List;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.domain.MemberAccountVO;
import com.example.spendolive.member.domain.MemberCardVO;
import com.example.spendolive.ott.domain.OttRoomDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPageDTO {

    private MemberVO memberInfo;
    private String profileInitial;
    private int thisMonthExpenseTotal;

    // 마이페이지에 표시할 이번 달 예산과 사용률
    private int thisMonthBudget;
    private int thisMonthBudgetPercent;

    private boolean accountConnected;
    private String openBankUserSeq;

    /* =========================================================
       [마이페이지 계좌·카드 연결 추가]
       담당자 로직에서 조회한 계좌·카드 목록을 Controller와 JSP까지 전달한다.
       ========================================================= */
    private List<MemberAccountVO> accountList;
    private List<MemberCardVO> cardList;
    
    private int warning_count;
    private int myReportCount;
    private List<MyPageReportDTO> myReportList;
    private List<OttRoomDTO> friendRoomList;
    private List<OttRoomDTO> hostedRecruitRoomList;
    private List<OttRoomDTO> joinedRecruitRoomList;
}
