<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="contextPath"
       value="${pageContext.request.contextPath}" />

<link rel="stylesheet"
      href="${contextPath}/resources/css/alert.css">

<div class="alert-detail-container">

    <div class="alert-detail-header">

        <h2>${alert.title}</h2>

        <div class="alert-detail-date">
            ${alert.createdAt}
        </div>

    </div>

    <div class="alert-detail-content">
        ${alert.content}
    </div>

    <div class="alert-detail-footer">

        <a href="${contextPath}/spendolive/alert/center.do"
           class="btn-back">

            목록으로

        </a>

    </div>

</div>