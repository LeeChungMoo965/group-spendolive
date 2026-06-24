<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>
        SpendOlive | 캘린더
    </title>
    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
</head>
<body>
    <main>
        <section class="page-hero">
            <div class="container">
                <p class="eyebrow">
                    SPENDING CALENDAR
                </p>
                <h1>
                    캘린더
                </h1>
                <p class="hero-text">
                    월별로 넘겨보는 큰 달력입니다. 날짜에는 지출 금액과 카테고리만 간단히 보여줍니다.
                </p>
            </div>
        </section>
        <section class="section compact">
            <div class="container">
                <div class="section-title row-title">
                    <div>
                        <p class="eyebrow">
                            MONTHLY VIEW
                        </p>
                        <h2>
                            월별 지출 캘린더
                        </h2>
                    </div>
                    <div class="calendar-controls">
                        <button class="btn btn-light" onclick="changeMonth(-1)">
                            ‹ 이전달
                        </button>
                        <button class="btn btn-primary" onclick="location.href='${contextPath}/spendolive/expense.do#expense-form'">
                            + 지출등록
                        </button>
                        <button class="btn btn-light" onclick="changeMonth(1)">
                            다음달 ›
                        </button>
                    </div>
                </div>
                <div class="calendar-page-layout">
                    <div class="calendar">
                        <div class="calendar-header">
                            <span id="calendarTitle">
                                2026년 6월
                            </span>
                            <button class="detail-link" onclick="location.href='${contextPath}/spendolive/expense.do'">
                                자세히보기
                            </button>
                        </div>
                        <div class="calendar-grid">
                            <span>
                                일
                            </span>
                            <span>
                                월
                            </span>
                            <span>
                                화
                            </span>
                            <span>
                                수
                            </span>
                            <span>
                                목
                            </span>
                            <span>
                                금
                            </span>
                            <span>
                                토
                            </span>
                            <button>
                            </button>
                            <button>
                                1
                            </button>
                            <button>
                                2
                            </button>
                            <button>
                                3
                            </button>
                            <button>
                                4
                            </button>
                            <button class="fixedday">
                                5
                                <br>
                                <small>
                                500,000원 · 고정
                            </small>
                        </button>
                        <button>
                            6
                        </button>
                        <button>
                            7
                        </button>
                        <button class="fixedday">
                            8
                            <br>
                            <small>
                            69,000원 · 고정
                        </small>
                    </button>
                    <button class="spend">
                        9
                        <br>
                        <small>
                        12,000원 · 식비
                    </small>
                </button>
                <button class="ottday">
                    10
                    <br>
                    <small>
                    17,000원 · OTT
                </small>
            </button>
            <button>
                11
            </button>
            <button>
                12
            </button>
            <button>
                13
            </button>
            <button>
                14
            </button>
            <button class="ottday">
                15
                <br>
                <small>
                13,900원 · OTT
            </small>
        </button>
        <button>
            16
        </button>
        <button>
            17
        </button>
        <button>
            18
        </button>
        <button class="spend">
            19
            <br>
            <small>
            50,000원 · 교통
        </small>
    </button>
    <button>
        20
    </button>
    <button>
        21
    </button>
    <button>
        22
    </button>
    <button>
        23
    </button>
    <button class="spend">
        24
        <br>
        <small>
        34,000원 · 생활
    </small>
</button>
<button>
    25
</button>
<button>
    26
</button>
<button>
    27
</button>
<button>
    28
</button>
<button>
    29
</button>
<button>
    30
</button>
<button>
</button>
<button>
</button>
<button>
</button>
<button>
</button>
</div>
</div>
<aside class="side-panel card">
<h3>
    이번 달 주요 지출
</h3>
<div class="side-event">
    <strong>
    06.05 월세
</strong>
<span>
    500,000원 · 고정지출
</span>
</div>
<div class="side-event">
    <strong>
    06.10 Netflix
</strong>
<span>
    17,000원 · OTT지출
</span>
</div>
<button class="btn btn-outline full" onclick="location.href='${contextPath}/spendolive/expense.do'">
    지출관리에서 보기
</button>
</aside>
</div>
</div>
</section>
</main>
<script src="${contextPath}/resources/js/app.js">
</script>
</body>
</html>
