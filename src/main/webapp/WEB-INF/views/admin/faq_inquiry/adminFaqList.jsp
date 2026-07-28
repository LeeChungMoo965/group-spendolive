<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%-- admin.css는 common/header.jsp가 role=='ADMIN'일 때 이미 자동으로 로드해줌 → 여기서 또 링크할 필요 없음 --%>

<div class="admin-main admin-board-page">

    <div class="hero">
        <div>
            <p class="hero-kicker">ADMIN</p>
            <h1>FAQ 관리</h1>
            <p>사용자 화면(자주 묻는 질문)에 노출되는 FAQ를 추가·수정·삭제합니다.</p>
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
        <a href="${contextPath}/admin/inquiry/list.do" class="admin-board-tab">문의사항</a>
        <a href="${contextPath}/spendolive/admin/faq/list.do" class="admin-board-tab active">자주 묻는 질문</a>
    </div>

    <div class="panel">
        <div class="panel-header">
            <div class="panel-title">
                <p class="section-kicker">FAQ LIST</p>
                <h2>FAQ 목록 (총 ${faqList.size()}건)</h2>
            </div>
            <%-- 새 FAQ 작성: 페이지 이동 대신 모달을 연다 (adminFaq.js가 처리) --%>
            <button type="button" class="btn primary" data-action="create">+ 새 FAQ 작성</button>
        </div>

        <c:if test="${empty faqGroups}">
            <div class="table-wrap">
                <table class="admin-table">
                    <tbody>
                        <tr><td style="text-align:center;padding:40px;color:var(--muted);">등록된 FAQ가 없습니다.</td></tr>
                    </tbody>
                </table>
            </div>
        </c:if>

        <c:forEach var="entry" items="${faqGroups}">
            <h3 class="category-group-heading">${entry.value[0].categoryLabel}
                <span class="category-count">${entry.value.size()}건</span>
            </h3>

            <div class="table-wrap">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width:60px;text-align:center;">번호</th>
                            <th>질문</th>
                            <th style="width:80px;text-align:center;">노출</th>
                            <th style="width:150px;text-align:center;">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="faq" items="${entry.value}" varStatus="s">
                            <tr>
                                <td style="text-align:center;">${s.count}</td>
                                <td>
                                    <a href="javascript:void(0)" data-action="edit" data-faq-id="${faq.faq_id}">${faq.question}</a>
                                </td>
                                <td style="text-align:center;">
                                    <c:choose>
                                        <c:when test="${faq.use_yn == 'Y'}"><span class="badge green">노출</span></c:when>
                                        <c:otherwise><span class="badge gray">숨김</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <%-- 순서변경/삭제는 adminFaq.js가 data-action으로 가로채 AJAX 처리.
                                         (예전 form POST → 전체 새로고침 방식 제거) --%>
                                    <div class="table-actions" style="justify-content:center;">
                                        <button type="button" class="mini-btn" data-action="moveUp"
                                                data-faq-id="${faq.faq_id}" ${s.index == 0 ? 'disabled' : ''}>▲</button>
                                        <button type="button" class="mini-btn" data-action="moveDown"
                                                data-faq-id="${faq.faq_id}" ${s.index == entry.value.size()-1 ? 'disabled' : ''}>▼</button>
                                        <button type="button" class="mini-btn" data-action="edit"
                                                data-faq-id="${faq.faq_id}">수정</button>
                                        <button type="button" class="mini-btn danger" data-action="delete"
                                                data-faq-id="${faq.faq_id}">삭제</button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:forEach>
    </div>

    <%-- ══════════════════════════════════════════════════════════
         FAQ 작성/수정 모달용 숨김 템플릿 + 모달 컨테이너
         (문의 상세 모달과 동일한 방식: adminFaq.js가 템플릿을 모달 안에 복사)
         체크박스에 name="useYn"을 줘서 이제 노출값이 정상 전송됨(기존 버그 수정)
         ══════════════════════════════════════════════════════════ --%>

    <%-- 새 FAQ 작성 템플릿 (빈 폼, data-faq-id=0) --%>
    <div class="admin-faq-form-tpl" id="adminFaqCreateTpl" style="display:none">
        <form class="faq-modal-form" data-faq-id="0" onsubmit="return false;">
            <h2 style="margin-top:0;">새 FAQ 등록</h2>
            <div class="field">
                <label>카테고리 <span>필수</span></label>
                <select name="category" required>
                    <option value="">선택하세요</option>
                    <option value="account">계정·로그인</option>
                    <option value="expense">지출관리</option>
                    <option value="ott">OTT관리</option>
                    <option value="notice">공지·알림</option>
                    <option value="etc">기타</option>
                </select>
                <span class="hint">새로 등록하면 선택한 카테고리 맨 마지막 순서로 추가돼요. 순서는 ▲▼로 바꿀 수 있어요.</span>
            </div>
            <div class="field">
                <label>질문 <span>필수</span></label>
                <input type="text" name="question" placeholder="질문을 입력하세요" required>
            </div>
            <div class="field">
                <label>답변 <span>필수</span></label>
                <textarea name="answer" placeholder="답변 내용을 입력하세요" required></textarea>
            </div>
            <label class="check-row">
                <input type="checkbox" name="useYn" value="Y" checked>
                <span>사용자 화면에 노출</span>
            </label>
            <div class="form-actions" style="margin-top:20px;">
                <button type="button" class="btn btn-outline" style="flex:1;height:46px" data-action="closeModal">취소</button>
                <button type="submit" class="btn btn-primary" style="flex:2;height:46px">등록</button>
            </div>
        </form>
    </div>

    <%-- FAQ별 수정 템플릿 (기존 값 채워둠) --%>
    <c:forEach var="entry" items="${faqGroups}">
        <c:forEach var="faq" items="${entry.value}">
            <div class="admin-faq-form-tpl" id="adminFaqEditTpl${faq.faq_id}" style="display:none">
                <form class="faq-modal-form" data-faq-id="${faq.faq_id}" onsubmit="return false;">
                    <h2 style="margin-top:0;">FAQ 수정</h2>
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
                    </div>
                    <div class="field">
                        <label>질문 <span>필수</span></label>
                        <input type="text" name="question" value="${faq.question}" required>
                    </div>
                    <div class="field">
                        <label>답변 <span>필수</span></label>
                        <textarea name="answer" required>${faq.answer}</textarea>
                    </div>
                    <label class="check-row">
                        <input type="checkbox" name="useYn" value="Y" ${faq.use_yn == 'Y' ? 'checked' : ''}>
                        <span>사용자 화면에 노출</span>
                    </label>
                    <div class="form-actions" style="margin-top:20px;">
                        <button type="button" class="btn btn-outline" style="flex:1;height:46px" data-action="closeModal">취소</button>
                        <button type="submit" class="btn btn-primary" style="flex:2;height:46px">수정</button>
                    </div>
                </form>
            </div>
        </c:forEach>
    </c:forEach>

    <%-- 모달 컨테이너 (문의 모달과 같은 공통 .modal 시스템 재사용) --%>
    <div class="modal" id="adminFaqModal">
        <%-- 모달 박스에는 stopPropagation을 걸지 않는다.
             (걸면 취소/X 버튼 클릭이 #adminBoardArea 위임 핸들러까지 전파되지 못해 안 닫힘)
             배경 클릭 닫기는 adminfaq.js가 e.target === #adminFaqModal 로 판별하므로 문제 없음. --%>
        <div class="modal-box modal-admin-inquiry">
            <button type="button" class="modal-close" data-action="closeModal">✕</button>
            <div id="adminFaqModalBody"></div>
        </div>
    </div>
    </div>
</div>

<script src="${contextPath}/resources/js/admin.js"></script>
<%-- 문의↔FAQ는 탭으로 오가고, 탭 전환은 admin.js가 #adminBoardArea만 innerHTML로
     교체한다(=swap된 HTML 안의 script는 실행 안 됨). 따라서 두 화면 모두에서
     양쪽 스크립트를 미리 로드해둬야, 어느 탭으로 넘어가도 핸들러가 살아있다.
     (이벤트 위임 방식이라 innerHTML 교체돼도 리스너 유지됨) --%>
<script src="${contextPath}/resources/js/adminInquiry.js"></script>
<script src="${contextPath}/resources/js/adminfaq.js"></script>
