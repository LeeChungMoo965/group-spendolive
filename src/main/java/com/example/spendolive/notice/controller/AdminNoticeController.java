package com.example.spendolive.notice.controller;

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

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.notice.domain.NoticeDTO;
import com.example.spendolive.notice.service.NoticeService;

@Controller
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    private final NoticeService noticeService;

    public AdminNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    private boolean isAdmin(HttpSession session) {
        MemberVO m = (MemberVO) session.getAttribute("memberInfo");
        return m != null && "ADMIN".equals(m.getRole());
    }

    /* ─── 목록 ─────────────────────────────────────────────── */
    @GetMapping("/list.do")
    public ModelAndView list(HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/faq_inquiry/adminNoticeList.jsp");
        try {
            mav.addObject("noticeList", noticeService.getNoticeList(null));
        } catch (Exception e) {
            mav.addObject("noticeList", List.of());
            mav.addObject("errorMsg", "공지 목록을 불러오는 중 오류가 발생했습니다.");
        }
        return mav;
    }

    /* ─── 작성 폼 ───────────────────────────────────────────── */
    @GetMapping("/write.do")
    public ModelAndView write(HttpSession session) {
        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/faq_inquiry/adminNoticeWrite.jsp");
        return mav;
    }

    /* ─── 등록 처리 ─────────────────────────────────────────── */
    @PostMapping("/insert.do")
    public ModelAndView insert(
            @RequestParam(value = "title",    required = false) String title,
            @RequestParam(value = "content",  required = false) String content,
            @RequestParam(value = "pinned_yn", defaultValue = "N") String pinned_yn,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");

        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            ra.addFlashAttribute("errorMsg", "제목과 내용을 모두 입력해 주세요.");
            return new ModelAndView("redirect:/admin/notice/write.do");
        }
        if (!"Y".equals(pinned_yn)) pinned_yn = "N";

        MemberVO m = (MemberVO) session.getAttribute("memberInfo");
        NoticeDTO dto = new NoticeDTO();
        dto.setTitle(title.strip());
        dto.setContent(content.strip());
        dto.setPinned_yn(pinned_yn);
        dto.setAdmin_id(m.getId());

        try {
            noticeService.insertNotice(dto);
            ra.addFlashAttribute("msg", "공지사항이 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return new ModelAndView("redirect:/admin/notice/write.do");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("errorMsg", "등록 중 오류가 발생했습니다.");
            return new ModelAndView("redirect:/admin/notice/write.do");
        }
        return new ModelAndView("redirect:/admin/notice/list.do");
    }

    /* ─── 수정 폼 ───────────────────────────────────────────── */
    @GetMapping("/edit.do")
    public ModelAndView edit(
            @RequestParam(value = "notice_id", defaultValue = "0") int notice_id,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (notice_id <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 공지 번호입니다.");
            return new ModelAndView("redirect:/admin/notice/list.do");
        }

        NoticeDTO notice = null;
        try { notice = noticeService.getNoticeDetail(notice_id); } catch (Exception ignored) {}

        if (notice == null) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 공지사항입니다.");
            return new ModelAndView("redirect:/admin/notice/list.do");
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/faq_inquiry/adminNoticeWrite.jsp");
        mav.addObject("notice", notice);
        return mav;
    }

    /* ─── 수정 처리 ─────────────────────────────────────────── */
    @PostMapping("/update.do")
    public ModelAndView update(
            @RequestParam(value = "notice_id", defaultValue = "0") int notice_id,
            @RequestParam(value = "title",    required = false) String title,
            @RequestParam(value = "content",  required = false) String content,
            @RequestParam(value = "pinned_yn", defaultValue = "N") String pinned_yn,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (notice_id <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 공지 번호입니다.");
            return new ModelAndView("redirect:/admin/notice/list.do");
        }
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            ra.addFlashAttribute("errorMsg", "제목과 내용을 모두 입력해 주세요.");
            return new ModelAndView("redirect:/admin/notice/edit.do?notice_id=" + notice_id);
        }
        if (!"Y".equals(pinned_yn)) pinned_yn = "N";

        NoticeDTO dto = new NoticeDTO();
        dto.setNotice_id(notice_id);
        dto.setTitle(title.strip());
        dto.setContent(content.strip());
        dto.setPinned_yn(pinned_yn);

        try {
            noticeService.updateNotice(dto);
            ra.addFlashAttribute("msg", "공지사항이 수정되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "수정 중 오류가 발생했습니다.");
            return new ModelAndView("redirect:/admin/notice/edit.do?notice_id=" + notice_id);
        }
        return new ModelAndView("redirect:/admin/notice/list.do");
    }

    /* ─── 삭제 처리 ─────────────────────────────────────────── */
    @PostMapping("/delete.do")
    public ModelAndView delete(
            @RequestParam(value = "notice_id", defaultValue = "0") int notice_id,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (notice_id <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 공지 번호입니다.");
            return new ModelAndView("redirect:/admin/notice/list.do");
        }
        try {
            noticeService.deleteNotice(notice_id);
            ra.addFlashAttribute("msg", "공지사항이 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "삭제 중 오류가 발생했습니다.");
        }
        return new ModelAndView("redirect:/admin/notice/list.do");
    }
}