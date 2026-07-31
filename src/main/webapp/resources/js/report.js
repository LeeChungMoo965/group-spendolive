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
        const reported_member_id = reportButton.dataset.reported_member_id;
        const chat_text = reportButton.dataset.chat_text;
        if (!room_id || !reported_member_id || !chat_text) {
            showFailure(reportButton, { message: '신고할 회원 정보를 찾을 수 없습니다.' });
            return;
        }
        const body = new URLSearchParams({ room_id });
        if (reported_member_id) {
            body.append('reported_member_id', reported_member_id);
        }
        if (chat_text) {
            body.append('chat_text', chat_text);
        }
        // 모듈화된 함수 호출
        await executeRequest({
            button: reportButton,
            confirmMessage: '신고 하시겠습니까?',
            requestUrl: '/report/report.do',
            bodyData: body,
            modalTitle : '신고를 처리하고 있습니다.',
            fallbackErrorMessage: '신고 결과를 확인하지 못했습니다. 다시 시도해주세요.'
        },'report');
    });
    // 결제 중 새로고침이나 창 닫기를 시도하면 브라우저 기본 경고를 표시합니다.
    
  })();



