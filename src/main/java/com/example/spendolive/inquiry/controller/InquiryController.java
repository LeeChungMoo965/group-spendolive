package com.example.spendolive.inquiry.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.inquiry.domain.InquiryVO;
import com.example.spendolive.inquiry.service.InquiryService;
import com.example.spendolive.member.domain.MemberVO;

@Controller
@RequestMapping("/spendolive/inquiry")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    /* ─── 내 문의 조회 ────────────────────────────────────── */
    @GetMapping("/list.do")
    public ModelAndView inquiryList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/inquiry/inquiryList.jsp");

        try {
            int totalPages = inquiryService.getMyInquiryTotalPages(memberInfo.getId());
            int currentPage = Math.min(Math.max(page, 1), totalPages);

            mav.addObject("inquiryList", inquiryService.getMyInquiryList(memberInfo.getId(), currentPage));
            mav.addObject("currentPage", currentPage);
            mav.addObject("totalPages", totalPages);
        } catch (Exception e) {
            System.err.println("[InquiryController.inquiryList] 목록 로드 실패: " + e.getMessage());
            mav.addObject("inquiryList", List.of());
            mav.addObject("currentPage", 1);
            mav.addObject("totalPages", 1);
            mav.addObject("errorMsg", "문의 목록을 불러오는 중 오류가 발생했습니다.");
        }
        return mav;
    }

    /* ─── 문의 작성 폼 ────────────────────────────────────── */
    @GetMapping("/write.do")
    public ModelAndView inquiryWriteForm(HttpSession session) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/inquiry/inquiryWrite.jsp");
        return mav;
    }

    /* ─── 문의 등록 처리 ──────────────────────────────────── */
    @PostMapping("/write.do")
    public ModelAndView inquiryWrite(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "inquiryType", required = false) String inquiryType,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            HttpSession session, RedirectAttributes ra) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        if (category == null || category.isBlank()
                || inquiryType == null || inquiryType.isBlank()
                || title == null || title.isBlank()
                || content == null || content.isBlank()) {
            ra.addFlashAttribute("errorMsg", "카테고리, 유형, 제목, 내용을 모두 입력해 주세요.");
            return new ModelAndView("redirect:/spendolive/inquiry/write.do");
        }

        // TODO: 첨부파일 실제 저장 로직은 아직 없음 (attachments 배열만 받아둔 상태).
        // 파일 저장이 필요하면 별도 FileStorageService + inquiry_file_tb(07_inquiry.sql에 이미 준비됨) 연결.

        InquiryVO inquiry = new InquiryVO();
        inquiry.setId(memberInfo.getId());
        inquiry.setCategory(category);
        inquiry.setInquiryType(inquiryType);
        inquiry.setTitle(title.strip());
        inquiry.setContent(content.strip());

        try {
            inquiryService.writeInquiry(inquiry);
            ra.addFlashAttribute("msg", "문의가 접수되었습니다. 답변까지 영업일 기준 1~2일 소요됩니다.");
        } catch (DataAccessException e) {
            System.err.println("[InquiryController.inquiryWrite] 등록 실패: " + e.getMessage());
            ra.addFlashAttribute("errorMsg", "문의 접수 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return new ModelAndView("redirect:/spendolive/inquiry/write.do");
        }

        return new ModelAndView("redirect:/spendolive/inquiry/list.do");
    }

    /* ─── 문의 상세 ───────────────────────────────────────── */
    @GetMapping("/detail.do")
    public ModelAndView inquiryDetail(
            @RequestParam(value = "inquiryNo", defaultValue = "0") int inquiryNo,
            HttpSession session, RedirectAttributes ra) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        InquiryVO inquiry = null;
        try {
            inquiry = inquiryService.getInquiryDetail(inquiryNo);
        } catch (Exception e) {
            System.err.println("[InquiryController.inquiryDetail] 조회 실패: " + e.getMessage());
        }

        // 존재하지 않거나 본인 문의가 아니면 목록으로
        if (inquiry == null || !inquiry.getId().equals(memberInfo.getId())) {
            ra.addFlashAttribute("errorMsg", "존재하지 않거나 접근할 수 없는 문의입니다.");
            return new ModelAndView("redirect:/spendolive/inquiry/list.do");
        }

        // TODO: 상세 페이지 JSP(inquiryDetail.jsp)는 아직 없음. 만들어지면 아래 두 줄 활성화.
        // ModelAndView mav = new ModelAndView("common/layout");
        // mav.addObject("body_page", "/WEB-INF/views/inquiry/inquiryDetail.jsp");
        // mav.addObject("inquiry", inquiry);
        // return mav;
        return new ModelAndView("redirect:/spendolive/inquiry/list.do");
    }
}