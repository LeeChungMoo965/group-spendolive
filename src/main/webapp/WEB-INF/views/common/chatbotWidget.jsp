<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>

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
