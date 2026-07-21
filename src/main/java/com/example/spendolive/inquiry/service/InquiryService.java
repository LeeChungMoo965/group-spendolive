package com.example.spendolive.inquiry.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.spendolive.inquiry.domain.InquiryFileVO;
import com.example.spendolive.inquiry.domain.InquiryVO;
import com.example.spendolive.inquiry.repository.InquiryFileRepository;
import com.example.spendolive.inquiry.repository.InquiryRepository;
import com.example.spendolive.notification.domain.NotificationType;
import com.example.spendolive.notification.service.NotificationService;

@Service
public class InquiryService {

    private static final int PAGE_SIZE = 5;
    private static final int PAGINATION_THRESHOLD = 10; // 이 개수 이하면 페이지네이션 없이 전부 표시

    private final InquiryRepository inquiryRepository;
    private final InquiryFileRepository inquiryFileRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public InquiryService(InquiryRepository inquiryRepository,
                           InquiryFileRepository inquiryFileRepository,
                           FileStorageService fileStorageService,
                           NotificationService notificationService) {
        this.inquiryRepository = inquiryRepository;
        this.inquiryFileRepository = inquiryFileRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    /**
     * 문의 등록 + 첨부파일 저장을 한 트랜잭션으로 처리한다.
     * 파일 검증(FileStorageService.storeFiles)에서 예외가 나면 inquiry insert도 롤백된다.
     */
    @Transactional
    public void writeInquiry(InquiryVO inquiry, MultipartFile[] attachments) {
        int inquiryId = inquiryRepository.insertInquiry(inquiry);
        inquiry.setInquiryId(inquiryId);

        List<InquiryFileVO> files = fileStorageService.storeFiles(inquiryId, attachments);
        for (InquiryFileVO file : files) {
            inquiryFileRepository.insertFile(file);
        }
    }

    /**
     * @param status null이면 전체, 아니면 WAIT/DONE/REVIEW 중 하나 (DB 저장값, 대문자)로 필터링
     */
    public List<InquiryVO> getMyInquiryList(String id, int page, String status) {
        int totalCount = inquiryRepository.countByMemberId(id, status);
        if (totalCount == 0) {
            return Collections.emptyList();
        }

        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        int safePage = Math.min(Math.max(page, 1), totalPages);
        int offset = (safePage - 1) * PAGE_SIZE;
        List<InquiryVO> list = inquiryRepository.findByMemberId(id, status, offset, PAGE_SIZE);

        // 각 문의에 첨부파일 목록을 채워 넣는다 (목록 카드에서 썸네일 표시용)
        for (InquiryVO inquiry : list) {
            inquiry.setFiles(inquiryFileRepository.findByInquiryId(inquiry.getInquiryId()));
        }
        return list;
    }

    public int getMyInquiryTotalPages(String id, String status) {
        int totalCount = inquiryRepository.countByMemberId(id, status);
        if (totalCount == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    public InquiryVO getInquiryDetail(int inquiryId) {
        InquiryVO inquiry = inquiryRepository.findById(inquiryId);
        if (inquiry != null) {
            inquiry.setFiles(inquiryFileRepository.findByInquiryId(inquiryId));
        }
        return inquiry;
    }

    /**
     * 첨부파일 미리보기/다운로드 요청 시 접근 권한을 확인한다.
     * 관리자는 전체 열람 가능, 일반 회원은 본인 문의의 첨부파일만 열람 가능
     */
    public InquiryFileVO getInquiryFile(int fileId, String memberId, boolean isAdmin) {
        InquiryFileVO file = inquiryFileRepository.findById(fileId);
        if (file == null) {
            return null;
        }
        if (isAdmin) {
            return file;
        }
        InquiryVO inquiry = inquiryRepository.findById(file.getInquiryId());
        if (inquiry == null || !inquiry.getId().equals(memberId)) {
            return null;
        }
        return file;
    }

    /* ─── 관리자용: 전체 회원 문의 목록/답변 ─────────────────── */

    /**
     * @param status null이면 전체, 아니면 WAIT/DONE/REVIEW 중 하나로 필터링
     */
    public List<InquiryVO> getAllInquiriesForAdmin(String status, int page) {
        int totalCount = inquiryRepository.countAllForAdmin(status);
        if (totalCount == 0) {
            return Collections.emptyList();
        }
        if (totalCount <= PAGINATION_THRESHOLD) {
            return inquiryRepository.findAllForAdmin(status, 0, totalCount);
        }
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;
        return inquiryRepository.findAllForAdmin(status, offset, PAGE_SIZE);
    }

    public int getAdminInquiryTotalPages(String status) {
        int totalCount = inquiryRepository.countAllForAdmin(status);
        if (totalCount <= PAGINATION_THRESHOLD) {
            return 1;
        }
        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    /** 화면에 표시할 "몇 번째 문의인지" 계산용 (inquiry_id는 삭제된 데이터 때문에 듬성듬성 빌 수 있어서 따로 계산) */
    public int getAdminInquiryTotalCount(String status) {
        return inquiryRepository.countAllForAdmin(status);
    }

    public int getAdminPageSize() {
        return PAGE_SIZE;
    }

    /** 답변 등록/수정 + 상태 변경(보통 DONE, 검토만 하고 싶으면 REVIEW로도 가능) + 문의 작성자에게 답변 완료 알림 발송 */
    public void replyToInquiry(int inquiryId, String replyContent, String status) {
        inquiryRepository.replyToInquiry(inquiryId, replyContent, status);

        InquiryVO inquiry = inquiryRepository.findById(inquiryId);
        if (inquiry != null && inquiry.getId() != null && !inquiry.getId().isBlank()) {
            notificationService.createNotification(
                    inquiry.getId(),
                    NotificationType.INQUIRY_REPLY,
                    "문의하신 내용에 답변이 등록되었습니다",
                    "\"" + inquiry.getTitle() + "\" 문의에 관리자 답변이 등록되었습니다.",
                    "/spendolive/inquiry/list.do"
            );
        }
    }
}