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
            가족 또는 지인과 함께 쓸 OTT 공유방을 만들고, 서비스 종류별 공유 정보와 정산 상태를 관리합니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott.do" class="btn btn-outline">OTT관리로 돌아가기</a>
            <a href="#createRoom" class="btn btn-primary">새 공유방 만들기</a>
        </div>
    </div>
</section>

<section class="section compact ott-page-section">
    <div class="container ott-wide-container">
        <div class="ott-room-layout">
            <article id="createRoom" class="card ott-form-card">
                <div class="panel-header">
                    <div>
                        <p class="eyebrow">CREATE ROOM</p>
                        <h2>공유방 만들기</h2>
                    </div>
                    <span>지인 초대형</span>
                </div>

                <form action="${contextPath}/spendolive/ott/friends/create.do" method="post" class="ott-form-grid">
                    <label>
                        OTT 종류
                        <select name="ottServiceId" required>
                            <option value="">선택</option>
                            <c:forEach var="service" items="${serviceList}">
                                <option value="${service.ottServiceId}">${service.serviceName}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label>
                        구독 종류
                        <input type="text" name="planName" placeholder="예: 프리미엄, 스탠다드" value="프리미엄">
                    </label>

                    <label>
                        공유방 이름
                        <input type="text" name="roomName" placeholder="예: 우리 가족 Netflix 공유방">
                    </label>

                    <label>
                        전체 구독료
                        <input type="number" name="totalPrice" min="0" placeholder="예: 17000" required>
                    </label>

                    <label>
                        결제일
                        <input type="number" name="billingDay" min="1" max="31" value="1" required>
                    </label>

                    <label>
                        최대 인원
                        <input type="number" name="memberLimit" min="2" max="10" value="4" required>
                    </label>

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
                                        <p>${room.serviceName} · ${room.currentMemberCount}/${room.memberLimit}명 · 결제일 매월 ${room.billingDay}일</p>
                                    </div>
                                    <div class="ott-room-meta">
                                        <span class="status-pill ${room.status}">${room.status}</span>
                                        <b><fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원 / 1인</b>
                                        <small>초대코드 ${room.inviteCode}</small>
                                        <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-outline btn-mini">대화방</a>
                                    </div>
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

        <div class="ott-extra-grid ott-friends-extra">
            <article class="card status-box">
                <h3>정산 요청</h3>
                <p class="card-desc">내가 방장인 공유방에 대해 이번 달 정산 요청을 만들 수 있습니다.</p>

                <c:choose>
                    <c:when test="${not empty hostedRoomList}">
                        <div class="settlement-request-list">
                            <c:forEach var="room" items="${hostedRoomList}">
                                <form action="${contextPath}/spendolive/ott/settlement/request.do" method="post" class="settlement-request-row">
                                    <input type="hidden" name="roomId" value="${room.roomId}">
                                    <input type="hidden" name="returnPage" value="friends">
                                    <strong>${room.roomName}</strong>
                                    <input type="month" name="settlementMonth" value="${selectedSettlementMonth}">
                                    <input type="date" name="dueDate" value="${defaultDueDate}">
                                    <button type="submit" class="btn btn-outline">정산 요청</button>
                                </form>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-box">방장으로 만든 공유방이 없습니다.</div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="card status-box">
                <h3>정산 상태</h3>
                <c:choose>
                    <c:when test="${not empty settlementList}">
                        <div class="status-list">
                            <c:forEach var="settlement" items="${settlementList}">
                                <div class="status-row wide">
                                    <span>
                                        ${settlement.roomName}<br>
                                        <small>${settlement.settlementMonth} · 마감 ${settlement.dueDate}</small>
                                    </span>
                                    <em class="${settlement.status eq 'DONE' ? 'done' : 'wait'}">${settlement.status}</em>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-box">아직 생성된 정산 요청이 없습니다.</div>
                    </c:otherwise>
                </c:choose>
            </article>
        </div>
    </div>
</section>
