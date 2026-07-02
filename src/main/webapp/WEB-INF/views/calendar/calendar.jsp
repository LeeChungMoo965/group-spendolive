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
    <script src="
https://cdn.jsdelivr.net/npm/fullcalendar@6.1.21/index.global.min.js
"></script>
<script src="${contextPath}/resources/js/calendar.js"></script>
<script src="${contextPath}/resources/js/app.js">
</script>
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
                        <span id="calendarTitle"></span>
                        <button class="detail-link" id="detailBtn">자세히보기</button>
                    </div>
                        <div id="calendar"></div> 
                    </div>
                    <aside class="side-panel card">
                        <h3>이번 달 주요 지출</h3>
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

</body>
</html>
