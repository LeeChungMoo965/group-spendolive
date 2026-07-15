package com.example.spendolive.faq.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.faq.domain.FaqVO;
import com.example.spendolive.faq.service.FaqService;
import com.example.spendolive.member.domain.MemberVO;

@Controller
@RequestMapping("/spendolive/admin/faq")
public class AdminFaqController {

    private final FaqService faqService;

    public AdminFaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    private boolean isAdmin(HttpSession session) {
        MemberVO m = (MemberVO) session.getAttribute("memberInfo");
        return m != null && "ADMIN".equals(m.getRole());
    }

    /* ─── 목록 ─────────────────────────────────────────────── */
    @GetMapping("/list.do")
    public ModelAndView list(HttpSession session) {
        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/adminFaqList.jsp");
        try {
            mav.addObject("faqList", faqService.getAllFaqList());
            mav.addObject("faqGroups", faqService.getAllFaqGroupedByCategory());
        } catch (Exception e) {
            mav.addObject("faqList", List.of());
            mav.addObject("faqGroups", Map.of());
            mav.addObject("errorMsg", "FAQ 목록을 불러오는 중 오류가 발생했습니다.");
        }
        return mav;
    }

    /* ─── 작성 폼 ───────────────────────────────────────────── */
    @GetMapping("/write.do")
    public ModelAndView write(HttpSession session) {
        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/adminFaqWrite.jsp");
        return mav;
    }

    /* ─── 등록 처리 ─────────────────────────────────────────── */
    @PostMapping("/insert.do")
    public ModelAndView insert(
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "question",  required = false) String question,
            @RequestParam(value = "answer",    required = false) String answer,
            @RequestParam(value = "useYn",     defaultValue = "N") String useYn,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");

        if (category == null || category.isBlank()
                || question == null || question.isBlank()
                || answer == null || answer.isBlank()) {
            ra.addFlashAttribute("errorMsg", "카테고리, 질문, 답변을 모두 입력해 주세요.");
            return new ModelAndView("redirect:/spendolive/admin/faq/write.do");
        }
        if (!"Y".equals(useYn)) useYn = "N";

        FaqVO faq = new FaqVO();
        faq.setCategory(category);
        faq.setQuestion(question.strip());
        faq.setAnswer(answer.strip());
        faq.setSortOrder(faqService.getNextSortOrder(category)); // 항상 해당 카테고리 맨 뒤에 추가
        faq.setUseYn(useYn);

        try {
            faqService.insertFaq(faq);
            ra.addFlashAttribute("msg", "FAQ가 등록되었습니다.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("errorMsg", "등록 중 오류가 발생했습니다.");
            return new ModelAndView("redirect:/spendolive/admin/faq/write.do");
        }
        return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
    }

    /* ─── 수정 폼 ───────────────────────────────────────────── */
    @GetMapping("/edit.do")
    public ModelAndView edit(
            @RequestParam(value = "faq_id", defaultValue = "0") int faq_id,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (faq_id <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 FAQ 번호입니다.");
            return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
        }

        FaqVO faq = null;
        try { faq = faqService.getFaqDetail(faq_id); } catch (Exception ignored) {}

        if (faq == null) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 FAQ입니다.");
            return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/admin/adminFaqWrite.jsp");
        mav.addObject("faq", faq);
        return mav;
    }

    /* ─── 수정 처리 ─────────────────────────────────────────── */
    @PostMapping("/update.do")
    public ModelAndView update(
            @RequestParam(value = "faq_id",     defaultValue = "0") int faq_id,
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "question",  required = false) String question,
            @RequestParam(value = "answer",    required = false) String answer,
            @RequestParam(value = "useYn",     defaultValue = "N") String useYn,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (faq_id <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 FAQ 번호입니다.");
            return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
        }
        if (category == null || category.isBlank()
                || question == null || question.isBlank()
                || answer == null || answer.isBlank()) {
            ra.addFlashAttribute("errorMsg", "카테고리, 질문, 답변을 모두 입력해 주세요.");
            return new ModelAndView("redirect:/spendolive/admin/faq/edit.do?faq_id=" + faq_id);
        }
        if (!"Y".equals(useYn)) useYn = "N";

        FaqVO faq = new FaqVO();
        faq.setFaqId(faq_id);
        faq.setCategory(category);
        faq.setQuestion(question.strip());
        faq.setAnswer(answer.strip());
        faq.setUseYn(useYn);

        try {
            faqService.updateFaq(faq);
            ra.addFlashAttribute("msg", "FAQ가 수정되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "수정 중 오류가 발생했습니다.");
            return new ModelAndView("redirect:/spendolive/admin/faq/edit.do?faq_id=" + faq_id);
        }
        return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
    }

    /* ─── 순서 위로 ─────────────────────────────────────────── */
    @PostMapping("/moveUp.do")
    public ModelAndView moveUp(
            @RequestParam(value = "faq_id", defaultValue = "0") int faq_id,
            HttpSession session) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (faq_id > 0) faqService.moveFaqUp(faq_id);
        return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
    }

    /* ─── 순서 아래로 ───────────────────────────────────────── */
    @PostMapping("/moveDown.do")
    public ModelAndView moveDown(
            @RequestParam(value = "faq_id", defaultValue = "0") int faq_id,
            HttpSession session) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (faq_id > 0) faqService.moveFaqDown(faq_id);
        return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
    }

    /* ─── 삭제 처리 ─────────────────────────────────────────── */
    @PostMapping("/delete.do")
    public ModelAndView delete(
            @RequestParam(value = "faq_id", defaultValue = "0") int faq_id,
            HttpSession session, RedirectAttributes ra) {

        if (!isAdmin(session)) return new ModelAndView("redirect:/spendolive/main.do");
        if (faq_id <= 0) {
            ra.addFlashAttribute("errorMsg", "잘못된 FAQ 번호입니다.");
            return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
        }
        try {
            faqService.deleteFaq(faq_id);
            ra.addFlashAttribute("msg", "FAQ가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "삭제 중 오류가 발생했습니다.");
        }
        return new ModelAndView("redirect:/spendolive/admin/faq/list.do");
    }
}
