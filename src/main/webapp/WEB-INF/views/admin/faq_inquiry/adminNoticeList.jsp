<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%-- admin.css는 common/header.jsp가 role=='ADMIN'일 때 이미 자동으로 로드해줌 → 여기서 또 링크할 필요 없음 --%>

<div class="admin-main" data-admin-page="notice">

    <div class="hero">
        <div>
            <p class="hero-kicker">ADMIN</p>
            <h1>공지사항 관리</h1>
            <p>사용자 화면(알림&공지사항)에 노출되는 공지를 추가·수정·삭제합니다.</p>
        </div>
    </div>

    <c:if test="${not empty msg}">
        <div class="flash-ok">${msg}</div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <div class="panel">
        <div class="panel-header">
            <div class="panel-title">
                <p class="section-kicker">NOTICE LIST</p>
                <h2>공지사항 목록 (총 ${totalCount}건)</h2>
            </div>
            <a href="${contextPath}/admin/notice/write.do" class="btn primary">+ 새 공지 작성</a>
        </div>

        <div class="table-wrap">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th style="width:60px;text-align:center;">번호</th>
                        <th style="width:80px;text-align:center;">구분</th>
                        <th>제목</th>
                        <th style="width:120px;">작성자</th>
                        <th style="width:120px;">등록일</th>
                        <th style="width:150px;text-align:center;">관리</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty noticeList}">
                            <tr><td colspan="6" style="text-align:center;padding:40px;color:var(--muted);">등록된 공지사항이 없습니다.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="notice" items="${noticeList}" varStatus="s">
                                <tr>
                                    <td style="text-align:center;">${notice.notice_id}</td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${notice.pinned_yn == 'Y'}"><span class="badge green">중요</span></c:when>
                                            <c:otherwise><span class="badge gray">일반</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${contextPath}/admin/notice/edit.do?notice_id=${notice.notice_id}">${notice.title}</a>
                                    </td>
                                    <td>${notice.admin_id}</td>
                                    <td>${notice.created_at}</td>
                                    <td>
                                        <div class="table-actions" style="justify-content:center;">
                                            <a href="${contextPath}/admin/notice/edit.do?notice_id=${notice.notice_id}" class="mini-btn">수정</a>
                                            <form action="${contextPath}/admin/notice/delete.do" method="post" style="display:inline;"
                                                  onsubmit="return confirm('정말 삭제하시겠습니까?');">
                                                <input type="hidden" name="notice_id" value="${notice.notice_id}">
                                                <button type="submit" class="mini-btn danger">삭제</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <c:if test="${totalPages > 1}">
            <%-- 관리자 문의 목록과 동일한 윈도우 방식 페이지네이션.
                 20개 넘을 때만 나타나고, 1번/마지막 번호는 항상 고정 노출 --%>
            <c:set var="pgStart" value="${currentPage - 2 < 1 ? 1 : currentPage - 2}" />
            <c:set var="pgEnd" value="${currentPage + 2 > totalPages ? totalPages : currentPage + 2}" />

            <div class="admin-pagination">
                <c:if test="${pgStart > 1}">
                    <a class="admin-pg-btn" href="${contextPath}/admin/notice/list.do?page=1">1</a>
                    <c:if test="${pgStart > 2}">
                        <span>…</span>
                    </c:if>
                </c:if>

                <c:forEach begin="${pgStart}" end="${pgEnd}" var="p">
                    <a class="admin-pg-btn ${p == currentPage ? 'active' : ''}"
                       href="${contextPath}/admin/notice/list.do?page=${p}">${p}</a>
                </c:forEach>

                <c:if test="${pgEnd < totalPages}">
                    <c:if test="${pgEnd < totalPages - 1}">
                        <span>…</span>
                    </c:if>
                    <a class="admin-pg-btn" href="${contextPath}/admin/notice/list.do?page=${totalPages}">${totalPages}</a>
                </c:if>
            </div>
        </c:if>
    </div>
</div>
