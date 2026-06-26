<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/notice.css">

<section class="page-hero">
    <div class="container">
        <div class="notice-hero-text">
            <p class="eyebrow">NOTICE CENTER</p>
            <h1>공지사항 · 알림센터</h1>
            <p class="hero-text">
                SpendOlive의 공지사항과 개인 알림을 한눈에 확인하세요.
            </p>
        </div>
    </div>
</section>

<section class="section compact notice-list-section">
    <div class="container">
        <div class="notice-board-wrap">

            <div class="notice-board-tabs">
                <button type="button"
                    id="noticeTabBtn"
                    class="notice-board-tab"
                    onclick="setBoardTab('notice')">
                공지사항
            </button>

            <button type="button"
                    id="alertTabBtn"
                    class="notice-board-tab active"
                    onclick="setBoardTab('alert')">
                알림
            </button>
            </div>

            <div class="card table-card notice-board-card">
                <div class="notice-row-title">
                    <div>
                        <p class="eyebrow" id="listEyebrow">NOTICE LIST</p>
                        <h2 id="listTitle">공지사항</h2>


                     
                     <div id="boardFilter" class="notification-filter"></div>
                        
                    </div>
                </div>

                <div class="table-wrap">
                    <table>
                        <colgroup>
                                <col style="width:55px">     <!-- 번호 -->
                                <col style="width:45px">     <!-- 찜 -->
                                <col style="width:110px">     <!-- 구분 -->
                                <col>                        <!-- 제목(남는 공간 전부) -->
                                <col style="width:90px">    <!-- 작성자 -->
                                <col style="width:120px">    <!-- 등록일 -->
                            </colgroup>


                        <thead>
                            <tr>
                                <th>번호</th>
                                <th class="star-column"></th>
                                <th>구분</th>
                                <th>제목</th>
                                <th id="writerTypeHeader">작성자</th>
                                <th>등록일</th>
                            </tr>
                        </thead>

                        <tbody id="noticeTableBody">
                            
                                  
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</section>

<script>
    const loginYn = ${loginYn};
</script>

<script src="${contextPath}/resources/js/notice.js"></script>