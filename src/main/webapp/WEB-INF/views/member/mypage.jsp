<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero mypage-hero">
    <div class="container mypage-container">
        <p class="eyebrow">MY PAGE</p>
        <h1>마이페이지</h1>
        <p class="hero-text">프로필, 이번 달 지출 총액, 계좌연동, 신고/차단 관리, 나의 OTT 공유방을 한 화면에서 확인합니다.</p>
    </div>
</section>

<section class="section compact mypage-page">
    <div class="container mypage-container">
        <c:if test="${param.profileUpdated == 'Y'}">
            <div class="mypage-alert done">회원정보가 수정되었습니다.</div>
        </c:if>
        <c:if test="${param.profileError == 'passwordMismatch'}">
            <div class="mypage-alert warn">비밀번호와 비밀번호 확인이 일치하지 않습니다.</div>
        </c:if>
        <c:if test="${param.profileError == 'updateFailed'}">
            <div class="mypage-alert warn">회원정보 수정 중 오류가 발생했습니다. 이메일/전화번호 중복 여부를 확인해 주세요.</div>
        </c:if>

        <div class="mypage-top-grid">
            <article class="card mypage-profile-card">
                <div class="avatar">${profileInitial}</div>
                <div>
                    <p class="eyebrow">PROFILE</p>
                    <h3>${memberInfo.member_name}</h3>
                    <p class="mypage-muted">${memberInfo.nickname} · ${memberInfo.id}</p>
                    <p class="mypage-muted">가입일 ${memberInfo.created_at}</p>
                </div>
                <a href="#profile-edit" class="btn btn-outline full">회원정보 수정</a>
            </article>

            <article class="card mypage-stat-card">
                <p class="eyebrow">MONTHLY EXPENSE</p>
                <h3>이번 달 지출 총액</h3>
                <strong><fmt:formatNumber value="${thisMonthExpenseTotal}" pattern="#,#0" />원</strong>
                <p class="mypage-muted">이번 달 등록된 지출 내역 합계입니다.</p>
                <a href="${contextPath}/spendolive/expense/list.do" class="btn btn-primary full">지출관리로 이동</a>
            </article>

            <article class="card mypage-bank-card">
                <p class="eyebrow">OPEN BANKING</p>
                <h3>계좌관리</h3>
                <c:choose>
                    <c:when test="${accountConnected}">
                        <p class="mypage-muted">연결된 오픈뱅킹 계정</p>
                        <div class="mypage-account-box">
                            <span>사용자번호</span>
                            <strong>${memberInfo.open_bank_user_seq_no}</strong>
                        </div>
                        <a href="${contextPath}/member/openBankingAuth.do" class="btn btn-outline full">계좌 다시 연동하기</a>
                    </c:when>
                    <c:otherwise>
                        <p class="mypage-muted">현재 연결된 계좌가 없습니다.</p>
                        <a href="${contextPath}/member/openBankingAuth.do" class="btn btn-primary full">안전한 오픈뱅킹 계좌 연동하기</a>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="card mypage-report-card">
                <p class="eyebrow">REPORT</p>
                <h3>신고 · 차단관리</h3>
                <div class="mypage-report-summary mypage-report-summary-vertical">
                    <div class="mypage-report-line">
                        <span>내 패널티</span>
                        <strong>${warningCount}번째</strong>
                    </div>
                    <div class="mypage-report-line">
                        <span>내가 신고한 건수</span>
                        <strong>${myReportCount}건</strong>
                    </div>
                </div>
                <a href="#report-manage" class="btn btn-outline full">신고/차단 내역 보기</a>
            </article>
        </div>

        <article id="profile-edit" class="card mypage-panel mypage-profile-edit">
            <div class="mypage-panel-head">
                <div>
                    <p class="eyebrow">EDIT PROFILE</p>
                    <h2>회원정보 수정</h2>
                </div>
                <span>비밀번호 변경 시 확인 입력이 필요합니다.</span>
            </div>

            <form action="${contextPath}/spendolive/mypage/update.do" method="post" class="mypage-edit-form">
                <label>
                    이름
                    <input type="text" name="member_name" value="${memberInfo.member_name}" required>
                </label>
                <label>
                    닉네임
                    <input type="text" name="nickname" value="${memberInfo.nickname}">
                </label>
                <label>
                    이메일
                    <input type="email" name="email" value="${memberInfo.email}" required>
                </label>
                <label>
                    전화번호
                    <input type="text" name="phone" value="${memberInfo.phone}">
                </label>
                <label>
                    새 비밀번호
                    <input type="password" name="password" placeholder="변경할 때만 입력">
                </label>
                <label>
                    새 비밀번호 확인
                    <input type="password" name="passwordConfirm" placeholder="비밀번호 확인">
                </label>
                <div class="mypage-form-actions">
                    <button type="submit" class="btn btn-primary">수정 완료</button>
                    <a href="${contextPath}/spendolive/mypage.do" class="btn btn-outline">취소</a>
                </div>
            </form>
        </article>

        <article id="report-manage" class="card mypage-panel">
            <div class="mypage-panel-head">
                <div>
                    <p class="eyebrow">REPORT HISTORY</p>
                    <h2>신고 · 차단 내역</h2>
                </div>
                <span>내가 신고한 상대와 처리 상태를 확인합니다.</span>
            </div>

            <c:choose>
                <c:when test="${empty myReportList}">
                    <div class="mypage-empty">내가 신고한 내역이 없습니다.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table class="mypage-table">
                            <thead>
                                <tr>
                                    <th>신고 상대</th>
                                    <th>상세 이유</th>
                                    <th>접수 상태</th>
                                    <th>차단 유무</th>
                                    <th>신고일</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="report" items="${myReportList}">
                                    <tr>
                                        <td>
                                            <strong>${report.reportedMemberNickname}</strong>
                                            <small>${report.reportedMemberId}</small>
                                        </td>
                                        <td class="mypage-reason">${report.reportReason}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${report.reportStatus == 'WAIT'}"><span class="chip wait">접수</span></c:when>
                                                <c:when test="${report.reportStatus == 'PROCESSING'}"><span class="chip request">처리중</span></c:when>
                                                <c:when test="${report.reportStatus == 'COMPLETE'}"><span class="chip done">처리완료</span></c:when>
                                                <c:when test="${report.reportStatus == 'REJECT'}"><span class="chip muted-chip">반려</span></c:when>
                                                <c:otherwise><span class="chip muted-chip">${report.reportStatus}</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${report.blockedYn == 'Y'}"><span class="chip done">차단됨</span></c:when>
                                                <c:otherwise><span class="chip muted-chip">미차단</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${report.createdAt}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </article>

        <article class="card mypage-panel">
            <div class="mypage-panel-head">
                <div>
                    <p class="eyebrow">FRIENDS ROOM</p>
                    <h2>가족 · 지인들과의 공유방</h2>
                </div>
                <a href="${contextPath}/spendolive/ott/friends.do" class="btn btn-outline">가족 · 지인 공유방 관리</a>
            </div>

            <c:choose>
                <c:when test="${empty friendRoomList}">
                    <div class="mypage-empty">가족 · 지인들과 만든 공유방이 없습니다.</div>
                </c:when>
                <c:otherwise>
                    <div class="mypage-room-list">
                        <c:forEach var="room" items="${friendRoomList}">
                            <div class="mypage-room-card">
                                <div>
                                    <strong>${room.roomName}</strong>
                                    <p>${room.serviceName} · ${room.planName} · ${room.currentMemberCount}/${room.memberLimit}명</p>
                                    <small>결제일 매월 ${room.billingDay}일 · 상태 ${room.status}</small>
                                </div>
                                <div class="mypage-room-actions">
                                    <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-primary">대화방</a>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </article>

        <article class="card mypage-panel">
            <div class="mypage-panel-head">
                <div>
                    <p class="eyebrow">RECRUIT ROOM</p>
                    <h2>외부인들과의 공유방</h2>
                </div>
                <a href="${contextPath}/spendolive/ott/recruit.do" class="btn btn-outline">모든 모집글 관리</a>
            </div>

            <div class="mypage-room-two-col">
                <section>
                    <h3>내가 만든 방</h3>
                    <c:choose>
                        <c:when test="${empty hostedRecruitRoomList}">
                            <div class="mypage-empty small">내가 만든 외부 모집방이 없습니다.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="mypage-room-list compact">
                                <c:forEach var="room" items="${hostedRecruitRoomList}">
                                    <div class="mypage-room-card">
                                        <div>
                                            <strong>${room.roomName}</strong>
                                            <p>${room.serviceName} · ${room.currentMemberCount}/${room.memberLimit}명</p>
                                            <small>내가 만든 방 · ${room.status}</small>
                                        </div>
                                        <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-primary">대화방</a>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section>
                    <h3>내가 참여한 방</h3>
                    <c:choose>
                        <c:when test="${empty joinedRecruitRoomList}">
                            <div class="mypage-empty small">내가 참여한 외부 모집방이 없습니다.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="mypage-room-list compact">
                                <c:forEach var="room" items="${joinedRecruitRoomList}">
                                    <div class="mypage-room-card">
                                        <div>
                                            <strong>${room.roomName}</strong>
                                            <p>${room.serviceName} · ${room.currentMemberCount}/${room.memberLimit}명</p>
                                            <small>참여중 · 방장 ${room.hostNickname}</small>
                                        </div>
                                        <a href="${contextPath}/spendolive/ott/chat/room.do?roomId=${room.roomId}" class="btn btn-primary">대화방</a>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </article>
    </div>
</section>
