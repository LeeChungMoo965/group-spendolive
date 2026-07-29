package com.example.spendolive.mypage.service;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.Expense.service.ExpenseService;
import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.domain.MemberAccountVO;
import com.example.spendolive.member.domain.MemberCardVO;
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
    private final ExpenseService expenseService;

    public MyPageServiceImpl(MemberService memberService,
                             MyPageRepository myPageRepository,
                             MyPageReportRepository myPageReportRepository,
                             OttService ottService,
                             ExpenseService expenseService) {
        this.memberService = memberService;
        this.myPageRepository = myPageRepository;
        this.myPageReportRepository = myPageReportRepository;
        this.ottService = ottService;
        this.expenseService = expenseService;
    }

    @Override
    public MyPageDTO getMyPage(String loginId) throws Exception {
    
        MemberVO memberInfo = memberService.getMemberById(loginId);
    
        /* =========================================================
           [마이페이지 계좌·카드 연결 추가 시작]
           담당자가 만든 계좌·카드 목록 조회 메서드를 호출한다.
           조회 결과가 null 또는 빈 목록이어도 마이페이지가 500 오류 없이 열리게 처리한다.
           첫 번째 계좌는 오픈뱅킹 연결 여부 확인용으로만 사용한다.
           ========================================================= */
        List<MemberAccountVO> accountInfoList = memberService.getAccountById(loginId);
        List<MemberCardVO> cardInfoList = memberService.getCardById(loginId);

        accountInfoList = accountInfoList == null ? List.of() : accountInfoList;
        cardInfoList = cardInfoList == null ? List.of() : cardInfoList;
        MemberAccountVO linkedAccountInfo = accountInfoList.isEmpty() ? null : accountInfoList.get(0);
        /* [마이페이지 계좌·카드 연결 추가 끝] */
        MyPageDTO myPage = new MyPageDTO();
    
        myPage.setMemberInfo(memberInfo);
        myPage.setProfileInitial(makeProfileInitial(memberInfo));
        // 메인·지출관리와 같은 ExpenseService 결과를 합산한다.
        // 반복 지출은 화면에서 자동 생성되므로 DB SUM만 사용하면 마이페이지 금액과 달라질 수 있다.
        int thisMonthExpenseTotal = memberInfo == null
                ? 0
                : expenseService.getExpenseList(
                        Long.valueOf(memberInfo.getMember_id()),
                        YearMonth.now().toString()
                  ).stream()
                   .mapToInt(expense -> expense.getAmount() == null ? 0 : expense.getAmount())
                   .sum();

        // 현재 달 예산을 조회해 지출 대비 사용률을 계산한다.
        int thisMonthBudget = memberInfo == null
                ? 0
                : expenseService.getMonthlyBudget(
                        Long.valueOf(memberInfo.getMember_id()),
                        YearMonth.now().toString()
                );

        int thisMonthBudgetPercent = thisMonthBudget <= 0
                ? 0
                : (int) Math.round(thisMonthExpenseTotal * 100.0 / thisMonthBudget);

        // 계산 결과를 마이페이지 DTO에 저장한다.
        myPage.setThisMonthExpenseTotal(thisMonthExpenseTotal);
        myPage.setThisMonthBudget(thisMonthBudget);
        myPage.setThisMonthBudgetPercent(thisMonthBudgetPercent);
    
        myPage.setAccountConnected(isAccountConnected(linkedAccountInfo));
    
        myPage.setOpenBankUserSeq(
                linkedAccountInfo == null
                        ? null
                        : linkedAccountInfo.getOpen_bank_user_seq()
        );
        /* [마이페이지 계좌·카드 연결 추가] JSP로 전달할 전체 목록을 DTO에 저장한다. */
        myPage.setAccountList(accountInfoList);
        myPage.setCardList(cardInfoList);
    
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
