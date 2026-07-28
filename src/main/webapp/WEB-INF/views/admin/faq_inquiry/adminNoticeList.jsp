<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%-- admin.css는 common/header.jsp가 role=='ADMIN'일 때 이미 자동으로 로드해줌 → 여기서 또 링크할 필요 없음 --%>

<div class="admin-main admin-board-page">

    <div class="hero">
        <div>
            <p class="hero-kicker">ADMIN</p>
            <h1>공지사항 관리</h1>
            <p>사용자 화면(알림&공지사항)에 노출되는 공지를 추가·수정·삭제합니다.</p>
        </div>
    </div>

    <%-- msg/errorMsg는 더 이상 flash로 넘어오지 않음(redirect가 없어졌으므로).
         등록/수정/삭제 결과 메시지는 adminNotice.js가 toast(showToast)로 띄워준다. --%>

    <div class="panel">
        <div class="panel-header">
            <div class="panel-title">
                <p class="section-kicker">NOTICE LIST</p>
                <%-- 총 건수는 이제 서버 렌더링이 아니라 JS가 AJAX 응답으로 채워 넣음 --%>
                <h2>공지사항 목록 (총 <span id="noticeTotalCount">0</span>건)</h2>
            </div>
            <%-- 새 공지 작성: 페이지 이동 대신 모달을 연다 (adminNotice.js가 처리) --%>
            <button type="button" id="noticeCreateBtn" class="btn primary">+ 새 공지 작성</button>
        </div>

        <div class="table-wrap">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th style="width:60px;text-align:center;">번호</th>
                        <th style="width:80px;text-align:center;">구분</th>
                        <th>제목</th>
                        <th style="width:120px;">작성자</th>
                        <th style="width:120px;">등록일</th>
                        <th style="width:150px;text-align:center;">관리</th>
                    </tr>
                </thead>
                <%-- 목록 내용은 전부 adminNotice.js의 renderNoticeTable()이 채워 넣는다.
                     최초에는 빈 tbody 상태로 렌더되고, 페이지 로드 직후 JS가
                     /admin/notice/ajax/list.do 를 호출해서 실제 데이터를 그려준다. --%>
                <tbody id="adminNoticeTableBody">
                    <tr><td colspan="6" style="text-align:center;padding:40px;color:var(--muted);">불러오는 중...</td></tr>
                </tbody>
            </table>
        </div>

        <%-- 페이지네이션도 JS가 totalPages/currentPage를 받아서 버튼을 직접 그려 넣음 --%>
        <div id="adminNoticePagination" class="admin-pagination"></div>
    </div>

    <%-- ══════════════════════════════════════════════════════════
         공지 작성/수정 모달 (문의/FAQ 모달과 같은 공통 .modal 시스템 재사용)
         작성=빈 폼 / 수정=adminNotice.js가 ajax/detail.do로 값을 채워 넣음.
         ※ 모달 박스에는 stopPropagation을 걸지 않는다(걸면 X/취소 클릭이 막힘).
         ══════════════════════════════════════════════════════════ --%>
    <div class="modal" id="adminNoticeModal">
        <div class="modal-box modal-admin-inquiry">
            <button type="button" class="modal-close" data-action="closeNoticeModal">✕</button>
            <form id="noticeModalForm" onsubmit="return false;">
                <input type="hidden" id="modalNoticeId" name="notice_id" value="0">
                <h2 id="noticeModalHeading" style="margin-top:0;">새 공지사항 등록</h2>

                <div class="form-group">
                    <label for="modalNoticeTitleInput">제목</label>
                    <input type="text" id="modalNoticeTitleInput" name="title" placeholder="공지 제목을 입력하세요" required>
                </div>

                <div class="form-group">
                    <label>구분</label>
                    <div class="pin-group">
                        <input type="checkbox" id="modalNoticePinned" name="pinned_yn" value="Y">
                        <label for="modalNoticePinned">중요 공지로 설정</label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="modalNoticeContent">내용</label>
                    <textarea id="modalNoticeContent" name="content" placeholder="공지 내용을 입력하세요" required></textarea>
                </div>

                <div class="btn-row" style="display:flex;gap:.5rem;justify-content:flex-end;margin-top:16px;">
                    <button type="button" class="btn btn-outline" data-action="closeNoticeModal">취소</button>
                    <button type="button" id="noticeModalSubmitBtn" class="btn btn-primary">등록</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="${contextPath}/resources/js/adminNotice.js"></script>
