<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="requestURI" value="${pageContext.request.requestURI}" />
<header class="site-header">
    <div class="container header-inner">
        <a href="${contextPath}/spendolive/main.do" class="logo">
            <img src="${contextPath}/resources/images/logo.png" alt="SpendOlive" style="width:42px;height:42px;border-radius:50%;object-fit:cover;">
            <span>
                SpendOlive
            </span>
        </a>
        <nav class="nav">
            <a href="#" class="notification-link" aria-label="알림" title="알림" onclick="alert('알림 기능은 준비 중입니다.'); return false;">
                🔔
            </a>
            <a href="${contextPath}/spendolive/expense.do" class="${fn:contains(requestURI, '/expense') ? 'active' : ''}">
                지출관리
            </a>
            <a href="${contextPath}/spendolive/calendar.do" class="${fn:contains(requestURI, '/calendar') ? 'active' : ''}">
                캘린더
            </a>
            <a href="${contextPath}/spendolive/ott.do" class="${fn:contains(requestURI, '/ott') ? 'active' : ''}">
                OTT관리
            </a>
            <a href="${contextPath}/spendolive/mypage.do" class="${fn:contains(requestURI, '/mypage') ? 'active' : ''}">
                마이페이지
            </a>
        </nav>
        <div class="header-actions">
        <c:choose>
            <c:when test="${isLogOn == true && not empty memberInfo}">
            <a class="btn btn-light" href="${contextPath}/member/logout.do">
                로그아웃
            </a>
           
                <strong>${memberInfo.member_name}님</strong>
            </c:when>
            <c:otherwise>


            <a class="btn btn-light" href="${contextPath}/member/loginForm.do">
                로그인
            </a>
            <a class="btn btn-primary" href="${contextPath}/member/signup.do">
                회원가입
            </a>
            </c:otherwise>
        </c:choose>
        </div>
    </div>
</header>
