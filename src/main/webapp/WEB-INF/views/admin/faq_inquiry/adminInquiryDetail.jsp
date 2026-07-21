<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%-- admin.css는 common/header.jsp가 role=='ADMIN'일 때 이미 자동으로 로드해줌 --%>

<div class="admin-main">

    <div class="hero">
        <div>
            <p class="hero-kicker">ADMIN</p>
            <h1>문의 상세</h1>
        </div>
    </div>

    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <div class="panel">
        <div class="panel-header">
            <div class="panel-title">
                <p class="section-kicker">문의 #${inquiry.inquiry_id}</p>
                <h2>${inquiry.title}</h2>
                <p>
                    ${inquiry.category} · ${inquiry.inquiry_type} · 작성자
                    <c:choose>
                        <c:when test="${not empty inquiry.writer_nickname}">${inquiry.writer_nickname} (${inquiry.id})</c:when>
                        <c:otherwise>${inquiry.id}</c:otherwise>
                    </c:choose>
                    · ${inquiry.reg_date}
                    &nbsp;
                    <c:choose>
                        <c:when test="${inquiry.statusCode == 'done'}"><span class="badge green">답변완료</span></c:when>
                        <c:when test="${inquiry.statusCode == 'review'}"><span class="badge blue">검토중</span></c:when>
                        <c:otherwise><span class="badge yellow">답변대기</span></c:otherwise>
                    </c:choose>
                </p>
            </div>
            <a href="${contextPath}/admin/inquiry/list.do" class="btn ghost">목록으로</a>
        </div>

        <div class="form-group">
            <label>문의 내용</label>
            <div style="white-space:pre-wrap;padding:14px;border:1px solid #eef0e1;border-radius:12px;background:#fffef8;">${inquiry.content}</div>
        </div>

        <c:if test="${not empty inquiry.files}">
            <div class="form-group">
                <label>첨부파일</label>
                <div style="display:flex;flex-wrap:wrap;gap:8px;">
                    <c:forEach var="file" items="${inquiry.files}">
                        <c:choose>
                            <c:when test="${file.image}">
                                <a href="${contextPath}/spendolive/inquiry/file/${file.file_id}" target="_blank">
                                    <img src="${contextPath}/spendolive/inquiry/file/${file.file_id}" alt="${file.origin_name}"
                                         style="width:96px;height:96px;object-fit:cover;border-radius:12px;border:1px solid #eef0e1;">
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${contextPath}/spendolive/inquiry/file/${file.file_id}" target="_blank" class="mini-btn">📎 ${file.origin_name}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
            </div>
        </c:if>

        <form action="${contextPath}/admin/inquiry/reply.do" method="post">
            <input type="hidden" name="inquiry_id" value="${inquiry.inquiry_id}">

            <div class="form-row" style="display:grid;grid-template-columns:1fr 160px;gap:14px;">
                <div class="form-group">
                    <label for="reply_content">답변 내용</label>
                    <textarea id="reply_content" name="reply_content" class="form-textarea"
                              placeholder="답변 내용을 입력하세요" required>${inquiry.reply_content}</textarea>
                </div>
                <div class="form-group">
                    <label for="status">처리 상태</label>
                    <select id="status" name="status" class="select-input" style="width:100%;">
                        <option value="DONE"   ${inquiry.status == 'REVIEW' ? '' : 'selected'}>답변 완료</option>
                        <option value="REVIEW" ${inquiry.status == 'REVIEW' ? 'selected' : ''}>검토 중</option>
                    </select>
                </div>
            </div>

            <div class="toolbar" style="justify-content:flex-end;margin-top:20px;">
                <button type="submit" class="btn primary">${empty inquiry.reply_content ? '답변 등록' : '답변 수정'}</button>
            </div>
        </form>
    </div>
</div>
