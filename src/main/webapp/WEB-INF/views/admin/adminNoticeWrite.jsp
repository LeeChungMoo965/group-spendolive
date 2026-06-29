<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<style>
.write-wrap { max-width: 800px; margin: 40px auto; padding: 0 20px; }
.write-wrap h2 { font-size: 1.4rem; font-weight: 700; margin-bottom: 28px; }
.form-group { margin-bottom: 18px; }
.form-group label { display:block; font-weight:600; font-size:.9rem; color:#374151; margin-bottom:6px; }
.form-group input[type="text"],
.form-group textarea {
    width:100%; padding:10px 14px; border:1px solid #d1d5db;
    border-radius:8px; font-size:.95rem; box-sizing:border-box;
}
.form-group input[type="text"]:focus,
.form-group textarea:focus { outline:none; border-color:#22c55e; }
.form-group textarea { min-height:280px; resize:vertical; }
.pin-group { display:flex; align-items:center; gap:10px; }
.pin-group input[type="checkbox"] { width:18px; height:18px; cursor:pointer; }
.pin-group label { margin:0; font-weight:500; cursor:pointer; }
.btn-row { display:flex; gap:10px; justify-content:flex-end; margin-top:28px; }
.btn-submit { background:#22c55e; color:#fff; padding:10px 26px; border-radius:8px; border:none; font-size:.95rem; font-weight:600; cursor:pointer; }
.btn-cancel { background:#f3f4f6; color:#374151; padding:10px 20px; border-radius:8px; border:none; font-size:.95rem; cursor:pointer; text-decoration:none; display:inline-block; }
.flash-err { background:#fee2e2; border:1px solid #fca5a5; color:#991b1b; padding:12px 18px; border-radius:8px; margin-bottom:18px; }
</style>

<section class="page-hero">
    <div class="container">
        <div class="notice-hero-text">
            <p class="eyebrow">ADMIN</p>
            <h1>${empty notice ? '공지사항 작성' : '공지사항 수정'}</h1>
        </div>
    </div>
</section>

<div class="write-wrap">
    <h2>${empty notice ? '새 공지사항 등록' : '공지사항 수정'}</h2>

    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <form action="${contextPath}/spendolive/admin/notice/${empty notice ? 'insert' : 'update'}.do" method="post" id="noticeForm">

        <c:if test="${not empty notice}">
            <input type="hidden" name="noticeId" value="${notice.noticeId}">
        </c:if>

        <div class="form-group">
            <label for="title">제목</label>
            <input type="text" id="title" name="title" value="${notice.title}" placeholder="공지 제목을 입력하세요" required>
        </div>

        <div class="form-group">
            <label>구분</label>
            <div class="pin-group">
                <input type="checkbox" id="pinnedYn" value="Y" ${notice.pinnedYn == 'Y' ? 'checked' : ''}>
                <label for="pinnedYn">중요 공지로 설정</label>
            </div>
        </div>

        <div class="form-group">
            <label for="content">내용</label>
            <textarea id="content" name="content" placeholder="공지 내용을 입력하세요" required>${notice.content}</textarea>
        </div>

        <div class="btn-row">
            <a href="${contextPath}/spendolive/admin/notice/list.do" class="btn-cancel">취소</a>
            <button type="submit" class="btn-submit">${empty notice ? '등록' : '수정'}</button>
        </div>
    </form>
</div>

<script>
document.getElementById("noticeForm").addEventListener("submit", function () {
    var cb = document.getElementById("pinnedYn");
    cb.name = "";
    var h = document.createElement("input");
    h.type = "hidden";
    h.name = "pinnedYn";
    h.value = cb.checked ? "Y" : "N";
    this.appendChild(h);
});
</script>
