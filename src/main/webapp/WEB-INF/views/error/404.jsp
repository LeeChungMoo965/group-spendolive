<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<link rel="stylesheet" href="${contextPath}/resources/css/error.css">
<link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
<head>
<meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 - 페이지를 찾을 수 없음</title>
</head>

<header class="site-header" style="top : 1rem; left : 1rem">
            <img src="${contextPath}/resources/images/logo.png" alt="SpendOlive" style="width:42px;height:42px;border-radius:50%;object-fit:cover;background:radial-gradient(circle at 18% 12%,rgba(122,127,54,.17),transparent 34%),radial-gradient(circle at 82% 20%,rgba(196,138,44,.12),transparent 38%),#f7f7ef">
            
</header>
<body class="auth-body">

    <div class="error-container">
        <div class="error-code">404</div>
        <h1 class="error-title">페이지를 찾을 수 없습니다</h1>
        <p class="error-message">
            존재하지 않는 주소이거나, 페이지가 변경/삭제되었을 수 있습니다.<br>
            입력하신 주소가 정확한지 다시 한번 확인해 주세요.
        </p>
        <div class="btn-group">
            <a href="javascript:history.back();" class="btn btn-secondary">이전 페이지</a>
            <a href="${contextPath}/spendolive/main.do" class="btn btn-primary">메인으로</a>
        </div>
    </div>
</body>
</html>