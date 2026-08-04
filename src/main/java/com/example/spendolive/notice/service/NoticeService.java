package com.example.spendolive.notice.service;

import java.util.List;

import com.example.spendolive.notice.domain.NoticeDTO;

public interface NoticeService {

    List<NoticeDTO> getNoticeList(String id);

    // 관리자 공지 목록 전용 (페이지네이션): 20개 이하면 전체, 넘으면 20개씩 페이지 분리
    List<NoticeDTO> getNoticeListForAdmin(int page);
    int getNoticeAdminTotalPages();

    NoticeDTO getNoticeDetail(int notice_id);

    // 사용자 상세 페이지 전용: 로그인 회원 기준 찜 여부(star_yn)까지 포함해서 조회
    NoticeDTO getNoticeDetailForUser(int notice_id, String id);

    int getNoticeCount();

    int getPinnedCount();

    List<NoticeDTO> getImportantList(String id);

    void readNotice(int notice_id, String id);

    List<NoticeDTO> getUnreadNoticeList(String id);

    void toggleNoticeStar(int notice_id, String id);

    // 관리자 기능
    int insertNotice(NoticeDTO notice);
    void updateNotice(NoticeDTO notice);
    void deleteNotice(int notice_id);
}