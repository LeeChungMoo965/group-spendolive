<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<style>
    .chat-message-row.system {
        justify-content: center;
    }

    .chat-system-bubble {
        max-width: min(720px, 90%);
        padding: 11px 16px;
        border-radius: 999px;
        border: 1px dashed #c8d6a1;
        background: #f8f6dd;
        color: #5c5f20;
        text-align: center;
        box-shadow: 0 8px 18px rgba(70, 78, 38, 0.06);
    }

    .chat-system-bubble strong {
        display: block;
        margin-bottom: 4px;
        font-size: 12px;
        letter-spacing: 0.08em;
        text-transform: uppercase;
    }

    .chat-system-bubble p {
        margin: 0;
        line-height: 1.5;
        word-break: keep-all;
    }

    .chat-system-bubble small {
        display: block;
        margin-top: 5px;
        font-size: 11px;
        color: #85884a;
    }
</style>

<section class="page-hero ott-sub-hero chat-page-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">SHARE CHAT</p>
        <h1>${chatRoom.roomName}</h1>
        <p class="hero-text">
            ${chatRoom.serviceName} 공유방 대화입니다. 결제 완료 후 참여 중인 멤버와 방장만 대화방에 들어올 수 있습니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott/friends.do" class="btn btn-outline">내 공유방</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=manage" class="btn btn-outline">참여방 관리</a>
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
                            <c:choose>
                                <c:when test="${message.systemYn eq 'Y'}">
                                    <div class="chat-message-row system">
                                        <div class="chat-system-bubble">
                                            <strong>${message.senderName}</strong>
                                            <p>${message.messageContent}</p>
                                            <small>${message.createdAt}</small>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="chat-message-row ${message.mineYn eq 'Y' ? 'mine' : 'other'}">
                                        <div class="chat-message-bubble">
                                            <strong>${message.senderName}</strong>
                                            <p>${message.messageContent}</p>
                                            <small>${message.createdAt}</small>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
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
        const isSystem = message.systemYn === 'Y';
        const row = document.createElement('div');
        row.className = isSystem
            ? 'chat-message-row system'
            : 'chat-message-row ' + (message.mineYn === 'Y' ? 'mine' : 'other');

        const bubble = document.createElement('div');
        bubble.className = isSystem ? 'chat-system-bubble' : 'chat-message-bubble';

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
