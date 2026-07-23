<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero">
    <div class="container">
        <p class="eyebrow">MY PAGE</p>
        <h1>마이페이지</h1>
        <p class="hero-text">프로필, 이번 달 지출 총액, 계좌연동, 신고/차단 관리, 나의 OTT 공유방을 한 화면에서 확인합니다.</p>
    </div>
</section>

<section class="section compact mypage-page">
    <div class="container">
        <c:if test="${param.profileUpdated == 'Y'}">
            <div class="alert done">회원정보가 수정되었습니다.</div>
        </c:if>
        <c:if test="${param.profileError == 'passwordMismatch'}">
            <div class="alert warn">새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.</div>
        </c:if>
        <c:if test="${param.profileError == 'currentPasswordMismatch'}">
            <div class="alert warn">현재 비밀번호가 일치하지 않습니다.</div>
        </c:if>
        <c:if test="${param.profileError == 'passwordCheckRequired'}">
            <div class="alert warn">비밀번호 변경 전 확인 버튼을 눌러주세요.</div>
        </c:if>
        <c:if test="${param.profileError == 'emailNotVerified'}">
            <div class="alert warn">이메일을 변경하려면 이메일 인증을 완료해야 합니다.</div>
        </c:if>
        <c:if test="${param.profileError == 'phoneNotVerified'}">
            <div class="alert warn">전화번호를 변경하려면 전화번호 인증을 완료해야 합니다.</div>
        </c:if>
        <c:if test="${param.profileError == 'updateFailed'}">
            <div class="alert warn">회원정보 수정 중 오류가 발생했습니다. 이메일/전화번호 중복 여부를 확인해 주세요.</div>
        </c:if>
        <%-- =====================================================
             [마이페이지 계좌·카드 연결 추가]
             계좌 제목 수정 결과 메시지
             ===================================================== --%>
        <c:if test="${param.accountNameUpdated == 'Y'}">
            <div class="alert done">계좌 제목이 수정되었습니다.</div>
        </c:if>
        <c:if test="${param.assetError == 'invalidAccountName'}">
            <div class="alert warn">계좌 제목은 1자 이상 20자 이하로 입력해주세요.</div>
        </c:if>
        <c:if test="${param.assetError == 'accountNameUpdateFailed'}">
            <div class="alert warn">계좌 제목 수정 중 오류가 발생했습니다.</div>
        </c:if>
        <%-- 주계좌 변경 결과 메시지 --%>
        <c:if test="${param.primaryAccountUpdated == 'Y'}">
            <div class="alert done">주계좌가 변경되었습니다.</div>
        </c:if>
        <c:if test="${param.assetError == 'primaryAccountUpdateFailed'}">
            <div class="alert warn">주계좌 변경 중 오류가 발생했습니다.</div>
        </c:if>

        <c:if test="${param.withdrawError == 'confirmRequired'}">
            <div class="alert warn">회원탈퇴를 진행하려면 확인 문구를 정확히 입력해주세요.</div>
        </c:if>
        <c:if test="${param.withdrawError == 'failed'}">
            <div class="alert warn">회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요.</div>
        </c:if>

        <div class="mypage-top-grid">
            <article class="card mypage-profile-card">
                <p class="eyebrow">PROFILE</p>
                <div class="avatar">${profileInitial}</div>
                <div>
                    <h3>${memberInfo.member_name}</h3>
                    <p class="mypage-muted">닉네임 : ${memberInfo.nickname}</p>
                    <p class="mypage-muted">아이디 : ${memberInfo.id}</p>
                    <p class="mypage-muted">가입일 : ${memberInfo.created_at}</p>
                </div>
                <%-- [마이페이지 화면 전환 추가] 최초에는 숨기고 버튼을 눌렀을 때 회원정보 수정 영역 표시 --%>
                <button type="button" class="btn btn-primary full" onclick="showMyPagePanel('profile-edit')">회원정보 수정</button>
            </article>

            <article class="card mypage-stat-card">
                <p class="eyebrow">MONTHLY EXPENSE</p>
                <h3>이번 달 지출 총액</h3>
                <strong class="mypage-monthly-expense-amount">
                    <fmt:formatNumber value="${thisMonthExpenseTotal}" pattern="#,##0" />원
                </strong>

                <%-- 이번 달 예산과 지출 대비 사용률을 표시한다. --%>
                <div class="mypage-budget-summary">
                    <div>
                        <span>이번 달 예산</span>
                        <c:choose>
                            <c:when test="${thisMonthBudget > 0}">
                                <strong>
                                    <fmt:formatNumber value="${thisMonthBudget}" pattern="#,##0" />원
                                </strong>
                            </c:when>
                            <c:otherwise>
                                <strong>미설정</strong>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:if test="${thisMonthBudget > 0}">
                        <div>
                            <span>예산 사용률</span>
                            <strong>${thisMonthBudgetPercent}%</strong>
                        </div>
                    </c:if>
                </div>

                <p class="mypage-muted">이번 달 등록된 지출 내역과 설정 예산입니다.</p>
                <a href="${contextPath}/spendolive/expense/list.do" class="btn btn-primary full">지출관리로 이동</a>
            </article>

            <%-- =====================================================
                 [마이페이지 주계좌 표시 추가]
                 STATUS가 YES인 계좌만 상단 계좌관리 카드에 표시한다.
                 연결 계좌는 있지만 주계좌가 없으면 목록에서 선택하도록 안내한다.
                 ===================================================== --%>
            <article class="card mypage-bank-card">
                <p class="eyebrow">OPEN BANKING</p>
                <h3>계좌관리</h3>
                <c:choose>
                    <c:when test="${not empty currentAccount}">
                        <c:set var="currentBankName" value="${bankNameMap[currentAccount.bank_code]}" />
                        <p class="mypage-muted">현재 주계좌</p>
                        <div class="mypage-account-box">
                            <span><c:out value="${empty currentAccount.account_name ? '주계좌' : currentAccount.account_name}" /></span>
                            <strong>
                                <c:out value="${empty currentBankName ? currentAccount.bank_code : currentBankName}" />
                                · <c:out value="${currentAccount.account_number}" />
                            </strong>
                            <span>잔여금</span>
                            <strong><fmt:formatNumber value="${currentAccount.balance}" pattern="#,##0" />원</strong>
                        </div>
                    </c:when>
                    <c:when test="${not empty accountList}">
                        <p class="mypage-muted">연결된 계좌 중 주계좌를 선택해주세요.</p>
                        <div class="mypage-account-box">
                            <span>현재 주계좌</span>
                            <strong>설정된 주계좌가 없습니다.</strong>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="mypage-muted">현재 연결된 계좌가 없습니다.</p>
                        <div class="mypage-account-box">
                            <span>현재 주계좌</span>
                            <strong>계좌 정보가 없습니다.</strong>
                        </div>
                    </c:otherwise>
                </c:choose>
                <button type="button" class="btn btn-primary full" onclick="showMyPagePanel('asset-manage')">나의 계좌 · 카드 목록보기</button>
            </article>

            <article class="card mypage-report-card">
                <p class="eyebrow">REPORT</p>
                <h3>신고 · 차단관리</h3>
                <div class="mypage-report-summary mypage-report-summary-vertical">
                    <div class="mypage-report-line">
                        <span>내 패널티</span>
                        <strong>${warning_count}번째</strong>
                    </div>
                    <div class="mypage-report-line">
                        <span>내가 신고한 건수</span>
                        <strong>${myReportCount}건</strong>
                    </div>
                </div>
                <%-- [마이페이지 화면 전환 추가] 버튼을 눌렀을 때 신고·차단 영역 표시 --%>
                <button type="button" class="btn btn-primary full" onclick="showMyPagePanel('report-manage')">신고/차단 내역 보기</button>
            </article>
        </div>

        <%-- [마이페이지 화면 전환 추가] 최초 진입 시 숨겨지는 회원정보 수정 영역 --%>
        <article id="profile-edit" class="card mypage-panel mypage-profile-edit mypage-toggle-panel is-hidden">
            <div class="mypage-panel-head">
                <div>
                    <p class="eyebrow">EDIT PROFILE</p>
                    <h2>회원정보 수정</h2>
                </div>
                <span>비밀번호 변경 시 확인 입력이 필요합니다.</span>
            </div>

            <form action="${contextPath}/spendolive/mypage/update.do" method="post" class="mypage-edit-form" id="mypageProfileForm">
                <input type="hidden" id="originalEmail" value="${memberInfo.email}">
                <input type="hidden" id="originalPhone" value="${memberInfo.phone}">
                <input type="hidden" id="emailVerified" value="N">
                <input type="hidden" id="phoneVerified" value="N">
                <input type="hidden" id="passwordChecked" name="passwordChecked" value="N">

                <div class="mypage-form-section">
                    <div class="mypage-form-section-head">
                        <h3>기본 정보</h3>
                        <p>이름과 닉네임을 수정합니다.</p>
                    </div>
                    <div class="mypage-form-stack">
                        <label class="mypage-field">
                            이름
                            <input type="text" name="member_name" value="${memberInfo.member_name}" required>
                        </label>
                        <label class="mypage-field">
                            닉네임
                            <input type="text" name="nickname" value="${memberInfo.nickname}">
                        </label>
                    </div>
                </div>

                <div class="mypage-form-section">
                    <div class="mypage-form-section-head">
                        <h3>연락처 인증</h3>
                        <p>이메일 또는 전화번호를 바꿀 때만 인증을 진행하면 됩니다.</p>
                    </div>

                    <div class="mypage-verify-group">
                        <div class="mypage-field-with-button">
                            <label class="mypage-field">
                                이메일
                                <input type="email" name="email" id="mypageEmail" value="${memberInfo.email}" required>
                            </label>
                            <button type="button" class="btn btn-primary full" onclick="sendMyPageEmailCode()">이메일 인증</button>
                        </div>
                        <div class="mypage-code-row">
                            <input type="text" id="mypageEmailCode" placeholder="이메일 인증번호 입력">
                            <button type="button" class="btn btn-primary full" onclick="verifyMyPageEmailCode()">확인</button>
                        </div>
                        <p class="mypage-help" id="emailVerifyMessage">이메일을 변경할 때만 인증이 필요합니다.</p>
                    </div>

                    <div class="mypage-verify-group">
                        <div class="mypage-field-with-button">
                            <label class="mypage-field">
                                전화번호
                                <input type="text" name="phone" id="mypagePhone" value="${memberInfo.phone}">
                            </label>
                            <button type="button" class="btn btn-primary full" onclick="sendMyPagePhoneCode()">전화번호 인증</button>
                        </div>
                        <div class="mypage-code-row">
                            <input type="text" id="mypagePhoneCode" placeholder="문자 인증번호 입력">
                            <button type="button" class="btn btn-primary full" onclick="verifyMyPagePhoneCode()">확인</button>
                        </div>
                        <p class="mypage-help" id="phoneVerifyMessage">전화번호를 변경할 때만 인증이 필요합니다.</p>
                    </div>
                </div>

                <div class="mypage-form-section">
                    <div class="mypage-form-section-head">
                        <h3>비밀번호 변경</h3>
                        <p>비밀번호를 바꾸지 않을 경우 아래 입력칸은 비워두면 됩니다.</p>
                    </div>
                    <div class="mypage-form-stack">
                        <label class="mypage-field">
                            현재 비밀번호
                            <input type="password" name="currentPassword" id="currentPassword" placeholder="비밀번호 변경 시 입력">
                        </label>
                        <label class="mypage-field">
                            새 비밀번호
                            <input type="password" name="password" id="newPassword" placeholder="변경할 때만 입력">
                        </label>
                        <div class="mypage-verify-group">
                            <div class="mypage-field-with-button">
                                <label class="mypage-field">
                                    새 비밀번호 확인
                                    <input type="password" name="passwordConfirm" id="passwordConfirm" placeholder="새 비밀번호 재입력">
                                </label>
                                <button type="button" class="btn btn-primary full" onclick="checkMyPagePassword()">비밀번호 확인</button>
                            </div>
                            <p class="mypage-help" id="passwordCheckMessage">비밀번호를 변경할 때는 현재 비밀번호와 새 비밀번호 확인이 필요합니다.</p>
                        </div>
                    </div>
                </div>

                <div class="mypage-form-actions">
                    <button type="submit" class="btn btn-primary">수정 완료</button>
                    <a href="${contextPath}/spendolive/mypage.do" class="btn btn-danger-outline">취소</a>
                </div>
            </form>
        </article>

        <%-- [마이페이지 화면 전환 추가] 최초 진입 시 숨겨지는 신고·차단 내역 영역 --%>
        <article id="report-manage" class="card mypage-panel mypage-toggle-panel is-hidden">
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

                                            <strong>${report.reported_member_nickname}</strong>
                                            <small>${report.reported_member_id}</small>

                                        </td>
                                        <td class="mypage-reason">${report.report_reason}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${report.report_status == 'WAIT'}"><span class="chip wait">접수</span></c:when>
                                                <c:when test="${report.report_status == 'PROCESSING'}"><span class="chip request">처리중</span></c:when>
                                                <c:when test="${report.report_status == 'COMPLETE'}"><span class="chip done">처리완료</span></c:when>
                                                <c:when test="${report.report_status == 'REJECT'}"><span class="chip muted-chip">반려</span></c:when>
                                                <c:otherwise><span class="chip muted-chip">${report.report_status}</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${report.blocked_yn == 'Y'}"><span class="chip done">차단됨</span></c:when>
                                                <c:otherwise><span class="chip muted-chip">미차단</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${report.created_at}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </article>

        <%-- =====================================================
             [마이페이지 계좌·카드 연결 추가 시작]
             담당자가 만든 조회 결과를 계좌와 카드로 나누어 출력한다.
             한 페이지에 각각 4칸을 보여주며 부족한 칸은 JavaScript가 빈 칸으로 채운다.
             계좌의 수정 버튼은 ACCOUNT_NAME을 변경하고 거래내역 버튼은 Ajax로 상세 내역을 조회한다.
             ===================================================== --%>
        <article id="asset-manage" class="card mypage-panel mypage-toggle-panel is-hidden">
            <div class="mypage-panel-head">
                <div>
                    <p class="eyebrow">MY ASSETS</p>
                    <h2>나의 계좌 · 카드 목록</h2>
                </div>
                <span>계좌와 카드를 각각 한 화면에 4개씩 확인합니다.</span>
            </div>

            <section class="mypage-asset-section">
                <div class="mypage-asset-section-head">
                    <h3>계좌</h3>
                    <div class="mypage-asset-pager" data-pager-for="accountAssetList">
                        <button type="button" class="btn btn-outline btn-mini" data-page-direction="prev">이전</button>
                        <span><b data-current-page>1</b> / <b data-total-page>1</b></span>
                        <button type="button" class="btn btn-outline btn-mini" data-page-direction="next">다음</button>
                    </div>
                </div>

                <div class="mypage-asset-list" id="accountAssetList" data-empty-text="계좌 정보가 없습니다.">
                    <c:forEach var="account" items="${accountList}">
                        <c:set var="accountBankName" value="${bankNameMap[account.bank_code]}" />
                        <div class="mypage-asset-item" data-asset-item>
                            <div class="mypage-asset-main">
                                <form action="${contextPath}/spendolive/mypage/account/name/update.do" method="post" class="mypage-asset-title-form">
                                    <input type="hidden" name="accountIdx" value="${account.account_idx}">
                                    <input type="text" name="accountName" maxlength="20" readonly
                                           value="${fn:escapeXml(empty account.account_name ? '계좌' : account.account_name)}"
                                           aria-label="계좌 제목">
                                    <button type="button" class="btn btn-outline btn-mini" onclick="toggleAccountNameEdit(this)">수정</button>
                                </form>
                                <p>
                                    <c:out value="${empty accountBankName ? account.bank_code : accountBankName}" />
                                    계좌번호 - <c:out value="${account.account_number}" />
                                </p>
                            </div>
                            <div class="mypage-account-balance">
                                <span>남은 금액</span>
                                <strong><fmt:formatNumber value="${account.balance}" pattern="#,##0" />원</strong>
                            </div>
                            <%-- 기존 버튼 스타일을 활용해 주계좌 선택과 거래내역 버튼을 함께 표시한다. --%>
                            <div class="mypage-room-actions">
                                <c:choose>
                                    <c:when test="${account.status eq 'YES'}">
                                        <span class="chip done">주계좌</span>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${contextPath}/spendolive/mypage/account/primary/update.do" method="post">
                                            <input type="hidden" name="accountIdx" value="${account.account_idx}">
                                            <button type="submit" class="btn btn-primary btn-mini"
                                                    onclick="return confirm('이 계좌를 주계좌로 설정할까요?');">주계좌로 설정</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                                <button type="button"
                                        class="btn btn-outline btn-mini transaction-history-btn"
                                        data-account-idx="${account.account_idx}"
                                        data-account-name="${fn:escapeXml(empty account.account_name ? '계좌' : account.account_name)}"
                                        data-bank-name="${fn:escapeXml(empty accountBankName ? account.bank_code : accountBankName)}"
                                        data-account-number="${fn:escapeXml(account.account_number)}"
                                        data-current-balance="${account.balance}">거래내역</button>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <%-- 선택한 계좌의 거래내역과 거래 직후 잔액을 표시한다. --%>
                <div id="accountTransactionPanel" class="mypage-form-section is-hidden" hidden>
                    <div class="mypage-form-section-head">
                        <div>
                            <h3 id="accountTransactionTitle">계좌 거래내역</h3>
                            <p id="accountTransactionAccountInfo">계좌를 선택해주세요.</p>
                        </div>
                        <button type="button" class="btn btn-outline btn-mini" onclick="closeAccountTransactions()">닫기</button>
                    </div>

                    <div class="mypage-account-box">
                        <span>현재 잔액</span>
                        <strong id="accountTransactionCurrentBalance">0원</strong>
                    </div>

                    <div class="table-wrap">
                        <table class="mypage-table">
                            <thead>
                                <tr>
                                    <th>거래일시</th>
                                    <th>구분</th>
                                    <th>거래금액</th>
                                    <th>거래 후 잔액</th>
                                </tr>
                            </thead>
                            <tbody id="accountTransactionBody">
                                <tr>
                                    <td colspan="4">거래내역 버튼을 눌러주세요.</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="mypage-account-box">
                        <span>계좌 등록 당시 잔액</span>
                        <strong id="accountTransactionInitialBalance">0원</strong>
                    </div>
                </div>
            </section>

            <section class="mypage-asset-section">
                <div class="mypage-asset-section-head">
                    <h3>카드</h3>
                    <div class="mypage-asset-pager" data-pager-for="cardAssetList">
                        <button type="button" class="btn btn-outline btn-mini" data-page-direction="prev">이전</button>
                        <span><b data-current-page>1</b> / <b data-total-page>1</b></span>
                        <button type="button" class="btn btn-outline btn-mini" data-page-direction="next">다음</button>
                    </div>
                </div>

                <div class="mypage-asset-list" id="cardAssetList" data-empty-text="카드 정보가 없습니다.">
                    <c:forEach var="card" items="${cardList}">
                        <div class="mypage-asset-item mypage-card-item" data-asset-item>
                            <div class="mypage-asset-main">
                                <strong><c:out value="${empty card.card_company ? '카드' : card.card_company}" /></strong>
                                <p>
                                    <c:out value="${empty card.card_company ? '카드' : card.card_company}" />
                                    카드번호 - <c:out value="${card.card_number}" />
                                </p>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </section>
        </article>
        <%-- [마이페이지 계좌·카드 연결 추가 끝] --%>

        <article class="card mypage-panel">
            <div class="mypage-panel-head">
                <div>
                    <p class="eyebrow">FRIENDS ROOM</p>
                    <h2>가족 · 지인들과의 공유방</h2>
                </div>
                <a href="${contextPath}/spendolive/ott/friends.do" class="btn btn-primary">가족 · 지인 공유방 관리</a>
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
                                    <strong>${room.room_name}</strong>
                                    <p>${room.service_name} · ${room.plan_name} · ${room.current_member_count}/${room.member_limit}명</p>
                                    <small>결제일 매월 ${room.billing_day}일 · 상태 ${room.status}</small>
                                </div>
                                <div class="mypage-room-actions">
                                    <a href="${contextPath}/spendolive/ott/chat/room.do?room_id=${room.room_id}" class="btn btn-primary full">대화방</a>
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
                <a href="${contextPath}/spendolive/ott/recruit.do" class="btn btn-primary">모든 모집글 관리</a>
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
                                            <strong>${room.room_name}</strong>
                                            <p>${room.service_name} · ${room.current_member_count}/${room.member_limit}명</p>
                                            <small>내가 만든 방 · ${room.status}</small>
                                        </div>
                                        <a href="${contextPath}/spendolive/ott/chat/room.do?room_id=${room.room_id}" class="btn btn-primary">대화방</a>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section>
                    <h3>내가 신청/참여한 방</h3>
                    <c:choose>
                        <c:when test="${empty joinedRecruitRoomList}">
                            <div class="mypage-empty small">내가 신청하거나 참여한 외부 모집방이 없습니다.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="mypage-room-list compact">
                                <c:forEach var="room" items="${joinedRecruitRoomList}">
                                    <div class="mypage-room-card">
                                        <div>
                                            <strong>${room.room_name}</strong>
                                            <p>${room.service_name} · ${room.current_member_count}/${room.member_limit}명</p>
                                            <small>방장 ${room.host_nickname}</small>
                                            <c:choose>
                                                <c:when test="${room.my_application_status eq 'APPLIED'}">
                                                    <span class="status-pill APPLIED">승인 대기중</span>
                                                </c:when>
                                                <c:when test="${room.my_application_status eq 'REJECTED'}">
                                                    <span class="status-pill REJECTED">거절됨</span>
                                                </c:when>
                                                <c:when test="${room.my_application_status eq 'ACTIVE'}">
                                                    <span class="status-pill ACTIVE">참여중</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-pill ACTIVE">참여중</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <c:if test="${room.my_application_status eq 'ACTIVE' or empty room.my_application_status}">
                                            <a href="${contextPath}/spendolive/ott/chat/room.do?room_id=${room.room_id}" class="btn btn-primary">대화방</a>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </article>

        <div id="withdraw-section" class="withdraw-row">
            <button type="button" class="btn btn-danger-outline" onclick="openWithdrawModal()">회원탈퇴</button>
        </div>

    </div>
</section>

<div class="withdraw-modal" id="withdrawModal" aria-hidden="true">
    <div class="withdraw-modal-box">
        <button type="button" class="withdraw-close" onclick="closeWithdrawModal()" aria-label="회원탈퇴 창 닫기">×</button>
        <p class="eyebrow">ACCOUNT DELETE</p>
        <h2>회원탈퇴</h2>
        <p class="mypage-muted">회원탈퇴를 하면 현재 계정으로 다시 로그인할 수 없습니다. 오픈뱅킹 연결 정보도 함께 해제됩니다.</p>
        <ul class="withdraw-list">
            <li>회원 상태가 탈퇴 상태로 변경됩니다.</li>
            <li>로그인 세션이 즉시 종료됩니다.</li>
            <li>기존 지출/정산 이력은 서비스 기록 보존을 위해 바로 삭제하지 않습니다.</li>
        </ul>
        <form action="${contextPath}/spendolive/mypage/withdraw.do" method="post" id="withdrawForm">
            <label class="mypage-field">
                확인 문구 입력
                <input type="text" name="withdrawConfirm" id="withdrawConfirm" placeholder="탈퇴합니다">
            </label>
            <p class="mypage-help warn">위 입력칸에 <strong>탈퇴합니다</strong>를 정확히 입력해야 탈퇴할 수 있습니다.</p>
            <div class="withdraw-actions">
                <button type="button" class="btn btn-primary btn-mini" onclick="closeWithdrawModal()">취소</button>
                <button type="button" class="btn btn-danger-outline" onclick="submitWithdrawForm()">회원탈퇴 진행</button>
            </div>
        </form>
    </div>
</div>

<script>
/* =========================================================
   [마이페이지 계좌·카드 연결 JavaScript 추가 시작]
   화면 전환, 계좌 제목 수정, 계좌·카드 4개 단위 페이지 처리를 담당한다.
   ========================================================= */
// 상단 버튼을 누른 경우에만 회원정보·신고내역·계좌카드 영역을 표시한다.
function showMyPagePanel(panelId) {
    document.querySelectorAll('.mypage-toggle-panel').forEach(function (panel) {
        panel.classList.add('is-hidden');
    });

    const target = document.getElementById(panelId);
    if (!target) {
        return;
    }

    target.classList.remove('is-hidden');
    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    window.history.replaceState(null, '', '#' + panelId);
}

// 계좌 제목은 처음에는 읽기 전용으로 표시하고, 수정 버튼을 누르면 입력과 저장이 가능해진다.
function toggleAccountNameEdit(button) {
    const form = button.closest('.mypage-asset-title-form');
    const input = form ? form.querySelector('input[name="accountName"]') : null;
    if (!form || !input) {
        return;
    }

    if (input.readOnly) {
        input.readOnly = false;
        input.focus();
        input.select();
        button.textContent = '저장';
        return;
    }

    if (!input.value.trim()) {
        alert('계좌 제목을 입력해주세요.');
        input.focus();
        return;
    }

    form.submit();
}

// 계좌와 카드 목록은 4개 단위로 보이며, 부족한 칸은 빈 정보 카드로 채운다.
function initializeAssetPager(listId) {
    const list = document.getElementById(listId);
    const pager = document.querySelector('[data-pager-for="' + listId + '"]');
    if (!list || !pager) {
        return;
    }

    let items = Array.from(list.querySelectorAll('[data-asset-item]'));
    const emptyText = list.dataset.emptyText || '정보가 없습니다.';
    const requiredSlots = Math.max(4, Math.ceil(items.length / 4) * 4);

    while (items.length < requiredSlots) {
        const emptyItem = document.createElement('div');
        emptyItem.className = 'mypage-asset-item mypage-asset-empty';
        emptyItem.setAttribute('data-asset-item', '');
        emptyItem.textContent = emptyText;
        list.appendChild(emptyItem);
        items.push(emptyItem);
    }

    const totalPages = Math.max(1, Math.ceil(items.length / 4));
    let currentPage = 1;
    const currentPageText = pager.querySelector('[data-current-page]');
    const totalPageText = pager.querySelector('[data-total-page]');
    const prevButton = pager.querySelector('[data-page-direction="prev"]');
    const nextButton = pager.querySelector('[data-page-direction="next"]');

    function renderPage() {
        items.forEach(function (item, index) {
            const itemPage = Math.floor(index / 4) + 1;
            item.classList.toggle('is-hidden', itemPage !== currentPage);
        });

        currentPageText.textContent = currentPage;
        totalPageText.textContent = totalPages;
        prevButton.disabled = currentPage === 1;
        nextButton.disabled = currentPage === totalPages;
    }

    prevButton.addEventListener('click', function () {
        if (currentPage > 1) {
            currentPage -= 1;
            renderPage();
        }
    });

    nextButton.addEventListener('click', function () {
        if (currentPage < totalPages) {
            currentPage += 1;
            renderPage();
        }
    });

    renderPage();
}

document.addEventListener('DOMContentLoaded', function () {
    initializeAssetPager('accountAssetList');
    initializeAssetPager('cardAssetList');

    // 거래내역 버튼마다 선택한 계좌 번호를 Ajax 조회 함수로 전달한다.
    document.querySelectorAll('.transaction-history-btn').forEach(function (button) {
        button.addEventListener('click', function () {
            loadAccountTransactions(button);
        });
    });

    const targetId = window.location.hash.replace('#', '');
    if (['profile-edit', 'report-manage', 'asset-manage'].includes(targetId)) {
        showMyPagePanel(targetId);
    }
});

/* 선택한 계좌의 거래내역을 Ajax로 조회한다. */
function loadAccountTransactions(button) {
    const accountIdx = button.dataset.accountIdx;
    const panel = document.getElementById('accountTransactionPanel');
    const tbody = document.getElementById('accountTransactionBody');
    const currentBalance = Number(button.dataset.currentBalance || 0);

    document.getElementById('accountTransactionTitle').textContent =
        (button.dataset.accountName || '계좌') + ' 거래내역';
    document.getElementById('accountTransactionAccountInfo').textContent =
        (button.dataset.bankName || '') + ' 계좌번호 - ' + (button.dataset.accountNumber || '-');
    document.getElementById('accountTransactionCurrentBalance').textContent =
        formatWon(currentBalance);
    document.getElementById('accountTransactionInitialBalance').textContent = '-';

    tbody.innerHTML = '<tr><td colspan="4">거래내역을 불러오는 중입니다.</td></tr>';
    // mypage-form-section의 display:grid가 hidden 표시를 덮어쓰지 않도록 숨김 클래스도 함께 제거한다.
    panel.classList.remove('is-hidden');
    panel.hidden = false;

    fetch(
        '${contextPath}/spendolive/mypage/account/transactions.do?accountIdx='
        + encodeURIComponent(accountIdx),
        {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        }
    )
        .then(function (response) {
            if (response.status === 401) {
                throw new Error('로그인이 필요합니다.');
            }
            if (!response.ok) {
                throw new Error('거래내역 조회에 실패했습니다.');
            }
            return response.json();
        })
        .then(function (transactionList) {
            renderAccountTransactions(transactionList, currentBalance);
            panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        })
        .catch(function (error) {
            tbody.innerHTML = '';
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = 4;
            cell.textContent = error.message;
            row.appendChild(cell);
            tbody.appendChild(row);
        });
}

/* 최신 거래가 위로 오도록 전달된 거래내역과 거래 직후 잔액을 표에 출력한다. */
function renderAccountTransactions(transactionList, currentBalance) {
    const tbody = document.getElementById('accountTransactionBody');
    const initialBalanceTarget = document.getElementById('accountTransactionInitialBalance');
    const transactions = Array.isArray(transactionList) ? transactionList : [];

    tbody.innerHTML = '';

    if (transactions.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 4;
        cell.textContent = '등록된 거래내역이 없습니다.';
        row.appendChild(cell);
        tbody.appendChild(row);
        initialBalanceTarget.textContent = formatWon(currentBalance);
        return;
    }

    transactions.forEach(function (transaction) {
        const row = document.createElement('tr');
        const signedAmount = getSignedTransactionAmount(transaction);

        appendTransactionCell(row, formatTransactionDate(transaction.tran_date));
        appendTransactionCell(row, transaction.inout_type || '-');
        appendTransactionCell(row, formatSignedWon(signedAmount));

        if (transaction.balance_after === null || transaction.balance_after === undefined) {
            appendTransactionCell(row, '기록 없음');
        } else {
            appendTransactionCell(row, formatWon(Number(transaction.balance_after)));
        }

        tbody.appendChild(row);
    });

    // 가장 오래된 거래의 직후 잔액에서 그 거래 금액을 되돌려 계좌 등록 당시 잔액을 계산한다.
    const oldestTransaction = transactions[transactions.length - 1];
    if (oldestTransaction.balance_after === null || oldestTransaction.balance_after === undefined) {
        initialBalanceTarget.textContent = '기록 없음';
    } else {
        const initialBalance =
            Number(oldestTransaction.balance_after) - getSignedTransactionAmount(oldestTransaction);
        initialBalanceTarget.textContent = formatWon(initialBalance);
    }
}

/* 사용자 입력값을 HTML 문자열로 합치지 않고 textContent로 안전하게 출력한다. */
function appendTransactionCell(row, value) {
    const cell = document.createElement('td');
    cell.textContent = value;
    row.appendChild(cell);
}

/* 출금은 음수, 입금은 양수로 맞춰 화면 표시와 최초 잔액 계산에 사용한다. */
function getSignedTransactionAmount(transaction) {
    const amount = Math.abs(Number(transaction.tran_amt || 0));
    return transaction.inout_type === '출금' ? amount * -1 : amount;
}

function formatWon(value) {
    return Number(value || 0).toLocaleString('ko-KR') + '원';
}

function formatSignedWon(value) {
    const amount = Number(value || 0);
    const sign = amount > 0 ? '+' : amount < 0 ? '-' : '';
    return sign + Math.abs(amount).toLocaleString('ko-KR') + '원';
}

/* DB의 yyyyMMddHHmmss 거래일시를 화면용 형식으로 변환한다. */
function formatTransactionDate(value) {
    const date = String(value || '');
    if (date.length !== 14) {
        return date || '-';
    }

    return date.substring(0, 4) + '-'
        + date.substring(4, 6) + '-'
        + date.substring(6, 8) + ' '
        + date.substring(8, 10) + ':'
        + date.substring(10, 12) + ':'
        + date.substring(12, 14);
}

function closeAccountTransactions() {
    const panel = document.getElementById('accountTransactionPanel');
    if (!panel) {
        return;
    }

    // mypage-form-section의 display:grid보다 우선하는 공통 숨김 클래스로 거래내역 영역을 닫는다.
    panel.classList.add('is-hidden');
    panel.hidden = true;
}
/* [마이페이지 계좌·카드 연결 JavaScript 추가 끝] */

(function () {
    const emailInput = document.getElementById('mypageEmail');
    const phoneInput = document.getElementById('mypagePhone');
    const passwordFields = ['currentPassword', 'newPassword', 'passwordConfirm'];

    if (emailInput) {
        emailInput.addEventListener('input', function () {
            document.getElementById('emailVerified').value = 'N';
            setMessage('emailVerifyMessage', '이메일을 변경했다면 인증을 다시 진행해주세요.', 'warn');
        });
    }

    if (phoneInput) {
        phoneInput.addEventListener('input', function () {
            document.getElementById('phoneVerified').value = 'N';
            setMessage('phoneVerifyMessage', '전화번호를 변경했다면 인증을 다시 진행해주세요.', 'warn');
        });
    }

    passwordFields.forEach(function (fieldId) {
        const field = document.getElementById(fieldId);
        if (field) {
            field.addEventListener('input', function () {
                document.getElementById('passwordChecked').value = 'N';
                setMessage('passwordCheckMessage', '비밀번호 변경 전 확인 버튼을 눌러주세요.', 'warn');
            });
        }
    });

    const form = document.getElementById('mypageProfileForm');
    if (form) {
        form.addEventListener('submit', function (event) {
            const emailChanged = document.getElementById('mypageEmail').value.trim() !== document.getElementById('originalEmail').value.trim();
            const phoneChanged = document.getElementById('mypagePhone').value.trim() !== document.getElementById('originalPhone').value.trim();
            const newPassword = document.getElementById('newPassword').value.trim();

            if (emailChanged && document.getElementById('emailVerified').value !== 'Y') {
                alert('이메일을 변경하려면 이메일 인증을 완료해주세요.');
                event.preventDefault();
                return;
            }

            if (phoneChanged && document.getElementById('phoneVerified').value !== 'Y') {
                alert('전화번호를 변경하려면 전화번호 인증을 완료해주세요.');
                event.preventDefault();
                return;
            }

            if (newPassword && document.getElementById('passwordChecked').value !== 'Y') {
                alert('비밀번호 변경 전 비밀번호 확인 버튼을 눌러주세요.');
                event.preventDefault();
            }
        });
    }
})();

function postForm(url, data) {
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        body: new URLSearchParams(data)
    });
}

function setMessage(id, message, type) {
    const target = document.getElementById(id);
    if (!target) {
        return;
    }
    target.textContent = message;
    target.classList.remove('success', 'warn');
    if (type) {
        target.classList.add(type);
    }
}

function sendMyPageEmailCode() {
    const email = document.getElementById('mypageEmail').value.trim();
    if (!email) {
        alert('이메일을 입력해주세요.');
        return;
    }

    postForm('${contextPath}/spendolive/mypage/email/send.do', { email: email })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'SUCCESS') {
                document.getElementById('emailVerified').value = 'N';
                setMessage('emailVerifyMessage', '인증번호를 발송했습니다. 이메일을 확인해주세요.', 'success');
            } else {
                setMessage('emailVerifyMessage', '인증번호 발송에 실패했습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('emailVerifyMessage', '인증번호 발송 중 오류가 발생했습니다.', 'warn');
        });
}

function verifyMyPageEmailCode() {
    const email = document.getElementById('mypageEmail').value.trim();
    const inputCode = document.getElementById('mypageEmailCode').value.trim();
    if (!inputCode) {
        alert('이메일 인증번호를 입력해주세요.');
        return;
    }

    postForm('${contextPath}/spendolive/mypage/email/verify.do', { email: email, inputCode: inputCode })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'true') {
                document.getElementById('emailVerified').value = 'Y';
                setMessage('emailVerifyMessage', '이메일 인증이 완료되었습니다.', 'success');
            } else {
                document.getElementById('emailVerified').value = 'N';
                setMessage('emailVerifyMessage', '인증번호가 일치하지 않습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('emailVerifyMessage', '이메일 인증 확인 중 오류가 발생했습니다.', 'warn');
        });
}

function sendMyPagePhoneCode() {
    const phone = document.getElementById('mypagePhone').value.trim();
    if (!phone) {
        alert('전화번호를 입력해주세요.');
        return;
    }

    postForm('${contextPath}/spendolive/mypage/phone/send.do', { phone: phone })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'SUCCESS') {
                document.getElementById('phoneVerified').value = 'N';
                setMessage('phoneVerifyMessage', '인증번호를 발송했습니다. 문자를 확인해주세요.', 'success');
            } else {
                setMessage('phoneVerifyMessage', '인증번호 발송에 실패했습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('phoneVerifyMessage', '인증번호 발송 중 오류가 발생했습니다.', 'warn');
        });
}

function verifyMyPagePhoneCode() {
    const phone = document.getElementById('mypagePhone').value.trim();
    const inputCode = document.getElementById('mypagePhoneCode').value.trim();
    if (!inputCode) {
        alert('문자 인증번호를 입력해주세요.');
        return;
    }

    postForm('${contextPath}/spendolive/mypage/phone/verify.do', { phone: phone, inputCode: inputCode })
        .then(function (res) { return res.text(); })
        .then(function (result) {
            if (result === 'true') {
                document.getElementById('phoneVerified').value = 'Y';
                setMessage('phoneVerifyMessage', '전화번호 인증이 완료되었습니다.', 'success');
            } else {
                document.getElementById('phoneVerified').value = 'N';
                setMessage('phoneVerifyMessage', '인증번호가 일치하지 않습니다.', 'warn');
            }
        })
        .catch(function () {
            setMessage('phoneVerifyMessage', '전화번호 인증 확인 중 오류가 발생했습니다.', 'warn');
        });
}






function submitWithdrawForm() {
    const confirmInput = document.getElementById('withdrawConfirm');
    const form = document.getElementById('withdrawForm');

    if (!confirmInput || !form) {
        return;
    }

    if (confirmInput.value.trim() !== '탈퇴합니다') {
        alert('확인 문구를 정확히 입력해주세요.');
        confirmInput.focus();
        return;
    }

    if (confirm('정말 회원탈퇴를 진행할까요? 탈퇴 후에는 현재 계정으로 다시 로그인할 수 없습니다.')) {
        form.submit();
    }
}

document.addEventListener('click', function (event) {
    const modal = document.getElementById('withdrawModal');
    if (modal && event.target === modal) {
        closeWithdrawModal();
    }
});

function checkMyPagePassword() {
    const currentPassword = document.getElementById('currentPassword').value.trim();
    const newPassword = document.getElementById('newPassword').value.trim();
    const passwordConfirm = document.getElementById('passwordConfirm').value.trim();

    if (!currentPassword || !newPassword || !passwordConfirm) {
        document.getElementById('passwordChecked').value = 'N';
        setMessage('passwordCheckMessage', '현재 비밀번호, 새 비밀번호, 새 비밀번호 확인을 모두 입력해주세요.', 'warn');
        return;
    }

    if (newPassword !== passwordConfirm) {
        document.getElementById('passwordChecked').value = 'N';
        setMessage('passwordCheckMessage', '새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.', 'warn');
        return;
    }

    document.getElementById('passwordChecked').value = 'Y';
    setMessage('passwordCheckMessage', '새 비밀번호 확인이 완료되었습니다. 저장 시 현재 비밀번호도 다시 확인됩니다.', 'success');
}
</script>

