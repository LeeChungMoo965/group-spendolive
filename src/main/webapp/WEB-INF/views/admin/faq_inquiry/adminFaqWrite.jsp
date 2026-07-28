<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<div class="admin-main" data-admin-page="faq" data-admin-title="FAQ 관리">
    <section class="hero">
        <div>
            <p class="hero-kicker">FAQ Management</p>
            <h1>${empty faq ? 'FAQ 추가' : 'FAQ 수정'}</h1>
            <p>이 화면은 기존 주소로 직접 접근했을 때 사용하는 호환용 화면입니다. 일반적인 추가·수정은 FAQ 목록 페이지 안에서 처리됩니다.</p>
        </div>
    </section>

    <c:if test="${not empty errorMsg}"><div class="flash-err"><c:out value="${errorMsg}" /></div></c:if>

    <section class="panel">
        <div class="panel-header">
            <div class="panel-title"><h2>${empty faq ? '새 FAQ 등록' : 'FAQ 수정'}</h2></div>
            <a class="btn ghost" href="${contextPath}/spendolive/admin/faq/list.do#${empty faq ? 'add' : 'list'}">통합 관리 화면으로</a>
        </div>

        <form action="${contextPath}/spendolive/admin/faq/${empty faq ? 'insert' : 'update'}.do" method="post">
            <c:if test="${not empty faq}"><input type="hidden" name="faq_id" value="${faq.faq_id}"></c:if>
            <input type="hidden" name="useYn" id="fallbackFaqUseYn" value="${empty faq || faq.use_yn eq 'Y' ? 'Y' : 'N'}">

            <div class="form-grid" style="grid-template-columns:260px minmax(0,1fr);">
                <div class="form-field">
                    <label>카테고리</label>
                    <select class="form-input" name="category" required>
                        <option value="account" ${faq.category eq 'account' ? 'selected' : ''}>계정·로그인</option>
                        <option value="expense" ${faq.category eq 'expense' ? 'selected' : ''}>지출관리</option>
                        <option value="ott" ${faq.category eq 'ott' ? 'selected' : ''}>OTT관리</option>
                        <option value="notice" ${faq.category eq 'notice' ? 'selected' : ''}>공지·알림</option>
                        <option value="etc" ${faq.category eq 'etc' ? 'selected' : ''}>기타</option>
                    </select>
                </div>
                <div class="form-field"><label>질문</label><input class="form-input" type="text" name="question" value="<c:out value='${faq.question}' />" required></div>
            </div>
            <div class="form-field"><label>답변</label><textarea class="form-textarea" name="answer" required><c:out value="${faq.answer}" /></textarea></div>
            <label class="check-row" style="margin-top:16px;"><input type="checkbox" ${empty faq || faq.use_yn eq 'Y' ? 'checked' : ''} onchange="document.getElementById('fallbackFaqUseYn').value=this.checked?'Y':'N'"><span>사용자 화면에 노출</span></label>
            <div class="toolbar" style="justify-content:flex-end;margin-top:18px;margin-bottom:0;"><a class="btn ghost" href="${contextPath}/spendolive/admin/faq/list.do">취소</a><button class="btn primary" type="submit">${empty faq ? '등록' : '수정'}</button></div>
        </form>
    </section>
</div>
