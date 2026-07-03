<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/faq.css">

<%--
  TODO: 컨트롤러에서 model.addAttribute("inquiryList", ...) 로 아래 형태의 리스트를 전달해야 함
  각 항목 필드 (예시, 실제 VO 필드명에 맞춰 조정):
    - inquiryId   : 문의 번호 (InquiryVO.getInquiryId())
    - category    : 카테고리명 (예: "지출관리")
    - statusCode  : "wait" | "done" | "review"
    - statusLabel : "답변 대기" | "답변 완료" | "검토 중"
    - regDate     : 등록일 (yyyy.MM.dd 형식 문자열 또는 java.util.Date)
    - title       : 제목
    - preview     : 본문 미리보기
    - hasReply    : boolean, 답변 여부

  페이지네이션: 문의가 10개 이하면 전부 한 페이지에 표시되고 페이지 버튼 자체가 안 보임.
        10개를 넘으면 그때부터 5개씩 페이지네이션 (InquiryService.PAGE_SIZE / PAGINATION_THRESHOLD)
  참고: 상태 필터(전체/답변대기/답변완료/검토중) 버튼은 클라이언트 JS라서 "현재 페이지 안에서만" 필터링됨.
        예를 들어 2페이지에 있는 "답변 완료" 문의는 1페이지에서 필터를 눌러도 안 보임.
--%>

<div class="faq-page">
    <div class="page-hero">
        <div class="wrap">
            <p class="eyebrow">MY INQUIRIES</p>
            <h1>내 문의 조회</h1>
            <p class="hero-sub">SpendOlive에 남긴 문의 내역과 답변을 확인하세요.</p>
        </div>
    </div>

    <div class="wrap">
        <div class="board-header">
            <h2>문의 내역</h2>
            <a class="btn btn-primary" href="${contextPath}/spendolive/inquiry/write.do">+ 새 문의 작성</a>
        </div>

        <c:if test="${not empty inquiryList}">
            <div class="filters">
                <button type="button" class="filter-btn active" onclick="filterInquiryStatus(this,'all')">전체</button>
                <button type="button" class="filter-btn" onclick="filterInquiryStatus(this,'wait')">답변 대기</button>
                <button type="button" class="filter-btn" onclick="filterInquiryStatus(this,'done')">답변 완료</button>
                <button type="button" class="filter-btn" onclick="filterInquiryStatus(this,'review')">검토 중</button>
            </div>

            <div class="inq-list" id="inqList">
                <c:forEach var="inq" items="${inquiryList}">
                    <div class="inq-card" data-status="${inq.statusCode}" onclick="goInquiryDetail('${contextPath}','${inq.inquiryId}')">
                        <div class="inq-top">
                            <span class="inq-category">${inq.category}</span>
                            <div class="inq-meta">
                                <span class="badge ${inq.statusCode}">${inq.statusLabel}</span>
                                <span class="inq-date">${inq.regDate}</span>
                            </div>
                        </div>
                        <div class="inq-title">${inq.title}</div>
                        <div class="inq-preview">${inq.preview}</div>
                        <div class="inq-bottom">
                            <c:choose>
                                <c:when test="${inq.hasReply}">
                                    <div class="inq-reply"><span class="reply-dot"></span> 관리자 답변이 달렸습니다</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="inq-reply"><span class="reply-dot none"></span> 아직 답변이 없습니다</div>
                                </c:otherwise>
                            </c:choose>
                            <span class="inq-no">문의 #${inq.inquiryId}</span>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <c:forEach begin="1" end="${totalPages}" var="p">
                        <a class="pg-btn ${p == currentPage ? 'active' : ''}"
                           href="${contextPath}/spendolive/inquiry/list.do?page=${p}">${p}</a>
                    </c:forEach>
                </div>
            </c:if>
        </c:if>

        <c:if test="${empty inquiryList}">
            <div class="empty-box">
                <div class="icon-big">📭</div>
                <p>아직 등록한 문의가 없습니다.</p>
                <a class="btn btn-primary" href="${contextPath}/spendolive/inquiry/write.do">+ 새 문의 작성</a>
            </div>
        </c:if>
    </div>
</div>

<script>
    // 문의 제출 후 리다이렉트로 넘어온 1회성 메시지 (main.jsp와 동일한 패턴)
    var msg = "${msg}";
    if (msg && msg !== "") {
        alert(msg);
    }
</script>
<script src="${contextPath}/resources/js/faq.js"></script>

