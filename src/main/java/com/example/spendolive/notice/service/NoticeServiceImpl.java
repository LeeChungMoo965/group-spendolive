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
    public List<NoticeDTO> getNoticeList() {
        return noticeRepository.findAll();
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
}