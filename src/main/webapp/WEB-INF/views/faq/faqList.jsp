<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/faq.css">

<div class="faq-page">
    <div class="page-hero">
        <div class="wrap">
            <p class="eyebrow">FAQ</p>
            <h1>자주 묻는 질문</h1>
            <p class="hero-sub">SpendOlive 이용 중 궁금한 점을 빠르게 해결하세요.</p>
            <div class="search-bar">
                <input type="text" id="faqSearchInput" placeholder="질문을 검색해보세요 (예: 비밀번호 변경)">
                <button type="button">검색</button>
            </div>
        </div>
    </div>

    <div class="wrap">
        <div class="cats">
            <button type="button" class="cat-btn active" onclick="filterFaqCat(this,'all')">전체</button>
            <button type="button" class="cat-btn" onclick="filterFaqCat(this,'account')">계정·로그인</button>
            <button type="button" class="cat-btn" onclick="filterFaqCat(this,'expense')">지출관리</button>
            <button type="button" class="cat-btn" onclick="filterFaqCat(this,'ott')">OTT관리</button>
            <button type="button" class="cat-btn" onclick="filterFaqCat(this,'notice')">공지·알림</button>
            <button type="button" class="cat-btn" onclick="filterFaqCat(this,'etc')">기타</button>
        </div>

        <%-- TODO: 실제 연동 시 faqList(Controller에서 전달하는 카테고리별 목록)를 c:forEach로 순회하도록 교체 --%>

        <p class="section-label">계정·로그인</p>
        <div class="faq-list" data-cat="account">
            <div class="faq-item" onclick="toggleFaq(this)">
                <div class="faq-q">
                    <div class="faq-q-left"><span class="q-badge">Q</span>비밀번호를 잊었어요. 어떻게 재설정하나요?</div>
                    <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
                <div class="faq-a"><div class="faq-a-inner">로그인 화면 하단의 <strong>아이디/비밀번호 찾기</strong>를 클릭하세요. 가입 시 등록한 이메일로 재설정 링크를 보내드립니다. 링크는 24시간 동안 유효합니다.</div></div>
            </div>
            <div class="faq-item" onclick="toggleFaq(this)">
                <div class="faq-q">
                    <div class="faq-q-left"><span class="q-badge">Q</span>카카오 계정으로 로그인하면 기존 데이터가 유지되나요?</div>
                    <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
                <div class="faq-a"><div class="faq-a-inner">네, 동일한 이메일 주소로 연동되어 있다면 기존 지출 내역과 OTT 정보가 모두 유지됩니다. 처음 카카오 로그인 시 이메일 일치 여부를 확인해 주세요.</div></div>
            </div>
        </div>

        <p class="section-label">지출관리</p>
        <div class="faq-list" data-cat="expense">
            <div class="faq-item" onclick="toggleFaq(this)">
                <div class="faq-q">
                    <div class="faq-q-left"><span class="q-badge">Q</span>고정지출과 변동지출의 차이는 무엇인가요?</div>
                    <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
                <div class="faq-a"><div class="faq-a-inner"><strong>고정지출</strong>은 매월 일정하게 발생하는 지출(월세, 구독료 등)이고, <strong>변동지출</strong>은 금액이 달라지는 지출(식비, 교통비 등)입니다. 두 유형을 분리해 예산 분석 정확도를 높일 수 있습니다.</div></div>
            </div>
            <div class="faq-item" onclick="toggleFaq(this)">
                <div class="faq-q">
                    <div class="faq-q-left"><span class="q-badge">Q</span>지출 내역을 수정하거나 삭제할 수 있나요?</div>
                    <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
                <div class="faq-a"><div class="faq-a-inner">네, 지출관리 페이지에서 항목을 클릭하면 수정·삭제가 가능합니다. 삭제된 내역은 복구되지 않으니 주의해 주세요.</div></div>
            </div>
        </div>

        <p class="section-label">OTT관리</p>
        <div class="faq-list" data-cat="ott">
            <div class="faq-item" onclick="toggleFaq(this)">
                <div class="faq-q">
                    <div class="faq-q-left"><span class="q-badge">Q</span>OTT 공유 멤버를 추가하려면 어떻게 하나요?</div>
                    <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
                <div class="faq-a"><div class="faq-a-inner">OTT관리 → 멤버 모집 페이지에서 초대 링크를 생성하거나, 친구의 아이디를 직접 검색해 추가할 수 있습니다. 방장은 멤버 승인 후 정산 설정을 진행하면 됩니다.</div></div>
            </div>
        </div>

        <p class="section-label">공지·알림</p>
        <div class="faq-list" data-cat="notice">
            <div class="faq-item" onclick="toggleFaq(this)">
                <div class="faq-q">
                    <div class="faq-q-left"><span class="q-badge">Q</span>알림이 오지 않아요. 어떻게 해야 하나요?</div>
                    <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
                <div class="faq-a"><div class="faq-a-inner">마이페이지 → 알림 설정에서 알림 수신 여부를 확인해 주세요. 브라우저의 알림 권한도 허용되어 있는지 확인이 필요합니다. 그래도 해결되지 않으면 문의하기를 통해 알려주세요.</div></div>
            </div>
        </div>

        <div class="contact-banner">
            <div>
                <h3>원하는 답을 찾지 못하셨나요?</h3>
                <p>고객센터로 직접 문의하시면 빠르게 도와드립니다.</p>
            </div>
            <a class="contact-btn" href="${contextPath}/spendolive/inquiry/write.do">문의하기 →</a>
        </div>
    </div>
</div>

<script src="${contextPath}/resources/js/faq.js"></script>
