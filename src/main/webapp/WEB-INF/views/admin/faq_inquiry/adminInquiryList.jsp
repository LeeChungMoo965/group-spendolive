<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%-- admin.css는 common/header.jsp가 role=='ADMIN'일 때 이미 자동으로 로드해줌 --%>

<div class="admin-main">

    <div class="hero">
        <div>
            <p class="hero-kicker">ADMIN</p>
            <h1>문의 관리</h1>
            <p>회원이 남긴 문의를 확인하고 답변을 등록합니다.</p>
        </div>
    </div>

    <c:if test="${not empty msg}">
        <div class="flash-ok">${msg}</div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <div class="admin-board-tabs">
        <a href="${contextPath}/spendolive/admin/inquiry/list.do" class="admin-board-tab active">문의사항</a>
        <a href="${contextPath}/spendolive/admin/faq/list.do" class="admin-board-tab">자주 묻는 질문</a>
    </div>

    <div class="panel">
        <div class="panel-header">
            <div class="panel-title">
                <p class="section-kicker">INQUIRY LIST</p>
                <h2>문의 목록 (총 ${inquiryList.size()}건)</h2>
            </div>
        </div>

        <div class="toolbar">
            <div class="toolbar-left filter-pills">
                <a class="${currentStatus == 'all' ? 'active' : ''}"
                   href="${contextPath}/spendolive/admin/inquiry/list.do?status=all">전체</a>
                <a class="${currentStatus == 'wait' ? 'active' : ''}"
                   href="${contextPath}/spendolive/admin/inquiry/list.do?status=wait">답변 대기</a>
                <a class="${currentStatus == 'done' ? 'active' : ''}"
                   href="${contextPath}/spendolive/admin/inquiry/list.do?status=done">답변 완료</a>
                <a class="${currentStatus == 'review' ? 'active' : ''}"
                   href="${contextPath}/spendolive/admin/inquiry/list.do?status=review">검토 중</a>
            </div>
        </div>

        <div class="table-wrap">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th style="width:60px;text-align:center;">번호</th>
                        <th style="width:110px;text-align:center;">카테고리</th>
                        <th style="width:130px;text-align:center;">유형</th>
                        <th>제목</th>
                        <th style="width:120px;text-align:center;">작성자</th>
                        <th style="width:90px;text-align:center;">상태</th>
                        <th style="width:110px;text-align:center;">등록일</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty inquiryList}">
                            <tr><td colspan="7" style="text-align:center;padding:40px;color:var(--muted);">해당 조건의 문의가 없습니다.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="inq" items="${inquiryList}">
                                <tr>
                                    <td style="text-align:center;">${inq.inquiry_id}</td>
                                    <td style="text-align:center;">${inq.category}</td>
                                    <td style="text-align:center;">${inq.inquiry_type}</td>
                                    <td>
                                        <a href="${contextPath}/spendolive/admin/inquiry/detail.do?inquiryNo=${inq.inquiry_id}">${inq.title}</a>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${not empty inq.writer_nickname}">${inq.writer_nickname}</c:when>
                                            <c:otherwise>${inq.id}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${inq.statusCode == 'done'}"><span class="badge green">완료</span></c:when>
                                            <c:when test="${inq.statusCode == 'review'}"><span class="badge blue">검토중</span></c:when>
                                            <c:otherwise><span class="badge yellow">대기</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">${inq.reg_date}</td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <c:if test="${totalPages > 1}">
            <div class="admin-pagination">
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <a class="admin-pg-btn ${p == currentPage ? 'active' : ''}"
                       href="${contextPath}/spendolive/admin/inquiry/list.do?status=${currentStatus}&page=${p}">${p}</a>
                </c:forEach>
            </div>
        </c:if>
    </div>
</div>
