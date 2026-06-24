<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero ott-sub-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">FRIENDS SHARE ROOM</p>
        <h1>가족 · 지인 공유방</h1>
        <p class="hero-text">
            가족 또는 지인과 함께 쓸 OTT 공유방을 만들고, 다음 달 이용분 선결제·마감·환불 상태까지 관리합니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott.do" class="btn btn-outline">OTT관리로 돌아가기</a>
            <a href="#createRoom" class="btn btn-primary">새 공유방 만들기</a>
        </div>
    </div>
</section>

<section class="section compact ott-page-section">
    <div class="container ott-wide-container">
        <div class="ott-system-guide card">
            <strong>정산 규칙</strong>
            <span>${settlementGuide}</span>
            <span>결제 마감일이 지나면 미결제자는 자동 추방되고, 결제일 전까지 빈자리 재모집 상태로 전환됩니다.</span>
            <span>방 삭제 요청 시 이번 이용 기간까지만 유지되며, 다음 이용분 결제 완료 건은 자동 환불 처리됩니다.</span>
        </div>

        <div class="ott-room-layout">
            <article id="createRoom" class="card ott-form-card">
                <div class="panel-header">
                    <div>
                        <p class="eyebrow">CREATE ROOM</p>
                        <h2>공유방 만들기</h2>
                    </div>
                    <span>지인 초대형</span>
                </div>

                <form action="${contextPath}/spendolive/ott/friends/create.do" method="post" class="ott-form-grid ott-fixed-plan-form">
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

            <article class="card ott-room-list-card">
                <div class="panel-header">
                    <div>
                        <p class="eyebrow">MY ROOMS</p>
                        <h2>내 공유방</h2>
                    </div>
                    <span>${fn:length(myRoomList)}개</span>
                </div>

                <c:choose>
                    <c:when test="${not empty myRoomList}">
                        <div class="ott-room-card-list">
                            <c:forEach var="room" items="${myRoomList}">
                                <div class="ott-room-card">
                                    <div>
                                        <strong>${room.roomName}</strong>
                                        <p>${room.serviceName} · ${room.planName} · ${room.currentMemberCount}/${room.memberLimit}명 · 결제일 매월 ${room.billingDay}일</p>
                                        <c:if test="${room.status eq 'CLOSE_REQUESTED'}">
                                            <small class="danger-text">방 삭제 예약됨 · ${room.closeEffectiveDate} 종료 예정</small>
                                        </c:if>
                                        <c:if test="${room.status eq 'REPLACE_RECRUITING'}">
                                            <small class="warn-text">미결제자 발생으로 대체 모집 중</small>
                                        </c:if>
                                    </div>
                                    <div class="ott-room-meta">
                                        <span class="status-pill ${room.status}">${room.status}</span>
                                        <b><fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원 / 1인</b>
                                        <small>분담금 + 수수료 3%</small>
                                        <small>초대코드 ${room.inviteCode}</small>
                                        <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-outline btn-mini">대화방</a>
                                    </div>

                                    <c:if test="${room.hostMemberId eq loginId and room.status ne 'CLOSE_REQUESTED' and room.status ne 'CLOSED'}">
                                        <form action="${contextPath}/spendolive/ott/room/close-request.do" method="post" class="room-close-form">
                                            <input type="hidden" name="roomId" value="${room.roomId}">
                                            <input type="hidden" name="returnPage" value="friends">
                                            <input type="hidden" name="closeReason" value="파티장 요청">
                                            <input type="text" name="closeNotice" placeholder="참여자에게 보여줄 종료 공지 입력">
                                            <button type="submit" class="btn btn-outline btn-mini" onclick="return confirm('방 삭제 요청을 진행할까요? 이번 이용 기간 종료일까지 유지되고, 다음 이용분 결제 완료 건은 자동 환불됩니다.');">방 삭제 요청</button>
                                        </form>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-box">아직 참여 중인 지인 공유방이 없습니다.</div>
                    </c:otherwise>
                </c:choose>
            </article>
        </div>

        <article class="card ott-tab-panel settlement-panel-wide" style="margin-top: 24px;">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">SETTLEMENT & STATUS</p>
                    <h2>정산 관리</h2>
                </div>
                <span>지인 공유방 정산 요청과 상태 확인</span>
            </div>

            <div class="settlement-stack">
                <section class="settlement-wide-block settlement-request-block">
                    <div class="settlement-sub-header">
                        <div>
                            <h3>정산 요청 보내기</h3>
                            <p>정산월은 다음 이용분 기준입니다. 마감일은 결제일 5일 전으로 자동 계산됩니다.</p>
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
                                                <small>결제 가능 시작일 = 전월 결제일 / 마감일 = 이용 시작일 5일 전</small>
                                            </div>

                                            <button type="submit" class="btn btn-primary settlement-send-btn">정산 요청 보내기</button>
                                        </form>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-box">방장으로 만든 공유방이 없습니다.</div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="settlement-wide-block settlement-status-block">
                    <div class="settlement-sub-header">
                        <div>
                            <h3>정산 상태</h3>
                            <p>결제 가능 기간, 마감일, 이용 기간, 내 결제 상태를 확인합니다.</p>
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
                            <div class="empty-box">아직 정산 내역이 없습니다.</div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </article>
    </div>
</section>
