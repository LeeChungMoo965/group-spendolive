package com.example.spendolive.admin.dashboard.domain;

/**
 * 관리자 메인 대시보드에 표시할 주요 운영 현황이다.
 */
public class AdminDashboardDTO {

    private int totalMemberCount;
    private int recruitingPartyCount;
    private int ottServiceCount;
    private int pendingReportCount;
    private int pendingInquiryCount;

    public int getTotalMemberCount() {
        return totalMemberCount;
    }

    public void setTotalMemberCount(int totalMemberCount) {
        this.totalMemberCount = totalMemberCount;
    }

    public int getRecruitingPartyCount() {
        return recruitingPartyCount;
    }

    public void setRecruitingPartyCount(int recruitingPartyCount) {
        this.recruitingPartyCount = recruitingPartyCount;
    }

    public int getOttServiceCount() {
        return ottServiceCount;
    }

    public void setOttServiceCount(int ottServiceCount) {
        this.ottServiceCount = ottServiceCount;
    }

    public int getPendingReportCount() {
        return pendingReportCount;
    }

    public void setPendingReportCount(int pendingReportCount) {
        this.pendingReportCount = pendingReportCount;
    }

    public int getPendingInquiryCount() {
        return pendingInquiryCount;
    }

    public void setPendingInquiryCount(int pendingInquiryCount) {
        this.pendingInquiryCount = pendingInquiryCount;
    }
}
