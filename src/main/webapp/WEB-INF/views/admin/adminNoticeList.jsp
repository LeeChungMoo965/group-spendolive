<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/notice.css">
<style>
.admin-wrap { max-width: 960px; margin: 40px auto; padding: 0 20px; }
.admin-wrap h2 { font-size: 1.4rem; font-weight: 700; margin-bottom: 20px; }
.admin-btn-row { display:flex; justify-content:flex-end; margin-bottom:14px; }
.admin-table { width:100%; border-collapse:collapse; font-size:.9rem; }
.admin-table th, .admin-table td { padding:11px 14px; border-bottom:1px solid #e5e7eb; text-align:left; }
.admin-table th { background:#f9fafb; font-weight:600; color:#374151; }
.admin-table tr:hover td { background:#f0fdf4; }
.col-num  { width:50px;  text-align:center; }
.col-pin  { width:70px;  text-align:center; }
.col-act  { width:140px; text-align:center; }
.flash-ok  { background:#d1fae5; border:1px solid #6ee7b7; color:#065f46; padding:12px 18px; border-radius:8px; margin-bottom:18px; }
.flash-err { background:#fee2e2; border:1px solid #fca5a5; color:#991b1b; padding:12px 18px; border-radius:8px; margin-bottom:18px; }
.btn-write  { background:#22c55e; color:#fff; padding:8px 18px; border-radius:8px; font-size:.9rem; border:none; cursor:pointer; text-decoration:none; }
.btn-edit   { background:#3b82f6; color:#fff; padding:5px 11px; border-radius:6px; font-size:.82rem; border:none; cursor:pointer; text-decoration:none; }
.btn-del    { background:#ef4444; color:#fff; padding:5px 11px; border-radius:6px; font-size:.82rem; border:none; cursor:pointer; margin-left:5px; }
.chip { padding:2px 10px; border-radius:20px; font-size:.78rem; font-weight:600; }
.chip-important { background:#fef3c7; color:#92400e; }
.chip-normal    { background:#f3f4f6; color:#6b7280; }
</style>

<section class="page-hero">
    <div class="container">
        <div class="notice-hero-text">
            <p class="eyebrow">ADMIN</p>
            <h1>공지사항 관리</h1>
        </div>
    </div>
</section>

<div class="admin-wrap">

    <c:if test="${not empty msg}">
        <div class="flash-ok">${msg}</div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <h2>공지사항 목록 (총 ${noticeList.size()}건)</h2>

    <div class="admin-btn-row">
        <a href="${contextPath}/spendolive/admin/notice/write.do" class="btn-write">+ 새 공지 작성</a>
    </div>

    <table class="admin-table">
        <thead>
            <tr>
                <th class="col-num">번호</th>
                <th class="col-pin">구분</th>
                <th>제목</th>
                <th>작성자</th>
                <th>등록일</th>
                <th class="col-act">관리</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty noticeList}">
                    <tr><td colspan="6" style="text-align:center;padding:40px;color:#9ca3af;">등록된 공지사항이 없습니다.</td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="notice" items="${noticeList}" varStatus="s">
                        <tr>
                            <td class="col-num">${s.count}</td>
                            <td class="col-pin">
                                <c:choose>
                                    <c:when test="${notice.pinned_yn == 'Y'}"><span class="chip chip-important">중요</span></c:when>
                                    <c:otherwise><span class="chip chip-normal">일반</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${contextPath}/spendolive/notice/detail.do?notice_id=${notice.notice_id}"
                                   style="color:#111;text-decoration:none;">${notice.title}</a>
                            </td>

                            <td>${notice.admin_id}</td>

                            <td>${notice.created_at}</td>
                            <td class="col-act">
                                <a href="${contextPath}/spendolive/admin/notice/edit.do?notice_id=${notice.notice_id}" class="btn-edit">수정</a>
                                <form action="${contextPath}/spendolive/admin/notice/delete.do" method="post" style="display:inline;"
                                      onsubmit="return confirm('정말 삭제하시겠습니까?');">
                                    <input type="hidden" name="notice_id" value="${notice.notice_id}">
                                    <button type="submit" class="btn-del">삭제</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>
