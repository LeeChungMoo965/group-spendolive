<%-- [공통 AJAX 로딩 규격화]
     모든 사용자·관리자 AJAX가 공유하는 로딩 팝업을 한 번만 렌더링한다.
     각 화면은 별도 팝업 HTML을 만들지 않고 ajaxloading.js의 공통 요청 함수만 호출한다. --%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<div id="globalLoadingOverlay" class="global-loading-overlay" role="status" aria-live="polite" aria-busy="false" hidden>
    <div class="global-loading-box">
        <div class="global-loading-spinner" aria-hidden="true"></div>
        <strong id="globalLoadingMessage" class="global-loading-message">처리 중입니다.</strong>
        <p id="globalLoadingDescription" class="global-loading-description">잠시만 기다려주세요.</p>
    </div>
</div>
