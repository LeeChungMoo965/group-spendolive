package com.example.spendolive.faq.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
        mav.addObject("body_page", "/WEB-INF/views/admin/faq_inquiry/adminFaqList.jsp");
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

    /* ════════════════════════════════════════════════════════════
       AJAX 전용 (페이지 이동 없이 JSON) — adminFaq.js가 호출.
       목록/작성폼/수정폼(GET)은 그대로 두고, 실제 등록/수정/순서변경/삭제만 여기서 처리.
       ════════════════════════════════════════════════════════════ */

    /** AJAX: FAQ 등록 */
    @PostMapping("/ajax/insert.do")
    @ResponseBody
    public ResponseEntity<?> ajaxInsert(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "answer",   required = false) String answer,
            @RequestParam(value = "useYn",    defaultValue = "N") String useYn,
            HttpSession session) {

        if (!isAdmin(session)) return forbidden();
        if (isBlank(category) || isBlank(question) || isBlank(answer)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("result", "INVALID_PARAM", "message", "카테고리, 질문, 답변을 모두 입력해 주세요."));
        }
        if (!"Y".equals(useYn)) useYn = "N";

        FaqVO faq = new FaqVO();
        faq.setCategory(category);
        faq.setQuestion(question.strip());
        faq.setAnswer(answer.strip());
        faq.setSort_order(faqService.getNextSortOrder(category));
        faq.setUse_yn(useYn);

        try {
            faqService.insertFaq(faq);
            return ResponseEntity.ok(Map.of("result", "OK", "message", "FAQ가 등록되었습니다."));
        } catch (DataAccessException e) {
            System.err.println("[AdminFaqController.ajaxInsert] 등록 실패: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("result", "ERROR", "message", "등록 중 오류가 발생했습니다."));
        }
    }

    /** AJAX: FAQ 수정 */
    @PostMapping("/ajax/update.do")
    @ResponseBody
    public ResponseEntity<?> ajaxUpdate(
            @RequestParam(value = "faq_id",   defaultValue = "0") int faq_id,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "answer",   required = false) String answer,
            @RequestParam(value = "useYn",    defaultValue = "N") String useYn,
            HttpSession session) {

        if (!isAdmin(session)) return forbidden();
        if (faq_id <= 0) return badId();
        if (isBlank(category) || isBlank(question) || isBlank(answer)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("result", "INVALID_PARAM", "message", "카테고리, 질문, 답변을 모두 입력해 주세요."));
        }
        if (!"Y".equals(useYn)) useYn = "N";

        FaqVO faq = new FaqVO();
        faq.setFaq_id(faq_id);
        faq.setCategory(category);
        faq.setQuestion(question.strip());
        faq.setAnswer(answer.strip());
        faq.setUse_yn(useYn);

        try {
            faqService.updateFaq(faq);
            return ResponseEntity.ok(Map.of("result", "OK", "message", "FAQ가 수정되었습니다."));
        } catch (Exception e) {
            System.err.println("[AdminFaqController.ajaxUpdate] 수정 실패: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("result", "ERROR", "message", "수정 중 오류가 발생했습니다."));
        }
    }

    /** AJAX: 순서 위로 */
    @PostMapping("/ajax/moveUp.do")
    @ResponseBody
    public ResponseEntity<?> ajaxMoveUp(
            @RequestParam(value = "faq_id", defaultValue = "0") int faq_id,
            HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        if (faq_id <= 0) return badId();
        try {
            faqService.moveFaqUp(faq_id);
            return ResponseEntity.ok(Map.of("result", "OK"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("result", "ERROR", "message", "순서 변경 중 오류가 발생했습니다."));
        }
    }

    /** AJAX: 순서 아래로 */
    @PostMapping("/ajax/moveDown.do")
    @ResponseBody
    public ResponseEntity<?> ajaxMoveDown(
            @RequestParam(value = "faq_id", defaultValue = "0") int faq_id,
            HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        if (faq_id <= 0) return badId();
        try {
            faqService.moveFaqDown(faq_id);
            return ResponseEntity.ok(Map.of("result", "OK"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("result", "ERROR", "message", "순서 변경 중 오류가 발생했습니다."));
        }
    }

    /** AJAX: FAQ 삭제 */
    @PostMapping("/ajax/delete.do")
    @ResponseBody
    public ResponseEntity<?> ajaxDelete(
            @RequestParam(value = "faq_id", defaultValue = "0") int faq_id,
            HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        if (faq_id <= 0) return badId();
        try {
            faqService.deleteFaq(faq_id);
            return ResponseEntity.ok(Map.of("result", "OK", "message", "FAQ가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("result", "ERROR", "message", "삭제 중 오류가 발생했습니다."));
        }
    }

    /* ── 공통 응답 헬퍼 ── */
    private boolean isBlank(String s) { return s == null || s.isBlank(); }
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("result", "FORBIDDEN", "message", "관리자만 접근할 수 있습니다."));
    }
    private ResponseEntity<?> badId() {
        return ResponseEntity.badRequest()
                .body(Map.of("result", "INVALID_PARAM", "message", "잘못된 FAQ 번호입니다."));
    }
}