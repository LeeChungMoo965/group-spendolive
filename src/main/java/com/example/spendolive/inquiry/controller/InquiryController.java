package com.example.spendolive.inquiry.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spendolive.inquiry.domain.InquiryFileVO;
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
            @RequestParam(value = "status", defaultValue = "all") String status,
            HttpSession session, RedirectAttributes ra) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            ra.addFlashAttribute("msg", "로그인이 필요한 기능입니다. 로그인 후 이용해 주세요.");
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/inquiry/inquiryList.jsp");

        // 화면 필터 코드(all/wait/done/review) → DB 저장값(WAIT/DONE/REVIEW, all은 필터 없음=null)
        String normalizedStatus = normalizeStatusFilter(status);

        try {
            int totalPages = inquiryService.getMyInquiryTotalPages(memberInfo.getId(), normalizedStatus);
            int currentPage = Math.min(Math.max(page, 1), totalPages);

            mav.addObject("inquiryList",
                    inquiryService.getMyInquiryList(memberInfo.getId(), currentPage, normalizedStatus));
            mav.addObject("currentPage", currentPage);
            mav.addObject("totalPages", totalPages);
            mav.addObject("currentStatus", status.toLowerCase()); // 필터 버튼 active 표시 + 페이지네이션 링크 유지용
        } catch (Exception e) {
            System.err.println("[InquiryController.inquiryList] 목록 로드 실패: " + e.getMessage());
            mav.addObject("inquiryList", List.of());
            mav.addObject("currentPage", 1);
            mav.addObject("totalPages", 1);
            mav.addObject("currentStatus", "all");
            mav.addObject("errorMsg", "문의 목록을 불러오는 중 오류가 발생했습니다.");
        }
        return mav;
    }

    /** 화면 필터 코드를 DB status 컬럼값으로 변환. all이거나 알 수 없는 값이면 null(필터 없음). */
    private String normalizeStatusFilter(String status) {
        if (status == null) return null;
        switch (status.toLowerCase()) {
            case "wait": return "WAIT";
            case "done": return "DONE";
            case "review": return "REVIEW";
            default: return null; // "all" 포함
        }
    }

    /* ─── 문의 작성 폼 ────────────────────────────────────── */
    @GetMapping("/write.do")
    public ModelAndView inquiryWriteForm(HttpSession session, RedirectAttributes ra) {
        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            ra.addFlashAttribute("msg", "로그인이 필요한 기능입니다. 로그인 후 이용해 주세요.");
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
            @RequestParam(value = "inquiry_type", required = false) String inquiry_type,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            HttpSession session, RedirectAttributes ra) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            ra.addFlashAttribute("msg", "로그인이 필요한 기능입니다. 로그인 후 이용해 주세요.");
            return new ModelAndView("redirect:/member/loginForm.do");
        }

        if (category == null || category.isBlank()
                || inquiry_type == null || inquiry_type.isBlank()
                || title == null || title.isBlank()
                || content == null || content.isBlank()) {
            ra.addFlashAttribute("errorMsg", "카테고리, 유형, 제목, 내용을 모두 입력해 주세요.");
            return new ModelAndView("redirect:/spendolive/inquiry/write.do");
        }

        InquiryVO inquiry = new InquiryVO();
        inquiry.setId(memberInfo.getId());
        inquiry.setCategory(category);
        inquiry.setInquiry_type(inquiry_type);
        inquiry.setTitle(title.strip());
        inquiry.setContent(content.strip());

        try {
            inquiryService.writeInquiry(inquiry, attachments);
            ra.addFlashAttribute("msg", "문의가 접수되었습니다. 답변까지 영업일 기준 1~2일 소요됩니다.");
        } catch (IllegalArgumentException e) {
            // 첨부파일 검증 실패 (용량/확장자/개수 초과 등)
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return new ModelAndView("redirect:/spendolive/inquiry/write.do");
        } catch (DataAccessException e) {
            System.err.println("[InquiryController.inquiryWrite] 등록 실패: " + e.getMessage());
            ra.addFlashAttribute("errorMsg", "문의 접수 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return new ModelAndView("redirect:/spendolive/inquiry/write.do");
        } catch (RuntimeException e) {
            // 파일 디스크 저장 실패 등
            System.err.println("[InquiryController.inquiryWrite] 첨부파일 저장 실패: " + e.getMessage());
            ra.addFlashAttribute("errorMsg", "첨부파일 저장 중 오류가 발생했습니다. 다시 시도해 주세요.");
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
            ra.addFlashAttribute("msg", "로그인이 필요한 기능입니다. 로그인 후 이용해 주세요.");
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
        ModelAndView mav = new ModelAndView("common/layout");
        mav.addObject("body_page", "/WEB-INF/views/inquiry/inquiryDetail.jsp");
        mav.addObject("inquiry", inquiry);
        return mav;
        
    }

    /* ─── 첨부파일 미리보기/다운로드 ──────────────────────── */
    @GetMapping("/file/{file_id}")
    public ResponseEntity<Resource> viewInquiryFile(
            @PathVariable("file_id") int file_id, HttpSession session) {

        MemberVO memberInfo = (MemberVO) session.getAttribute("memberInfo");
        if (memberInfo == null) {
            return ResponseEntity.status(401).build();
        }

        // 본인 문의에 달린 첨부파일이 맞는지 확인 (관리자는 전체 열람 가능)
        boolean isAdmin = "ADMIN".equals(memberInfo.getRole());
        InquiryFileVO file = inquiryService.getInquiryFile(file_id, memberInfo.getId(), isAdmin);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Path.of(file.getFile_path());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getOrigin_name() + "\"")
                    .body(resource);
        } catch (IOException e) {
            System.err.println("[InquiryController.viewInquiryFile] 파일 읽기 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}