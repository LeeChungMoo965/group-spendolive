package com.example.spendolive.notice.domain;

public class NoticeDTO {

    private int noticeId;
    private int adminId;

    private String title;
    private String content;

    private String pinnedYn;

    private String createdAt;
    private String updatedAt;

    public NoticeDTO() {
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPinnedYn() {
        return pinnedYn;
    }

    public void setPinnedYn(String pinnedYn) {
        this.pinnedYn = pinnedYn;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}