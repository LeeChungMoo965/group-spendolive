<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/faq.css">

<%--
  참고: 상태 필터(전체/답변대기/답변완료/검토중)는 서버에 status 파라미터로 다시 요청해서
        전체 문의 중 해당 상태만 걸러 1페이지부터 보여준다 (InquiryController.normalizeStatusFilter 참고).
        페이지네이션: 문의(필터 적용 후 기준)가 10개 이하면 전부 한 페이지, 넘으면 5개씩.
--%>

<div class="faq-page">
    <div class="page-hero">
        <div class="wrap">
            <p class="eyebrow">MY INQUIRIES</p>
            <h1>내 문의 조회</h1>
            <p class="hero-sub">SpendOlive에 남긴 문의 내역과 답변을 확인하세요.</p>
        </div>
    </div>

    <div class="wrap">
        <div class="board-header">
            <h2>문의 내역</h2>
            <a class="btn btn-primary" href="${contextPath}/spendolive/inquiry/write.do">+ 새 문의 작성</a>
        </div>

        <div class="filters">
            <a class="filter-btn ${currentStatus == 'all' ? 'active' : ''}"
               href="${contextPath}/spendolive/inquiry/list.do?status=all">전체</a>
            <a class="filter-btn ${currentStatus == 'wait' ? 'active' : ''}"
               href="${contextPath}/spendolive/inquiry/list.do?status=wait">답변 대기</a>
            <a class="filter-btn ${currentStatus == 'done' ? 'active' : ''}"
               href="${contextPath}/spendolive/inquiry/list.do?status=done">답변 완료</a>
            <a class="filter-btn ${currentStatus == 'review' ? 'active' : ''}"
               href="${contextPath}/spendolive/inquiry/list.do?status=review">검토 중</a>
        </div>

        <c:if test="${not empty inquiryList}">
            <div class="inq-list" id="inqList">
                <c:forEach var="inq" items="${inquiryList}">
                    <div class="inq-card" onclick="goInquiryDetail('${contextPath}','${inq.inquiry_id}')">
                        <div class="inq-top">
                            <span class="inq-category">${inq.category}</span>
                            <div class="inq-meta">
                                <span class="badge ${inq.statusCode}">${inq.statusLabel}</span>
                                <span class="inq-date">${inq.reg_date}</span>
                            </div>
                        </div>
                        <div class="inq-title">${inq.title}</div>
                        <div class="inq-preview">${inq.preview}</div>
                        <c:if test="${not empty inq.files}">
                            <div class="inq-attachments" onclick="event.stopPropagation()">
                                <c:forEach var="file" items="${inq.files}">
                                    <c:choose>
                                        <c:when test="${file.image}">
                                            <img src="${contextPath}/spendolive/inquiry/file/${file.file_id}"
                                                 alt="${file.origin_name}" class="inq-thumb"
                                                 onclick="event.stopPropagation(); openInqLightbox(this.src, '${file.origin_name}')">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${contextPath}/spendolive/inquiry/file/${file.file_id}"
                                               target="_blank" class="inq-file-link">📎 ${file.origin_name}</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </div>
                        </c:if>
                        <div class="inq-bottom">
                            <c:choose>
                                <c:when test="${inq.hasReply}">
                                    <div class="inq-reply"><span class="reply-dot"></span> 관리자 답변이 달렸습니다</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="inq-reply"><span class="reply-dot none"></span> 아직 답변이 없습니다</div>
                                </c:otherwise>
                            </c:choose>
                            <span class="inq-no">문의 #${inq.inquiry_id}</span>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <c:forEach begin="1" end="${totalPages}" var="p">
                        <a class="pg-btn ${p == currentPage ? 'active' : ''}"
                           href="${contextPath}/spendolive/inquiry/list.do?status=${currentStatus}&page=${p}">${p}</a>
                    </c:forEach>
                </div>
            </c:if>
        </c:if>

        <c:if test="${empty inquiryList}">
            <div class="empty-box">
                <div class="icon-big">📭</div>
                <c:choose>
                    <c:when test="${currentStatus == 'all'}">
                        <p>아직 등록한 문의가 없습니다.</p>
                        <a class="btn btn-primary" href="${contextPath}/spendolive/inquiry/write.do">+ 새 문의 작성</a>
                    </c:when>
                    <c:otherwise>
                        <p>해당 상태의 문의가 없습니다.</p>
                        <a class="btn btn-outline" href="${contextPath}/spendolive/inquiry/list.do?status=all">전체 문의 보기</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </div>

    <%-- 첨부 사진 확대보기 (01-foundation.css의 공통 .modal 시스템 재사용) --%>
    <div class="modal" id="inqLightbox" onclick="closeInqLightbox(event)">
        <div class="modal-box modal-photo" onclick="event.stopPropagation()">
            <button type="button" class="modal-close" onclick="closeInqLightbox(event)">✕</button>
            <img src="" alt="" id="inqLightboxImg">
        </div>
    </div>
</div>

<script>
    // 문의 제출 후 리다이렉트로 넘어온 1회성 메시지 (main.jsp와 동일한 패턴)
    var msg = "${msg}";
    if (msg && msg !== "") {
        alert(msg);
    }

    // 첨부 사진 확대보기
    function openInqLightbox(src, name) {
        var img = document.getElementById('inqLightboxImg');
        img.src = src;
        img.alt = name || '';
        document.getElementById('inqLightbox').classList.add('show');
    }
    function closeInqLightbox(e) {
        document.getElementById('inqLightbox').classList.remove('show');
    }
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeInqLightbox();
    });
</script>
<script src="${contextPath}/resources/js/faq.js"></script>

