package com.example.spendolive.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.domain.MemberAccountVO;
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
    
        // 마이페이지 요청이 들어왔을 때만 계좌 테이블을 별도로 조회
        MemberAccountVO accountInfo = memberService.getAccountById(loginId);
    
        MyPageDTO myPage = new MyPageDTO();
    
        myPage.setMemberInfo(memberInfo);
        myPage.setProfileInitial(makeProfileInitial(memberInfo));
        myPage.setThisMonthExpenseTotal(
                memberInfo == null
                        ? 0
                        : myPageRepository.selectThisMonthExpenseTotal(
                                memberInfo.getMember_id()
                        )
        );
    
        myPage.setAccountConnected(isAccountConnected(accountInfo));
    
        myPage.setOpenBankUserSeq(
                accountInfo == null
                        ? null
                        : accountInfo.getOpen_bank_user_seq()
        );
    
        myPage.setWarning_count(Math.max(
                memberInfo == null ? 0 : memberInfo.getWarning_count(),
                myPageReportRepository.selectwarning_count(loginId)
        ));
    
        myPage.setMyReportCount(
                myPageReportRepository.selectMyReportCount(loginId)
        );
    
        myPage.setMyReportList(
                myPageReportRepository.selectMyReportList(loginId)
        );
    
        myPage.setFriendRoomList(
                ottService.getFriendRooms(loginId)
        );
    
        myPage.setHostedRecruitRoomList(
                ottService.getHostedRecruitRooms(loginId)
        );
    
        myPage.setJoinedRecruitRoomList(
                ottService.getJoinedRecruitRooms(loginId)
        );
    
        return myPage;
    }


    @Override
    @Transactional
    public void withdrawMember(String loginId) throws Exception {
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException("회원탈퇴 대상 아이디가 없습니다.");
        }

        int updatedCount = myPageRepository.withdrawMember(loginId);
        if (updatedCount == 0) {
            throw new IllegalStateException("이미 탈퇴했거나 존재하지 않는 회원입니다.");
        }
    }

    private boolean isAccountConnected(MemberAccountVO accountInfo) {
        return accountInfo != null
                && accountInfo.getOpen_bank_token() != null
                && !accountInfo.getOpen_bank_token().isBlank()
                && accountInfo.getOpen_bank_user_seq() != null
                && !accountInfo.getOpen_bank_user_seq().isBlank();
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
