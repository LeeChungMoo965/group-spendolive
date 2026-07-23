<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<main class="admin-main">

<section class="hero">
    <div>
        <div class="hero-kicker">Admin Dashboard</div>
        <h1>관리자 대시보드</h1>
        <p>회원, 공개 파티, OTT 항목, 신고와 문의 현황을 한눈에 확인하는 SpendOlive 관리자 메인 화면입니다. 실제 기능 연결 전, 발표와 UI 검토를 위한 더미 데이터 화면입니다.</p>
    </div>
    <aside class="hero-card">
        <div class="hero-card-title"><span>오늘의 관리 상태</span><span>2026.06</span></div>
        <div class="progress-list">
            <div class="progress-item">
                <div class="progress-label"><span>신고 처리율</span><span>76%</span></div>
                <div class="progress-bar"><span style="--w:76%"></span></div>
            </div>
            <div class="progress-item">
                <div class="progress-label"><span>문의 답변율</span><span>68%</span></div>
                <div class="progress-bar"><span style="--w:68%"></span></div>
            </div>
            <div class="progress-item">
                <div class="progress-label"><span>파티 안정도</span><span>91%</span></div>
                <div class="progress-bar"><span style="--w:91%"></span></div>
            </div>
        </div>
    </aside>
</section>

<section class="stat-grid">
    <article class="stat-card"><div class="stat-icon">👤</div><small>Total Members</small><strong>1,284</strong><p>전체 가입 회원 수</p></article>
    <article class="stat-card"><div class="stat-icon">🏠</div><small>Open Parties</small><strong>86</strong><p>현재 공개 모집 중인 파티</p></article>
    <article class="stat-card"><div class="stat-icon">🎬</div><small>OTT Services</small><strong>6</strong><p>등록된 OTT 종류</p></article>
    <article class="stat-card"><div class="stat-icon">🚨</div><small>Reports</small><strong>18</strong><p>처리 대기 신고 건수</p></article>
    <article class="stat-card"><div class="stat-icon">📩</div><small>Inquiries</small><strong>27</strong><p>답변 대기 문의 건수</p></article>
</section>

<section class="content-grid">
    <div>
        <section class="panel">
            <div class="panel-header">
                <div class="panel-title">
                    <div class="section-kicker">Weekly Overview</div>
                    <h2>주간 관리 현황</h2>
                    <p>신고, 문의, 신규 가입 흐름을 막대 그래프로 확인합니다.</p>
                </div>
            </div>
            <div class="chart-card">
                <div class="bar-chart">
                    <div class="bar-row"><span>월</span><div class="bar-track"><span class="bar-fill" style="--w:42%"></span></div><strong>42</strong></div>
                    <div class="bar-row"><span>화</span><div class="bar-track"><span class="bar-fill" style="--w:58%"></span></div><strong>58</strong></div>
                    <div class="bar-row"><span>수</span><div class="bar-track"><span class="bar-fill" style="--w:48%"></span></div><strong>48</strong></div>
                    <div class="bar-row"><span>목</span><div class="bar-track"><span class="bar-fill" style="--w:72%"></span></div><strong>72</strong></div>
                    <div class="bar-row"><span>금</span><div class="bar-track"><span class="bar-fill" style="--w:64%"></span></div><strong>64</strong></div>
                </div>
            </div>
        </section>
        <section class="panel">
            <div class="panel-header"><div class="panel-title"><div class="section-kicker">Recent Members</div><h2>최근 가입 회원</h2></div><a class="btn" href="member.html">회원관리 이동</a></div>
            <div class="table-wrap">
                <table class="admin-table">
                    <thead><tr><th>회원</th><th>가입일</th><th>상태</th><th>권한</th></tr></thead>
                    <tbody>
                        <tr><td><div class="user-cell"><div class="avatar">김</div><div class="cell-main"><strong>김민수</strong><span>minsu01</span></div></div></td><td>2026-06-26</td><td><span class="badge green">ACTIVE</span></td><td>USER</td></tr>
                        <tr><td><div class="user-cell"><div class="avatar">박</div><div class="cell-main"><strong>박지현</strong><span>jihyun88</span></div></div></td><td>2026-06-25</td><td><span class="badge green">ACTIVE</span></td><td>USER</td></tr>
                        <tr><td><div class="user-cell"><div class="avatar">이</div><div class="cell-main"><strong>이도윤</strong><span>doyoon</span></div></div></td><td>2026-06-24</td><td><span class="badge yellow">WARNING</span></td><td>USER</td></tr>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
    <aside>
        <section class="panel">
            <div class="panel-title"><div class="section-kicker">Quick Tasks</div><h2>빠른 관리</h2><p>자주 확인하는 관리 메뉴입니다.</p></div>
            <div class="activity-list" style="margin-top:18px">
                <a class="activity-item" href="report.html"><div><strong>신고 대기 18건</strong><span>처리 상태 변경 필요</span></div><span class="badge red">긴급</span></a>
                <a class="activity-item" href="${contextPath}/admin/inquiry/list.do"><div><strong>문의 답변 27건</strong><span>회원 정보 확인 후 답변</span></div><span class="badge yellow">대기</span></a>
                <a class="activity-item" href="party.html"><div><strong>삭제 요청 4건</strong><span>파티 상태 검토 필요</span></div><span class="badge blue">검토</span></a>
            </div>
        </section>
        <section class="panel">
            <div class="panel-title"><div class="section-kicker">Notice</div><h2>최근 공지</h2></div>
            <div class="activity-list" style="margin-top:18px">
                <div class="activity-item"><div><strong>정산 정책 안내</strong><span>2026-06-25</span></div><span class="badge green">게시중</span></div>
                <div class="activity-item"><div><strong>OTT 가격 업데이트</strong><span>2026-06-20</span></div><span class="badge gray">일반</span></div>
            </div>
        </section>
    </aside>
</section>

</main>

<footer class="footer">
    <div class="footer-inner">
        <div>SpendOlive Admin UI Preview</div>
        <div>DB 연결 없이 화면 확인용으로 제작된 독립 HTML 프로젝트입니다.</div>
    </div>
</footer>
<div class="toast" aria-live="polite"></div>
<script src="js/admin.js"></script>

</div>