function loadNotificationBadge() {
    const badge = document.getElementById("notificationBadge");
    if (!badge) return;

    fetch("/spendolive/notification/ajax/unreadCount.do")
        .then(response => {
            if (!response.ok) throw new Error("HTTP " + response.status);
            return response.json();
        })
        .then(data => {
            const count = (data && data.unreadCount) ? data.unreadCount : 0;
            if (count <= 0) {
                badge.style.display = "none";
                badge.textContent   = "";
            } else {
                badge.textContent   = count > 99 ? "99+" : String(count);
                badge.style.display = "inline-flex";
            }
        })
        .catch(() => {
            // 비로그인이거나 서버 오류 → 배지 숨김
            badge.style.display = "none";
        });
}

// ── 즉시 실행 (DOMContentLoaded 타이밍과 무관하게)
(function tryBadge() {
    const badge = document.getElementById("notificationBadge");
    if (badge) {
        loadNotificationBadge();
    } else {
        // badge 요소가 아직 파싱 안 됐으면 DOM 준비 후 재시도
        document.addEventListener("DOMContentLoaded", loadNotificationBadge);
    }
})();

// ── 탭 포커스 복귀 시 자동 갱신
document.addEventListener("visibilitychange", function () {
    if (document.visibilityState === "visible") {
        loadNotificationBadge();
    }
});