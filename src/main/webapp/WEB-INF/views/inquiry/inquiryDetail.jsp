<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<div class="faq-page">
    <div class="page-hero">
        <div class="wrap">
            <p class="eyebrow">MY INQUIRIES</p>
            <h1>문의 상세</h1>
            <p class="hero-sub">남기신 문의 내용과 답변을 확인하세요.</p>
        </div>
    </div>

    <div class="wrap">
        <div class="board-header">
            <h2>문의 #${inquiry.inquiry_id}</h2>
            <a class="btn btn-outline" href="${contextPath}/spendolive/inquiry/list.do">← 목록으로</a>
        </div>

        <div class="inq-card" style="cursor:default">
            <div class="inq-top">
                <span class="inq-category">${inquiry.categoryLabel} · ${inquiry.inquiryTypeLabel}</span>
                <div class="inq-meta">
                    <span class="badge ${inquiry.statusCode}">${inquiry.statusLabel}</span>
                    <span class="inq-date">${inquiry.reg_date}</span>
                </div>
            </div>

            <div class="inq-title" style="font-size:18px;margin-top:8px">${inquiry.title}</div>
            <div class="inq-preview" style="white-space:pre-line;-webkit-line-clamp:unset">${inquiry.content}</div>

            <c:if test="${not empty inquiry.files}">
                <div class="inq-attachments">
                    <c:forEach var="file" items="${inquiry.files}">
                        <c:choose>
                            <c:when test="${file.image}">
                                <img src="${contextPath}/spendolive/inquiry/file/${file.file_id}"
                                     alt="${file.origin_name}" class="inq-thumb"
                                     onclick="openInqLightbox(this.src, '${file.origin_name}')">
                            </c:when>
                            <c:otherwise>
                                <a href="${contextPath}/spendolive/inquiry/file/${file.file_id}"
                                   target="_blank" class="inq-file-link">📎 ${file.origin_name}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
            </c:if>
        </div>

        <c:choose>
            <c:when test="${inquiry.hasReply}">
                <div class="inq-card" style="cursor:default;margin-top:14px;background:#f8fbf7">
                    <div class="inq-top">
                        <span class="inq-category">관리자 답변</span>
                        <span class="inq-date">${inquiry.reply_date}</span>
                    </div>
                    <div class="inq-preview" style="white-space:pre-line;-webkit-line-clamp:unset;margin-top:8px">${inquiry.reply_content}</div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-box" style="margin-top:14px">
                    <p>아직 답변이 등록되지 않았습니다. 조금만 기다려 주세요.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="modal" id="inqLightbox" onclick="closeInqLightbox(event)">
        <div class="modal-box modal-photo" onclick="event.stopPropagation()">
            <button type="button" class="modal-close" onclick="closeInqLightbox(event)">✕</button>
            <img src="" alt="" id="inqLightboxImg">
        </div>
    </div>
</div>

<script src="${contextPath}/resources/js/faq.js"></script>