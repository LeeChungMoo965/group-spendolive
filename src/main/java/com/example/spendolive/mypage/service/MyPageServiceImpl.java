package com.example.spendolive.mypage.service;

import org.springframework.stereotype.Service;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.mypage.domain.MyPageDTO;
import com.example.spendolive.mypage.repository.MyPageReportRepository;
import com.example.spendolive.mypage.repository.MyPageRepository;
import com.example.spendolive.ott.service.OttService;

@Service
public class MyPageServiceImpl implements MyPageService {

    private final MemberService memberService;
    private final MyPageRepository myPageRepository;
    private final MyPageReportRepository myPageReportRepository;
    private final OttService ottService;

    public MyPageServiceImpl(MemberService memberService,
                             MyPageRepository myPageRepository,
                             MyPageReportRepository myPageReportRepository,
                             OttService ottService) {
        this.memberService = memberService;
        this.myPageRepository = myPageRepository;
        this.myPageReportRepository = myPageReportRepository;
        this.ottService = ottService;
    }

    @Override
    public MyPageDTO getMyPage(String loginId) throws Exception {
        MemberVO memberInfo = memberService.getMemberById(loginId);
        MyPageDTO myPage = new MyPageDTO();

        myPage.setMemberInfo(memberInfo);
        myPage.setProfileInitial(makeProfileInitial(memberInfo));
        myPage.setThisMonthExpenseTotal(memberInfo == null ? 0 : myPageRepository.selectThisMonthExpenseTotal(memberInfo.getMember_id()));
        myPage.setAccountConnected(isAccountConnected(memberInfo));
        myPage.setWarningCount(Math.max(
                memberInfo == null ? 0 : memberInfo.getWarning_count(),
                myPageReportRepository.selectWarningCount(loginId)
        ));
        myPage.setMyReportCount(myPageReportRepository.selectMyReportCount(loginId));
        myPage.setMyReportList(myPageReportRepository.selectMyReportList(loginId));
        myPage.setFriendRoomList(ottService.getFriendRooms(loginId));
        myPage.setHostedRecruitRoomList(ottService.getHostedRecruitRooms(loginId));
        myPage.setJoinedRecruitRoomList(ottService.getJoinedRecruitRooms(loginId));

        return myPage;
    }

    private boolean isAccountConnected(MemberVO memberInfo) {
        return memberInfo != null
                && memberInfo.getOpen_bank_token() != null
                && !memberInfo.getOpen_bank_token().isBlank()
                && memberInfo.getOpen_bank_user_seq_no() != null
                && !memberInfo.getOpen_bank_user_seq_no().isBlank();
    }

    private String makeProfileInitial(MemberVO memberInfo) {
        if (memberInfo == null) {
            return "회";
        }

        String name = memberInfo.getMember_name();
        if (name != null && !name.isBlank()) {
            return name.substring(0, 1);
        }

        String nickname = memberInfo.getNickname();
        if (nickname != null && !nickname.isBlank()) {
            return nickname.substring(0, 1);
        }

        return "회";
    }
}
