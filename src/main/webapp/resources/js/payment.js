
function moveAfterSuccess(result) {
    showStatusModal(
        'payment',
        'success',
        '결제가 완료되었습니다.',
        result.message || '참여한 방으로 이동합니다.',
        { hideClose: true }
    );

    window.setTimeout(function () {
        window.location.href = result.redirectUrl;
    }, 1200);
}
  function showFailure(targetButton, result, prefix = 'payment') {
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

    if (result && result.code === 'CARD_REQUIRED') {
        showStatusModal(prefix, 'error', '결제 카드가 필요합니다.', result.message || '카드를 먼저 등록해주세요.', {
            actionText: '카드 등록하기',
            onAction: () => {
                if (typeof window.requestBillingAuth === 'function') {
                    window.requestBillingAuth();
                } else {
                    window.location.href = contextPath + '/spendolive/mypage.do';
                }
            }
        });
        return;
    }

    const defaultTitle = prefix === 'payment' ? '결제를 완료하지 못했습니다.' : '실패하였습니다.';
    showStatusModal(
        prefix,
        'error',
        defaultTitle,
        result && result.message ? result.message : '잠시 후 다시 시도해주세요.'
    );
}
const paymentCloseBtn = document.getElementById('StatusCloseButton');
if (paymentCloseBtn) {
    paymentCloseBtn.addEventListener('click', function() {
        hideStatusModal('payment');
    });
}

const paymentActionBtn = document.getElementById('StatusActionButton');
if (paymentActionBtn) {
    paymentActionBtn.addEventListener('click', function () {
      if (typeof window.modalActionHandler === 'function') {
          window.modalActionHandler();
        }
    });
}










//결제 관련 AJAX
(function () {
  const paymentButton = document.getElementById('paymentSubmitButton');

  if (!paymentButton) {
      return;
  }
  // 결제 처리 상태에 따라 하나의 팝업을 진행·성공·실패 화면으로 재사용합니다.
  // 결제 요청의 응답이 끊기면 DB에 결제가 저장됐는지 여러 번 다시 확인합니다.
 

  paymentButton.addEventListener('click', async function () {
    const room_id = paymentButton.dataset.room_id;
    if (!room_id) {
        showFailure(paymentButton, {
            message: '결제할 방 정보를 찾을 수 없습니다.'
        });
        return;
    }

    // URL 파라미터 생성
    const body = new URLSearchParams();
    body.append('room_id', room_id);
    // 공통 모듈 함수 호출
    await executeRequest({
        button: paymentButton,
        confirmMessage: '표시된 금액으로 결제하시겠습니까?',
        requestUrl: '/payment/paymenting.do',
        bodyData: body,
        checkStatusFunc: () => checkPaymentStatus('payment',room_id),
        // 필요하다면 에러 메시지도 커스텀 전달 가능
        fallbackErrorMessage: '결제 결과를 확인하지 못했습니다. 카드 승인 내역을 확인한 뒤 다시 시도해주세요.'
    });
});
  // 결제 중 새로고침이나 창 닫기를 시도하면 브라우저 기본 경고를 표시합니다.
 
})();
// 정산금 출금
(function () {
    // 결제 처리 상태에 따라 하나의 팝업을 진행·성공·실패 화면으로 재사용합니다.
    // 결제 요청의 응답이 끊기면 DB에 결제가 저장됐는지 여러 번 다시 확인합니다.
    
  
    document.addEventListener('click', async function (event) {
    const adminpaymentButton = event.target.closest('.adminpaymentSubmitButton');
    if (!adminpaymentButton) return;

    const room_id = adminpaymentButton.dataset.room_id;
    const member_login_id = adminpaymentButton.dataset.member_login_id;

    if (!room_id || !member_login_id) {
        showFailure(adminpaymentButton, { message: '결제할 방 정보를 찾을 수 없습니다.' });
        return;
    }

    // Body 파라미터 구성
    const body = new URLSearchParams({ room_id });
    if (member_login_id) {
        body.append('member_login_id', member_login_id);
    }

    // 공통 모듈 호출
    await executeRequest({
        button: adminpaymentButton,
        confirmMessage: '표시된 금액으로 결제하시겠습니까?',
        requestUrl: '/admin/settlement/paymenting.do',
        bodyData: body,
        checkStatusFunc: () => checkPaymentStatus('admin/settlement',room_id, member_login_id),
        fallbackErrorMessage: '결제 결과를 확인하지 못했습니다. 카드 승인 내역을 확인한 뒤 다시 시도해주세요.'
    });
});
    // 결제 중 새로고침이나 창 닫기를 시도하면 브라우저 기본 경고를 표시합니다.
    
  })();

  
  //정산금 송금
  (function () {
    // 결제 처리 상태에 따라 하나의 팝업을 진행·성공·실패 화면으로 재사용합니다.
    // 결제 요청의 응답이 끊기면 DB에 결제가 저장됐는지 여러 번 다시 확인합니다.
    
  
    document.addEventListener('click', async function (event) {
        const adminsettlementButton = event.target.closest('.adminsettlementSubmitButton');
        if (!adminsettlementButton) return;
    
        const room_id = adminsettlementButton.dataset.room_id;
        const host_id = adminsettlementButton.dataset.host_id;
        if (!room_id || !host_id) {
            showFailure(adminsettlementButton, { message: '송금할 방 정보를 찾을 수 없습니다.' });
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


  //환불
  (function () {
    // 결제 처리 상태에 따라 하나의 팝업을 진행·성공·실패 화면으로 재사용합니다.
    // 결제 요청의 응답이 끊기면 DB에 결제가 저장됐는지 여러 번 다시 확인합니다.
    
  
    document.addEventListener('click', async function (event) {
    const adminrefundButton = event.target.closest('.adminrefundSubmitButton');
    if (!adminrefundButton) return;
    const params = new URLSearchParams({
        paymentKey: adminrefundButton.dataset.paymentKey || '',
        payment_id: adminrefundButton.dataset.paymentId || '',
        id: adminrefundButton.dataset.id || '',
        settlement_id: adminrefundButton.dataset.settlementId || '',
        total_amount: adminrefundButton.dataset.totalAmount || ''
    });

    // 공통 모듈 호출
    await executeRequest({
        button: adminrefundButton,
        confirmMessage: '환불을 진행 하겠습니다?',
        requestUrl: '/admin/settlement/cancelpaymenting.do',
        bodyData: params,
        checkStatusFunc: () => checkPaymentStatus('admin/settlement',null, null, null, params),
        fallbackErrorMessage: '결제 결과를 확인하지 못했습니다. 카드 승인 내역을 확인한 뒤 다시 시도해주세요.'
    });
});
    // 결제 중 새로고침이나 창 닫기를 시도하면 브라우저 기본 경고를 표시합니다.
    //연기
  })();
  (function () {
    // 결제 처리 상태에 따라 하나의 팝업을 진행·성공·실패 화면으로 재사용합니다.
    // 결제 요청의 응답이 끊기면 DB에 결제가 저장됐는지 여러 번 다시 확인합니다.
    
  
    document.addEventListener('click', async function (event) {
        const adminlateButton = event.target.closest('.adminlateSubmitButton');
        if (!adminlateButton) return;
    
        const room_id = adminlateButton.dataset.room_id;
        const member_login_id = adminlateButton.dataset.member_login_id;
        const pay_late_day = adminlateButton.dataset.pay_late_day;
        if (!room_id || !member_login_id || !pay_late_day) {
            showFailure(adminlateButton, { message: '정산 연기를 할 내역을 찾을 수 없습니다.' });
            return;
        }
        const body = new URLSearchParams({ room_id });
        if (member_login_id) {
            body.append('member_login_id', member_login_id);
        }
        if (pay_late_day) {
            body.append('pay_late_day', pay_late_day);
        }
        // 모듈화된 함수 호출
        await executeRequest({
            button: adminlateButton,
            confirmMessage: '정산 연기 처리하겠습니다.',
            requestUrl: '/admin/settlement/paymentlate.do',
            bodyData: body,
            modalTitle: '정산 연기를 처리하고 있습니다.',
            fallbackErrorMessage: '정산 연기 처리 결과를 확인하지 못했습니다.'
        });
    });
    // 결제 중 새로고침이나 창 닫기를 시도하면 브라우저 기본 경고를 표시합니다.
    
  })();