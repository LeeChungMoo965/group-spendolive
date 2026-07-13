package com.example.spendolive.notice.service;

import java.util.List;

import com.example.spendolive.notice.domain.NoticeDTO;

public interface NoticeService {

    List<NoticeDTO> getNoticeList(String id);

    NoticeDTO getNoticeDetail(int noticeId);

    int getNoticeCount();

    int getPinnedCount();

    List<NoticeDTO> getImportantList(String id);

    void readNotice(int noticeId, String id);

    List<NoticeDTO> getUnreadNoticeList(String id);

    void toggleNoticeStar(int noticeId, String id);

    // 관리자 기능
    int insertNotice(NoticeDTO notice);
    void updateNotice(NoticeDTO notice);
    void deleteNotice(int noticeId);
}