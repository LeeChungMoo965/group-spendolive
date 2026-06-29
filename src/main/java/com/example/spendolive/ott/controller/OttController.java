package com.example.spendolive.ott.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttChatMessageDTO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.service.OttService;

@Controller
@RequestMapping("/spendolive")
public class OttController {

    private final OttService ottService;

    public OttController(OttService ottService) {
        this.ottService = ottService;
    }

    @GetMapping("/ott.do")
    public String ottMain(Model model, HttpSession session) {
        ottService.processScheduledOttJobs();
        String loginId = getLoginId(session);
        
        
        model.addAttribute("serviceList", ottService.getShareableServices());
        model.addAttribute("recruitRoomCount", ottService.getRecruitRoomCount());
        
        if (loginId != null) {
            model.addAttribute("myRoomCount", ottService.getMyRoomCount(loginId));
        } else {
            model.addAttribute("myRoomCount", 0);
        }

        model.addAttribute("body_page", "/WEB-INF/views/ott/ott.jsp");
        return "common/layout";
    }

    @GetMapping("/ott/friends.do")
    public String friends(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        ottService.processScheduledOttJobs();
        String loginId = getLoginId(session);
        
        if (loginId == null) {   
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요한 기능 입니다 로그인을 해주세요 !");
            return "redirect:/member/loginForm.do";
        }
        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String open_bank_token = memberVO.getOpen_bank_token();
        String open_bank_user_seq_no = memberVO.getOpen_bank_user_seq_no();
        if (open_bank_token  == null && open_bank_user_seq_no  == null) {

            redirectAttributes.addFlashAttribute("msg", "OTT관련 기능은 계좌연동이 필요합니다. 계좌연동을 해주세요 !");
            return "redirect:/spendolive/main.do";
        }
        addCommonOttModel(model, loginId);
        model.addAttribute("myRoomList", ottService.getFriendRooms(loginId));
        model.addAttribute("hostedRoomList", ottService.getHostedFriendRooms(loginId));
        model.addAttribute("settlementList", ottService.getFriendSettlements(loginId));
        model.addAttribute("body_page", "/WEB-INF/views/ott/ottFriends.jsp");
        return "common/layout";
    }

    @PostMapping("/ott/friends/create.do")
    public String createFriendRoom(@ModelAttribute OttRoomDTO roomDTO, HttpSession session) {
        String loginId = getLoginId(session);
        
        if (loginId == null) {
            
            return "redirect:/member/loginForm.do";
        }
        
        
        ottService.createFriendRoom(roomDTO, loginId);
        return "redirect:/spendolive/ott/friends.do?result=created";
    }

    @GetMapping("/ott/recruit.do")
    public String recruit(@RequestParam(value = "tab", required = false, defaultValue = "all") String tab,
                          @RequestParam(value = "ottServiceId", required = false) String ottServiceId,
                          @RequestParam(value = "roomNameKeyword", required = false) String roomNameKeyword,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        ottService.processScheduledOttJobs();
        String loginId = getLoginId(session);
        
        if (loginId == null) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요한 기능 입니다 로그인을 해주세요 !");
            return "redirect:/member/loginForm.do";
        }
        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String open_bank_token = memberVO.getOpen_bank_token();
        String open_bank_user_seq_no = memberVO.getOpen_bank_user_seq_no();
        if (open_bank_token  == null && open_bank_user_seq_no  == null) {

            redirectAttributes.addFlashAttribute("msg", "OTT관련 기능은 계좌연동이 필요합니다. 계좌연동을 해주세요 !");
            return "redirect:/spendolive/main.do";
        }
        Long selectedOttServiceId = parseOttServiceId(ottServiceId);

        addCommonOttModel(model, loginId);
        model.addAttribute("tab", tab);
        model.addAttribute("selectedOttServiceId", selectedOttServiceId);
        model.addAttribute("roomNameKeyword", roomNameKeyword);
        model.addAttribute("recruitRoomList", ottService.getRecruitRooms(loginId, selectedOttServiceId, roomNameKeyword));
        model.addAttribute("hostedRoomList", ottService.getHostedRecruitRooms(loginId));
        model.addAttribute("joinedRoomList", ottService.getJoinedRecruitRooms(loginId));
        model.addAttribute("hostedRoomMemberList", ottService.getHostedRoomMembers(loginId));
        model.addAttribute("settlementList", ottService.getRecruitSettlements(loginId));
        model.addAttribute("body_page", "/WEB-INF/views/ott/ottRecruit.jsp");
        return "common/layout";
    }

    @PostMapping("/ott/recruit/create.do")
    public String createRecruitRoom(@ModelAttribute OttRoomDTO roomDTO, HttpSession session, RedirectAttributes redirectAttributes) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            
            return "redirect:/member/loginForm.do";
        }
        MemberVO memberVO = (MemberVO) session.getAttribute("memberInfo");
        String open_bank_token = memberVO.getOpen_bank_token();
        String open_bank_user_seq_no = memberVO.getOpen_bank_user_seq_no();
        if (open_bank_token  == null && open_bank_user_seq_no  == null) {

            redirectAttributes.addFlashAttribute("msg", "OTT관련 기능은 계좌연동이 필요합니다. 계좌연동을 해주세요 !");
            return "redirect:/spendolive/main.do";
        }
        ottService.createRecruitRoom(roomDTO, loginId);
        return "redirect:/spendolive/ott/recruit.do?tab=all&result=created";
    }

    @PostMapping("/ott/recruit/apply.do")
    public String applyRecruitRoom(@RequestParam("roomId") Long roomId, HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        // 신청/승인 상태를 만들지 않고 결제 담당자가 연결할 화면으로 넘긴다.
        ottService.applyRecruitRoom(roomId, loginId);
        return redirectToRoomPayment(roomId, "RECRUIT", null);
    }

    @GetMapping("/ott/friends/invite.do")
    public String enterFriendRoomByInvite(@RequestParam("code") String inviteCode) {
        OttRoomDTO room = ottService.getRoomByInviteCode(inviteCode);

        if (room == null || !"FRIEND".equals(room.getRoomMode()) || "CLOSED".equals(room.getStatus())) {
            return "redirect:/spendolive/ott.do?error=invalidInvite";
        }

        return redirectToRoomPayment(room.getRoomId(), "FRIEND", room.getInviteCode());
    }

    @GetMapping("/ott/chat/room.do")
    public String chatRoom(@RequestParam("roomId") Long roomId, Model model, HttpSession session) {
        ottService.processScheduledOttJobs();
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        OttRoomDTO chatRoom = ottService.getChatRoom(roomId, loginId);
        if (chatRoom == null) {
            return "redirect:/spendolive/ott.do?error=noChatAccess";
        }

        model.addAttribute("loginId", loginId);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("chatMessageList", ottService.getChatMessages(roomId, loginId));
        ottService.markChatRoomAsRead(roomId, loginId);
        model.addAttribute("body_page", "/WEB-INF/views/ott/ottChatRoom.jsp");
        return "common/layout";
    }

    @GetMapping("/ott/chat/messages.do")
    @ResponseBody
    public List<OttChatMessageDTO> chatMessages(@RequestParam("roomId") Long roomId, HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return Collections.emptyList();
        }

        List<OttChatMessageDTO> messages = ottService.getChatMessages(roomId, loginId);
        ottService.markChatRoomAsRead(roomId, loginId);
        return messages;
    }

    @PostMapping("/ott/chat/send.do")
    public String sendChatMessage(@RequestParam("roomId") Long roomId,
                                  @RequestParam("messageContent") String messageContent,
                                  HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        ottService.sendChatMessage(roomId, loginId, messageContent);
        return "redirect:/spendolive/ott/chat/room.do?roomId=" + roomId;
    }

    @PostMapping("/ott/settlement/request.do")
    public String requestSettlement(@RequestParam("roomId") Long roomId,
                                    @RequestParam(value = "settlementMonth", required = false) String settlementMonth,
                                    @RequestParam(value = "dueDate", required = false) String dueDate,
                                    @RequestParam(value = "returnPage", required = false, defaultValue = "recruit") String returnPage,
                                    HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        if (settlementMonth == null || settlementMonth.isBlank()) {
            settlementMonth = YearMonth.now().plusMonths(1).toString();
        }

        ottService.requestSettlement(roomId, loginId, settlementMonth, dueDate);

        if ("friends".equals(returnPage)) {
            return "redirect:/spendolive/ott/friends.do?result=settlementRequested";
        }

        return "redirect:/spendolive/ott/recruit.do?tab=settlement&result=settlementRequested";
    }

    @PostMapping("/ott/settlement/pay.do")
    public String paySettlement(@RequestParam("paymentId") Long paymentId,
                                @RequestParam(value = "returnPage", required = false, defaultValue = "recruit") String returnPage,
                                HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        ottService.markPaymentPaid(paymentId, loginId);

        if ("friends".equals(returnPage)) {
            return "redirect:/spendolive/ott/friends.do?result=paid";
        }

        return "redirect:/spendolive/ott/recruit.do?tab=settlement&result=paid";
    }

    @PostMapping("/ott/room/close-request.do")
    public String closeRoom(@RequestParam("roomId") Long roomId,
                            @RequestParam(value = "closeNotice", required = false) String closeNotice,
                            @RequestParam(value = "closeReason", required = false) String closeReason,
                            @RequestParam(value = "returnPage", required = false, defaultValue = "friends") String returnPage,
                            HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        ottService.requestRoomClose(roomId, loginId, closeNotice, closeReason);

        if ("recruit".equals(returnPage)) {
            return "redirect:/spendolive/ott/recruit.do?tab=apply&result=closeRequested";
        }

        return "redirect:/spendolive/ott/friends.do?result=closeRequested";
    }


    private String redirectToRoomPayment(Long roomId, String roomMode, String inviteCode) {
        String redirectUrl = "redirect:/payment/detail.do?roomId=" + roomId + "&roomMode=" + roomMode;

        if (inviteCode != null && !inviteCode.isBlank()) {
            redirectUrl += "&inviteCode=" + inviteCode;
        }

        return redirectUrl;
    }

    private Long parseOttServiceId(String ottServiceId) {
        if (ottServiceId == null || ottServiceId.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(ottServiceId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void addCommonOttModel(Model model, String loginId) {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        LocalDate today = LocalDate.now();

        model.addAttribute("loginId", loginId);
        model.addAttribute("serviceList", ottService.getShareableServices());
        model.addAttribute("selectedSettlementMonth", nextMonth.toString());
        model.addAttribute("today", today.toString());
        model.addAttribute("settlementGuide", "OTT별 최고 멤버십 기준 금액을 N분의 1로 나누고 서비스 수수료 3%를 더해 정산합니다. 결제 마감일은 이용 시작일 5일 전으로 자동 계산됩니다.");
    }

    private String getLoginId(HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null) {
            return null;
        }

        if (memberInfo.getId() != null && !memberInfo.getId().isBlank()) {
            return memberInfo.getId();
        }

        return String.valueOf(memberInfo.getMember_id());
    }
}
