<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<style>
    .invite-share-box {
        margin-top: 14px;
        padding: 14px;
        border: 1px solid rgba(126, 144, 61, 0.18);
        border-radius: 16px;
        background: rgba(255, 253, 238, 0.75);
    }

    .invite-share-box strong {
        display: block;
        margin-bottom: 6px;
        font-size: 14px;
    }

    .invite-url-row {
        display: flex;
        gap: 8px;
        align-items: center;
        margin-top: 10px;
    }

    .invite-url-input {
        flex: 1;
        min-width: 0;
        padding: 10px 12px;
        border: 1px solid #d9dfbd;
        border-radius: 12px;
        background: #fff;
        font-size: 13px;
    }

    .invite-share-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 10px;
    }

    .invite-qr-box {
        display: none;
        margin-top: 12px;
        padding: 12px;
        border-radius: 14px;
        background: #fff;
        text-align: center;
    }

    .invite-qr-box.show {
        display: block;
    }

    .invite-qr-box img {
        width: 180px;
        height: 180px;
    }
</style>

<section class="page-hero ott-sub-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">FRIENDS SHARE ROOM</p>
        <h1>가족 · 지인 공유방</h1>
        <p class="hero-text">
            가족 또는 지인과 함께 쓸 OTT 공유방을 만들고, 초대 URL·QR·카카오톡 링크로 결제 화면까지 바로 연결합니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott.do" class="btn btn-outline">OTT관리로 돌아가기</a>
            <a href="#createRoom" class="btn btn-primary">공유방 만들기</a>
        </div>
    </div>
</section>

<section class="section compact ott-page-section">
    <div class="container ott-wide-container">
        <div class="ott-system-guide card">
            <strong>정산 규칙</strong>
            <ol>
                <li>${settlementGuide}</li>
                <li>가족방은 승인 없이 초대 URL 또는 QR을 통해 결제 화면으로 이동합니다.</li>
                <li>방 삭제 요청 시 이번 이용 기간까지만 유지되며, 다음 이용분 결제 완료 건은 자동 환불 처리됩니다.</li>
            </ol>
        </div>

        <article class="card ott-tab-panel family-room-section">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">FAMILY & FRIENDS ROOMS</p>
                    <h2>가족 지인과의 공유방</h2>
                </div>
                <span>${fn:length(myRoomList)}개</span>
            </div>

            <c:choose>
                <c:when test="${not empty myRoomList}">
                    <div class="ott-room-card-list family-room-list">
                        <c:forEach var="room" items="${myRoomList}" varStatus="roomStatus">
                            <div class="ott-room-card family-room-card">
                                <div class="room-index-badge">${roomStatus.count}</div>

                                <div class="family-room-info">
                                    <strong>${room.roomName}</strong>
                                    <p>
                                        ${room.serviceName} · ${room.planName} · ${room.currentMemberCount}/${room.memberLimit}명 ·
                                        결제일 매월 ${room.billingDay}일
                                    </p>
                                    <small>
                                        1인 결제금액 <fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원
                                        <c:if test="${not empty room.inviteCode}"> · 초대코드 ${room.inviteCode}</c:if>
                                    </small>
                                    <c:if test="${room.status eq 'CLOSE_REQUESTED'}">
                                        <small class="danger-text">방 삭제 예약됨 · ${room.closeEffectiveDate} 종료 예정</small>
                                    </c:if>
                                    <c:if test="${room.status eq 'REPLACE_RECRUITING'}">
                                        <small class="warn-text">미결제자 발생으로 대체 모집 중</small>
                                    </c:if>

                                    <c:if test="${room.hostmember_id eq loginId and not empty room.inviteCode and room.status ne 'CLOSE_REQUESTED' and room.status ne 'CLOSED'}">
                                        <div class="invite-share-box" data-room-name="${fn:escapeXml(room.roomName)}">
                                            <strong>초대 링크 공유</strong>
                                            <small>URL 복사, QR 코드, 카카오톡 공유 중 하나로 초대할 수 있습니다. 링크를 타고 들어오면 결제 화면으로 이동합니다.</small>

                                            <div class="invite-url-row">
                                                <input type="text"
                                                       class="invite-url-input"
                                                       readonly
                                                       value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${contextPath}/spendolive/ott/friends/invite.do?code=${room.inviteCode}">
                                                <button type="button" class="btn btn-outline btn-mini invite-copy-btn">URL 복사</button>
                                            </div>

                                            <div class="invite-share-actions">
                                                <button type="button" class="btn btn-outline btn-mini invite-qr-btn">QR 코드 보기</button>
                                                <button type="button" class="btn btn-primary btn-mini invite-kakao-btn">카카오톡 공유</button>
                                            </div>

                                            <div class="invite-qr-box">
                                                <img alt="가족방 초대 QR 코드">
                                                <small>QR을 스캔하면 결제 화면으로 이동합니다.</small>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>

                                <div class="family-room-actions">
                                    <span class="status-pill ${room.status}">${room.status}</span>
                                    <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-outline btn-mini">대화방</a>

                                    <c:if test="${room.hostmember_id ne loginId and room.status ne 'CLOSE_REQUESTED' and room.status ne 'CLOSED'}">
                                        <c:choose>
                                            <c:when test="${room.leaveReservedYn eq 'Y'}">
                                                <small class="warn-text">나가기 예약됨 · ${room.leaveScheduledDate} 자동 퇴장</small>
                                                <form action="${contextPath}/spendolive/ott/room/leave-cancel.do" method="post" class="compact-close-form">
                                                    <input type="hidden" name="roomId" value="${room.roomId}">
                                                    <input type="hidden" name="returnPage" value="friends">
                                                    <button type="submit" class="btn btn-outline btn-mini" onclick="return confirm('나가기 예약을 취소할까요?');">예약 취소</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form action="${contextPath}/spendolive/ott/room/leave-reserve.do" method="post" class="compact-close-form">
                                                    <input type="hidden" name="roomId" value="${room.roomId}">
                                                    <input type="hidden" name="returnPage" value="friends">
                                                    <button type="submit" class="btn btn-outline btn-mini" onclick="return confirm('나가기 예약을 할까요? 다음 결제일 7일 전 자동으로 방에서 나가집니다.');">나가기 예약</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>

                                    <c:if test="${room.hostmember_id eq loginId and room.status ne 'CLOSE_REQUESTED' and room.status ne 'CLOSED'}">
                                        <form action="${contextPath}/spendolive/ott/room/close-request.do" method="post" class="room-close-form compact-close-form">
                                            <input type="hidden" name="roomId" value="${room.roomId}">
                                            <input type="hidden" name="returnPage" value="friends">
                                            <input type="hidden" name="closeReason" value="파티장 요청">
                                            <input type="text" name="closeNotice" placeholder="종료 공지 입력">
                                            <button type="submit" class="btn btn-outline btn-mini" onclick="return confirm('방 삭제 요청을 진행할까요? 이번 이용 기간 종료일까지 유지되고, 다음 이용분 결제 완료 건은 자동 환불됩니다.');">방 삭제 요청</button>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-box">아직 가족 지인과 만든 공유방이 없습니다.</div>
                </c:otherwise>
            </c:choose>
        </article>

        <article id="createRoom" class="card ott-form-card family-create-card">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">CREATE ROOM</p>
                    <h2>공유방 만들기</h2>
                </div>
                <span>가족 · 지인 전용</span>
            </div>

            <form action="${contextPath}/spendolive/ott/friends/create.do" method="post" class="ott-form-grid ott-fixed-plan-form" data-room-mode="FRIEND">
                <label>
                    OTT 종류
                    <select name="ottServiceId" class="ott-service-select" required>
                        <option value="">선택</option>
                        <c:forEach var="service" items="${serviceList}">
                            <option value="${service.ottServiceId}"
                                    data-service-name="${service.serviceName}"
                                    data-plan="${service.fixedPlanName}"
                                    data-base-price="${service.basePrice}"
                                    data-extra-fee="${service.extraMemberFee}"
                                    data-extra-count="${service.extraMemberCount}"
                                    data-total-price="${service.defaultPrice}"
                                    data-member-limit="${service.maxMemberLimit}"
                                    data-share-amount="${service.shareAmount}"
                                    data-fee-amount="${service.feeAmount}"
                                    data-person-amount="${service.perPersonAmount}">
                                ${service.serviceName}
                            </option>
                        </c:forEach>
                    </select>
                </label>

                <label>
                    공유방 이름
                    <input type="text" name="roomName" placeholder="예: 우리 가족 Netflix 공유방">
                </label>

                <label>
                    결제일
                    <input type="number" name="billingDay" min="1" max="31" value="1" required>
                </label>

                <input type="hidden" name="planName" class="ott-plan-input">
                <input type="hidden" name="totalPrice" class="ott-total-price-input">
                <input type="hidden" name="memberLimit" class="ott-member-limit-input">

                <div class="ott-fixed-plan-preview">
                    <strong>OTT를 선택하면 최고 멤버십 기준 금액이 자동 적용됩니다.</strong>
                    <p>구독종류, 전체 구독료, 최대 인원은 직접 입력하지 않고 서비스 규칙으로 고정됩니다.</p>
                </div>

                <button type="submit" class="btn btn-primary full ott-form-submit">공유방 생성</button>
            </form>
        </article>

        <article class="card ott-tab-panel settlement-panel-wide family-settlement-panel">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">SETTLEMENT</p>
                    <h2>정산 관리</h2>
                </div>
                <span>가족 지인 공유방만 표시</span>
            </div>

            <div class="settlement-stack">
                <section class="settlement-wide-block settlement-request-block">
                    <div class="settlement-sub-header">
                        <div>
                            <h3>정산 요청 보내기</h3>
                            <p>정산월은 다음 이용분 기준입니다. 마감일은 결제일 7일 전으로 자동 계산됩니다.</p>
                        </div>
                        <span>다음 이용분 선결제</span>
                    </div>

                    <c:choose>
                        <c:when test="${not empty hostedRoomList}">
                            <div class="settlement-request-list settlement-wide-list">
                                <c:forEach var="room" items="${hostedRoomList}">
                                    <c:if test="${room.status ne 'CLOSE_REQUESTED' and room.status ne 'CLOSED'}">
                                        <form action="${contextPath}/spendolive/ott/settlement/request.do" method="post" class="settlement-request-row wide-settlement-row">
                                            <input type="hidden" name="roomId" value="${room.roomId}">
                                            <input type="hidden" name="returnPage" value="friends">

                                            <div class="settlement-room-title">
                                                <strong>${room.roomName}</strong>
                                                <small>${room.serviceName} · ${room.planName} · 참여 ${room.currentMemberCount}/${room.memberLimit}명 · 1인 결제 <fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원</small>
                                            </div>

                                            <label class="settlement-field">
                                                <span>정산월</span>
                                                <input type="month" name="settlementMonth" value="${selectedSettlementMonth}">
                                            </label>

                                            <div class="settlement-auto-guide">
                                                <b>자동 계산</b>
                                                <small>결제 가능 시작일 = 전월 결제일 / 마감일 = 이용 시작일 7일 전</small>
                                            </div>

                                            <button type="submit" class="btn btn-primary settlement-send-btn">정산 요청 보내기</button>
                                        </form>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-box">방장으로 만든 가족 지인 공유방이 없습니다.</div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="settlement-wide-block settlement-status-block">
                    <div class="settlement-sub-header">
                        <div>
                            <h3>정산 상태</h3>
                            <p>가족 지인 공유방의 결제 가능 기간, 마감일, 내 결제 상태를 확인합니다.</p>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${not empty settlementList}">
                            <div class="status-list settlement-status-wide-list">
                                <c:forEach var="settlement" items="${settlementList}">
                                    <div class="status-row wide settlement-status-row">
                                        <span>
                                            <strong>${settlement.roomName}</strong><br>
                                            <small>
                                                ${settlement.settlementMonth} 이용분 · 결제기간 ${settlement.paymentStartDate} ~ ${settlement.paymentCloseDate}<br>
                                                이용기간 ${settlement.serviceStartDate} ~ ${settlement.serviceEndDate}
                                            </small>
                                        </span>
                                        <em class="${settlement.status eq 'DONE' or settlement.status eq 'CONFIRMED' ? 'done' : 'wait'}">${settlement.status}</em>

                                        <c:if test="${settlement.myRole eq 'MEMBER'}">
                                            <div class="settlement-pay-box">
                                                <b><fmt:formatNumber value="${settlement.myTotalAmount}" pattern="#,##0" />원</b>
                                                <small>${settlement.myPaymentStatus}</small>
                                                <c:if test="${settlement.myPaymentStatus eq 'UNPAID'}">
                                                    <form action="${contextPath}/spendolive/ott/settlement/pay.do" method="post">
                                                        <input type="hidden" name="paymentId" value="${settlement.paymentId}">
                                                        <input type="hidden" name="returnPage" value="friends">
                                                        <button type="submit" class="btn btn-primary btn-mini">결제 완료</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-box">아직 가족 지인 공유방 정산 내역이 없습니다.</div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty hostedSettlementPaymentList}">
                        <div class="team-payment-status-box">
                            <h3>팀원별 정산 상태</h3>
                            <div class="team-payment-list">
                                <c:forEach var="payment" items="${hostedSettlementPaymentList}">
                                    <div class="team-payment-row">
                                        <span>
                                            <strong>${payment.roomName}</strong>
                                            <small>${payment.settlementMonth} 이용분 · ${payment.member_name}(${payment.member_id})</small>
                                        </span>
                                        <b><fmt:formatNumber value="${payment.totalAmount}" pattern="#,##0" />원</b>
                                        <em class="${payment.paymentStatus eq 'PAID' or payment.paymentStatus eq 'CONFIRMED' ? 'done' : 'wait'}">${payment.paymentStatus}</em>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </section>
            </div>
        </article>
    </div>
</section>

<script>
    var msg = "${msg}";
    if (msg && msg !== "") {
        alert(msg);
    }
</script>

<script src="https://developers.kakao.com/sdk/js/kakao.js"></script>
<script>
(function () {
    var kakaoJavascriptKey = '${fn:escapeXml(kakaoJavascriptKey)}';

    if (window.Kakao && kakaoJavascriptKey && !window.Kakao.isInitialized()) {
        window.Kakao.init(kakaoJavascriptKey);
    }

    function copyText(text) {
        if (navigator.clipboard && window.isSecureContext) {
            return navigator.clipboard.writeText(text);
        }

        var temp = document.createElement('textarea');
        temp.value = text;
        temp.style.position = 'fixed';
        temp.style.left = '-9999px';
        document.body.appendChild(temp);
        temp.focus();
        temp.select();
        document.execCommand('copy');
        document.body.removeChild(temp);
        return Promise.resolve();
    }

    function buildSharePayload(roomName, inviteUrl) {
        return {
            objectType: 'feed',
            content: {
                title: roomName,
                description: 'SpendOlive 가족방 초대 링크입니다. 링크를 열면 결제 화면으로 이동합니다.',
                imageUrl: window.location.origin + '${contextPath}/resources/images/logo.png',
                link: {
                    mobileWebUrl: inviteUrl,
                    webUrl: inviteUrl
                }
            },
            buttons: [
                {
                    title: '결제하러 가기',
                    link: {
                        mobileWebUrl: inviteUrl,
                        webUrl: inviteUrl
                    }
                }
            ]
        };
    }

    function shareKakao(roomName, inviteUrl) {
        if (!kakaoJavascriptKey) {
            return copyText(inviteUrl).then(function () {
                alert('카카오 JavaScript 키가 아직 설정되지 않아 초대 URL을 대신 복사했습니다.');
            });
        }

        if (!window.Kakao || !window.Kakao.isInitialized()) {
            return copyText(inviteUrl).then(function () {
                alert('카카오 SDK가 연결되지 않아 초대 URL을 대신 복사했습니다.');
            });
        }

        try {
            var payload = buildSharePayload(roomName, inviteUrl);

            if (window.Kakao.Share && window.Kakao.Share.sendDefault) {
                window.Kakao.Share.sendDefault(payload);
                return Promise.resolve();
            }

            if (window.Kakao.Link && window.Kakao.Link.sendDefault) {
                window.Kakao.Link.sendDefault(payload);
                return Promise.resolve();
            }
        } catch (e) {
            console.error(e);
        }

        return copyText(inviteUrl).then(function () {
            alert('카카오톡 공유를 실행하지 못해 초대 URL을 대신 복사했습니다.');
        });
    }

    document.querySelectorAll('.invite-share-box').forEach(function (box) {
        var input = box.querySelector('.invite-url-input');
        var copyBtn = box.querySelector('.invite-copy-btn');
        var qrBtn = box.querySelector('.invite-qr-btn');
        var kakaoBtn = box.querySelector('.invite-kakao-btn');
        var qrBox = box.querySelector('.invite-qr-box');
        var qrImg = qrBox ? qrBox.querySelector('img') : null;
        var roomName = box.dataset.roomName || 'SpendOlive 가족방';

        if (copyBtn && input) {
            copyBtn.addEventListener('click', function () {
                copyText(input.value).then(function () {
                    alert('초대 URL을 복사했습니다.');
                });
            });
        }

        if (qrBtn && input && qrBox && qrImg) {
            qrBtn.addEventListener('click', function () {
                if (!qrImg.src) {
                    qrImg.src = 'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=' + encodeURIComponent(input.value);
                }
                qrBox.classList.toggle('show');
            });
        }

        if (kakaoBtn && input) {
            kakaoBtn.addEventListener('click', function () {
                shareKakao(roomName, input.value);
            });
        }
    });
})();
</script>
