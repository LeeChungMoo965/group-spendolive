
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/notice.css">

<section class="section compact">
    <div class="container">

        <div class="card notice-detail-card">

            <div class="notice-detail-top">

                <c:choose>
                    <c:when test="${notice.pinnedYn == 'Y'}">
                        <span class="chip notice-important">중요 공지</span>
                    </c:when>
                    <c:otherwise>
                        <span class="chip notice-normal">공지</span>
                    </c:otherwise>
                </c:choose>

                <button type="button"
                        class="notice-star-btn"
                        data-notice-id="${notice.noticeId}">
                    ☆
                </button>
            </div>

            <h1 class="notice-detail-title">${notice.title}</h1>

            <div class="notice-detail-info">
                <span>작성자 관리자</span>
                <span>등록일 ${notice.createdAt}</span>
            </div>

            <div class="notice-detail-content">
                ${notice.content}
            </div>

            <div class="notice-detail-actions">
                <a href="${contextPath}/spendolive/notice/center.do" class="btn btn-primary">
                    목록으로
                </a>
            </div>

        </div>

    </div>

    <script>
    const starBtn = document.querySelector(".notice-star-btn");

    if (starBtn) {
        const noticeId = starBtn.dataset.noticeId;
        const key = "notice_star_" + noticeId;

        if (localStorage.getItem(key) === "Y") {
            starBtn.textContent = "★";
            starBtn.classList.add("active");
        }

        starBtn.addEventListener("click", function () {
            if (localStorage.getItem(key) === "Y") {
                localStorage.removeItem(key);
                starBtn.textContent = "☆";
                starBtn.classList.remove("active");
            } else {
                localStorage.setItem(key, "Y");
                starBtn.textContent = "★";
                starBtn.classList.add("active");
            }
        });
    }
</script>
</section>

