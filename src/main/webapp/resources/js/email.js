(function () {
    const emailInput = document.getElementById('email');
    const emailButton = document.getElementById('emailButton');

    if (!emailButton || !emailInput) return;

    // 1. 회원가입 전용 모달 팝업 출력 함수
    function showMemberModal(type, titleText, messageText) {
        const overlay = document.getElementById('emailStatusOverlay');
        const title = document.getElementById('emailStatusTitle');
        const message = document.getElementById('emailStatusMessage');
        const spinner = document.getElementById('emailStatusSpinner');
        const icon = document.getElementById('emailStatusIcon');
        const closeBtn = document.getElementById('emailStatusCloseButton');
        const actions = document.getElementById('emailStatusActions');

        if (!overlay) return;

        overlay.hidden = false;
        if (title) title.textContent = titleText;
        if (message) message.textContent = messageText;

        // 아이콘 / 스피너 제어
        if (spinner) spinner.hidden = (type !== 'processing');
        if (icon) {
            icon.hidden = (type === 'processing');
            icon.textContent = (type === 'success') ? '✓' : '!';
        }

        // 버튼 영역 표시
        if (actions) actions.hidden = (type === 'processing');
        overlay.dataset.state = type;
        overlay.style.display = 'flex';

        // 2. 닫기 버튼을 눌렀을 때
        if (closeBtn) {
            closeBtn.onclick = function () {
                overlay.style.display = 'none';
            };
        }
    }

    // 2. 아이디 중복확인 버튼 클릭 이벤트
    checkIdButton.addEventListener('click', async function () {
        const userId = userIdInput.value.trim();

        if (!userId) {
            showMemberModal('error', '입력 오류', 'email을 입력해 주세요.');
            userIdInput.focus();
            return;
        }

        try {
            const response = await fetch('/member/sendEmail.do', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'Accept': 'application/json'
                },
                body: new URLSearchParams({ email: email })
            });

            const result = await response.json();

            // 백엔드가 보내주는 code 값으로 확인 (CHECK_COMPLETED = 사용 가능)
            if (result.code === 'CHECK_COMPLETED') {
                showMemberModal('success', '인증번호 전송!', result.message || '인증번호 전송 완료되었습니다.');
            } else {
                showMemberModal('error', '인증번호 전송 실패', result.message || '인증번호 발생 중 오류가 발생하였습니다. 잠시 후 다시 시도해주세요.');
            }

        } catch (error) {
            showMemberModal('error', '시스템 오류', '중복확인 중 오류가 발생했습니다.');
        }
    });
})();