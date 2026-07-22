<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero">
    <div class="container">
        <div class="notice-hero-text">
            <p class="eyebrow">ADMIN</p>
            <h1>${empty notice ? '공지사항 작성' : '공지사항 수정'}</h1>
        </div>
    </div>
</section>

<div class="admin-wrap narrow">
    <h2>${empty notice ? '새 공지사항 등록' : '공지사항 수정'}</h2>

    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <form action="${contextPath}/admin/notice/${empty notice ? 'insert' : 'update'}.do" method="post" id="noticeForm">

        <c:if test="${not empty notice}">
            <input type="hidden" name="notice_id" value="${notice.notice_id}">
        </c:if>

        <div class="form-group">
            <label for="title">제목</label>
            <input type="text" id="title" name="title" value="${notice.title}" placeholder="공지 제목을 입력하세요" required>
        </div>

        <div class="form-group">
            <label>구분</label>
            <div class="pin-group">
                <input type="checkbox" id="pinned_yn" value="Y" ${notice.pinned_yn == 'Y' ? 'checked' : ''}>
                <label for="pinned_yn">중요 공지로 설정</label>
            </div>
        </div>

        <div class="form-group">
            <label for="content">내용</label>
            <textarea id="content" name="content" placeholder="공지 내용을 입력하세요" required>${notice.content}</textarea>
        </div>

        <div class="btn-row">
            <a href="${contextPath}/admin/notice/list.do" class="btn btn-outline">취소</a>
            <button type="submit" class="btn btn-primary">${empty notice ? '등록' : '수정'}</button>
        </div>
    </form>
</div>

<script>
document.getElementById("noticeForm").addEventListener("submit", function () {
    var cb = document.getElementById("pinned_yn");
    cb.name = "";
    var h = document.createElement("input");
    h.type = "hidden";
    h.name = "pinned_yn";
    h.value = cb.checked ? "Y" : "N";
    this.appendChild(h);
});
</script>
