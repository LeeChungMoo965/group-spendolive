<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="requestURI" value="${pageContext.request.requestURI}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SpendOlive | 지출관리 플랫폼</title>
</head>
<body >

    <div id="modern-wrapper">
        
        <%-- 1. 상단 헤더 구역 --%>
        <header id="modern-header">
            <jsp:include page="/WEB-INF/views/common/header.jsp" />
        </header>
        <%-- 2. 중앙 컨텐츠 메인 구역 (사이드바 + 본문 알맹이) --%>
        <div id="modern-container">           
            <%-- 중앙 메인 본문 (장바구니, 주문상세 등 모든 페이지가 여기에 도킹됨) --%>
            <main id="modern-content">
                <jsp:include page="${body_page}" />
            </main>
            
        </div>
        
        <%-- 3. 하단 푸터 구역 --%>
        <footer id="modern-footer">
            <jsp:include page="/WEB-INF/views/common/footer.jsp" />
        </footer>
        
    </div>

</body>
</html>