<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%-- admin.css는 common/header.jsp가 role=='ADMIN'일 때 이미 자동으로 로드해줌 → 여기서 또 링크할 필요 없음 --%>

<div class="admin-main">

    <div class="hero">
        <div>
            <p class="hero-kicker">ADMIN</p>
            <h1>FAQ 관리</h1>
            <p>사용자 화면(자주 묻는 질문)에 노출되는 FAQ를 추가·수정·삭제합니다.</p>
        </div>
    </div>

    <c:if test="${not empty msg}">
        <div class="flash-ok">${msg}</div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <div class="admin-board-tabs">
        <a href="${contextPath}/spendolive/admin/inquiry/list.do" class="admin-board-tab">문의사항</a>
        <a href="${contextPath}/spendolive/admin/faq/list.do" class="admin-board-tab active">자주 묻는 질문</a>
    </div>

    <div class="panel">
        <div class="panel-header">
            <div class="panel-title">
                <p class="section-kicker">FAQ LIST</p>
                <h2>FAQ 목록 (총 ${faqList.size()}건)</h2>
            </div>
            <a href="${contextPath}/spendolive/admin/faq/write.do" class="btn primary">+ 새 FAQ 작성</a>
        </div>

        <div class="table-wrap">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th style="width:50px;text-align:center;">번호</th>
                        <th style="width:110px;text-align:center;">카테고리</th>
                        <th>질문</th>
                        <th style="width:70px;text-align:center;">순서</th>
                        <th style="width:80px;text-align:center;">노출</th>
                        <th style="width:150px;text-align:center;">관리</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty faqList}">
                            <tr><td colspan="6" style="text-align:center;padding:40px;color:var(--muted);">등록된 FAQ가 없습니다.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="faq" items="${faqList}" varStatus="s">
                                <tr>
                                    <td style="text-align:center;">${s.count}</td>
                                    <td style="text-align:center;">${faq.categoryLabel}</td>
                                    <td>
                                        <a href="${contextPath}/spendolive/admin/faq/edit.do?faqId=${faq.faqId}">${faq.question}</a>
                                    </td>
                                    <td style="text-align:center;">${faq.sortOrder}</td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${faq.useYn == 'Y'}"><span class="badge green">노출</span></c:when>
                                            <c:otherwise><span class="badge gray">숨김</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="table-actions" style="justify-content:center;">
                                            <a href="${contextPath}/spendolive/admin/faq/edit.do?faqId=${faq.faqId}" class="mini-btn">수정</a>
                                            <form action="${contextPath}/spendolive/admin/faq/delete.do" method="post" style="display:inline;"
                                                  onsubmit="return confirm('정말 삭제하시겠습니까?');">
                                                <input type="hidden" name="faqId" value="${faq.faqId}">
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
    </div>
</div>
