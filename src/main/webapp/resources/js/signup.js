function showMemberModal(prefix, type, titleText, messageText) {
    const overlay = document.getElementById(prefix + 'StatusOverlay');
    const title = document.getElementById(prefix + 'StatusTitle');
    const message = document.getElementById(prefix + 'StatusMessage');
    const spinner = document.getElementById(prefix + 'StatusSpinner');
    const icon = document.getElementById(prefix + 'StatusIcon');
    const closeBtn = document.getElementById(prefix + 'StatusCloseButton');
    const actions = document.getElementById(prefix + 'StatusActions');

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

    // 버튼 영역 제어
    if (actions) actions.hidden = (type === 'processing');
    overlay.dataset.state = type;
    overlay.style.display = 'flex';

    if (closeBtn) {
        closeBtn.onclick = function () {
            overlay.style.display = 'none';
        };
    }
}
(function () {
    const userIdInput = document.getElementById('userId');
    const checkIdButton = document.getElementById('checkIdButton');

    if (!checkIdButton || !userIdInput) return;



    // 2. 아이디 중복확인 버튼 클릭 이벤트
    checkIdButton.addEventListener('click', async function () {
        const userId = userIdInput.value.trim();

        if (!userId) {
            showMemberModal('id','error', '입력 오류', '아이디를 입력해 주세요.');
            userIdInput.focus();
            return;
        }

        try {
            const response = await fetch('/member/checkId.do', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'Accept': 'application/json'
                },
                body: new URLSearchParams({ id: userId })
            });

            const result = await response.json();

            // 백엔드가 보내주는 code 값으로 확인 (CHECK_COMPLETED = 사용 가능)
            if (result.code === 'CHECK_COMPLETED') {
                showMemberModal('id','success', '중복확인 성공', result.message || '사용 가능한 아이디입니다.');
            } else {
                showMemberModal('id','error', '중복확인 실패', result.message || '이미 사용 중인 아이디입니다.');
            }

        } catch (error) {
            showMemberModal('id','error', '시스템 오류', '중복확인 중 오류가 발생했습니다.');
        }
    });
})();
(function () {
    const emailInput = document.getElementById('email');
    const emailButton = document.getElementById('emailButton');
    if (!emailButton || !emailInput) return;

   
    // 2. 아이디 중복확인 버튼 클릭 이벤트
    emailButton.addEventListener('click', async function (e) {
        e.preventDefault(); // 🛑 브라우저의 기본 동작(새로고침 등)을 막아줍니다!
        console.log("👆 이메일 버튼이 클릭되었습니다!");
    
        const email = emailInput.value.trim();
        console.log("입력된 이메일:", email);
        if (!email) {
            showMemberModal('email','error', '입력 오류', 'email을 입력해 주세요.');
            emailInput.focus();
            return;
        }

        try {
            showMemberModal('email','processing', '인증번호 발송 중입니다.', '잠시만 기다려주세요.');
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
            if (result.code === 'SEND_COMPLETED') {
                showMemberModal('email','success', '인증번호 전송!', result.message || '인증번호 전송 완료되었습니다.');
                document.getElementById('emailAuthArea').style.display = 'block';
            } else {
                showMemberModal('email','error', '인증번호 전송 실패', result.message || '인증번호 발생 중 오류가 발생하였습니다. 잠시 후 다시 시도해주세요.');
            }

        } catch (error) {
            console.error("🚨 에러 원인:", error); // 👈 이 코드를 추가해 보세요!
    showMemberModal('email','error', '시스템 오류', '중복확인 중 오류가 발생했습니다.');
        }
    });
})();
(function () {
    const phoneInput = document.getElementById('phone');
    const phoneButton = document.getElementById('phoneButton');
    if (!phoneButton || !phoneInput) return;

   
    // 2. 아이디 중복확인 버튼 클릭 이벤트
    phoneButton.addEventListener('click', async function (e) {
    
        const phone = phoneInput.value.trim();
        if (!phone) {
            showMemberModal('phone','error', '입력 오류', 'phone을 입력해 주세요.');
            phoneInput.focus();
            return;
        }

        try {
            showMemberModal('phone','processing', '인증번호 발송 중입니다.', '잠시만 기다려주세요.');
            const response = await fetch('/member/sendSms.do', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'Accept': 'application/json'
                },
                body: new URLSearchParams({ phone: phone })
            });

            const result = await response.json();

            // 백엔드가 보내주는 code 값으로 확인 (CHECK_COMPLETED = 사용 가능)
            if (result.code === 'SEND_COMPLETED') {
                showMemberModal('phone','success', '인증번호 전송!', result.message || '인증번호 전송 완료되었습니다.');
                document.getElementById('phoneAuthArea').style.display = 'block';
            } else {
                showMemberModal('phone','error', '인증번호 전송 실패', result.message || '인증번호 발생 중 오류가 발생하였습니다. 잠시 후 다시 시도해주세요.');
            }

        } catch (error) {
            console.error("🚨 에러 원인:", error); // 👈 이 코드를 추가해 보세요!
    showMemberModal('phone','error', '시스템 오류', '중복확인 중 오류가 발생했습니다.');
        }
    });
})();