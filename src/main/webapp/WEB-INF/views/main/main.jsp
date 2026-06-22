<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">


    <main>
        <section class="hero">
            <div class="container hero-grid">
                <div>
                    <p class="eyebrow">
                        EXPENSE · CALENDAR · OTT MANAGEMENT
                    </p>
                    <h1>
                        SpendOlive로
                        <br>
                        지출을 한눈에 관리하세요
                    </h1>
                    <p class="hero-text">
                        메인에서는 지출관리, 캘린더, OTT관리, 마이페이지를 간단히 확인하고, 각 메뉴에서 더 자세한 기능을 사용할 수 있습니다.
                    </p>
                    <div class="hero-buttons">
                        <a href="${contextPath}/spendolive/expense.do" class="btn btn-primary btn-large">
                            지출관리 바로가기
                        </a>
                        <a href="${contextPath}/spendolive/calendar.do" class="btn btn-outline btn-large">
                            캘린더 보기
                        </a>
                    </div>
                    <div class="hero-stats">
                        <div>
                            <strong>
                            1,284,000원
                        </strong>
                        <span>
                            이번 달 총 지출
                        </span>
                    </div>
                    <div>
                        <strong>
                        3개
                    </strong>
                    <span>
                        지출 구분
                    </span>
                </div>
                <div>
                    <strong>
                    4건
                </strong>
                <span>
                    OTT 정산
                </span>
            </div>
        </div>
    </div>
    <div class="dashboard-preview">
        <div class="preview-header">
            <span>
                이번 달 요약
            </span>
            <strong>
            ₩1,284,000
        </strong>
    </div>
    <div class="spend-ring">
        <div class="ring-center">
            <span>
                예산 사용
            </span>
            <strong>
            72%
        </strong>
    </div>
</div>
<div class="category-list">
    <div>
        <span class="dot fixed">
        </span>
        <p>
            고정지출
        </p>
        <strong>
        620,000원
    </strong>
</div>
<div>
    <span class="dot variable">
    </span>
    <p>
        변동지출
    </p>
    <strong>
    578,000원
</strong>
</div>
<div>
    <span class="dot ott">
    </span>
    <p>
        OTT지출
    </p>
    <strong>
    86,000원
</strong>
</div>
</div>
</div>
</div>
</section>
<section class="section compact">
    <div class="container">
        <div class="section-title">
            <p class="eyebrow">
                OVERVIEW
            </p>
            <h2>
                기능 한눈에 보기
            </h2>
        </div>
        <div class="grid-4">
            <article class="summary-card">
                <div class="icon">
                    💳
                </div>
                <h3>
                    지출관리
                </h3>
                <p>
                    고정지출, 변동지출, OTT지출을 나누어 등록하고 통계와 랭킹을 확인합니다.
                </p>
                <a href="${contextPath}/spendolive/expense/list.do" class="btn btn-outline full">
                    자세히 보기
                </a>
            </article>
            <article class="summary-card">
                <div class="icon">
                    📅
                </div>
                <h3>
                    캘린더
                </h3>
                <p>
                    월별 달력에서 날짜별 지출 금액과 카테고리를 편하게 확인합니다.
                </p>
                <a href="${contextPath}/spendolive/calendar.do" class="btn btn-outline full">
                    자세히 보기
                </a>
            </article>
            <article class="summary-card">
                <div class="icon">
                    🎬
                </div>
                <h3>
                    OTT관리
                </h3>
                <p>
                    지인과의 공유방, 다른 사람들과의 모집 게시판, 정산 요청을 관리합니다.
                </p>
                <a href="${contextPath}/spendolive/ott.do" class="btn btn-outline full">
                    자세히 보기
                </a>
            </article>
            <article class="summary-card">
                <div class="icon">
                    👤
                </div>
                <h3>
                    마이페이지
                </h3>
                <p>
                    나의 지출 현황, 정산 상태, 회원정보를 한 화면에서 확인합니다.
                </p>
                <a href="${contextPath}/spendolive/mypage.do" class="btn btn-outline full">
                    자세히 보기
                </a>
            </article>
        </div>
    </div>
</section>
</main>

<script src="${contextPath}/resources/js/app.js">
</script>

