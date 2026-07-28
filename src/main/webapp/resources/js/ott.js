/* =========================================================
   OTT 채팅방
   - 페이지 data 속성에서 서버값을 읽는다.
   - 1.5초마다 메시지 목록을 갱신한다.
   ========================================================= */
(function initializeOttChatPage() {
    const page = document.getElementById('ottChatPage');

    if (!page) {
        return;
    }

    const msg = page.dataset.message || '';
    const room_id = page.dataset.roomId || '';
    const contextPath = page.dataset.contextPath || '';

    if (msg) {
        alert(msg);
    }
    const list = document.getElementById('chatMessageList');
    const form = document.getElementById('chatSendForm');
    const input = document.getElementById('chatMessageInput');

    if (!room_id || !list || !form || !input) {
        return;
    }

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
                reportLink.href = '/report/report.do?reported_member_id='+message.sender_id+'&room_id='+room_id+'&chat_text='+message.message_content;
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


/* =========================================================
   OTT 가족방 초대 공유
   - URL 복사, QR 표시, 카카오톡 공유를 처리한다.
   ========================================================= */
// 가족방 초대 공유 - URL 복사, QR, 카카오톡 공유 처리
(function () {
    var page = document.getElementById('ottFriendsPage');

    if (!page) {
        return;
    }

    var kakaoJavascriptKey = page.dataset.kakaoKey || '';
    var contextPath = page.dataset.contextPath || '';

    if (window.Kakao && kakaoJavascriptKey && !window.Kakao.isInitialized()) {
        window.Kakao.init(kakaoJavascriptKey);
    }

    // 초대 URL 복사
    function copyText(text) {
        if (navigator.clipboard && window.isSecureContext) {
            return navigator.clipboard.writeText(text);
        }

        var temp = document.createElement('textarea');
        temp.value = text;
        temp.style.position = 'fixed';
        temp.style.left = '-9999px';
        document.body.appendChild(temp);
        temp.focus();
        temp.select();
        document.execCommand('copy');
        document.body.removeChild(temp);
        return Promise.resolve();
    }

    // 카카오 공유 데이터 생성
    function buildSharePayload(room_name, inviteUrl) {
        return {
            objectType: 'feed',
            content: {
                title: room_name,
                description: 'SpendOlive 가족방 초대 링크입니다. 링크를 열면 결제 화면으로 이동합니다.',
                imageUrl: window.location.origin + contextPath + '/resources/images/logo.png',
                link: {
                    mobileWebUrl: inviteUrl,
                    webUrl: inviteUrl
                }
            },
            buttons: [
                {
                    title: '결제하러 가기',
                    link: {
                        mobileWebUrl: inviteUrl,
                        webUrl: inviteUrl
                    }
                }
            ]
        };
    }

    // 카카오 공유 실패 시 URL 복사
    function shareKakao(room_name, inviteUrl) {
        if (!kakaoJavascriptKey) {
            return copyText(inviteUrl).then(function () {
                alert('카카오 JavaScript 키가 아직 설정되지 않아 초대 URL을 대신 복사했습니다.');
            });
        }

        if (!window.Kakao || !window.Kakao.isInitialized()) {
            return copyText(inviteUrl).then(function () {
                alert('카카오 SDK가 연결되지 않아 초대 URL을 대신 복사했습니다.');
            });
        }

        try {
            var payload = buildSharePayload(room_name, inviteUrl);

            if (window.Kakao.Share && window.Kakao.Share.sendDefault) {
                window.Kakao.Share.sendDefault(payload);
                return Promise.resolve();
            }

            if (window.Kakao.Link && window.Kakao.Link.sendDefault) {
                window.Kakao.Link.sendDefault(payload);
                return Promise.resolve();
            }
        } catch (e) {
            console.error(e);
        }

        return copyText(inviteUrl).then(function () {
            alert('카카오톡 공유를 실행하지 못해 초대 URL을 대신 복사했습니다.');
        });
    }

    // 초대 공유 버튼 이벤트 연결
    document.querySelectorAll('.invite-share-box').forEach(function (box) {
        var input = box.querySelector('.invite-url-input');
        var copyBtn = box.querySelector('.invite-copy-btn');
        var qrBtn = box.querySelector('.invite-qr-btn');
        var kakaoBtn = box.querySelector('.invite-kakao-btn');
        var qrBox = box.querySelector('.invite-qr-box');
        var qrImg = qrBox ? qrBox.querySelector('img') : null;
        var room_name = box.dataset.roomName || 'SpendOlive 가족방';

        if (copyBtn && input) {
            copyBtn.addEventListener('click', function () {
                copyText(input.value).then(function () {
                    alert('초대 URL을 복사했습니다.');
                });
            });
        }

        if (qrBtn && input && qrBox && qrImg) {
            qrBtn.addEventListener('click', function () {
            if (!qrImg.getAttribute('src')) {
                qrImg.src =
                    'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data='
                    + encodeURIComponent(input.value);
            }
                qrBox.classList.toggle('show');
            });
        }

        if (kakaoBtn && input) {
            kakaoBtn.addEventListener('click', function () {
                shareKakao(room_name, input.value);
            });
        }
    });
})();
