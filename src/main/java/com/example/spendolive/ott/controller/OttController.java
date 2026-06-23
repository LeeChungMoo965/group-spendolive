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
    public String friends(Model model, HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        model.addAttribute("serviceList", ottService.getShareableServices());
        model.addAttribute("myRoomList", ottService.getMyRooms(loginId));
        model.addAttribute("hostedRoomList", ottService.getHostedRooms(loginId));
        model.addAttribute("settlementList", ottService.getMySettlements(loginId));
        model.addAttribute("selectedSettlementMonth", YearMonth.now().toString());
        model.addAttribute("defaultDueDate", LocalDate.now().plusDays(7).toString());
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
                          Model model,
                          HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        model.addAttribute("tab", tab);
        model.addAttribute("serviceList", ottService.getShareableServices());
        model.addAttribute("recruitRoomList", ottService.getRecruitRooms(loginId));
        model.addAttribute("hostedRoomList", ottService.getHostedRooms(loginId));
        model.addAttribute("hostedRoomMemberList", ottService.getHostedRoomMembers(loginId));
        model.addAttribute("settlementList", ottService.getMySettlements(loginId));
        model.addAttribute("selectedSettlementMonth", YearMonth.now().toString());
        model.addAttribute("defaultDueDate", LocalDate.now().plusDays(7).toString());
        model.addAttribute("body_page", "/WEB-INF/views/ott/ottRecruit.jsp");
        return "common/layout";
    }

    @PostMapping("/ott/recruit/create.do")
    public String createRecruitRoom(@ModelAttribute OttRoomDTO roomDTO, HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
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

        ottService.applyRecruitRoom(roomId, loginId);
        return "redirect:/spendolive/ott/recruit.do?tab=apply&result=applied";
    }

    @PostMapping("/ott/recruit/application/approve.do")
    public String approveApplication(@RequestParam("roomMemberId") Long roomMemberId, HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        ottService.approveApplication(roomMemberId, loginId);
        return "redirect:/spendolive/ott/recruit.do?tab=apply&result=approved";
    }

    @PostMapping("/ott/recruit/application/reject.do")
    public String rejectApplication(@RequestParam("roomMemberId") Long roomMemberId, HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        ottService.rejectApplication(roomMemberId, loginId);
        return "redirect:/spendolive/ott/recruit.do?tab=apply&result=rejected";
    }

    @GetMapping("/ott/chat/room.do")
    public String chatRoom(@RequestParam("roomId") Long roomId, Model model, HttpSession session) {
        String loginId = getLoginId(session);

        if (loginId == null) {
            return "redirect:/member/loginForm.do";
        }

        OttRoomDTO chatRoom = ottService.getChatRoom(roomId, loginId);
        if (chatRoom == null) {
            return "redirect:/spendolive/ott.do?error=noChatAccess";
        }

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
            settlementMonth = YearMonth.now().toString();
        }

        if (dueDate == null || dueDate.isBlank()) {
            dueDate = LocalDate.now().plusDays(7).toString();
        }

        ottService.requestSettlement(roomId, loginId, settlementMonth, dueDate);

        if ("friends".equals(returnPage)) {
            return "redirect:/spendolive/ott/friends.do?result=settlementRequested";
        }

        return "redirect:/spendolive/ott/recruit.do?tab=settlement&result=settlementRequested";
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
