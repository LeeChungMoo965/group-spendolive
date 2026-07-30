
function moveAfterSuccess(result) {
    showStatusModal(
        'report',
        'success',
        '신고가 완료되었습니다.',
        result.message || '전 화면 으로 이동합니다.',
        { hideClose: true }
    );

    window.setTimeout(function () {
        window.location.href = result.redirectUrl;
    }, 1200);
}
  function showFailure(targetButton, result, prefix = 'report') {
    isProcessing = false;
    if (targetButton) {
        targetButton.disabled = false;
    }

    if (result && result.code === 'LOGIN_REQUIRED') {
        showStatusModal(prefix, 'error', '로그인이 필요합니다.', result.message || '다시 로그인해주세요.', {
            actionText: '로그인 화면으로',
            onAction: () => {
                window.location.href = result.redirectUrl || (contextPath + '/member/loginForm.do');
            }
        });
        return;
    }

    

    const defaultTitle = prefix === 'report' ? '신고를 완료하지 못했습니다.' : '실패하였습니다.';
    showStatusModal(
        prefix,
        'error',
        defaultTitle,
        result && result.message ? result.message : '잠시 후 다시 시도해주세요.'
    );
}
const reportCloseBtn = document.getElementById('StatusCloseButton');
if (reportCloseBtn) {
    reportCloseBtn.addEventListener('click', function() {
        hideStatusModal('report');
    });
}

const reportActionBtn = document.getElementById('StatusActionButton');
if (reportActionBtn) {
    reportActionBtn.addEventListener('click', function () {
      if (typeof window.modalActionHandler === 'function') {
          window.modalActionHandler();
        }
    });
}
(function () {
  
    document.addEventListener('click', async function (event) {
        const reportButton = event.target.closest('.reportSubmitButton');
        if (!reportButton) return;
    
        const room_id = reportButton.dataset.room_id;
        const host_id = reportButton.dataset.host_id;
        if (!room_id || !host_id) {
            showFailure(reportButton, { message: '신고할 회원 정보를 찾을 수 없습니다.' });
            return;
        }
        const body = new URLSearchParams({ room_id });
        if (host_id) {
            body.append('host_id', host_id);
        }
        // 모듈화된 함수 호출
        await executeRequest({
            button: adminsettlementButton,
            confirmMessage: '표시된 금액으로 결제하시겠습니까?',
            requestUrl: '/admin/settlement/pay.do',
            bodyData: body,
            checkStatusFunc: () => checkPaymentStatus('admin/settlement',room_id, null, host_id),
            fallbackErrorMessage: '송금 결과를 확인하지 못했습니다. 송금 내역을 확인한 뒤 다시 시도해주세요.'
        });
    });
    // 결제 중 새로고침이나 창 닫기를 시도하면 브라우저 기본 경고를 표시합니다.
    
  })();



