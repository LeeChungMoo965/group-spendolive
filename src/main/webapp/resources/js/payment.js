//결제 관련 AJAX
(function () {
  const contextPath = 'http://localhost:8080';
  const paymentButton = document.getElementById('paymentSubmitButton');
  const overlay = document.getElementById('paymentStatusOverlay');
  const spinner = document.getElementById('paymentStatusSpinner');
  const icon = document.getElementById('paymentStatusIcon');
  const title = document.getElementById('paymentStatusTitle');
  const message = document.getElementById('paymentStatusMessage');
  const actions = document.getElementById('paymentStatusActions');
  const closeButton = document.getElementById('paymentStatusCloseButton');
  const actionButton = document.getElementById('paymentStatusActionButton');

  let paymentInProgress = false;
  let actionHandler = null;

  if (!paymentButton || !overlay) {
      return;
  }

  // 결제 처리 상태에 따라 하나의 팝업을 진행·성공·실패 화면으로 재사용합니다.
  function showStatusModal(state, modalTitle, modalMessage, option) {
      const settings = option || {};

      overlay.hidden = false;
      title.textContent = modalTitle;
      message.textContent = modalMessage;
      overlay.dataset.state = state;

      spinner.hidden = state !== 'processing';
      icon.hidden = state === 'processing';
      icon.textContent = state === 'success' ? '✓' : '!';

      actions.hidden = state === 'processing';
      closeButton.hidden = state === 'success' || settings.hideClose === true;

      actionHandler = typeof settings.onAction === 'function'
          ? settings.onAction
          : null;

      if (settings.actionText && actionHandler) {
          actionButton.textContent = settings.actionText;
          actionButton.hidden = false;
      } else {
          actionButton.hidden = true;
      }
  }

  function hideStatusModal() {
      if (paymentInProgress) {
          return;
      }
      overlay.hidden = true;
      actionHandler = null;
  }

  function wait(milliseconds) {
      return new Promise(function (resolve) {
          window.setTimeout(resolve, milliseconds);
      });
  }

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

  // 결제 요청의 응답이 끊기면 DB에 결제가 저장됐는지 여러 번 다시 확인합니다.
  async function checkPaymentStatus(roomId) {
      for (let attempt = 0; attempt < 6; attempt += 1) {
          try {
              const response = await fetch(
                  contextPath
                  + '/payment/status.do?roomId='
                  + encodeURIComponent(roomId),
                  {
                      method: 'GET',
                      credentials: 'same-origin',
                      headers: {
                          'Accept': 'application/json'
                      }
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
          }

          await wait(1500);
      }

      return null;
  }

  function moveAfterSuccess(result) {
      showStatusModal(
          'success',
          '결제가 완료되었습니다.',
          result.message || '참여한 방으로 이동합니다.',
          { hideClose: true }
      );

      window.setTimeout(function () {
          window.location.href = result.redirectUrl;
      }, 1200);
  }

  function showFailure(result) {
      paymentInProgress = false;
      paymentButton.disabled = false;

      if (result && result.code === 'LOGIN_REQUIRED') {
          showStatusModal(
              'error',
              '로그인이 필요합니다.',
              result.message || '다시 로그인해주세요.',
              {
                  actionText: '로그인 화면으로',
                  onAction: function () {
                      window.location.href = result.redirectUrl
                          || contextPath + '/member/loginForm.do';
                  }
              }
          );
          return;
      }

      if (result && result.code === 'CARD_REQUIRED') {
          showStatusModal(
              'error',
              '결제 카드가 필요합니다.',
              result.message || '카드를 먼저 등록해주세요.',
              {
                  actionText: '카드 등록하기',
                  onAction: function () {
                      if (typeof window.requestBillingAuth === 'function') {
                          window.requestBillingAuth();
                      } else {
                          window.location.href = contextPath + '/spendolive/mypage.do';
                      }
                  }
              }
          );
          return;
      }

      showStatusModal(
          'error',
          '결제를 완료하지 못했습니다.',
          result && result.message
              ? result.message
              : '잠시 후 다시 시도해주세요.'
      );
  }

  paymentButton.addEventListener('click', async function () {
      if (paymentInProgress) {
          return;
      }

      const roomId = paymentButton.dataset.roomId;
      if (!roomId) {
          showFailure({
              message: '결제할 방 정보를 찾을 수 없습니다.'
          });
          return;
      }

      if (!window.confirm('표시된 금액으로 결제하시겠습니까?')) {
          return;
      }

      paymentInProgress = true;
      paymentButton.disabled = true;

      showStatusModal(
          'processing',
          '결제를 처리하고 있습니다.',
          '창을 닫거나 새로고침하지 말아주세요.'
      );

      const controller = new AbortController();
      const timeoutId = window.setTimeout(function () {
          controller.abort();
      }, 30000);

      try {
          const body = new URLSearchParams();
          body.append('roomId', roomId);

          const response = await fetch(
              contextPath + '/payment/paymenting.do',
              {
                  method: 'POST',
                  credentials: 'same-origin',
                  headers: {
                      'Accept': 'application/json',
                      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                  },
                  body: body.toString(),
                  signal: controller.signal
              }
          );

          const result = await readJson(response);

          if (response.ok && result.success) {
              paymentInProgress = false;
              moveAfterSuccess(result);
              return;
          }

          showFailure(result);

      } catch (error) {
          showStatusModal(
              'processing',
              '결제 결과를 확인하고 있습니다.',
              '통신이 잠시 끊겨 실제 결제 상태를 다시 확인합니다.'
          );

          const statusResult = await checkPaymentStatus(roomId);

          if (statusResult && statusResult.success) {
              paymentInProgress = false;
              moveAfterSuccess(statusResult);
              return;
          }

          showFailure(statusResult || {
              message: '결제 결과를 확인하지 못했습니다. 카드 승인 내역을 확인한 뒤 다시 시도해주세요.'
          });

      } finally {
          window.clearTimeout(timeoutId);
      }
  });

  closeButton.addEventListener('click', hideStatusModal);

  actionButton.addEventListener('click', function () {
      if (actionHandler) {
          actionHandler();
      }
  });

  // 결제 중 새로고침이나 창 닫기를 시도하면 브라우저 기본 경고를 표시합니다.
  window.addEventListener('beforeunload', function (event) {
      if (!paymentInProgress) {
          return;
      }
      event.preventDefault();
      event.returnValue = '';
  });
})();
