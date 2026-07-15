<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />


<section class="page-hero">
    <div class="container">
        <div class="notice-hero-text">
            <p class="eyebrow">ADMIN</p>
            <h1>${empty faq ? 'FAQ 작성' : 'FAQ 수정'}</h1>
        </div>
    </div>
</section>

<div class="write-wrap">
    <h2>${empty faq ? '새 FAQ 등록' : 'FAQ 수정'}</h2>

    <c:if test="${not empty errorMsg}">
        <div class="flash-err">⚠ ${errorMsg}</div>
    </c:if>

    <form action="${contextPath}/spendolive/admin/faq/${empty faq ? 'insert' : 'update'}.do" method="post" id="faqForm">

        <c:if test="${not empty faq}">
            <input type="hidden" name="faq_id" value="${faq.faq_id}">
        </c:if>

        <div class="form-row">
            <div class="form-group">
                <label for="category">카테고리</label>
                <select id="category" name="category" required>
                    <option value="">선택하세요</option>
                    <option value="account" ${faq.category == 'account' ? 'selected' : ''}>계정·로그인</option>
                    <option value="expense" ${faq.category == 'expense' ? 'selected' : ''}>지출관리</option>
                    <option value="ott"     ${faq.category == 'ott' ? 'selected' : ''}>OTT관리</option>
                    <option value="notice"  ${faq.category == 'notice' ? 'selected' : ''}>공지·알림</option>
                    <option value="etc"     ${faq.category == 'etc' ? 'selected' : ''}>기타</option>
                </select>
            </div>
            <div class="form-group">
                <label for="sort_order">노출 순서</label>
                <input type="number" id="sort_order" name="sort_order" value="${empty faq ? 0 : faq.sort_order}" min="0">
            </div>
        </div>

        <div class="form-group">
            <label for="question">질문</label>
            <input type="text" id="question" name="question" value="${faq.question}" placeholder="질문을 입력하세요" required>
        </div>

        <div class="form-group">
            <label for="answer">답변</label>
            <textarea id="answer" name="answer" placeholder="답변 내용을 입력하세요" required>${faq.answer}</textarea>
        </div>

        <div class="form-group">
            <div class="use-group">
                <input type="checkbox" id="use_yn" value="Y" ${empty faq || faq.use_yn == 'Y' ? 'checked' : ''}>
                <label for="use_yn">사용자 화면에 노출</label>
            </div>
        </div>

        <div class="btn-row">
            <a href="${contextPath}/spendolive/admin/faq/list.do" class="btn-cancel">취소</a>
            <button type="submit" class="btn-submit">${empty faq ? '등록' : '수정'}</button>
        </div>
    </form>
</div>

<script>
document.getElementById("faqForm").addEventListener("submit", function () {
    var cb = document.getElementById("use_yn");
    cb.name = "";
    var h = document.createElement("input");
    h.type = "hidden";
    h.name = "use_yn";
    h.value = cb.checked ? "Y" : "N";
    this.appendChild(h);
});
</script>
