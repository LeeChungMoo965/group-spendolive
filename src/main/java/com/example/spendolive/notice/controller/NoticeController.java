package com.example.spendolive.notice.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.notice.domain.NoticeDTO;
import com.example.spendolive.notice.service.NoticeService;

@Controller
@RequestMapping("/spendolive/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /* ─── 공지 센터 메인 ──────────────────────────────────── */
    @GetMapping("/center.do")
    public ModelAndView noticeCenter(
            @RequestParam(value = "tab", required = false, defaultValue = "notice") String tab,
            HttpSession session) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        String id = (memberInfo != null) ? memberInfo.getId() : null;

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/notice/noticeCenter.jsp");
        mav.addObject("loginYn", memberInfo != null);
        mav.addObject("tab", tab);

        try {
            mav.addObject("noticeList",    noticeService.getNoticeList(id));
            mav.addObject("noticeCount",   noticeService.getNoticeCount());
            mav.addObject("importantCount", noticeService.getPinnedCount());
        } catch (Exception e) {
            System.err.println("[NoticeController.noticeCenter] 공지 로드 실패: " + e.getMessage());
            mav.addObject("noticeList",    List.of());
            mav.addObject("noticeCount",   0);
            mav.addObject("importantCount", 0);
            mav.addObject("errorMsg", "공지사항을 불러오는 중 오류가 발생했습니다.");
        }

        return mav;
    }

    /* ─── 공지 상세 ───────────────────────────────────────── */
    @GetMapping("/detail.do")
    public ModelAndView noticeDetail(
            @RequestParam(value = "noticeId", required = false, defaultValue = "0") int noticeId,
            HttpSession session) {

        if (noticeId <= 0) {
            ModelAndView mav = new ModelAndView("common/layout");
            mav.addObject("body_page", "/WEB-INF/views/notice/noticeCenter.jsp");
            mav.addObject("errorMsg", "잘못된 공지 번호입니다.");
            mav.addObject("loginYn", session.getAttribute("memberInfo") != null);
            return mav;
        }

        NoticeDTO notice = null;
        try {
            notice = noticeService.getNoticeDetail(noticeId);
        } catch (Exception e) {
            System.err.println("[NoticeController.noticeDetail] 조회 실패: " + e.getMessage());
        }

        if (notice == null) {
            ModelAndView mav = new ModelAndView("common/layout");
            mav.addObject("body_page", "/WEB-INF/views/notice/noticeCenter.jsp");
            mav.addObject("errorMsg", "존재하지 않는 공지사항입니다.");
            mav.addObject("loginYn", session.getAttribute("memberInfo") != null);
            return mav;
        }

        // 로그인 사용자 읽음 처리
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo != null && memberInfo.getId() != null) {
            try {
                noticeService.readNotice(noticeId, memberInfo.getId());
            } catch (Exception e) {
                System.err.println("[NoticeController.noticeDetail] 읽음 처리 실패: " + e.getMessage());
            }
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/notice/noticeDetail.jsp");
        mav.addObject("notice", notice);
        mav.addObject("loginYn", memberInfo != null);
        return mav;
    }

    /* ─── AJAX: 전체 공지 목록 ────────────────────────────── */
    @GetMapping("/ajax/noticeList.do")
    @ResponseBody
    public List<NoticeDTO> ajaxNoticeList(HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        String id = (memberInfo != null) ? memberInfo.getId() : null;

        try {
            return noticeService.getNoticeList(id);
        } catch (Exception e) {
            System.err.println("[NoticeController.ajaxNoticeList] 오류: " + e.getMessage());
            return List.of();
        }
    }

    /* ─── AJAX: 중요 공지 목록 ────────────────────────────── */
    @GetMapping("/ajax/importantList.do")
    @ResponseBody
    public List<NoticeDTO> ajaxImportantList(HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        String id = (memberInfo != null) ? memberInfo.getId() : null;

        try {
            return noticeService.getImportantList(id);
        } catch (Exception e) {
            System.err.println("[NoticeController.ajaxImportantList] 오류: " + e.getMessage());
            return List.of();
        }
    }

    /* ─── AJAX: 안 읽은 공지 목록 ────────────────────────── */
    @GetMapping("/ajax/unreadNoticeList.do")
    @ResponseBody
    public List<NoticeDTO> ajaxUnreadNoticeList(HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null || memberInfo.getId() == null) {
            return List.of();
        }

        try {
            return noticeService.getUnreadNoticeList(memberInfo.getId());
        } catch (Exception e) {
            System.err.println("[NoticeController.ajaxUnreadNoticeList] 오류: " + e.getMessage());
            return List.of();
        }
    }

    /* ─── AJAX: 찜 토글 ───────────────────────────────────── */
    @PostMapping("/ajax/star.do")
    @ResponseBody
    public Map<String, String> toggleNoticeStar(
            @RequestParam(value = "noticeId", required = false, defaultValue = "0") int noticeId,
            HttpSession session) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");

        if (memberInfo == null || memberInfo.getId() == null) {
            return Map.of("result", "LOGIN_REQUIRED");
        }
        if (noticeId <= 0) {
            return Map.of("result", "INVALID_PARAM");
        }

        try {
            noticeService.toggleNoticeStar(noticeId, memberInfo.getId());
            return Map.of("result", "OK");
        } catch (Exception e) {
            System.err.println("[NoticeController.toggleNoticeStar] 오류: " + e.getMessage());
            return Map.of("result", "ERROR");
        }
    }
}