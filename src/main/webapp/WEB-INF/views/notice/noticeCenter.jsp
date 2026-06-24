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

<section class="section compact notice-list-section">
    <div class="container">
        <div class="notice-board-wrap">

            <div class="notice-board-tabs">
                <button type="button"
                        class="notice-board-tab active"
                        onclick="loadNoticeList(); setBoardTab('notice');"
                        id="noticeTabBtn">
                    공지사항
                </button>

                <button type="button"
                        class="notice-board-tab"
                        onclick="loadAlertList(); setBoardTab('alert');"
                        id="alertTabBtn">
                    알림
                </button>
            </div>

            <div class="card table-card notice-board-card">
                <div class="notice-row-title">
                    <div>
                        <p class="eyebrow" id="listEyebrow">NOTICE LIST</p>
                        <h2 id="listTitle">공지사항</h2>
                    </div>
                </div>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>번호</th>
                                <th>구분</th>
                                <th>제목</th>
                                <th id="writerTypeHeader">작성자</th>
                                <th>등록일</th>
                            </tr>
                        </thead>

                        <tbody id="noticeTableBody">
                            <c:choose>
                                <c:when test="${param.tab eq 'alarm'}">
                                    <c:if test="${empty alertList}">
                                        <tr>
                                            <td colspan="5" class="notice-empty">
                                                <div class="empty-icon">🔔</div>
                                                <p>수신된 알림이 없습니다.</p>
                                                <span>새 알림이 도착하면 여기에 표시됩니다.</span>
                                            </td>
                                        </tr>
                                    </c:if>

                                    <c:forEach var="alert" items="${alertList}" varStatus="status">
                                        <tr>
                                            <td>${status.count}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${alert.readYn eq 'N'}">
                                                        <span class="chip notice-important">NEW</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="chip notice-normal">읽음</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a class="notice-title-link"
                                                   href="${contextPath}/spendolive/alert/detail.do?alertId=${alert.alertId}">
                                                    ${alert.title}
                                                </a>
                                            </td>
                                            <td>${alert.alertType}</td>
                                            <td>${alert.createdAt}</td>
                                        </tr>
                                    </c:forEach>
                                </c:when>

                                <c:otherwise>
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
                                                    <c:when test="${notice.pinnedYn eq 'Y'}">
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
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</section>

<script>
const contextPath = "${contextPath}";

function loadNoticeList() {
    fetch(contextPath + "/spendolive/notice/ajax/noticeList.do")
        .then(response => response.json())
        .then(data => drawNoticeList(data));
}

function loadImportantList() {
    fetch(contextPath + "/spendolive/notice/ajax/importantList.do")
        .then(response => response.json())
        .then(data => drawNoticeList(data));
}

function loadAlertList() {
    fetch(contextPath + "/spendolive/alert/ajax/alertList.do")
        .then(response => response.json())
        .then(data => drawAlertList(data));
}

function loadUnreadList() {
    fetch(contextPath + "/spendolive/alert/ajax/unreadList.do")
        .then(response => response.json())
        .then(data => drawAlertList(data));
}

function drawNoticeList(list) {
    const tbody = document.getElementById("noticeTableBody");
    let html = "";

    if (list.length === 0) {
        html = `
            <tr>
                <td colspan="5" class="notice-empty">
                    <div class="empty-icon">📄</div>
                    <p>등록된 공지사항이 없습니다.</p>
                    <span>관리자가 공지사항을 등록하면 여기에 표시됩니다.</span>
                </td>
            </tr>
        `;
    }

    list.forEach((notice, index) => {
        const typeBadge = notice.pinnedYn === "Y"
            ? `<span class="chip notice-important">중요</span>`
            : `<span class="chip notice-normal">공지</span>`;

        html +=
            "<tr>" +
                "<td>" + (index + 1) + "</td>" +
                "<td>" + typeBadge + "</td>" +
                "<td>" +
                    "<a class='notice-title-link' href='" + contextPath + "/spendolive/notice/detail.do?noticeId=" + notice.noticeId + "'>" +
                        notice.title +
                    "</a>" +
                "</td>" +
                "<td>관리자</td>" +
                "<td>" + notice.createdAt + "</td>" +
            "</tr>";
    });

    tbody.innerHTML = html;
}

function drawAlertList(list) {
    const tbody = document.getElementById("noticeTableBody");
    let html = "";

    if (list.length === 0) {
        html = `
            <tr>
                <td colspan="5" class="notice-empty">
                    <div class="empty-icon">🔔</div>
                    <p>수신된 알림이 없습니다.</p>
                    <span>새 알림이 도착하면 여기에 표시됩니다.</span>
                </td>
            </tr>
        `;
    }

    list.forEach((notification, index) => {
        const readBadge = notification.readYn === "N"
            ? `<span class="chip notice-important">NEW</span>`
            : `<span class="chip notice-normal">읽음</span>`;

        html +=
            "<tr>" +
                "<td>" + (index + 1) + "</td>" +
                "<td>" + readBadge + "</td>" +
                "<td>" + notification.title + "</td>" +
                "<td>" + notification.notificationType + "</td>" +
                "<td>" + notification.createdAt + "</td>" +
            "</tr>";
    });

    tbody.innerHTML = html;
}

function setBoardTab(mode) {
    const eyebrow = document.getElementById("listEyebrow");
    const title = document.getElementById("listTitle");
    const header = document.getElementById("writerTypeHeader");
    const noticeBtn = document.getElementById("noticeTabBtn");
    const alertBtn = document.getElementById("alertTabBtn");

    if (mode === "alert") {
        eyebrow.textContent = "ALERT LIST";
        title.textContent = "알림";
        header.textContent = "유형";

        noticeBtn.classList.remove("active");
        alertBtn.classList.add("active");
    } else {
        eyebrow.textContent = "NOTICE LIST";
        title.textContent = "공지사항";
        header.textContent = "작성자";

        alertBtn.classList.remove("active");
        noticeBtn.classList.add("active");
    }
}
</script>
