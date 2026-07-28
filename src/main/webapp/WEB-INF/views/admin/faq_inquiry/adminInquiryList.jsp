<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%-- admin.css는 common/header.jsp가 role=='ADMIN'일 때 이미 자동으로 로드해줌 --%>

<div class="admin-main" data-admin-page="inquiry">

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

    <div id="adminBoardArea">
    <div class="admin-board-tabs">
        <a href="${contextPath}/admin/inquiry/list.do" class="admin-board-tab active">문의사항</a>
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
                   href="${contextPath}/admin/inquiry/list.do?status=all">전체</a>
                <a class="${currentStatus == 'wait' ? 'active' : ''}"
                   href="${contextPath}/admin/inquiry/list.do?status=wait">답변 대기</a>
                <a class="${currentStatus == 'done' ? 'active' : ''}"
                   href="${contextPath}/admin/inquiry/list.do?status=done">답변 완료</a>
                <a class="${currentStatus == 'review' ? 'active' : ''}"
                   href="${contextPath}/admin/inquiry/list.do?status=review">검토 중</a>
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
                                        <a href="javascript:void(0)" onclick="openAdminInquiryModal(${inq.inquiry_id})">${inq.title}</a>
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
            <%-- 현재 페이지 기준 앞뒤 2개씩만 보여주는 윈도우 방식 --%>
            <c:set var="pgStart" value="${currentPage - 2 < 1 ? 1 : currentPage - 2}" />
            <c:set var="pgEnd" value="${currentPage + 2 > totalPages ? totalPages : currentPage + 2}" />

            <div class="admin-pagination">
                <c:if test="${pgStart > 1}">
                    <a class="admin-pg-btn" href="${contextPath}/admin/inquiry/list.do?status=${currentStatus}&page=1">1</a>
                    <c:if test="${pgStart > 2}">
                        <span>…</span>
                    </c:if>
                </c:if>

                <c:forEach begin="${pgStart}" end="${pgEnd}" var="p">
                    <a class="admin-pg-btn ${p == currentPage ? 'active' : ''}"
                       href="${contextPath}/admin/inquiry/list.do?status=${currentStatus}&page=${p}">${p}</a>
                </c:forEach>

                <c:if test="${pgEnd < totalPages}">
                    <c:if test="${pgEnd < totalPages - 1}">
                        <span>…</span>
                    </c:if>
                    <a class="admin-pg-btn" href="${contextPath}/admin/inquiry/list.do?status=${currentStatus}&page=${totalPages}">${totalPages}</a>
                </c:if>
            </div>
        </c:if>
    </div>

    <%-- 문의별 상세+답변폼 숨김 템플릿 --%>
    <c:forEach var="inq" items="${inquiryList}">
        <div id="adminInqDetailTpl${inq.inquiry_id}" style="display:none">
            <p class="section-kicker">문의 #${inq.inquiry_id}</p>
            <h2 style="margin:4px 0 10px;">${inq.title}</h2>
            <p style="color:var(--muted);margin-bottom:16px;">
                ${inq.category} · ${inq.inquiry_type} · 작성자
                <c:choose>
                    <c:when test="${not empty inq.writer_nickname}">${inq.writer_nickname} (${inq.id})</c:when>
                    <c:otherwise>${inq.id}</c:otherwise>
                </c:choose>
                · ${inq.reg_date}
                &nbsp;
                <c:choose>
                    <c:when test="${inq.statusCode == 'done'}"><span class="badge green">답변완료</span></c:when>
                    <c:when test="${inq.statusCode == 'review'}"><span class="badge blue">검토중</span></c:when>
                    <c:otherwise><span class="badge yellow">답변대기</span></c:otherwise>
                </c:choose>
            </p>

            <div>
                <label>문의 내용</label>
                <div style="white-space:pre-wrap;padding:14px;border:1px solid #eef0e1;border-radius:12px;background:#fffef8;">${inq.content}</div>
            </div>

            <c:if test="${not empty inq.files}">
                <div>
                    <label>첨부파일</label>
                    <div style="display:flex;flex-wrap:wrap;gap:8px;">
                        <c:forEach var="file" items="${inq.files}">
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

            <%-- 답변 등록/수정 폼. listStatus/listPage는 등록 후 지금 보고 있던
                 필터·페이지 그대로 목록(팝업이 있는 화면)으로 돌아오기 위한 값 --%>
            <form action="${contextPath}/admin/inquiry/reply.do" method="post">
                <input type="hidden" name="inquiry_id" value="${inq.inquiry_id}">
                <input type="hidden" name="listStatus" value="${currentStatus}">
                <input type="hidden" name="listPage" value="${currentPage}">

                <div style="display:grid;grid-template-columns:1fr 160px;gap:14px;">
                    <div>
                        <label for="reply_content_${inq.inquiry_id}">답변 내용</label>
                        <textarea id="reply_content_${inq.inquiry_id}" name="reply_content" class="form-textarea"
                                  placeholder="답변 내용을 입력하세요" required>${inq.reply_content}</textarea>
                    </div>
                    <div>
                        <label for="status_${inq.inquiry_id}">처리 상태</label>
                        <select id="status_${inq.inquiry_id}" name="status" class="select-input" style="width:100%;">
                            <option value="DONE"   ${inq.status == 'REVIEW' ? '' : 'selected'}>답변 완료</option>
                            <option value="REVIEW" ${inq.status == 'REVIEW' ? 'selected' : ''}>검토 중</option>
                        </select>
                    </div>
                </div>

                <div class="toolbar" style="justify-content:flex-end;margin-top:20px;">
                    <button type="submit" class="btn primary">${empty inq.reply_content ? '답변 등록' : '답변 수정'}</button>
                </div>
            </form>
        </div>
    </c:forEach>

    <div class="modal" id="adminInqDetailModal" onclick="closeAdminInquiryModal(event)">
        <div class="modal-box" onclick="event.stopPropagation()">
            <button type="button" class="modal-close" onclick="closeAdminInquiryModal(event)">✕</button>
            <div id="adminInqDetailBody"></div>
        </div>
    </div>
    </div>
</div>
