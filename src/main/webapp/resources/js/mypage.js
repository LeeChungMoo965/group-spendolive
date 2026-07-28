/* JSP에서 전달한 컨텍스트 경로를 페이지 data 속성에서 읽는다. */
function getMyPageContextPath() {
    const page = document.querySelector('.mypage-page');
    return page ? (page.dataset.contextPath || '') : '';
}

/* =========================================================
   [마이페이지 계좌·카드 연결 JavaScript 추가 시작]
   화면 전환, 계좌 제목 수정, 계좌·카드 4개 단위 페이지 처리를 담당한다.
   ========================================================= */
// 상단 버튼을 누른 경우에만 회원정보·신고내역·계좌카드 영역을 표시한다.
function showMyPagePanel(panelId) {
    document.querySelectorAll('.mypage-toggle-panel').forEach(function (panel) {
        panel.classList.add('is-hidden');
    });

    const target = document.getElementById(panelId);
    if (!target) {
        return;
    }

    target.classList.remove('is-hidden');
    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    window.history.replaceState(null, '', '#' + panelId);
}

// 계좌 제목은 처음에는 읽기 전용으로 표시하고, 수정 버튼을 누르면 입력과 저장이 가능해진다.
function toggleAccountNameEdit(button) {
    const form = button.closest('.mypage-asset-title-form');
    const input = form ? form.querySelector('input[name="accountName"]') : null;
    if (!form || !input) {
        return;
    }

    if (input.readOnly) {
        input.readOnly = false;
        input.focus();
        input.select();
        button.textContent = '저장';
        return;
    }

    if (!input.value.trim()) {
        alert('계좌 제목을 입력해주세요.');
        input.focus();
        return;
    }

    form.submit();
}

// 계좌와 카드 목록은 4개 단위로 보이며, 부족한 칸은 빈 정보 카드로 채운다.
function initializeAssetPager(listId) {
    const list = document.getElementById(listId);
    const pager = document.querySelector('[data-pager-for="' + listId + '"]');
    if (!list || !pager) {
        return;
    }

    let items = Array.from(list.querySelectorAll('[data-asset-item]'));
    const emptyText = list.dataset.emptyText || '정보가 없습니다.';
    const requiredSlots = Math.max(4, Math.ceil(items.length / 4) * 4);

    while (items.length < requiredSlots) {
        const emptyItem = document.createElement('div');
        emptyItem.className = 'mypage-asset-item mypage-asset-empty';
        emptyItem.setAttribute('data-asset-item', '');
        emptyItem.textContent = emptyText;
        list.appendChild(emptyItem);
        items.push(emptyItem);
    }

    const totalPages = Math.max(1, Math.ceil(items.length / 4));
    let currentPage = 1;
    const currentPageText = pager.querySelector('[data-current-page]');
    const totalPageText = pager.querySelector('[data-total-page]');
    const prevButton = pager.querySelector('[data-page-direction="prev"]');
    const nextButton = pager.querySelector('[data-page-direction="next"]');

    function renderPage() {
        items.forEach(function (item, index) {
            const itemPage = Math.floor(index / 4) + 1;
            item.classList.toggle('is-hidden', itemPage !== currentPage);
        });

        currentPageText.textContent = currentPage;
        totalPageText.textContent = totalPages;
        prevButton.disabled = currentPage === 1;
        nextButton.disabled = currentPage === totalPages;
    }

    prevButton.addEventListener('click', function () {
        if (currentPage > 1) {
            currentPage -= 1;
            renderPage();
        }
    });

    nextButton.addEventListener('click', function () {
        if (currentPage < totalPages) {
            currentPage += 1;
            renderPage();
        }
    });

    renderPage();
}

document.addEventListener('DOMContentLoaded', function () {
    initializeAssetPager('accountAssetList');
    initializeAssetPager('cardAssetList');

    // 거래내역 버튼마다 선택한 계좌 번호를 Ajax 조회 함수로 전달한다.
    document.querySelectorAll('.transaction-history-btn').forEach(function (button) {
        button.addEventListener('click', function () {
            loadAccountTransactions(button);
        });
    });

    const targetId = window.location.hash.replace('#', '');
    if (['profile-edit', 'report-manage', 'asset-manage'].includes(targetId)) {
        showMyPagePanel(targetId);
    }
});

/* 선택한 계좌의 거래내역을 Ajax로 조회한다. */
function loadAccountTransactions(button) {
    const accountIdx = button.dataset.accountIdx;
    const panel = document.getElementById('accountTransactionPanel');
    const tbody = document.getElementById('accountTransactionBody');
    const currentBalance = Number(button.dataset.currentBalance || 0);

    document.getElementById('accountTransactionTitle').textContent =
        (button.dataset.accountName || '계좌') + ' 거래내역';
    document.getElementById('accountTransactionAccountInfo').textContent =
        (button.dataset.bankName || '') + ' 계좌번호 - ' + (button.dataset.accountNumber || '-');
    document.getElementById('accountTransactionCurrentBalance').textContent =
        formatWon(currentBalance);
    document.getElementById('accountTransactionInitialBalance').textContent = '-';

    tbody.innerHTML = '<tr><td colspan="4">거래내역을 불러오는 중입니다.</td></tr>';
    // mypage-form-section의 display:grid가 hidden 표시를 덮어쓰지 않도록 숨김 클래스도 함께 제거한다.
    panel.classList.remove('is-hidden');
    panel.hidden = false;

    fetch(
        getMyPageContextPath() + '/spendolive/mypage/account/transactions.do?accountIdx='
        + encodeURIComponent(accountIdx),
        {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        }
    )
        .then(function (response) {
            if (response.status === 401) {
                throw new Error('로그인이 필요합니다.');
            }
            if (!response.ok) {
                throw new Error('거래내역 조회에 실패했습니다.');
            }
            return response.json();
        })
        .then(function (transactionList) {
            renderAccountTransactions(transactionList, currentBalance);
            panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        })
        .catch(function (error) {
            tbody.innerHTML = '';
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = 4;
            cell.textContent = error.message;
            row.appendChild(cell);
            tbody.appendChild(row);
        });
}

/* 최신 거래가 위로 오도록 전달된 거래내역과 거래 직후 잔액을 표에 출력한다. */
function renderAccountTransactions(transactionList, currentBalance) {
    const tbody = document.getElementById('accountTransactionBody');
    const initialBalanceTarget = document.getElementById('accountTransactionInitialBalance');
    const transactions = Array.isArray(transactionList) ? transactionList : [];

    tbody.innerHTML = '';

    if (transactions.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 4;
        cell.textContent = '등록된 거래내역이 없습니다.';
        row.appendChild(cell);
        tbody.appendChild(row);
        initialBalanceTarget.textContent = formatWon(currentBalance);
        return;
    }

    transactions.forEach(function (transaction) {
        const row = document.createElement('tr');
        const signedAmount = getSignedTransactionAmount(transaction);

        appendTransactionCell(row, formatTransactionDate(transaction.tran_date));
        appendTransactionCell(row, transaction.inout_type || '-');
        appendTransactionCell(row, formatSignedWon(signedAmount));

        if (transaction.balance_after === null || transaction.balance_after === undefined) {
            appendTransactionCell(row, '기록 없음');
        } else {
            appendTransactionCell(row, formatWon(Number(transaction.balance_after)));
        }

        tbody.appendChild(row);
    });

    // 가장 오래된 거래의 직후 잔액에서 그 거래 금액을 되돌려 계좌 등록 당시 잔액을 계산한다.
    const oldestTransaction = transactions[transactions.length - 1];
    if (oldestTransaction.balance_after === null || oldestTransaction.balance_after === undefined) {
        initialBalanceTarget.textContent = '기록 없음';
    } else {
        const initialBalance =
            Number(oldestTransaction.balance_after) - getSignedTransactionAmount(oldestTransaction);
        initialBalanceTarget.textContent = formatWon(initialBalance);
    }
}

/* 사용자 입력값을 HTML 문자열로 합치지 않고 textContent로 안전하게 출력한다. */
function appendTransactionCell(row, value) {
    const cell = document.createElement('td');
    cell.textContent = value;
    row.appendChild(cell);
}

/* 출금은 음수, 입금은 양수로 맞춰 화면 표시와 최초 잔액 계산에 사용한다. */
function getSignedTransactionAmount(transaction) {
    const amount = Math.abs(Number(transaction.tran_amt || 0));
    return transaction.inout_type === '출금' ? amount * -1 : amount;
}

function formatWon(value) {
    return Number(value || 0).toLocaleString('ko-KR') + '원';
}

function formatSignedWon(value) {
    const amount = Number(value || 0);
    const sign = amount > 0 ? '+' : amount < 0 ? '-' : '';
    return sign + Math.abs(amount).toLocaleString('ko-KR') + '원';
}

/* DB의 yyyyMMddHHmmss 거래일시를 화면용 형식으로 변환한다. */
function formatTransactionDate(value) {
    const date = String(value || '');
    if (date.length !== 14) {
        return date || '-';
    }

    return date.substring(0, 4) + '-'
        + date.substring(4, 6) + '-'
        + date.substring(6, 8) + ' '
        + date.substring(8, 10) + ':'
        + date.substring(10, 12) + ':'
        + date.substring(12, 14);
}

function closeAccountTransactions() {
    const panel = document.getElementById('accountTransactionPanel');
    if (!panel) {
        return;
    }

    // mypage-form-section의 display:grid보다 우선하는 공통 숨김 클래스로 거래내역 영역을 닫는다.
    panel.classList.add('is-hidden');
    panel.hidden = true;
}
/* [마이페이지 계좌·카드 연결 JavaScript 추가 끝] */

(function () {
    const emailInput = document.getElementById('mypageEmail');
    const phoneInput = document.getElementById('mypagePhone');
    const passwordFields = ['currentPassword', 'newPassword', 'passwordConfirm'];

    if (emailInput) {
        emailInput.addEventListener('input', function () {
            document.getElementById('emailVerified').value = 'N';
            setMessage('emailVerifyMessage', '이메일을 변경했다면 인증을 다시 진행해주세요.', 'warn');
        });
    }

    if (phoneInput) {
        phoneInput.addEventListener('input', function () {
            document.getElementById('phoneVerified').value = 'N';
            setMessage('phoneVerifyMessage', '전화번호를 변경했다면 인증을 다시 진행해주세요.', 'warn');
        });
    }

    passwordFields.forEach(function (fieldId) {
        const field = document.getElementById(fieldId);
        if (field) {
            field.addEventListener('input', function () {
                document.getElementById('passwordChecked').value = 'N';
                setMessage('passwordCheckMessage', '비밀번호 변경 전 확인 버튼을 눌러주세요.', 'warn');
            });
        }
    });

    const form = document.getElementById('mypageProfileForm');
    if (form) {
        form.addEventListener('submit', function (event) {
            const emailChanged = document.getElementById('mypageEmail').value.trim() !== document.getElementById('originalEmail').value.trim();
            const phoneChanged = document.getElementById('mypagePhone').value.trim() !== document.getElementById('originalPhone').value.trim();
            const newPassword = document.getElementById('newPassword').value.trim();

            if (emailChanged && document.getElementById('emailVerified').value !== 'Y') {
                alert('이메일을 변경하려면 이메일 인증을 완료해주세요.');
                event.preventDefault();
                return;
            }

            if (phoneChanged && document.getElementById('phoneVerified').value !== 'Y') {
                alert('전화번호를 변경하려면 전화번호 인증을 완료해주세요.');
                event.preventDefault();
                return;
            }

            if (newPassword && document.getElementById('passwordChecked').value !== 'Y') {
                alert('비밀번호 변경 전 비밀번호 확인 버튼을 눌러주세요.');
                event.preventDefault();
            }
        });
    }
})();

function postForm(url, data) {
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        body: new URLSearchParams(data)
    });
}

function setMessage(id, message, type) {
    const target = document.getElementById(id);
    if (!target) {
        return;
    }
    target.textContent = message;
    target.classList.remove('success', 'warn');
    if (type) {
        target.classList.add(type);
    }
}

function sendMyPageEmailCode() {
    const email = document.getElementById('mypageEmail').value.trim();
    if (!email) {
        alert('이메일을 입력해주세요.');
        return;
    }

    postForm(getMyPageContextPath() + '/spendolive/mypage/email/send.do', { email: email })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'SUCCESS') {
                document.getElementById('emailVerified').value = 'N';
                setMessage('emailVerifyMessage', '인증번호를 발송했습니다. 이메일을 확인해주세요.', 'success');
            } else {
                setMessage('emailVerifyMessage', '인증번호 발송에 실패했습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('emailVerifyMessage', '인증번호 발송 중 오류가 발생했습니다.', 'warn');
        });
}

function verifyMyPageEmailCode() {
    const email = document.getElementById('mypageEmail').value.trim();
    const inputCode = document.getElementById('mypageEmailCode').value.trim();
    if (!inputCode) {
        alert('이메일 인증번호를 입력해주세요.');
        return;
    }

    postForm(getMyPageContextPath() + '/spendolive/mypage/email/verify.do', { email: email, inputCode: inputCode })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'true') {
                document.getElementById('emailVerified').value = 'Y';
                setMessage('emailVerifyMessage', '이메일 인증이 완료되었습니다.', 'success');
            } else {
                document.getElementById('emailVerified').value = 'N';
                setMessage('emailVerifyMessage', '인증번호가 일치하지 않습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('emailVerifyMessage', '이메일 인증 확인 중 오류가 발생했습니다.', 'warn');
        });
}

function sendMyPagePhoneCode() {
    const phone = document.getElementById('mypagePhone').value.trim();
    if (!phone) {
        alert('전화번호를 입력해주세요.');
        return;
    }

    postForm(getMyPageContextPath() + '/spendolive/mypage/phone/send.do', { phone: phone })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'SUCCESS') {
                document.getElementById('phoneVerified').value = 'N';
                setMessage('phoneVerifyMessage', '인증번호를 발송했습니다. 문자를 확인해주세요.', 'success');
            } else {
                setMessage('phoneVerifyMessage', '인증번호 발송에 실패했습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('phoneVerifyMessage', '인증번호 발송 중 오류가 발생했습니다.', 'warn');
        });
}

function verifyMyPagePhoneCode() {
    const phone = document.getElementById('mypagePhone').value.trim();
    const inputCode = document.getElementById('mypagePhoneCode').value.trim();
    if (!inputCode) {
        alert('문자 인증번호를 입력해주세요.');
        return;
    }

    postForm(getMyPageContextPath() + '/spendolive/mypage/phone/verify.do', { phone: phone, inputCode: inputCode })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'true') {
                document.getElementById('phoneVerified').value = 'Y';
                setMessage('phoneVerifyMessage', '전화번호 인증이 완료되었습니다.', 'success');
            } else {
                document.getElementById('phoneVerified').value = 'N';
                setMessage('phoneVerifyMessage', '인증번호가 일치하지 않습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('phoneVerifyMessage', '전화번호 인증 확인 중 오류가 발생했습니다.', 'warn');
        });
}






function submitWithdrawForm() {
    const confirmInput = document.getElementById('withdrawConfirm');
    const form = document.getElementById('withdrawForm');

    if (!confirmInput || !form) {
        return;
    }

    if (confirmInput.value.trim() !== '탈퇴합니다') {
        alert('확인 문구를 정확히 입력해주세요.');
        confirmInput.focus();
        return;
    }

    if (confirm('정말 회원탈퇴를 진행할까요? 탈퇴 후에는 현재 계정으로 다시 로그인할 수 없습니다.')) {
        form.submit();
    }
}

document.addEventListener('click', function (event) {
    const modal = document.getElementById('withdrawModal');
    if (modal && event.target === modal) {
        closeWithdrawModal();
    }
});

function checkMyPagePassword() {
    const currentPassword = document.getElementById('currentPassword').value.trim();
    const newPassword = document.getElementById('newPassword').value.trim();
    const passwordConfirm = document.getElementById('passwordConfirm').value.trim();

    if (!currentPassword || !newPassword || !passwordConfirm) {
        document.getElementById('passwordChecked').value = 'N';
        setMessage('passwordCheckMessage', '현재 비밀번호, 새 비밀번호, 새 비밀번호 확인을 모두 입력해주세요.', 'warn');
        return;
    }

    if (newPassword !== passwordConfirm) {
        document.getElementById('passwordChecked').value = 'N';
        setMessage('passwordCheckMessage', '새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.', 'warn');
        return;
    }

    document.getElementById('passwordChecked').value = 'Y';
    setMessage('passwordCheckMessage', '새 비밀번호 확인이 완료되었습니다. 저장 시 현재 비밀번호도 다시 확인됩니다.', 'success');
}
