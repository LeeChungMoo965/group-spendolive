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
    

</head>

<body>
    <div id="modern-wrapper">
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





    <button id="chatbotToggle" class="chatbot-toggle">💬</button>

<div id="chatbotPanel" class="chatbot-panel">
  <div class="chatbot-header">
    <div>
      <strong>SpendOlive 챗봇</strong>
      <span>FAQ 기반으로 답변해드려요</span>
    </div>
    <button id="chatbotClose" class="chatbot-close">×</button>
  </div>
  <div id="chatbotBody" class="chatbot-body">
    <div class="chatbot-msg bot">안녕하세요! 궁금한 점을 물어보세요 🙂</div>
  </div>
  <div class="chatbot-footer">
    <input id="chatbotInput" type="text" placeholder="질문을 입력하세요">
    <button id="chatbotSend">전송</button>
  </div>
</div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/chatbot.css">
<script src="${pageContext.request.contextPath}/resources/js/chatbot.js"></script>
</body>
</html>
