<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>
        SpendOlive | OTT관리
    </title>
    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main>
        <section class="page-hero">
            <div class="container">
                <p class="eyebrow">
                    OTT MANAGEMENT
                </p>
                <h1>
                    OTT관리
                </h1>
                <p class="hero-text">
                    OTT 지출은 따로 관리하고, 지인과의 공유 정산 또는 다른 사람들과의 모집 게시판을 통해 구독 비용을 나누어 관리할 수 있습니다.
                </p>
            </div>
        </section>
        <section class="section compact">
            <div class="container">
                <div class="ott-two-grid">
                    <article class="card ott-panel">
                        <div class="panel-header">
                            <div>
                                <p class="eyebrow">
                                    WITH FRIENDS
                                </p>
                                <h2>
                                    지인과
                                </h2>
                            </div>
                            <span>
                                가족 · 친구 · 지인 공유
                            </span>
                        </div>
                        <div class="ott-service-grid">
                            <div class="ott-service netflix">
                                <strong>
                                Netflix
                            </strong>
                            <span>
                                4명 공유
                            </span>
                        </div>
                        <div class="ott-service disney">
                            <strong>
                            Disney+
                        </strong>
                        <span>
                            3명 공유
                        </span>
                    </div>
                    <div class="ott-service tving">
                        <strong>
                        TVING
                    </strong>
                    <span>
                        2명 공유
                    </span>
                </div>
            </div>
            <div class="function-list">
                <div>
                    <b>
                    OTT 종류별 공유
                </b>
                <span>
                    서비스별 공유방 생성
                </span>
            </div>
            <div>
                <b>
                정산 요청
            </b>
            <span>
                참여자에게 알림 발송
            </span>
        </div>
        <div>
            <b>
            대화방
        </b>
        <span>
            공유방 안에서 메시지 확인
        </span>
    </div>
    <div>
        <b>
        정산 상태
    </b>
    <span>
        완료/대기/요청 상태 확인
    </span>
</div>
</div>
<button class="btn btn-primary full">
    지인 공유방 만들기
</button>
</article>
<article class="card ott-panel">
    <div class="panel-header">
        <div>
            <p class="eyebrow">
                WITH OTHERS
            </p>
            <h2>
                다른사람들과
            </h2>
        </div>
        <span>
            모집 게시판 기반 공유
        </span>
    </div>
    <div class="board-list">
        <div>
            <strong>
            넷플릭스 프리미엄 모집
        </strong>
        <p>
            모집인원 2/4 · 1인 4,250원
        </p>
        <button class="btn btn-outline">
            신청하기
        </button>
    </div>
    <div>
        <strong>
        디즈니+ 스탠다드 모집
    </strong>
    <p>
        모집인원 3/4 · 1인 3,500원
    </p>
    <button class="btn btn-outline">
        신청하기
    </button>
</div>
</div>
<div class="function-list">
    <div>
        <b>
        모집글 작성
    </b>
    <span>
        OTT 종류, 모집인원, 금액 등록
    </span>
</div>
<div>
    <b>
    신청 관리
</b>
<span>
    참여 신청 승인/거절
</span>
</div>
<div>
    <b>
    정산/대화/알림
</b>
<span>
    지인 공유 기능과 동일하게 제공
</span>
</div>
</div>
<button class="btn btn-primary full">
    모집글 작성하기
</button>
</article>
</div>
<div class="ott-extra-grid">
    <div class="chat-box">
        <h3>
            공유방 대화 예시
        </h3>
        <div class="chat-message">
            <div class="avatar" style="width:38px;height:38px;border-radius:14px;font-size:16px;margin:0;">
                조
            </div>
            <div class="bubble">
                이번 달 정산 요청 보냈어!
            </div>
        </div>
    </div>
    <div class="status-box">
        <h3>
            정산 상태
        </h3>
        <div class="status-row">
            <span>
                조규호
            </span>
            <em class="done">
            완료
        </em>
    </div>
    <div class="status-row">
        <span>
            주수연
        </span>
        <em class="wait">
        대기
    </em>
</div>
<button class="btn btn-outline full" style="margin-top:16px;">
    정산 알림 보내기
</button>
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
