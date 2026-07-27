<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero payment-detail-page">
    <div class="container">
        <div class="table-card card payment-summary-card">
            <div class="payment-summary-head">
                <span class="status-pill PAYMENT_OPEN">결제 정보 확인</span>
                <h2><c:out value="${paymentAmount.roomName}" /></h2>
                <p>
                    결제 버튼을 누르면 등록된 주 결제 카드로 자동결제가 진행됩니다.
                </p>
            </div>

            <div class="table-wrap">
                <table class="payment-amount-table">
                    <thead>
                        <tr>
                            <th>구분</th>
                            <th>금액</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>OTT 사용료</strong></td>
                            <td>
                                <strong>
                                    <fmt:formatNumber value="${paymentAmount.baseAmount}" type="number" />원
                                </strong>
                            </td>
                        </tr>
                        <tr>
                            <td><strong>플랫폼 수수료 (${paymentAmount.feeRate}%)</strong></td>
                            <td>
                                <strong>
                                    + <fmt:formatNumber value="${paymentAmount.feeAmount}" type="number" />원
                                </strong>
                            </td>
                        </tr>
                        <tr class="payment-total-row">
                            <td><strong>최종 결제 금액</strong></td>
                            <td>
                                <strong>
                                    <fmt:formatNumber value="${paymentAmount.totalAmount}" type="number" />원
                                </strong>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="payment-auto-guide">
                결제 완료 후 매월 <strong>${paymentAmount.automaticPaymentDay}일</strong>에 자동결제됩니다.
            </div>

            <div class="payment-action-row">
                <a href="${contextPath}/spendolive/ott/friends.do"
                   class="btn btn-danger-outline">
                    취소하기
                </a>

                <button type="button"
                        id="paymentSubmitButton"
                        class="btn btn-primary"
                        data-room-id="${paymentAmount.roomId}">
                    결제하기
                </button>
            </div>
        </div>
    </div>
</section>
<jsp:include page="/WEB-INF/views/payment/popup.jsp" />
<script src="${contextPath}/resources/js/payment.js"></script>