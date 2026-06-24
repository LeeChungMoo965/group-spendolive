<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero ott-sub-hero chat-page-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">SHARE CHAT</p>
        <h1>${chatRoom.roomName}</h1>
        <p class="hero-text">
            ${chatRoom.serviceName} 공유방 대화입니다. 파티장이 수락한 참여자와 방장만 대화방에 들어올 수 있습니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott/friends.do" class="btn btn-outline">내 공유방</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=apply" class="btn btn-outline">신청관리</a>
        </div>
    </div>
</section>

<section class="section compact ott-page-section">
    <div class="container ott-wide-container">
        <article class="card chat-room-page-card">
            <div class="chat-room-page-head">
                <div>
                    <p class="eyebrow">LIVE ROOM</p>
                    <h2>공유방 대화</h2>
                    <span>3초마다 새 메시지를 확인합니다.</span>
                </div>
                <div class="chat-room-page-meta">
                    <b>${chatRoom.currentMemberCount}/${chatRoom.memberLimit}명</b>
                    <small>결제일 매월 ${chatRoom.billingDay}일</small>
                </div>
            </div>

            <div id="chatMessageList" class="chat-message-list-page">
                <c:choose>
                    <c:when test="${not empty chatMessageList}">
                        <c:forEach var="message" items="${chatMessageList}">
                            <div class="chat-message-row ${message.mineYn eq 'Y' ? 'mine' : 'other'}">
                                <div class="chat-message-bubble">
                                    <strong>${message.senderName}</strong>
                                    <p>${message.messageContent}</p>
                                    <small>${message.createdAt}</small>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-box chat-empty-box">아직 대화가 없습니다. 첫 메시지를 보내보세요.</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <form id="chatSendForm" action="${contextPath}/spendolive/ott/chat/send.do" method="post" class="chat-send-form">
                <input type="hidden" name="roomId" value="${chatRoom.roomId}">
                <input type="text" name="messageContent" id="chatMessageInput" placeholder="메시지를 입력하세요" autocomplete="off" required>
                <button type="submit" class="btn btn-primary">전송</button>
            </form>
        </article>
    </div>
</section>

<script>
(function () {
    const roomId = '${chatRoom.roomId}';
    const contextPath = '${contextPath}';
    const list = document.getElementById('chatMessageList');
    const form = document.getElementById('chatSendForm');
    const input = document.getElementById('chatMessageInput');

    function makeMessageRow(message) {
        const row = document.createElement('div');
        row.className = 'chat-message-row ' + (message.mineYn === 'Y' ? 'mine' : 'other');

        const bubble = document.createElement('div');
        bubble.className = 'chat-message-bubble';

        const sender = document.createElement('strong');
        sender.textContent = message.senderName || message.senderId || '알 수 없음';

        const content = document.createElement('p');
        content.textContent = message.messageContent || '';

        const time = document.createElement('small');
        time.textContent = message.createdAt || '';

        bubble.appendChild(sender);
        bubble.appendChild(content);
        bubble.appendChild(time);
        row.appendChild(bubble);
        return row;
    }

    function scrollToBottom() {
        list.scrollTop = list.scrollHeight;
    }

    function loadMessages() {
        fetch(contextPath + '/spendolive/ott/chat/messages.do?roomId=' + encodeURIComponent(roomId), {
            headers: { 'Accept': 'application/json' }
        })
            .then(function (response) { return response.json(); })
            .then(function (messages) {
                list.innerHTML = '';

                if (!messages || messages.length === 0) {
                    const empty = document.createElement('div');
                    empty.className = 'empty-box chat-empty-box';
                    empty.textContent = '아직 대화가 없습니다. 첫 메시지를 보내보세요.';
                    list.appendChild(empty);
                    return;
                }

                messages.forEach(function (message) {
                    list.appendChild(makeMessageRow(message));
                });
                scrollToBottom();
            })
            .catch(function () {
                // 네트워크 문제가 있어도 화면은 유지한다.
            });
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();

        if (!input.value.trim()) {
            return;
        }

        fetch(form.action, {
            method: 'POST',
            body: new FormData(form)
        })
            .then(function () {
                input.value = '';
                loadMessages();
                input.focus();
            });
    });

    scrollToBottom();
    setInterval(loadMessages, 3000);
})();
</script>
