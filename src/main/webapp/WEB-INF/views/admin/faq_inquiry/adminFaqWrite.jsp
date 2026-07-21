<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />


<div class="faq-page">
    <div class="page-hero">
        <div class="wrap">
            <p class="eyebrow">ADMIN</p>
            <h1>${empty faq ? 'FAQ 작성' : 'FAQ 수정'}</h1>
        </div>
    </div>

    <div class="wrap">
        <c:if test="${not empty errorMsg}">
            <div class="flash-err">⚠ ${errorMsg}</div>
        </c:if>

        <form class="form-card" action="${contextPath}/spendolive/admin/faq/${empty faq ? 'insert' : 'update'}.do" method="post" id="faqForm">

            <h2>${empty faq ? '새 FAQ 등록' : 'FAQ 수정'}</h2>

            <c:if test="${not empty faq}">
                <input type="hidden" name="faq_id" value="${faq.faq_id}">
            </c:if>

            <div class="field">
                <label>카테고리 <span>필수</span></label>
                <select name="category" required>
                    <option value="">선택하세요</option>
                    <option value="account" ${faq.category == 'account' ? 'selected' : ''}>계정·로그인</option>
                    <option value="expense" ${faq.category == 'expense' ? 'selected' : ''}>지출관리</option>
                    <option value="ott"     ${faq.category == 'ott' ? 'selected' : ''}>OTT관리</option>
                    <option value="notice"  ${faq.category == 'notice' ? 'selected' : ''}>공지·알림</option>
                    <option value="etc"     ${faq.category == 'etc' ? 'selected' : ''}>기타</option>
                </select>
                <c:if test="${empty faq}">
                    <span class="hint">새로 등록하면 선택한 카테고리 안에서 맨 마지막 순서로 추가돼요. 순서는 목록에서 ▲▼로 바꿀 수 있어요.</span>
                </c:if>
            </div>

            <div class="field">
                <label>질문 <span>필수</span></label>
                <input type="text" name="question" value="${faq.question}" placeholder="질문을 입력하세요" required>
            </div>

            <div class="field">
                <label>답변 <span>필수</span></label>
                <textarea name="answer" placeholder="답변 내용을 입력하세요" required>${faq.answer}</textarea>
            </div>

            <label class="check-row">
                <input type="checkbox" id="useYn" value="Y" ${empty faq || faq.use_yn == 'Y' ? 'checked' : ''}>
                <span>사용자 화면에 노출</span>
            </label>

            <div class="form-actions">
                <a class="btn btn-outline" style="flex:1;height:50px" href="${contextPath}/spendolive/admin/faq/list.do">취소</a>
                <button type="submit" class="btn btn-primary" style="flex:2;height:50px;font-size:15px">${empty faq ? '등록' : '수정'}</button>
            </div>
        </form>
    </div>
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
