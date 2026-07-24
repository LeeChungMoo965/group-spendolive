package com.example.spendolive.inquiry.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.inquiry.domain.InquiryVO;
import com.example.spendolive.inquiry.service.InquiryService;
import com.example.spendolive.member.domain.MemberVO;

@Controller
@RequestMapping("/admin/inquiry")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    public AdminInquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    private boolean isAdmin(HttpSession session) {
        MemberVO m = (MemberVO) session.getAttribute("memberInfo");
        return m != null && "ADMIN".equals(m.getRole());
    }

    /** 화면 필터 코드(all/wait/done/review) → DB status 값(WAIT/DONE/REVIEW, all은 null=필터 없음) */
    private String normalizeStatusFilter(String status) {
        if (status == null) return null;
        switch (status.toLowerCase()) {
            case "wait": return "WAIT";
            case "done": return "DONE";
            case "review": return "REVIEW";
            default: return null; // "all" 포함
        }
    }

    /* ─── 전체 문의 목록 ──────────────────────────────────── */
    @GetMapping("/list.do")
    public ModelAndView list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "status", defaultValue = "all") String status,
            HttpSession session) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/faq_inquiry/adminInquiryList.jsp");

        String normalizedStatus = normalizeStatusFilter(status);
        try {
            int totalPages = inquiryService.getAdminInquiryTotalPages(normalizedStatus);
            int currentPage = Math.min(Math.max(page, 1), totalPages);
            int totalCount = inquiryService.getAdminInquiryTotalCount(normalizedStatus);
            int pageSize = inquiryService.getAdminPageSize();

            mav.addObject("inquiryList", inquiryService.getAllInquiriesForAdmin(normalizedStatus, currentPage));
            mav.addObject("currentPage", currentPage);
            mav.addObject("totalPages", totalPages);
            mav.addObject("currentStatus", status.toLowerCase());
            // 목록은 최신순(내림차순)이라, 화면 맨 위 줄이 startNumber, 그 아래로 1씩 감소하며 매김 (오래된 문의=1)
            mav.addObject("startNumber", totalCount - (currentPage - 1) * pageSize);
        } catch (Exception e) {
            System.err.println("[AdminInquiryController.list] 목록 로드 실패: " + e.getMessage());
            mav.addObject("inquiryList", List.of());
            mav.addObject("currentPage", 1);
            mav.addObject("totalPages", 1);
            mav.addObject("currentStatus", "all");
            mav.addObject("startNumber", 0);
            mav.addObject("errorMsg", "문의 목록을 불러오는 중 오류가 발생했습니다.");
        }
        return mav;
    }

    /* ─── 문의 상세 + 답변 폼 ────────────────────────────── */
    @GetMapping("/detail.do")
    public ModelAndView detail(
            @RequestParam(value = "inquiryNo", defaultValue = "0") int inquiryNo,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (inquiryNo <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 문의 번호입니다.");
            return new ModelAndView("redirect:/admin/inquiry/list.do");
        }

        InquiryVO inquiry = null;
        try {
            inquiry = inquiryService.getInquiryDetail(inquiryNo);
        } catch (Exception e) {
            System.err.println("[AdminInquiryController.detail] 조회 실패: " + e.getMessage());
        }

        if (inquiry == null) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 문의입니다.");
            return new ModelAndView("redirect:/admin/inquiry/list.do");
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/faq_inquiry/adminInquiryDetail.jsp");
        mav.addObject("inquiry", inquiry);
        return mav;
    }

    /* ─── 답변 등록/수정 ─────────────────────────────────── */
    @PostMapping("/reply.do")
    public ModelAndView reply(
            @RequestParam(value = "inquiry_id", defaultValue = "0") int inquiry_id,
            @RequestParam(value = "reply_content", required = false) String reply_content,
            @RequestParam(value = "status", defaultValue = "DONE") String status,
            // 목록이 팝업(모달) 방식으로 바뀌면서, 답변 등록 후에는 상세 페이지가 아니라
            // 원래 보고 있던 목록(필터/페이지 유지)으로 돌아가야 하므로 폼에서 같이 넘겨받음
            @RequestParam(value = "listStatus", defaultValue = "all") String listStatus,
            @RequestParam(value = "listPage", defaultValue = "1") int listPage,
            HttpSession session, RedirectAttributes ra) {

        String backToList = "redirect:/admin/inquiry/list.do?status=" + listStatus + "&page=" + listPage;

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (inquiry_id <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 문의 번호입니다.");
            return new ModelAndView(backToList);
        }
        if (reply_content == null || reply_content.isBlank()) {
            ra.addFlashAttribute("errorMsg", "답변 내용을 입력해 주세요.");
            return new ModelAndView(backToList);
        }
        // 관리자가 고를 수 있는 상태는 DONE(답변완료) / REVIEW(검토중) 둘 중 하나로 제한
        if (!"DONE".equals(status) && !"REVIEW".equals(status)) {
            status = "DONE";
        }

        try {
            inquiryService.replyToInquiry(inquiry_id, reply_content.strip(), status);
            ra.addFlashAttribute("msg", "답변이 등록되었습니다.");
        } catch (DataAccessException e) {
            System.err.println("[AdminInquiryController.reply] 답변 등록 실패: " + e.getMessage());
            ra.addFlashAttribute("errorMsg", "답변 등록 중 오류가 발생했습니다.");
        }
        return new ModelAndView(backToList);
    }
}
