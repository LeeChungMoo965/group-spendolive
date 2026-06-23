<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>
        SpendOlive | 로그인
    </title>
    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
</head>
<body class="auth-body">
    <div class="auth-wrap">
        <aside class="auth-brand-panel">
        <a href="${contextPath}/spendolive/main.do" class="auth-logo">
            <img src="${contextPath}/resources/images/logo.png" alt="SpendOlive" style="width:42px;height:42px;border-radius:50%;object-fit:cover;">
            <span>
                SpendOlive
            </span>
        </a>
        <div class="auth-brand-copy">
            <p class="auth-label">
                LOGIN
            </p>
            <h1>
                오늘의 지출을
                <br>
                더 쉽게 관리하세요
            </h1>
            <p class="auth-desc">
                지출관리, 캘린더, OTT관리, 정산 상태를 로그인 후 한 번에 확인할 수 있습니다.
            </p>
        </div>
        <div class="auth-brand-stats">
            <div>
                <strong>
                3개
            </strong>
            <span>
                지출 구분
            </span>
        </div>
        <div>
            <strong>
            월별
        </strong>
        <span>
            캘린더 관리
        </span>
    </div>
    <div>
        <strong>
        OTT
    </strong>
    <span>
        공유 정산
    </span>
</div>
</div>
</aside>
<main class="auth-form-panel">
    <section class="auth-card">
        <div class="auth-card-header">
            <p class="eyebrow">
                WELCOME BACK
            </p>
            <h2>
                로그인
            </h2>
            <p>
                SpendOlive 계정으로 로그인하고 나의 지출 현황을 확인하세요.
            </p>
        </div>
        <div align="center">
        <a href="${kakaoAuthUrl}">
            <img src="${contextPath}/resources/images/kakao_login_medium_wide.png" alt="카카오 로그인 버튼" />
        </a>
        </div>
        <div class="auth-divider">
            또는 일반 로그인
        </div>
        
        <form action="${contextPath}/member/login.do" method="post">
            <div class="auth-form-group">
                <label for="loginId">
                    아이디 또는 이메일
                </label>
                <input id="loginId" type="text" name="id" placeholder="아이디 또는 이메일을 입력하세요">
            </div>
            <div class="auth-form-group">
                <label for="loginPw">
                    비밀번호
                </label>
                <input id="loginPw" type="password" name="password" placeholder="비밀번호를 입력하세요">
            </div>
            <div class="auth-options">
                <label>
                    <input type="checkbox">
                    로그인 상태 유지
                </label>
                <a href="#">
                    아이디/비밀번호 찾기
                </a>
            </div>
            <button class="auth-btn auth-btn-primary" type="submit">
                로그인
            </button>
        </form>
        <div class="auth-link-row">
            <span>
                아직 회원이 아니신가요?
            </span>
            <a href="${contextPath}/member/signup.do">
                회원가입
            </a>
        </div>
    </section>
</main>
</div>
<script src="${contextPath}/resources/js/app.js">
</script>
</body>
</html>
