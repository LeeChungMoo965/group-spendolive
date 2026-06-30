package com.example.spendolive.notice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.notice.domain.NoticeDTO;
import com.example.spendolive.notice.repository.NoticeRepository;


@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeServiceImpl(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    public List<NoticeDTO> getNoticeList(String id) {
        return noticeRepository.findAll(id);
    }

    @Override
    public NoticeDTO getNoticeDetail(int noticeId) {
        return noticeRepository.findById(noticeId);
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
    public void readNotice(int noticeId, String id) {
        noticeRepository.insertNoticeRead(noticeId, id);
    }


    @Override
    public List<NoticeDTO> getUnreadNoticeList(String id) {
    return noticeRepository.findUnreadByMemberId(id);
    }

    @Override
    public void toggleNoticeStar(int noticeId, String id) {
        noticeRepository.toggleNoticeStar(noticeId, id);
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
    public void deleteNotice(int noticeId) {
        noticeRepository.deleteNotice(noticeId);
    }
}