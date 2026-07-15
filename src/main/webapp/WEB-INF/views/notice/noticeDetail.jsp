<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/notice.css">

<section class="section compact">
    <div class="container">

        <%-- 오류 메시지 (notice=null 또는 서버 오류) --%>
        <c:if test="${not empty errorMsg}">
            <div style="background:#fee2e2;border:1px solid #fca5a5;color:#991b1b;
                        padding:16px 20px;border-radius:10px;margin-bottom:24px;font-weight:500;">
                ⚠ ${errorMsg}
                <div style="margin-top:10px;">
                    <a href="${contextPath}/spendolive/notice/center.do" class="btn btn-primary">목록으로</a>
                </div>
            </div>
        </c:if>

        <%-- 정상 공지 상세 --%>
        <c:if test="${not empty notice}">
            <div class="card notice-detail-card">

                <div class="notice-detail-top">
                    <c:choose>
                        <c:when test="${notice.pinned_yn == 'Y'}">
                            <span class="chip notice-important">중요 공지</span>
                        </c:when>
                        <c:otherwise>
                            <span class="chip notice-normal">공지</span>
                        </c:otherwise>
                    </c:choose>

                    <button type="button" class="notice-star-btn" id="detailStarBtn"
                            data-notice-id="${notice.notice_id}"
                            data-login="${not empty loginYn ? loginYn : false}">
                        ☆
                    </button>
                </div>

                <h1 class="notice-detail-title">${notice.title}</h1>

                <div class="notice-detail-info">
                    <span>작성자 ${not empty notice.admin_id ? notice.admin_id : '관리자'}</span>

                    <span>등록일 ${notice.created_at}</span>
                </div>

                <div class="notice-detail-content">
                    ${notice.content}
                </div>

                <div class="notice-detail-actions">
                    <a href="${contextPath}/spendolive/notice/center.do" class="btn btn-primary">목록으로</a>
                </div>
            </div>
        </c:if>

    </div>
</section>

<script>
(function () {
    var btn = document.getElementById("detailStarBtn");
    if (!btn) return;

    var notice_id = btn.dataset.notice_id;
    var isLogin  = btn.dataset.login === "true";
    var lsKey    = "notice_star_" + notice_id;

    function setStar(active) {
        btn.textContent = active ? "★" : "☆";
        active ? btn.classList.add("active") : btn.classList.remove("active");
    }

    setStar(localStorage.getItem(lsKey) === "Y");

    btn.addEventListener("click", function () {
        if (isLogin) {
            fetch("/spendolive/notice/ajax/star.do", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: "notice_id=" + notice_id
            })
            .then(function(r){ return r.json(); })
            .then(function(data) {
                if (data.result === "OK") {
                    var nowActive = btn.textContent.trim() === "★";
                    setStar(!nowActive);
                    nowActive ? localStorage.removeItem(lsKey) : localStorage.setItem(lsKey, "Y");
                } else if (data.result === "LOGIN_REQUIRED") {
                    alert("로그인이 필요합니다.");
                } else {
                    alert("처리 중 오류가 발생했습니다.");
                }
            })
            .catch(function(){ alert("네트워크 오류가 발생했습니다."); });
        } else {
            var nowActive = btn.textContent.trim() === "★";
            setStar(!nowActive);
            nowActive ? localStorage.removeItem(lsKey) : localStorage.setItem(lsKey, "Y");
        }
    });
})();
</script>
