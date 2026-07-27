
<%-- 결제 처리 중에는 뒤쪽 화면을 조작하지 못하도록 전체 화면 팝업을 표시합니다. --%>
<div id="paymentStatusOverlay"
     class="payment-status-overlay"
     role="dialog"
     aria-modal="true"
     aria-labelledby="paymentStatusTitle"
     aria-describedby="paymentStatusMessage"
     hidden>
    <div class="payment-status-box">
        <div id="paymentStatusSpinner"
             class="payment-status-spinner"
             aria-hidden="true"></div>

        <div id="paymentStatusIcon"
             class="payment-status-icon"
             aria-hidden="true"
             hidden></div>

        <h3 id="paymentStatusTitle">결제를 처리하고 있습니다.</h3>
        <p id="paymentStatusMessage">
            창을 닫거나 새로고침하지 말아주세요.
        </p>

        <div id="paymentStatusActions"
             class="payment-status-actions"
             hidden>
            <button type="button"
                    id="paymentStatusCloseButton"
                    class="btn btn-outline">
                확인
            </button>
            <button type="button"
                    id="paymentStatusActionButton"
                    class="btn btn-primary"
                    hidden>
                이동하기
            </button>
        </div>
    </div>
</div>
