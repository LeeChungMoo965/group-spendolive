<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%--
    OTT 채팅방 화면
    AJAX 폴링으로 메시지 조회 및 전송
--%>

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

<%-- 채팅방 기본정보 --%>
<section class="page-hero ott-sub-hero chat-page-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">SHARE CHAT</p>
        <h1>${chatRoom.room_name}</h1>
        <p class="hero-text">
            ${chatRoom.service_name} 공유방 대화입니다. 결제 완료 후 참여 중인 멤버와 방장만 대화방에 들어올 수 있습니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott/friends.do" class="btn btn-outline">내 공유방</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=manage" class="btn btn-outline">참여방 관리</a>
        </div>
    </div>
</section>

<%-- 메시지 목록 및 전송 --%>
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
                    <b>${chatRoom.current_member_count}/${chatRoom.member_limit}명</b>
                    <small>결제일 매월 ${chatRoom.billing_day}일</small>
                </div>
            </div>

            <%-- 메시지 유형별 화면 분기 --%>
            <div id="chatMessageList" class="chat-message-list-page">
                <c:choose>
                    <c:when test="${not empty chatMessageList}">
                        <c:forEach var="message" items="${chatMessageList}">
                            <c:choose>
                                <c:when test="${message.system_yn eq 'Y'}">
                                    <div class="chat-message-row system">
                                        <div class="chat-system-bubble">

                                            <strong>${message.sender_id}</strong>
                                            <p>${message.message_content}</p>

                                            <small>${message.created_at}</small>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="chat-message-row ${message.mine_yn eq 'Y' ? 'mine' : 'other'}">
                                        <div class="chat-message-bubble">

                                            <strong>${message.sender_name}</strong>
                                            <p>${message.message_content}</p>

                                            <small>${message.created_at}</small>
                                            
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

            <%-- 메시지 전송 폼 --%>
            <form id="chatSendForm" action="${contextPath}/spendolive/ott/chat/send.do" method="post" class="chat-send-form">
                <input type="hidden" name="room_id" value="${chatRoom.room_id}">
                <input type="text" name="message_content" id="chatMessageInput" placeholder="메시지를 입력하세요" autocomplete="off" required>
                <button type="submit" class="btn btn-primary">전송</button>
            </form>
        </article>
    </div>
</section>

<script>
// 채팅 AJAX 처리 - 1.5초마다 메시지 조회 후 화면 갱신
(function () {
    const room_id = '${chatRoom.room_id}';
    const contextPath = '${contextPath}';
    const list = document.getElementById('chatMessageList');
    const form = document.getElementById('chatSendForm');
    const input = document.getElementById('chatMessageInput');

    // 메시지를 textContent로 화면에 출력
    function makeMessageRow(message) {
        const isSystem = message.system_yn === 'Y';
        const row = document.createElement('div');
        row.className = isSystem
            ? 'chat-message-row system'
            : 'chat-message-row ' + (message.mine_yn === 'Y' ? 'mine' : 'other');

        const bubble = document.createElement('div');
        bubble.className = isSystem ? 'chat-system-bubble' : 'chat-message-bubble';

        const sender = document.createElement('strong');
        sender.textContent = message.sender_name || message.sender_id || '알 수 없음';

        const content = document.createElement('p');
        content.textContent = message.message_content || '';

        const time = document.createElement('small');
        time.textContent = message.created_at || '';

        if (!isSystem && message.mine_yn !== 'Y') {

                const reportLink = document.createElement('a');
                reportLink.href = '/report/report.do?reported_member_id='+message.sender_id+'?room_id='+room_id+'?chat_text='+message.message_content;
                reportLink.textContent = ' 신고하기';
                reportLink.className = 'danger-outline';
                // 필요한 경우 여기에 신고하기 클릭 이벤트 리스너를 달 수 있습니다.
                reportLink.addEventListener('click', function(e) {
                    e.preventDefault();
                    // 예: reportMessage(message.message_id); 
                    if (confirm('신고 하시겠습니까?')){
                        location.href ='/report/report.do?reported_member_id='+message.sender_id+'&room_id='+room_id+'&chat_text='+encodeURIComponent(message.message_content);
                    }
                });

                time.appendChild(reportLink);
            }
        bubble.appendChild(sender);
        bubble.appendChild(content);
        bubble.appendChild(time);
        row.appendChild(bubble);
        return row;
    }

    // 최신 메시지 위치로 이동
    function scrollToBottom() {
        list.scrollTop = list.scrollHeight;
    }

    // AJAX로 채팅 목록 갱신
    function loadMessages() {
        fetch(contextPath + '/spendolive/ott/chat/messages.do?room_id=' + encodeURIComponent(room_id), {
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

    // AJAX로 메시지 전송
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

    // 1.5초마다 새 메시지 조회
    scrollToBottom();
    setInterval(loadMessages, 1500);
})();
</script>
