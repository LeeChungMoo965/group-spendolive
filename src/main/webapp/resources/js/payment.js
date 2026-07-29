const contextPath = 'http://localhost:8080';
let isProcessing = false;
function showStatusModal(prefix,state, modalTitle, modalMessage, option) {
    const overlay = document.getElementById(prefix +'StatusOverlay');
    const spinner = document.getElementById(prefix +'StatusSpinner');
    const icon = document.getElementById(prefix +'StatusIcon');
    const title = document.getElementById(prefix +'StatusTitle');
    const message = document.getElementById(prefix +'StatusMessage');
    const actions = document.getElementById(prefix +'StatusActions');
    const closeButton = document.getElementById(prefix +'StatusCloseButton');
    const actionButton = document.getElementById(prefix +'StatusActionButton');
    const settings = option || {};
    if (!overlay) return;
    if (!title) return;
      overlay.hidden = false;
      title.textContent = modalTitle;
      message.textContent = modalMessage;
      overlay.dataset.state = state;

    if (spinner) spinner.hidden = state !== 'processing';
    if (icon) {
        icon.hidden = state === 'processing';
        icon.textContent = state === 'success' ? '✓' : '!';
    }
    if (actions) actions.hidden = state === 'processing';
    if (closeButton) closeButton.hidden = state === 'success' || settings.hideClose === true;
      window.modalActionHandler = typeof settings.onAction === 'function' ? settings.onAction : null;

        if (actionButton) {
            if (settings.actionText && window.modalActionHandler) {
            actionButton.textContent = settings.actionText;
            actionButton.hidden = false;
            } else {
            actionButton.hidden = true;
            }
        }
  }
  window.addEventListener('beforeunload', function (event) {
    if (!isProcessing) {
        return;
    }
    event.preventDefault();
    event.returnValue = '';
});
  async function readJson(response) {
    try {
        return await response.json();
    } catch (error) {
        return {
            success: false,
            code: 'INVALID_RESPONSE',
            message: '서버 응답을 확인할 수 없습니다.'
        };
    }
}
async function checkPaymentStatus(controllerurl,room_id, member_login_id = null, host_id = null, payment = null) {
    const params = new URLSearchParams({ room_id: room_id });
    if (member_login_id) {
        params.append('member_login_id', member_login_id);
    }
    if (host_id) {
        params.append('host_id', host_id);
    }
    if (payment) {
        params.append('payment', payment);
    }
    for (let attempt = 0; attempt < 6; attempt += 1) {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), 8000);
        try {
            const url = `${contextPath}/${controllerurl}/status.do?${params.toString()}`;
            const response = await fetch(url,
                {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: {'Accept': 'application/json'},
                    signal: controller.signal
                }
            );
            

            const result = await readJson(response);

            if (result.success
                    && (result.paymentStatus === 'PAID'
                        || result.paymentStatus === 'CONFIRMED')) {
                return result;
            }

            if (result.code === 'LOGIN_REQUIRED') {
                return result;
            }

            if (result.paymentStatus !== 'PROCESSING') {
                return result;
            }
        } catch (error) {
            // 일시적인 네트워크 오류는 다음 확인 차례에서 다시 시도합니다.
        } finally {
            clearTimeout(timer);
        }

        await wait(1500);
    }

    return null;
}
async function executePaymentRequest(options) {
    const {
        button,           // 클릭된 타겟 버튼 (disabled 처리용)
        confirmMessage,   // confirm 창 메시지
        requestUrl,       // 요청 API 경로
        bodyData,         // URLSearchParams 객체
        checkStatusFunc,  // 예외 발생 시 실행할 폴링 함수 () => checkPaymentStatus(id)
        modalTitle = '결제를 처리하고 있습니다.',
        modalDesc = '창을 닫거나 새로고침하지 말아주세요.'
    } = options;

    // 1. 중복 진행 방지
    if (isProcessing) return;

    // 2. 사용자 확인
    if (confirmMessage && !window.confirm(confirmMessage)) return;

    isProcessing = true;
    if (button) button.disabled = true;

    // 3. 상태 모달 열기
    showStatusModal('payment', 'processing', modalTitle, modalDesc);

    // 4. 타임아웃 컨트롤러 설정 (30초)
    const controller = new AbortController();
    const timeoutId = window.setTimeout(() => controller.abort(), 30000);

    try {
        const response = await fetch(contextPath + requestUrl, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            body: bodyData.toString(),
            signal: controller.signal
        });

        const result = await readJson(response);

        // Success
        if (response.ok && result.success) {
            isProcessing = false;
            moveAfterSuccess(result);
            return;
        }

        // Response Error
        showFailure(button, result);

    } catch (error) {
        // Network or Timeout Exception -> Fallback Check
        showStatusModal(
            'payment',
            'processing',
            '송금 결과를 확인하고 있습니다.',
            '통신이 잠시 끊겨 실제 결제 상태를 다시 확인합니다.'
        );

        const statusResult = await checkStatusFunc();

        if (statusResult && statusResult.success) {
            isProcessing = false;
            moveAfterSuccess(statusResult);
            return;
        }

        showFailure(button, statusResult || {
            message: '송금 결과를 확인하지 못했습니다. 송금 내역을 확인한 뒤 다시 시도해주세요.'
        });

    } finally {
        window.clearTimeout(timeoutId);
    }
}
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
  function hideStatusModal(prefix) {
    // 결제/정산 진행 중일 때는 닫기 방지
    if (isProcessing) return;

    const p = prefix || 'payment';
    const overlay = document.getElementById(p + 'StatusOverlay');
    if (overlay) {
        overlay.hidden = true;
    }
    window.modalActionHandler = null;
}
function wait(milliseconds) {
    return new Promise(resolve => window.setTimeout(resolve, milliseconds));
}
  const paymentCloseBtn = document.getElementById('paymentStatusCloseButton');
  if (paymentCloseBtn) {
      paymentCloseBtn.addEventListener('click', function() {
          hideStatusModal('payment');
      });
  }

  const paymentActionBtn = document.getElementById('paymentStatusActionButton');
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
    await executePaymentRequest({
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
    await executePaymentRequest({
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
        await executePaymentRequest({
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
    await executePaymentRequest({
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
        await executePaymentRequest({
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
