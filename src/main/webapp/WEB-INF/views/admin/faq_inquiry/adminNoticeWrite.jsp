<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<div class="admin-main" data-admin-page="notice" data-admin-title="공지사항 관리">
    <section class="hero">
        <div>
            <p class="hero-kicker">Notice Management</p>
            <h1>${empty notice ? '공지사항 추가' : '공지사항 수정'}</h1>
            <p>이 화면은 기존 주소로 직접 접근했을 때 사용하는 호환용 화면입니다. 일반적인 추가·수정은 공지 목록 페이지 안에서 처리됩니다.</p>
        </div>
    </section>

    <c:if test="${not empty errorMsg}"><div class="flash-err"><c:out value="${errorMsg}" /></div></c:if>

    <section class="panel">
        <div class="panel-header">
            <div class="panel-title"><h2>${empty notice ? '새 공지 등록' : '공지 수정'}</h2></div>
            <a class="btn ghost" href="${contextPath}/admin/notice/list.do#${empty notice ? 'add' : 'list'}">통합 관리 화면으로</a>
        </div>

        <form action="${contextPath}/admin/notice/${empty notice ? 'insert' : 'update'}.do" method="post">
            <c:if test="${not empty notice}"><input type="hidden" name="notice_id" value="${notice.notice_id}"></c:if>
            <input type="hidden" name="pinned_yn" id="fallbackPinnedYn" value="${notice.pinned_yn eq 'Y' ? 'Y' : 'N'}">
            <div class="form-field"><label>제목</label><input class="form-input" type="text" name="title" value="<c:out value='${notice.title}' />" required></div>
            <label class="check-row" style="margin-top:16px;"><input type="checkbox" ${notice.pinned_yn eq 'Y' ? 'checked' : ''} onchange="document.getElementById('fallbackPinnedYn').value=this.checked?'Y':'N'"><span>중요 공지로 설정</span></label>
            <div class="form-field" style="margin-top:16px;"><label>내용</label><textarea class="form-textarea" name="content" required><c:out value="${notice.content}" /></textarea></div>
            <div class="toolbar" style="justify-content:flex-end;margin-top:18px;margin-bottom:0;"><a class="btn ghost" href="${contextPath}/admin/notice/list.do">취소</a><button class="btn primary" type="submit">${empty notice ? '등록' : '수정'}</button></div>
        </form>
    </section>
</div>
