<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>
        SpendOlive | 회원가입
    </title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
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
                SIGN UP
            </p>
            <h1>
                안전하게 인증하고
                <br>
                회원가입하세요
            </h1>
            <p class="auth-desc">
                아이디 중복확인, 이메일 인증, 전화번호 인증을 통해 SpendOlive 계정을 생성할 수 있습니다.
            </p>
        </div>
        <div class="auth-brand-stats">
            <div>
                <strong>
                ID
            </strong>
            <span>
                중복확인
            </span>
        </div>
        <div>
            <strong>
            Email
        </strong>
        <span>
            이메일 인증
        </span>
    </div>
    <div>
        <strong>
        Phone
    </strong>
    <span>
        전화번호 인증
    </span>
</div>
</div>
</aside>
<main class="auth-form-panel">
    <section class="auth-card auth-card-wide">
        <div class="auth-card-header">
            <p class="eyebrow">
                CREATE ACCOUNT
            </p>
            <h2>
                회원가입
            </h2>
            <p>
                아이디 또는 이메일 형식으로 가입할 수 있습니다.
            </p>
        </div>

        <c:choose>
            <c:when test="${login_type == 'KAKAO'}"> 
            <form action="${contextPath}/member/addmember.do" method="post" onsubmit="return joinCheckKakao()">
            <div class="auth-grid-2">
                <div class="auth-form-group">
                    <label for="name">
                        이름
                    </label>
                    <input id="name" name="member_name" type="readonly" value="${member_name}">
                </div>
                <div class="auth-form-group">
                    <label for="nickname">
                        닉네임
                    </label>
                    <input id="nickname"name="nickname" type="text" placeholder="닉네임을 입력하세요">
                </div>
            </div>
               <div class="auth-form-group">
                <label for="email">
                    이메일
                </label>
                <div class="auth-input-row">
                    <input id="email" name="email"type="email" placeholder="example@email.com">
                    <button class="auth-btn auth-btn-light" type="button" onclick="sendEmail()">인증요청</button>
                </div>
            </div>
            <div class="auth-form-group" id="emailAuthArea" style="display:none;">
                <label for="emailAuthCode">인증번호 입력</label>
                <div class="auth-input-row">
                    <input id="emailAuthCode" type="text" placeholder="6자리 인증번호를 입력하세요">
                    <button class="auth-btn auth-btn-light" type="button" onclick="verifyEmail()">인증확인</button>
                </div>
                <p id="emailAuthResult" class="auth-result-text">이메일로 발송된 인증번호를 입력해 주세요.</p>
            </div>
        

            <div class="auth-form-group">
                <label for="phone">전화번호</label>
                <div class="auth-input-row">
                    <input id="phone" name="phone" type="tel" placeholder="010-0000-0000">
                    <button class="auth-btn auth-btn-light" type="button" onclick="sendSms()">인증요청</button>
                </div>
            </div>

            <div class="auth-form-group" id="phoneAuthArea" style="display:none;">
                <label for="phoneAuthCode">전화번호 인증번호 입력</label>
                <div class="auth-input-row">
                    <input id="phoneAuthCode" type="text" placeholder="6자리 인증번호를 입력하세요">
                    <button class="auth-btn auth-btn-light" type="button" onclick="verifySms()">인증확인</button>
                </div>
                <p id="phoneAuthResult" class="auth-result-text">휴대폰으로 발송된 인증번호를 입력해 주세요.</p>
            </div>
            <input id="password" type="hidden" name="password" value="KAKAO">
            <input id="login_type" type="hidden" name="login_type" value="${login_type}">
            </c:when>
        <c:otherwise>
         
        
        <form action="${contextPath}/member/addmember.do" method="post" onsubmit="return joinCheck()">
        <input id="userId" type="hidden" name="login_type" value="LOCAL">
            <div class="auth-grid-2">
                <div class="auth-form-group">
                    <label for="name">
                        이름
                    </label>
                    <input id="name" name="member_name" type="text" placeholder="이름을 입력하세요">
                </div>
                <div class="auth-form-group">
                    <label for="nickname">
                        닉네임
                    </label>
                    <input id="nickname"name="nickname" type="text" placeholder="닉네임을 입력하세요">
                </div>
            </div>
            <div class="auth-form-group">
                <label for="userId">
                    아이디
                </label>
                <div class="auth-input-row">
                    <input id="userId" type="text" name="id"placeholder="사용할 아이디를 입력하세요">
                    <button class="auth-btn auth-btn-light" type="button" onclick="checkId()">
                        중복확인
                    </button>
                </div>
                <p id="idResult" class="auth-result-text">
                    아이디 중복확인을 진행해주세요.
                </p>
            </div>
            <div class="auth-form-group">
                <label for="email">
                    이메일
                </label>
                <div class="auth-input-row">
                    <input id="email" name="email"type="email" placeholder="example@email.com">
                    <button class="auth-btn auth-btn-light" type="button" onclick="sendEmail()">인증요청</button>
                </div>
            </div>
            <div class="auth-form-group" id="emailAuthArea" style="display:none;">
                <label for="emailAuthCode">인증번호 입력</label>
                <div class="auth-input-row">
                    <input id="emailAuthCode" type="text" placeholder="6자리 인증번호를 입력하세요">
                    <button class="auth-btn auth-btn-light" type="button" onclick="verifyEmail()">인증확인</button>
                </div>
                <p id="emailAuthResult" class="auth-result-text">이메일로 발송된 인증번호를 입력해 주세요.</p>
            </div>
        

            <div class="auth-form-group">
                <label for="phone">전화번호</label>
                <div class="auth-input-row">
                    <input id="phone" name="phone" type="tel" placeholder="010-0000-0000">
                    <button class="auth-btn auth-btn-light" type="button" onclick="sendSms()">인증요청</button>
                </div>
            </div>

            <div class="auth-form-group" id="phoneAuthArea" style="display:none;">
                <label for="phoneAuthCode">전화번호 인증번호 입력</label>
                <div class="auth-input-row">
                    <input id="phoneAuthCode" type="text" placeholder="6자리 인증번호를 입력하세요">
                    <button class="auth-btn auth-btn-light" type="button" onclick="verifySms()">인증확인</button>
                </div>
                <p id="phoneAuthResult" class="auth-result-text">휴대폰으로 발송된 인증번호를 입력해 주세요.</p>
            </div>
            <div class="auth-grid-2">
                <div class="auth-form-group">
                    <label for="password">
                        비밀번호
                    </label>
                    <input id="password" type="password"name="password" placeholder="비밀번호를 입력하세요">
                </div>
                <div class="auth-form-group">
                    <label for="passwordCheck">
                        비밀번호 확인
                    </label>
                    <input id="passwordCheck" type="password" placeholder="비밀번호를 다시 입력하세요">
                </div>
            </div>
           </c:otherwise>
        </c:choose>
            <br>
            <button class="auth-btn auth-btn-primary" type="submit">
                회원가입
            </button>
        </form>
        <div class="auth-link-row">
            <span>
                이미 계정이 있으신가요?
            </span>
            <a href="${contextPath}/member/loginForm.do">
                로그인
            </a>
        </div>
    </section>
</main>
</div>
<script src="${contextPath}/resources/js/app.js">
</script>
</body>
</html>
