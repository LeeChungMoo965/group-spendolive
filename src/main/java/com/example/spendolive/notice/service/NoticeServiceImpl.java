package com.example.spendolive.notice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.notice.domain.NoticeDTO;
import com.example.spendolive.notice.repository.NoticeRepository;


@Service
public class NoticeServiceImpl implements NoticeService {

    // 관리자 공지 목록 전용: 20개 이하면 페이지네이션 없이 전부 표시, 넘으면 20개씩 페이지 분리
    // (사용자 화면의 공지/알림 AJAX 목록은 전체를 한 번에 받아 JS에서 자체 페이지네이션하므로
    //  이 상수와는 무관함 — getNoticeList(id)는 그대로 건드리지 않음)
    private static final int ADMIN_PAGE_SIZE = 20;
    private static final int ADMIN_PAGINATION_THRESHOLD = 20;

    private final NoticeRepository noticeRepository;

    public NoticeServiceImpl(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    public List<NoticeDTO> getNoticeList(String id) {
        return noticeRepository.findAll(id);
    }

    @Override
    public List<NoticeDTO> getNoticeListForAdmin(int page) {
        int totalCount = noticeRepository.countAll();
        if (totalCount <= ADMIN_PAGINATION_THRESHOLD) {
            return noticeRepository.findAllPaged(0, Math.max(totalCount, 1));
        }
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * ADMIN_PAGE_SIZE;
        return noticeRepository.findAllPaged(offset, ADMIN_PAGE_SIZE);
    }

    @Override
    public int getNoticeAdminTotalPages() {
        int totalCount = noticeRepository.countAll();
        if (totalCount <= ADMIN_PAGINATION_THRESHOLD) {
            return 1;
        }
        return (int) Math.ceil((double) totalCount / ADMIN_PAGE_SIZE);
    }

    @Override
    public NoticeDTO getNoticeDetail(int notice_id) {
        return noticeRepository.findById(notice_id);
    }

    @Override
    public NoticeDTO getNoticeDetailForUser(int notice_id, String id) {
        return noticeRepository.findByIdWithStar(notice_id, id);
    }

    @Override
    public int getNoticeCount() {
        return noticeRepository.countAll();
    }

    @Override
    public int getPinnedCount() {
        return noticeRepository.countPinned();
    }

    @Override
    public List<NoticeDTO> getImportantList(String id) {
    return noticeRepository.findImportantList(id);
    }

    @Override
    public void readNotice(int notice_id, String id) {
        noticeRepository.insertNoticeRead(notice_id, id);
    }


    @Override
    public List<NoticeDTO> getUnreadNoticeList(String id) {
    return noticeRepository.findUnreadBymember_id(id);
    }

    @Override
    public void toggleNoticeStar(int notice_id, String id) {
        noticeRepository.toggleNoticeStar(notice_id, id);
    }

    @Override
    public int insertNotice(NoticeDTO notice) {
        int newId = noticeRepository.insertNotice(notice);
        if (newId > 0) {
            try {
                noticeRepository.insertNoticeAlertForAll(notice.getTitle(), String.valueOf(newId));
            } catch (Exception e) {
                System.err.println("[NoticeServiceImpl] 알림 발송 실패 (공지는 등록됨): " + e.getMessage());
            }
        }
        return newId;
    }

    @Override
    public void updateNotice(NoticeDTO notice) {
        noticeRepository.updateNotice(notice);
    }

    @Override
    public void deleteNotice(int notice_id) {
        noticeRepository.deleteNotice(notice_id);
    }
}