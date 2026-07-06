<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero ott-sub-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">OTT RECRUIT</p>
        <h1>모든 모집글</h1>
        <p class="hero-text">
            외부 다른 사람들과 함께 이용할 OTT 파티를 찾고, 신청 버튼을 통해 결제 화면으로 이동합니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott.do" class="btn btn-outline">OTT관리로 돌아가기</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=write" class="btn btn-primary">모집글 작성</a>
        </div>
    </div>
</section>

<section class="section compact ott-page-section">
    <div class="container ott-wide-container">
        <div class="ott-system-guide card">
            <strong>정산 규칙</strong>
            <ol>
                <li>${settlementGuide}</li>
                <li>신청 버튼은 결제 화면으로 연결되며, 결제 완료 후 참여방 입장이 처리됩니다.</li>
                <li>방 삭제 요청 상태의 파티는 신규 결제 입장과 다음 이용분 결제가 막히고, 결제 완료 건은 환불됩니다.</li>
            </ol>
        </div>

        <div class="ott-tab-menu">
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=all" class="${tab eq 'all' ? 'active' : ''}">모든 모집글</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=write" class="${tab eq 'write' ? 'active' : ''}">모집글 작성</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=manage" class="${tab eq 'manage' or tab eq 'apply' ? 'active' : ''}">참여방 관리</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=settlement" class="${tab eq 'settlement' ? 'active' : ''}">정산 및 알림</a>
        </div>

        <div class="ott-tab-content">
            <c:choose>
                <c:when test="${tab eq 'all'}">
                    <%--
                        빠른 참가 영역
                        위치: 상단 탭 메뉴 아래, 모든 모집글 목록 카드 위
                        역할: 사용자가 OTT 종류만 선택하면 서버에서 가장 오래된 빈 모집방을 자동으로 찾아 입장 처리한다.
                        주의: 이 JSP는 화면과 요청 전송만 담당한다.
                             실제 방 찾기/구성원 저장 로직은 Controller → Service → Repository에서 처리한다.
                    --%>
                    <article class="card ott-tab-panel quick-join-panel">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">QUICK JOIN</p>
                                <h2>빠른 참가</h2>
                                <p class="panel-description">
                                    원하는 OTT를 선택하면 오래된 모집방 중 자리가 비어있는 방부터 자동으로 참여합니다.
                                </p>
                            </div>
                        </div>

                        <%--
                            빠른 참가 form

                            일반 신청하기와 차이점:
                            - 일반 신청하기는 특정 방 카드 안에서 누르기 때문에 roomId를 바로 보낼 수 있다.
                            - 빠른 참가는 사용자가 방을 직접 고르는 게 아니라 OTT만 고른다.
                            - 그래서 여기서는 roomId를 보내지 않고 ottServiceId만 보낸다.

                            이후 서버 흐름:
                            1. ottServiceId를 Controller로 보냄
                            2. Service/Repository에서 해당 OTT의 모집방 중 가장 오래된 빈 방을 찾음
                            3. 찾은 방의 roomId를 결제쪽으로 넘김
                            4. 결제쪽 개발자는 기존 신청하기처럼 roomId만 받아서 처리하면 됨
                        --%>
                        <form action="${contextPath}/spendolive/ott/recruit/quick-join.do"
                            method="post"
                            class="recruit-filter-form quick-join-form">

                                <%--
                                    빠른 참가에서는 roomId가 없다.
                                    사용자가 방을 고른 게 아니라 OTT만 고른 상태이기 때문이다.

                                    여기서 선택한 ottServiceId를 서버로 보내면,
                                    서버가 이 OTT에 해당하는 빈 모집방을 자동으로 찾는다.
                                --%>
                            <div class="recruit-filter-grid quick-join-grid">
                                <label>
                                    OTT 종류
                                    <select name="ottServiceId" class="form-control ott-service-select" required>
                                        <option value="">OTT 선택</option>

                                <%--
                                    serviceList는 Controller에서 model에 담아준 OTT 목록이다.
                                    value="${service.ottServiceId}" 값이 서버로 넘어간다.
                                --%>
                                    <c:forEach var="service" items="${serviceList}">
                                        <option value="${service.ottServiceId}">
                                            ${service.serviceName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </label>

                            <%--
                                빠른 참가 버튼 클릭 시:
                                /spendolive/ott/recruit/quick-join.do 로 POST 요청이 간다.
                                이때 넘어가는 값은 ottServiceId 하나다.
                            --%>
                                <div class="recruit-search-actions quick-join-actions">
                                    <button type="submit" class="btn btn-primary">
                                        빠른 참가
                                    </button>
                                </div>
                            </div>
                        </form>
                    </article>

                    <br>

                    <article class="card ott-tab-panel recruit-list-panel">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">ALL POSTS</p>
                                <h2>모든 모집글</h2>
                            </div>
                            <span>${fn:length(recruitRoomList)}개</span>
                        </div>

                        <form action="${contextPath}/spendolive/ott/recruit.do" method="get" class="recruit-search-form">
                            <input type="hidden" name="tab" value="all">

                            <label>
                                <span>OTT 종류</span>
                                <select name="ottServiceId">
                                    <option value="">전체 OTT</option>
                                    <c:forEach var="service" items="${serviceList}">
                                        <option value="${service.ottServiceId}" ${selectedOttServiceId eq service.ottServiceId ? 'selected' : ''}>
                                            ${service.serviceName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </label>

                            <label class="recruit-search-keyword">
                                <span>방 제목</span>
                                <input type="text" name="roomNameKeyword" value="${fn:escapeXml(roomNameKeyword)}" placeholder="예) 모집합니다">
                            </label>

                            <div class="recruit-search-actions">
                                <button type="submit" class="btn btn-primary">검색</button>
                                <a href="${contextPath}/spendolive/ott/recruit.do?tab=all" class="btn btn-outline">초기화</a>
                            </div>
                        </form>

                        <c:if test="${not empty selectedOttServiceId or not empty roomNameKeyword}">
                            <div class="recruit-search-result-text">
                                검색 조건에 맞는 모집글 <strong>${fn:length(recruitRoomList)}</strong>개가 조회되었습니다.
                            </div>
                        </c:if>

                        <c:choose>
                            <c:when test="${not empty recruitRoomList}">
                                <div class="recruit-card-grid">
                                    <c:forEach var="room" items="${recruitRoomList}">
                                        <div class="recruit-card ${room.hostMemberId eq loginId ? 'my-recruit-card' : ''}">
                                            <div class="recruit-card-head">
                                                <div>
                                                    <h3>${room.roomName}</h3>
                                                    <p>${room.serviceName} · ${room.planName}</p>
                                                </div>
                                                <div class="recruit-card-badges">
                                                    <c:if test="${room.hostMemberId eq loginId}">
                                                        <span class="owner-badge">내가 만든 방</span>
                                                    </c:if>
                                                    <span class="status-pill ${room.status}">${room.status}</span>
                                                </div>
                                            </div>

                                            <div class="recruit-info-grid">
                                                <div>
                                                    <span>모집 인원</span>
                                                    <strong>${room.currentMemberCount}/${room.memberLimit}명</strong>
                                                </div>
                                                <div>
                                                    <span>월 총액</span>
                                                    <strong><fmt:formatNumber value="${room.totalPrice}" pattern="#,##0" />원</strong>
                                                </div>
                                                <div>
                                                    <span>1인 결제금액</span>
                                                    <strong><fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원</strong>
                                                    <small>분담금 <fmt:formatNumber value="${room.shareAmount}" pattern="#,##0" />원 + 수수료 3%</small>
                                                </div>
                                                <div>
                                                    <span>결제일</span>
                                                    <strong>매월 ${room.billingDay}일</strong>
                                                </div>
                                            </div>

                                            <c:choose>
                                                <c:when test="${room.hostMemberId eq loginId}">
                                                    <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-outline full">내 모집글 대화방</a>
                                                </c:when>
                                                <c:when test="${(room.status eq 'RECRUITING' or room.status eq 'REPLACE_RECRUITING') and room.myApplicationStatus eq 'NONE'}">
                                                    <form action="${contextPath}/payment/detail.do" method="post">
                                                        <input type="hidden" name="roomId" value="${room.roomId}">
                                                        <button type="submit" class="btn btn-primary full">신청하기</button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-outline full" disabled>모집 마감</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-box">
                                    <c:choose>
                                        <c:when test="${not empty selectedOttServiceId or not empty roomNameKeyword}">검색 조건에 맞는 모집글이 없습니다.</c:when>
                                        <c:otherwise>현재 모집글이 없습니다.</c:otherwise>
                                    </c:choose>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </article>
                </c:when>

                <c:when test="${tab eq 'manage' or tab eq 'apply'}">
                    <article class="card ott-tab-panel manage-panel">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">ROOM MANAGEMENT</p>
                                <h2>참여방 관리</h2>
                            </div>
                            <span>내 모집글과 참여방</span>
                        </div>

                        <section class="manage-section">
                            <div class="settlement-sub-header">
                                <div>
                                    <h3>내가 만든방</h3>
                                    <p>내가 만든 모집글 목록과 결제 완료 후 참여 중인 사람을 확인합니다.</p>
                                </div>
                                <span>${fn:length(hostedRoomList)}개</span>
                            </div>

                            <c:choose>
                                <c:when test="${not empty hostedRoomList}">
                                    <div class="ott-room-card-list manage-room-list">
                                        <c:forEach var="room" items="${hostedRoomList}" varStatus="roomStatus">
                                            <div class="ott-room-card manage-room-card">
                                                <div class="room-index-badge">${roomStatus.count}</div>
                                                <div class="family-room-info">
                                                    <strong>${room.roomName}</strong>
                                                    <p>${room.serviceName} · ${room.planName} · ${room.currentMemberCount}/${room.memberLimit}명 · 결제일 매월 ${room.billingDay}일</p>
                                                    <small>1인 결제금액 <fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원</small>
                                                </div>
                                                <div class="family-room-actions">
                                                    <span class="status-pill ${room.status}">${room.status}</span>
                                                    <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-outline btn-mini">대화방</a>
                                                    <c:if test="${room.status ne 'CLOSE_REQUESTED' and room.status ne 'CLOSED'}">
                                                        <form action="${contextPath}/spendolive/ott/room/close-request.do" method="post" class="room-close-form compact-close-form">
                                                            <input type="hidden" name="roomId" value="${room.roomId}">
                                                            <input type="hidden" name="returnPage" value="recruit">
                                                            <input type="hidden" name="closeReason" value="파티장 요청">
                                                            <input type="text" name="closeNotice" placeholder="종료 공지 입력">
                                                            <button type="submit" class="btn btn-outline btn-mini" onclick="return confirm('방 삭제 요청을 진행할까요? 이번 이용 기간 종료일까지 유지되고, 다음 이용분 결제 완료 건은 자동 환불됩니다.');">방 삭제 요청</button>
                                                        </form>
                                                    </c:if>
                                                </div>

                                                <div class="application-mini-list">
                                                    <h4>참여자 현황</h4>
                                                    <c:set var="hasParticipant" value="false" />
                                                    <c:forEach var="member" items="${hostedRoomMemberList}">
                                                        <c:if test="${member.roomId eq room.roomId}">
                                                            <c:set var="hasParticipant" value="true" />
                                                            <div class="apply-manage-row application-row ACTIVE">
                                                                <div>
                                                                    <strong>${member.memberName}</strong>
                                                                    <p>아이디: ${member.memberId} · 참여일 ${member.joinedAt}</p>
                                                                </div>
                                                                <div class="apply-price-box">
                                                                    <span>참여중</span>
                                                                    <b><fmt:formatNumber value="${member.payAmount}" pattern="#,##0" />원</b>
                                                                </div>
                                                            </div>
                                                        </c:if>
                                                    </c:forEach>
                                                    <c:if test="${not hasParticipant}">
                                                        <div class="empty-box">아직 결제 완료 후 참여 중인 사람이 없습니다.</div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-box">내가 만든 모집글이 없습니다.</div>
                                </c:otherwise>
                            </c:choose>
                        </section>

                        <section class="manage-section joined-section">
                            <div class="settlement-sub-header">
                                <div>
                                    <h3>내가 참여한 방</h3>
                                    <p>결제 완료 후 참여 중인 외부 모집글 공유방입니다.</p>
                                </div>
                                <span>${fn:length(joinedRoomList)}개</span>
                            </div>

                            <c:choose>
                                <c:when test="${not empty joinedRoomList}">
                                    <div class="ott-room-card-list manage-room-list">
                                        <c:forEach var="room" items="${joinedRoomList}" varStatus="roomStatus">
                                            <div class="ott-room-card manage-room-card joined-room-card">
                                                <div class="room-index-badge">${roomStatus.count}</div>
                                                <div class="family-room-info">
                                                    <strong>${room.roomName}</strong>
                                                    <p>${room.serviceName} · ${room.planName} · ${room.currentMemberCount}/${room.memberLimit}명 · 결제일 매월 ${room.billingDay}일</p>
                                                    <small>1인 결제금액 <fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원 · 방장 ${room.hostNickname}</small>
                                                </div>
                                                <div class="family-room-actions">
                                                    <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-primary btn-mini">대화방</a>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-box">아직 참여 중인 외부 모집글 공유방이 없습니다.</div>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </article>
                </c:when>

                <c:when test="${tab eq 'settlement'}">
                    <article class="card ott-tab-panel settlement-panel-wide">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">SETTLEMENT & ALERT</p>
                                <h2>정산 및 알림</h2>
                            </div>
                            <span>참여자별 정산 상태 확인</span>
                        </div>

                        <div class="settlement-stack">
                            <section class="settlement-wide-block settlement-request-block">
                                <div class="settlement-sub-header">
                                    <div>
                                        <h3>정산 요청 보내기</h3>
                                        <p>결제 가능 시작일은 전월 결제일, 마감일은 다음 결제일 5일 전으로 자동 계산됩니다.</p>
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
                                                        <input type="hidden" name="returnPage" value="recruit">

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
                                        <div class="empty-box">정산 요청을 보낼 수 있는 내 모집글이 없습니다.</div>
                                    </c:otherwise>
                                </c:choose>
                            </section>

                            <section class="settlement-wide-block settlement-status-block">
                                <div class="settlement-sub-header">
                                    <div>
                                        <h3>내 정산 상태</h3>
                                        <p>내가 만든 모집글 또는 참여 중인 모집글의 정산 진행 상태입니다.</p>
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
                                                            ${settlement.settlementMonth} 이용분 · ${settlement.myRole}<br>
                                                            결제기간 ${settlement.paymentStartDate} ~ ${settlement.paymentCloseDate}<br>
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
                                                                    <input type="hidden" name="returnPage" value="recruit">
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

                                <c:if test="${not empty hostedSettlementPaymentList}">
                                    <div class="team-payment-status-box">
                                        <h3>팀원별 정산 상태</h3>
                                        <div class="team-payment-list">
                                            <c:forEach var="payment" items="${hostedSettlementPaymentList}">
                                                <div class="team-payment-row">
                                                    <span>
                                                        <strong>${payment.roomName}</strong>
                                                        <small>${payment.settlementMonth} 이용분 · ${payment.memberName}(${payment.memberId})</small>
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
                </c:when>

                <c:otherwise>
                    <article class="card ott-tab-panel">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">WRITE POST</p>
                                <h2>모집글 작성</h2>
                            </div>
                            <span>신청 버튼은 결제 화면으로 연결됩니다</span>
                        </div>

                        <form action="${contextPath}/spendolive/ott/recruit/create.do" method="post" class="ott-form-grid wide-form ott-fixed-plan-form" data-room-mode="RECRUIT">
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
                                모집글 제목
                                <input type="text" name="roomName" placeholder="비워두면 OTT - 최고 멤버십 - 모집으로 저장됩니다.">
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
                                <p>추가 계정 비용이 있는 OTT는 기본 구독료에 추가 비용을 더한 뒤 N분의 1로 계산합니다. 서비스 수수료는 3%입니다.</p>
                            </div>

                            <button type="submit" class="btn btn-primary full ott-form-submit">모집글 등록</button>
                        </form>
                    </article>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>
