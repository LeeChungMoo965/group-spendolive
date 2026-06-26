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

}
