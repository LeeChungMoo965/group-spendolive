<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="requestURI" value="${pageContext.request.requestURI}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SpendOlive | 지출관리 플랫폼</title>
    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
    <link href="https://fonts.googleapis.com/css2?family=Jua&display=swap" rel="stylesheet">

</head>

<body>
    <div id="modern-wrapper" >
        <header id="modern-header">
            <jsp:include page="/WEB-INF/views/common/header.jsp" />
        </header>

        <div id="modern-container">
            <main id="modern-content">
                <jsp:include page="${body_page}" />
            </main>
        </div>

        <jsp:include page="/WEB-INF/views/common/chatWidget.jsp" />

        <footer id="modern-footer">
            <jsp:include page="/WEB-INF/views/common/footer.jsp" />
        </footer>
    </div>


<jsp:include page="/WEB-INF/views/common/chatbotWidget.jsp" />
</body>
</html>
