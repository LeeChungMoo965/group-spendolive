<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero ott-sub-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">PUBLIC RECRUIT</p>
        <h1>모든 모집글</h1>
        <p class="hero-text">
            공개 모집 중인 OTT 공유방을 확인하고, 신청한 사람은 파티장이 수락해야만 공유방에 참여할 수 있습니다.
        </p>
        <div class="ott-page-actions">
            <a href="${contextPath}/spendolive/ott.do" class="btn btn-outline">OTT관리로 돌아가기</a>
            <a href="${contextPath}/spendolive/ott/recruit.do?tab=write" class="btn btn-primary">모집글 작성</a>
        </div>
    </div>
</section>

<section class="section compact ott-page-section">
    <div class="container ott-wide-container">
        <article class="card recruit-board-card wide-board-card">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">ALL POSTS</p>
                    <h2>모든 모집글</h2>
                </div>
                <span>${fn:length(recruitRoomList)}개 모집 중</span>
            </div>

            <c:choose>
                <c:when test="${not empty recruitRoomList}">
                    <div class="recruit-list">
                        <c:forEach var="room" items="${recruitRoomList}">
                            <div class="recruit-item">
                                <div class="recruit-main-info">
                                    <strong>${room.roomName}</strong>
                                    <p>
                                        ${room.serviceName} · 모집인원 ${room.currentMemberCount}/${room.memberLimit}명 ·
                                        1인당 <fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원
                                    </p>
                                    <small>방장 ${room.hostNickname} · 결제일 매월 ${room.billingDay}일</small>
                                </div>

                                <form action="${contextPath}/spendolive/ott/recruit/apply.do" method="post" class="recruit-action-form">
                                    <input type="hidden" name="roomId" value="${room.roomId}">
                                    <c:choose>
                                        <c:when test="${memberInfo.id eq room.hostMemberId}">
                                            <button type="button" class="btn btn-outline" disabled>내 모집글</button>
                                        </c:when>
                                        <c:when test="${room.myApplicationStatus eq 'APPLIED'}">
                                            <button type="button" class="btn btn-outline" disabled>승인대기</button>
                                        </c:when>
                                        <c:when test="${room.myApplicationStatus eq 'ACTIVE'}">
                                            <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-primary">대화방</a>
                                        </c:when>
                                        <c:when test="${room.currentMemberCount ge room.memberLimit}">
                                            <button type="button" class="btn btn-outline" disabled>모집완료</button>
                                        </c:when>
                                        <c:when test="${room.myApplicationStatus eq 'REJECTED'}">
                                            <button type="submit" class="btn btn-primary">다시 신청</button>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="submit" class="btn btn-primary">신청하기</button>
                                        </c:otherwise>
                                    </c:choose>
                                </form>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-box">현재 모집 중인 글이 없습니다. 아래 탭에서 직접 모집글을 작성해보세요.</div>
                </c:otherwise>
            </c:choose>
        </article>

        <div class="ott-tab-wrap">
            <div class="ott-tabs">
                <a href="${contextPath}/spendolive/ott/recruit.do?tab=write" class="${tab eq 'write' ? 'active' : ''}">모집글 작성</a>
                <a href="${contextPath}/spendolive/ott/recruit.do?tab=apply" class="${tab eq 'apply' ? 'active' : ''}">신청관리</a>
                <a href="${contextPath}/spendolive/ott/recruit.do?tab=settlement" class="${tab eq 'settlement' ? 'active' : ''}">정산, 알림</a>
            </div>

            <c:choose>
                <c:when test="${tab eq 'apply'}">
                    <article class="card ott-tab-panel">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">APPLICATIONS</p>
                                <h2>신청관리</h2>
                            </div>
                            <span>신청자는 아이디와 성함 기준으로 확인</span>
                        </div>

                        <c:choose>
                            <c:when test="${not empty hostedRoomMemberList}">
                                <div class="apply-manage-list">
                                    <c:forEach var="member" items="${hostedRoomMemberList}">
                                        <div class="apply-manage-row application-row ${member.status}">
                                            <div>
                                                <strong>${member.roomName}</strong>
                                                <p>
                                                    신청자 아이디: ${member.memberId} · 성함: ${member.memberName}
                                                </p>
                                                <small>
                                                    신청일 ${member.joinedAt} · 상태 ${member.status}
                                                </small>
                                            </div>

                                            <c:choose>
                                                <c:when test="${member.status eq 'APPLIED'}">
                                                    <div class="application-actions">
                                                        <form action="${contextPath}/spendolive/ott/recruit/application/approve.do" method="post">
                                                            <input type="hidden" name="roomMemberId" value="${member.roomMemberId}">
                                                            <button type="submit" class="btn btn-primary">수락</button>
                                                        </form>
                                                        <form action="${contextPath}/spendolive/ott/recruit/application/reject.do" method="post">
                                                            <input type="hidden" name="roomMemberId" value="${member.roomMemberId}">
                                                            <button type="submit" class="btn btn-outline">거절</button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="apply-price-box">
                                                        <span>${member.status}</span>
                                                        <b><fmt:formatNumber value="${member.payAmount}" pattern="#,##0" />원</b>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-box">아직 내 모집글에 신청한 사람이 없습니다.</div>
                            </c:otherwise>
                        </c:choose>
                    </article>
                </c:when>

                <c:when test="${tab eq 'settlement'}">
                    <article class="card ott-tab-panel settlement-panel-wide">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">SETTLEMENT & ALERT</p>
                                <h2>정산, 알림</h2>
                            </div>
                            <span>수락된 참여자에게만 정산 요청 보내기</span>
                        </div>

                        <div class="settlement-stack">
                            <section class="settlement-wide-block settlement-request-block">
                                <div class="settlement-sub-header">
                                    <div>
                                        <h3>정산 요청 보내기</h3>
                                        <p>내가 파티장인 모집글 중 수락된 참여자가 있는 방에만 정산 요청을 보낼 수 있습니다.</p>
                                    </div>
                                    <span>요청월 · 마감일 선택</span>
                                </div>

                                <c:choose>
                                    <c:when test="${not empty hostedRoomList}">
                                        <div class="settlement-request-list settlement-wide-list">
                                            <c:forEach var="room" items="${hostedRoomList}">
                                                <form action="${contextPath}/spendolive/ott/settlement/request.do" method="post" class="settlement-request-row wide-settlement-row">
                                                    <input type="hidden" name="roomId" value="${room.roomId}">
                                                    <input type="hidden" name="returnPage" value="recruit">

                                                    <div class="settlement-room-title">
                                                        <strong>${room.roomName}</strong>
                                                        <small>${room.serviceName} · 참여 ${room.currentMemberCount}/${room.memberLimit}명 · 1인당 <fmt:formatNumber value="${room.perPersonAmount}" pattern="#,##0" />원</small>
                                                    </div>

                                                    <label class="settlement-field">
                                                        <span>정산월</span>
                                                        <input type="month" name="settlementMonth" value="${selectedSettlementMonth}">
                                                    </label>

                                                    <label class="settlement-field">
                                                        <span>마감일</span>
                                                        <input type="date" name="dueDate" value="${defaultDueDate}">
                                                    </label>

                                                    <button type="submit" class="btn btn-primary settlement-send-btn">정산 요청 보내기</button>
                                                </form>
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
                                        <h3>정산 상태</h3>
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
                                                        <small>${settlement.settlementMonth} · ${settlement.myRole}</small>
                                                    </span>
                                                    <em class="${settlement.status eq 'DONE' ? 'done' : 'wait'}">${settlement.status}</em>
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
                </c:when>

                <c:otherwise>
                    <article class="card ott-tab-panel">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">WRITE POST</p>
                                <h2>모집글 작성</h2>
                            </div>
                            <span>모집글은 수락 방식으로 운영됩니다</span>
                        </div>

                        <form action="${contextPath}/spendolive/ott/recruit/create.do" method="post" class="ott-form-grid wide-form">
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
                                모집글 제목
                                <input type="text" name="roomName" placeholder="비워두면 OTT - 구독종류 - 모집으로 저장됩니다.">
                            </label>

                            <label>
                                전체 구독료
                                <input type="number" name="totalPrice" min="0" placeholder="예: 17000" required>
                            </label>

                            <label>
                                모집 인원
                                <input type="number" name="memberLimit" min="2" max="10" value="4" required>
                            </label>

                            <label>
                                결제일
                                <input type="number" name="billingDay" min="1" max="31" value="1" required>
                            </label>

                            <button type="submit" class="btn btn-primary full ott-form-submit">모집글 등록</button>
                        </form>
                    </article>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>
