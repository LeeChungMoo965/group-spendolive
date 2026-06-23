<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<link rel="stylesheet" href="${contextPath}/resources/css/notice.css">

<section class="page-hero">
    <div class="container">

        <div class="notice-hero-text">
            <p class="eyebrow">NOTICE CENTER</p>
            <h1>공지사항 · 알림센터</h1>
            <p class="hero-text">
                SpendOlive의 공지사항과 개인 알림을 한눈에 확인하세요.
            </p>
        </div>
    </div>
</section>

<section class="section compact">
    <div class="container grid-3">
        <div class="summary-card">
            <div class="icon">📢</div>
            <h3>공지사항</h3>
            <p><strong>${noticeCount}</strong>건</p>
        </div>

        <div class="summary-card">
            <div class="icon">🔔</div>
            <h3>읽지 않은 알림</h3>
            <p><strong>${unreadCount}</strong>건</p>
        </div>

        <div class="summary-card">
            <div class="icon">⭐</div>
            <h3>중요 공지</h3>
            <p><strong>${importantCount}</strong>건</p>
        </div>
    </div>
</section>

<section class="section compact notice-list-section">
    <div class="container">
        <div class="card table-card">

            <div class="row-title notice-row-title">
                <div>
                    <p class="eyebrow">NOTICE LIST</p>
                    <h2>공지사항</h2>
                </div>

                <div class="notice-tabs">
                    <a href="${contextPath}/spendolive/notice/center.do?tab=notice"
                       class="btn btn-primary">공지사항</a>

                    <a href="${contextPath}/spendolive/alert/center.do?tab=alarm"
                       class="btn btn-light">내 알림</a>
                </div>
            </div>

            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>번호</th>
                            <th>구분</th>
                            <th>제목</th>
                            <th>작성자</th>
                            <th>등록일</th>
                        </tr>
                    </thead>

                    <tbody>
                        <c:if test="${empty noticeList}">
                            <tr>
                                <td colspan="5" class="notice-empty">
                                    <div class="empty-icon">📄</div>
                                    <p>등록된 공지사항이 없습니다.</p>
                                    <span>관리자가 공지사항을 등록하면 여기에 표시됩니다.</span>
                                </td>
                            </tr>
                        </c:if>

                        <c:forEach var="notice" items="${noticeList}" varStatus="status">
                            <tr>
                                <td>${status.count}</td>

                                <td>
                                    <c:choose>
                                        <c:when test="${notice.pinnedYn == 'Y'}">
                                            <span class="chip notice-important">중요</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="chip notice-normal">공지</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <a class="notice-title-link"
                                       href="${contextPath}/spendolive/notice/detail.do?noticeId=${notice.noticeId}">
                                        ${notice.title}
                                    </a>
                                </td>

                                <td>관리자</td>
                                <td>${notice.createdAt}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

        </div>
    </div>
</section>