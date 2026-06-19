<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SpendOlive | 지출관리</title>
    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
</head>

<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main>
        <section class="page-hero">
            <div class="container page-hero-grid">
                <div>
                    <p class="eyebrow">EXPENSE MANAGER</p>
                    <h1>지출관리</h1>
                    <p class="hero-text">
                        고정지출, 변동지출, OTT지출을 각각 분리해서 등록하고 월별 지출, 카테고리 차트, 금액별 랭킹을 확인합니다.
                    </p>
                    <div class="hero-buttons">
                        <a href="#expense-form" class="btn btn-primary btn-large">지출 등록</a>
                        <a href="#expense-detail" class="btn btn-outline btn-large">구분별 보기</a>
                    </div>
                </div>

                <div class="dashboard-preview">
                    <div class="preview-header">
                        <span>지출 구분별 합계</span>
                        <strong>₩1,284,000</strong>
                    </div>

                    <div class="category-list">
                        <div>
                            <span class="dot fixed"></span>
                            <p>고정지출</p>
                            <strong>620,000원</strong>
                        </div>
                        <div>
                            <span class="dot variable"></span>
                            <p>변동지출</p>
                            <strong>578,000원</strong>
                        </div>
                        <div>
                            <span class="dot ott"></span>
                            <p>OTT지출</p>
                            <strong>86,000원</strong>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <section id="expense-detail" class="section compact">
            <div class="container">
                <div class="section-title">
                    <p class="eyebrow">EXPENSE TYPES</p>
                    <h2>지출 구분</h2>
                </div>

                <div class="grid-3">
                    <article class="card expense-category-card">
                        <div class="icon">📌</div>
                        <h3>고정지출</h3>
                        <p>매월 반복적으로 나가는 비용을 관리합니다.</p>
                        <strong>620,000원</strong>
                        <div class="tag-list">
                            <span class="tag">월세</span>
                            <span class="tag">통신비</span>
                            <span class="tag">보험료</span>
                            <span class="tag">관리비</span>
                        </div>
                    </article>

                    <article class="card expense-category-card">
                        <div class="icon">🛒</div>
                        <h3>변동지출</h3>
                        <p>월마다 금액이 달라지는 지출입니다.</p>
                        <strong>578,000원</strong>
                        <div class="tag-list">
                            <span class="tag">식비</span>
                            <span class="tag">교통비</span>
                            <span class="tag">생활비</span>
                            <span class="tag">쇼핑</span>
                        </div>
                    </article>

                    <article class="card expense-category-card">
                        <div class="icon">🎬</div>
                        <h3>OTT지출</h3>
                        <p>구독 지출을 따로 관리하고 공유 정산과 연결합니다.</p>
                        <strong>86,000원</strong>
                        <div class="tag-list">
                            <span class="tag">Netflix</span>
                            <span class="tag">Disney+</span>
                            <span class="tag">TVING</span>
                            <span class="tag">Wavve</span>
                        </div>
                    </article>
                </div>
            </div>
        </section>

        <section id="expense-form" class="section compact">
            <div class="container">
                <div class="expense-layout">
                    <div class="expense-form card">
                        <h3>빠른 지출 등록</h3>

                        <form action="${contextPath}/spendolive/expense/add.do" method="post" class="form-grid">
                            <label>
                                지출 제목
                                <input type="text" name="expenseTitle" placeholder="예: 점심 식사" required>
                            </label>

                            <label>
                                금액
                                <input type="number" name="amount" placeholder="예: 12000" min="0" required>
                            </label>

                            <label>
                                카테고리
                                <select name="categoryId" required>
                                    <option value="">카테고리 선택</option>
                                    <c:forEach var="category" items="${categoryList}">
                                        <option value="${category.categoryId}">
                                            ${category.categoryName}
                                            <c:choose>
                                                <c:when test="${category.expenseType == 'FIXED'}">(고정)</c:when>
                                                <c:when test="${category.expenseType == 'VARIABLE'}">(변동)</c:when>
                                                <c:when test="${category.expenseType == 'OTT'}">(OTT)</c:when>
                                                <c:otherwise>(${category.expenseType})</c:otherwise>
                                            </c:choose>
                                        </option>
                                    </c:forEach>
                                </select>
                            </label>

                            <label>
                                지출 날짜
                                <input type="date" name="expenseDate" required>
                            </label>

                            <label>
                                결제 수단
                                <select name="paymentMethod">
                                    <option value="">선택 안함</option>
                                    <option value="CARD">카드</option>
                                    <option value="CASH">현금</option>
                                    <option value="TRANSFER">계좌이체</option>
                                    <option value="KAKAO_PAY">카카오페이</option>
                                    <option value="NAVER_PAY">네이버페이</option>
                                </select>
                            </label>

                            <label>
                                반복 지출
                                <select name="repeatYn">
                                    <option value="N">아니오</option>
                                    <option value="Y">예</option>
                                </select>
                            </label>

                            <label>
                                반복 주기
                                <select name="repeatCycle">
                                    <option value="">없음</option>
                                    <option value="MONTHLY">매월</option>
                                    <option value="WEEKLY">매주</option>
                                    <option value="YEARLY">매년</option>
                                </select>
                            </label>

                            <label>
                                고정 지출
                                <select name="fixedYn">
                                    <option value="N">아니오</option>
                                    <option value="Y">예</option>
                                </select>
                            </label>

                            <label>
                                메모
                                <input type="text" name="memo" placeholder="선택 입력">
                            </label>

                            <button type="submit" class="btn btn-primary full">
                                등록하기
                            </button>
                        </form>
                    </div>

                    <div class="expense-table card">
                        <h3>최근 지출 내역</h3>

                        <div class="table-wrap">
                            <table>
                                <thead>
                                    <tr>
                                        <th>날짜</th>
                                        <th>내용</th>
                                        <th>구분</th>
                                        <th>카테고리</th>
                                        <th>금액</th>
                                        <th>결제수단</th>
                                        <th>반복</th>
                                        <th>고정</th>
                                        <th>관리</th>
                                    </tr>
                                </thead>

                                <tbody id="expenseRows">
                                    <c:forEach var="expense" items="${expenseList}">
                                        <tr>
                                            <td>
                                                <fmt:formatDate value="${expense.expenseDate}" pattern="yyyy.MM.dd" />
                                            </td>
                                            <td>${expense.expenseTitle}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${expense.expenseType == 'FIXED'}">고정지출</c:when>
                                                    <c:when test="${expense.expenseType == 'VARIABLE'}">변동지출</c:when>
                                                    <c:when test="${expense.expenseType == 'OTT'}">OTT지출</c:when>
                                                    <c:otherwise>${expense.expenseType}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${expense.categoryName}</td>
                                            <td>
                                                <fmt:formatNumber value="${expense.amount}" pattern="#,###" />원
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${expense.paymentMethod == 'CARD'}">카드</c:when>
                                                    <c:when test="${expense.paymentMethod == 'CASH'}">현금</c:when>
                                                    <c:when test="${expense.paymentMethod == 'TRANSFER'}">계좌이체</c:when>
                                                    <c:when test="${expense.paymentMethod == 'KAKAO_PAY'}">카카오페이</c:when>
                                                    <c:when test="${expense.paymentMethod == 'NAVER_PAY'}">네이버페이</c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${expense.repeatYn}</td>
                                            <td>${expense.fixedYn}</td>
                                            <td>
                                                <form action="${contextPath}/spendolive/expense/delete.do" method="post"
                                                      onsubmit="return confirm('이 지출 내역을 삭제하시겠습니까?');">
                                                    <input type="hidden" name="expenseId" value="${expense.expenseId}">
                                                    <button type="submit" class="btn btn-outline">삭제</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>

                                    <c:if test="${empty expenseList}">
                                        <tr>
                                            <td colspan="9" style="text-align:center;">
                                                등록된 지출 내역이 없습니다.
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <section class="section compact">
            <div class="container">
                <div class="analytics-grid">
                    <article class="card analytics-card">
                        <h3>
                            월별지출
                            <small>최대 3개월</small>
                        </h3>

                        <div class="bar-chart">
                            <div class="bar-item">
                                <span>4월</span>
                                <div class="bar-track">
                                    <div style="height:58%"></div>
                                </div>
                                <strong>98만</strong>
                            </div>

                            <div class="bar-item">
                                <span>5월</span>
                                <div class="bar-track">
                                    <div style="height:72%"></div>
                                </div>
                                <strong>116만</strong>
                            </div>

                            <div class="bar-item active">
                                <span>6월</span>
                                <div class="bar-track">
                                    <div style="height:86%"></div>
                                </div>
                                <strong>128만</strong>
                            </div>
                        </div>
                    </article>

                    <article class="card analytics-card">
                        <h3>월별 카테고리 차트</h3>

                        <div class="donut-wrap">
                            <div class="donut-chart"></div>
                            <div class="donut-legend">
                                <span><i class="legend-food"></i>식비 33%</span>
                                <span><i class="legend-traffic"></i>교통비 10%</span>
                                <span><i class="legend-ott"></i>OTT 7%</span>
                                <span><i class="legend-living"></i>생활비 50%</span>
                            </div>
                        </div>
                    </article>

                    <article class="card analytics-card">
                        <h3>월별 지출 랭킹</h3>

                        <ol class="ranking-list">
                            <li>
                                <span>월세</span>
                                <strong>500,000원</strong>
                            </li>
                            <li>
                                <span>식비</span>
                                <strong>420,000원</strong>
                            </li>
                            <li>
                                <span>생활용품</span>
                                <strong>246,000원</strong>
                            </li>
                            <li>
                                <span>교통비</span>
                                <strong>132,000원</strong>
                            </li>
                            <li>
                                <span>OTT 구독</span>
                                <strong>86,000원</strong>
                            </li>
                        </ol>
                    </article>
                </div>
            </div>
        </section>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

    <script src="${contextPath}/resources/js/app.js"></script>
</body>
</html>
