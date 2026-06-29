    <%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
    <c:set var="contextPath" value="${pageContext.request.contextPath}" />
    <c:set var="requestURI" value="${pageContext.request.requestURI}" />


    <header class="site-header">
        <div class="container header-inner">
            <a href="${contextPath}/spendolive/main.do" class="logo">
                <img src="${contextPath}/resources/images/logo.png" alt="SpendOlive" style="width:42px;height:42px;border-radius:50%;object-fit:cover;">
                <span>SpendOlive</span>
            </a>

            <nav class="nav">
                <a href="${contextPath}/spendolive/notice/center.do" class="${fn:contains(requestURI, '/notice') ? 'active' : ''}">🔔</a>
            </nav>

            <div class="header-actions">
                <c:choose>
                    <c:when test="${isLogOn == true && not empty memberInfo}">
                    <c:choose>
                        <c:when test="${memberInfo.open_bank_token == null || memberInfo.open_bank_user_seq_no == null || memberInfo.fintech_use_num == null}">
                            <a class="btn btn-light" href="${contextPath}/member/openBankingAuth.do">🏦 안전한 오픈뱅킹 계좌 연동하기</a>
                        </c:when>
                    </c:choose>
                        <strong><a class="btn btn-light" href="${contextPath}/member/logout.do">로그아웃</a>
                        ${memberInfo.member_name}님
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-light" href="${contextPath}/member/loginForm.do">로그인</a>
                        <a class="btn btn-primary" href="${contextPath}/member/signup.do">회원가입</a>
                    </c:otherwise>
                </c:choose>
                <div class="so-menu-wrapper">
                    <button type="button" class="so-menu-trigger-btn" id="soMenuBtn">
                        메뉴 ▾
                    </button>
                    
                    <ul class="so-menu-popover" id="soMenuPopover">
                        <li><a class="so-menu-link ${fn:contains(requestURI, '/notice') ? 'active' : ''}" href="${contextPath}/spendolive/notice/center.do"><strong>알림＆공지사항</strong></a></li>
                        <li><a class="so-menu-link ${fn:contains(requestURI, '/expense') ? 'active' : ''}" href="${contextPath}/spendolive/expense/list.do"><strong>지출관리</strong></a></li>
                        <li><a class="so-menu-link ${fn:contains(requestURI, '/calendar') ? 'active' : ''}" href="${contextPath}/spendolive/calendar.do"><strong>캘린더</strong></a></li>
                        <li><a class="so-menu-link ${fn:contains(requestURI, '/ott') ? 'active' : ''}" href="${contextPath}/spendolive/ott.do"><strong>OTT관리</strong></a></li>
                        <li><a class="so-menu-link ${fn:contains(requestURI, '/mypage') ? 'active' : ''}" href="${contextPath}/spendolive/mypage.do"><strong>마이페이지</strong></a></li>
                        <li><a class="so-menu-link" href="${contextPath}/member/openBankingAuth.do"><strong>🏦계좌 연동하기</strong></a></li>
                        <li><a class="so-menu-link" href="${contextPath}/payment/paymenting.do"><strong>정산하기</strong></a></li>
                        <li><a class="so-menu-link" onclick="requestBillingAuth()"><strong>카드 등록하기</strong></a></li>
                    </ul>
                </div>
                </strong>
            </div>
        </div>
    </header>
    <script src="https://js.tosspayments.com/v2/standard"></script>
    <script src="${contextPath}/resources/js/app.js"></script>
    <script>
    (function() {
        var btn = document.getElementById('soMenuBtn');
        var popover = document.getElementById('soMenuPopover');
        
        if (btn && popover) {
            // 1. 버튼 클릭 시 열고 닫기
            btn.addEventListener('click', function(e) {
                e.stopPropagation(); // 바깥 클릭 이벤트와 겹치지 않게 방지
                popover.classList.toggle('is-open');
            });
            
            // 2. 메뉴창 바깥 아무 데나 누르면 닫히게 처리
            document.addEventListener('click', function(e) {
                if (!popover.contains(e.target) && e.target !== btn) {
                    popover.classList.remove('is-open');
                }
            });
        }
    })();
    const clientKey = "";
        const customerKey = "${memberInfo.id}";
        const tossPayments = TossPayments(clientKey);

        //@docs https://docs.tosspayments.com/sdk/v2/js#tosspaymentspayment
        //const payment = tossPayments.payment({ customerKey });
        // 비회원 결제
        const payment = tossPayments.payment({ customerKey })
        // ------ '카드 등록하기' 버튼 누르면 결제창 띄우기 ------  
        //@docs https://docs.tosspayments.com/sdk/v2/js#paymentrequestpayment
        async function requestBillingAuth() {
            const contextPath = "${contextPath}";
            await payment.requestBillingAuth({
            method: "CARD", // 자동결제(빌링)는 카드만 지원합니다
            successUrl: window.location.origin + contextPath + "/payment/callback.do", 
            failUrl: window.location.origin + contextPath + "/payment/fail.do",
            customerEmail: '${memberInfo.email}',
            customerName: '${memberInfo.member_name}',
            });
        }
    </script>
