<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/notice.css">

<style>
.notif-detail-card {
    max-width: 760px; margin: 40px auto; padding: 36px 40px;
    background: var(--surface-1, #fff); border-radius: 14px;
    border: 1px solid var(--border, #e5e7eb);
}
.notif-detail-top {
    display: flex; align-items: center; gap: 10px; margin-bottom: 18px;
}
.notif-detail-title {
    font-size: 1.35rem; font-weight: 700; margin: 0 0 12px;
    color: var(--text-primary, #111);
}
.notif-detail-meta {
    font-size: 0.85rem; color: #6b7280; margin-bottom: 28px;
    display: flex; gap: 20px;
}
.notif-detail-message {
    font-size: 0.97rem; line-height: 1.8;
    color: var(--text-primary, #222);
    border-top: 1px solid var(--border, #e5e7eb);
    padding-top: 24px; min-height: 80px;
    white-space: pre-wrap;
}
.notif-detail-actions {
    margin-top: 32px; display: flex; gap: 10px;
}
.notif-badge {
    padding: 3px 12px; border-radius: 20px; font-size: .78rem; font-weight: 600;
}
.badge-home     { background:#dbeafe; color:#1e40af; }
.badge-personal { background:#fce7f3; color:#9d174d; }
.badge-ott      { background:#d1fae5; color:#065f46; }
</style>

<section class="section compact">
    <div class="container">

        <%-- 오류 메시지 --%>
        <c:if test="${not empty errorMsg}">
            <div style="max-width:760px;margin:40px auto;background:#fee2e2;border:1px solid #fca5a5;
                        color:#991b1b;padding:16px 20px;border-radius:10px;font-weight:500;">
                ⚠ ${errorMsg}
                <div style="margin-top:12px;">
                    <a href="${contextPath}/spendolive/notice/center.do" class="btn btn-primary">목록으로</a>
                </div>
            </div>
        </c:if>

        <%-- 정상 알림 상세 --%>
        <c:if test="${not empty notification}">
            <div class="notif-detail-card">

                <div class="notif-detail-top">
                    <c:choose>
                        <c:when test="${notification.notificationType == 'HOME'}">
                            <span class="notif-badge badge-home">공지</span>
                        </c:when>
                        <c:when test="${notification.notificationType == 'OTT'}">
                            <span class="notif-badge badge-ott">OTT</span>
                        </c:when>
                        <c:otherwise>
                            <span class="notif-badge badge-personal">개인</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <h1 class="notif-detail-title">${notification.title}</h1>

                <div class="notif-detail-meta">
                    <span>수신일 ${notification.createdAt}</span>
                    <span>${notification.readYn == 'Y' ? '읽음' : '안 읽음'}</span>
                </div>

                <div class="notif-detail-message">${notification.message}</div>

                <div class="notif-detail-actions">
                    <a href="${contextPath}/spendolive/notice/center.do?tab=alert" class="btn btn-primary">
                        알림 목록
                    </a>
                    <c:if test="${not empty notification.linkUrl}">
                        <a href="${contextPath}${notification.linkUrl}" class="btn btn-light">
                            관련 페이지 이동
                        </a>
                    </c:if>
                </div>
            </div>
        </c:if>

    </div>
</section>
