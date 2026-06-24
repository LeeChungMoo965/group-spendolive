<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<section class="page-hero ott-main-hero">
    <div class="container ott-wide-container">
        <p class="eyebrow">OTT MANAGEMENT</p>
        <h1>OTT관리</h1>
        <p class="hero-text">
            가족 또는 지인과는 초대형 공유방으로 관리하고, 외부 다른 사람들과는 모집글을 통해 구독 비용을 나눌 수 있습니다.
        </p>
    </div>
</section>

<section class="section compact ott-main-section">
    <div class="container ott-wide-container">
        <div class="ott-stat-grid">
            <div class="ott-stat-card">
                <span>내 공유방</span>
                <strong>${myRoomCount}</strong>
                <p>참여 중이거나 내가 만든 공유방</p>
            </div>
            <div class="ott-stat-card">
                <span>모든 모집글</span>
                <strong>${recruitRoomCount}</strong>
                <p>현재 모집 중인 공개 OTT 파티</p>
            </div>
            <div class="ott-stat-card">
                <span>공유 가능 OTT</span>
                <strong>${fn:length(serviceList)}</strong>
                <p>DB의 ott_service_tb 기준</p>
            </div>
        </div>

        <div class="ott-two-grid ott-choice-grid">
            <article class="card ott-panel ott-choice-card">
                <div class="panel-header">
                    <div>
                        <p class="eyebrow">WITH FAMILY & FRIENDS</p>
                        <h2>가족 또는 지인과</h2>
                    </div>
                    <span>초대 기반 공유</span>
                </div>

                <p class="card-desc">
                    내가 아는 사람들과 공유방을 만들고, OTT 종류별 공유 정보와 정산 상태를 한 곳에서 관리합니다.
                </p>

                <div class="ott-guide-grid">
                    <div>
                        <strong>OTT 공유방</strong>
                        <span>Netflix · Disney+ · TVING 등</span>
                    </div>
                    <div>
                        <strong>정산 요청</strong>
                        <span>참여자별 요청/완료 확인</span>
                    </div>
                    <div>
                        <strong>공유방 대화</strong>
                        <span>오른쪽 아래 말풍선에서 확인</span>
                    </div>
                </div>

                <a href="${contextPath}/spendolive/ott/friends.do" class="btn btn-primary full ott-main-btn">공유방 만들기</a>
            </article>

            <article class="card ott-panel ott-choice-card">
                <div class="panel-header">
                    <div>
                        <p class="eyebrow">WITH OTHERS</p>
                        <h2>외부 다른 사람들과</h2>
                    </div>
                    <span>모집 게시판 기반 공유</span>
                </div>

                <p class="card-desc">
                    모든 모집글을 확인하고, 원하는 OTT 파티에 신청하거나 직접 모집글을 작성할 수 있습니다.
                </p>

                <div class="ott-guide-grid">
                    <div>
                        <strong>모든 모집글</strong>
                        <span>모집인원 · 3% 수수료 포함 금액 확인</span>
                    </div>
                    <div>
                        <strong>모집글 작성</strong>
                        <span>OTT 선택 시 최고 멤버십 자동 계산</span>
                    </div>
                    <div>
                        <strong>신청관리</strong>
                        <span>신청자와 정산 알림 관리</span>
                    </div>
                </div>

                <a href="${contextPath}/spendolive/ott/recruit.do" class="btn btn-primary full ott-main-btn">모든 모집글</a>
            </article>
        </div>
    </div>
</section>
