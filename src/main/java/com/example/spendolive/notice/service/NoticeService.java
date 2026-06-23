package com.example.spendolive.notice.service;

import java.util.List;

import com.example.spendolive.notice.domain.NoticeDTO;

public interface NoticeService {

    List<NoticeDTO> getNoticeList();

    NoticeDTO getNoticeDetail(int noticeId);

    int getNoticeCount();

    int getPinnedCount();

    List<NoticeDTO> getImportantList();
}
