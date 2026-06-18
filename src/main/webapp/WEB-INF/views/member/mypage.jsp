<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>
        SpendOlive | 마이페이지
    </title>
    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main>
        <section class="page-hero">
            <div class="container">
                <p class="eyebrow">
                    MY PAGE
                </p>
                <h1>
                    마이페이지
                </h1>
                <p class="hero-text">
                    나의 지출 현황, 구분별 지출, OTT 정산 상태, 회원정보를 한 화면에서 확인합니다.
                </p>
            </div>
        </section>
        <section class="section compact">
            <div class="container">
                <div class="dashboard-grid">
                    <div class="profile-card card">
                        <div class="avatar">
                            조
                        </div>
                        <h3>
                            조규호
                        </h3>
                        <p>
                            이번 달 예산 1,800,000원
                        </p>
                        <button class="btn btn-outline full">
                            회원정보 수정
                        </button>
                    </div>
                    <div class="table-card card">
                        <h3>
                            나의 지출 요약
                        </h3>
                        <div class="table-wrap">
                            <table>
                                <thead>
                                    <tr>
                                        <th>
                                            구분
                                        </th>
                                        <th>
                                            금액
                                        </th>
                                        <th>
                                            상태
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>
                                            고정지출
                                        </td>
                                        <td>
                                            620,000원
                                        </td>
                                        <td>
                                            <span class="chip done">
                                                정상
                                            </span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            변동지출
                                        </td>
                                        <td>
                                            578,000원
                                        </td>
                                        <td>
                                            <span class="chip request">
                                                관리중
                                            </span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            OTT지출
                                        </td>
                                        <td>
                                            86,000원
                                        </td>
                                        <td>
                                            <span class="chip wait">
                                                정산대기
                                            </span>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="admin-card card">
                        <h3>
                            관리자 기능
                        </h3>
                        <ul>
                            <li>
                                회원 관리
                            </li>
                            <li>
                                지출 카테고리 관리
                            </li>
                            <li>
                                OTT 모집글 관리
                            </li>
                            <li>
                                신고/문의 관리
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </section>
    </main>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${contextPath}/resources/js/app.js">
    </script>
</body>
</html>
