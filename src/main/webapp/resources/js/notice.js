let currentPage = 1;
const pageSize = 10;
let currentNoticeData = [];



function setBoardTab(mode) {


    if (mode === "alert" && !loginYn) {
        alert("로그인이 필요한 기능 입니다 로그인을 해주세요 !");
        location.href = "/member/loginForm.do?log=notice";
        return;
    }

    
    const eyebrow = document.getElementById("listEyebrow");
    const title = document.getElementById("listTitle");
    const header = document.getElementById("writerTypeHeader");

    const noticeBtn = document.getElementById("noticeTabBtn");
    const alertBtn = document.getElementById("alertTabBtn");
    const boardFilter = document.getElementById("boardFilter");

    if (mode === "alert") {
        eyebrow.textContent = "ALERT LIST";
        title.textContent = "알림";
        header.textContent = "출처";

        noticeBtn.classList.remove("active");
        alertBtn.classList.add("active");

        boardFilter.innerHTML = `
            <button type="button" class="notification-filter-btn active"
            onclick="setFilterActive(this); loadNotificationList('all')">
                전체 알림
            </button>
            <button type="button" class="notification-filter-btn"
            onclick="setFilterActive(this); loadNotificationList('unread')">
                안 읽은 알림
            </button>
        `;
        

        loadNotificationList("all");

    } else {
        eyebrow.textContent = "NOTICE LIST";
        title.textContent = "공지사항";
        header.textContent = "작성자";

        alertBtn.classList.remove("active");
        noticeBtn.classList.add("active");

        boardFilter.innerHTML = `
            <button type="button" class="notification-filter-btn active"
            onclick="setFilterActive(this); loadNoticeList('all')">
                전체 공지
            </button>
            <button type="button" class="notification-filter-btn"
            onclick="setFilterActive(this); loadNoticeList('unread')">
                안 읽은 공지
            </button>
            <button type="button" class="notification-filter-btn"

                    onclick="setFilterActive(this); loadImportantNoticeList()">
                중요 공지
            </button>
        `;

        loadNoticeList("all");
    }
}

    function setFilterActive(button) {

        document.querySelectorAll(".notification-filter-btn")
            .forEach(btn => btn.classList.remove("active"));

        button.classList.add("active");
}


    function loadNoticeList(filter = "all") {

        // 비로그인 + 안 읽은 공지 필터 → 전체를 받아서 클라이언트에서 걸러냄
        const url = (filter === "unread" && loginYn)
            ? "/spendolive/notice/ajax/unreadNoticeList.do"
            : "/spendolive/notice/ajax/noticeList.do";

        fetch(url, { credentials: 'same-origin' })
            .then(response => response.json())
            .then(data => {

                // 비로그인 안 읽은 공지: localStorage 기준으로 필터링
                if (filter === "unread" && !loginYn) {
                    data = data.filter(notice =>
                        localStorage.getItem("notice_read_" + notice.noticeId) !== "Y"
                    );
                }

                currentNoticeData = data;
                currentPage = 1;
                drawNoticePage();
            });
}

function drawNoticePage() {
    const tbody = document.getElementById("noticeTableBody");
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    const pageData = currentNoticeData.slice(start, end);

    let html = "";

    if (pageData.length === 0) {
        html = `
            <tr>
                <td colspan="6" class="notice-empty">
                    공지사항이 없습니다.
                </td>
            </tr>
        `;
    } else {
        pageData.forEach((notice, index) => {
            const star = notice.starYn === "Y" ? "★" : "☆";

            const pinnedBadge = notice.pinnedYn === "Y"
                ? `<span class="chip notice-important">중요</span>`
                : `<span class="chip notice-normal">일반</span>`;

            const localRead =
                localStorage.getItem("notice_read_" + notice.noticeId) === "Y";

            const titleClass =
                notice.readYn === "Y" || (!loginYn && localRead)
                    ? "notice-read-title"
                    : "notice-unread-title";

            html += `
                <tr>
                <td>${notice.pinnedYn === "Y" ? "📌" : currentNoticeData.length - start - index }</td>
                    <td>
                        ${
                            loginYn
                            ? `<button type="button"
                                       class="notice-list-star-btn"
                                       onclick="toggleNoticeStar(event, ${notice.noticeId}, this)">
                                    ${star}
                               </button>`
                            : ""
                        }
                    </td>
                    <td>${pinnedBadge}</td>
                    <td>
                        <a class="notice-title-link ${titleClass}"
                           href="/spendolive/notice/detail.do?noticeId=${notice.noticeId}"
                           onclick="saveNoticeReadLocal(${notice.noticeId})">
                            ${notice.title}
                        </a>
                    </td>
                    <td>${notice.adminId}</td>
                    <td>${notice.created_at}</td>
                </tr>
            `;
        });
    }

    tbody.innerHTML = html;
    drawPagination();
}

function drawPagination() {
    const pagination = document.getElementById("noticePagination");
    if (!pagination) return;

    const totalPage = Math.ceil(currentNoticeData.length / pageSize);
    let html = "";

    for (let i = 1; i <= totalPage; i++) {
        html += `
            <button type="button"
                    class="notice-page-btn ${i === currentPage ? "active" : ""}"
                    onclick="moveNoticePage(${i})">
                ${i}
            </button>
        `;
    }

    pagination.innerHTML = html;
}

function moveNoticePage(page) {
    currentPage = page;
    drawNoticePage();
}

let currentNotifData   = [];
let currentNotifPage   = 1;
const notifPageSize    = 10;
let currentNotifFilter = "all";   // 현재 필터 기억 (읽음 처리 후 같은 필터로 재렌더)

function loadNotificationList(filter = "all") {
    currentNotifFilter = filter;

    fetch("/spendolive/notification/ajax/list.do", { credentials: 'same-origin' })
        .then(response => response.json())
        .then(data => {
            if (filter === "unread") {
                data = data.filter(n => n.readYn === "N");
            }
            currentNotifData = data;
            currentNotifPage = 1;
            drawNotifPage();
        })
        .catch(() => {
            document.getElementById("noticeTableBody").innerHTML = `
                <tr><td colspan="6" class="notice-empty">알림을 불러오는 중 오류가 발생했습니다.</td></tr>`;
        });
}

function drawNotifPage() {
    const tbody = document.getElementById("noticeTableBody");
    const start = (currentNotifPage - 1) * notifPageSize;
    const pageData = currentNotifData.slice(start, start + notifPageSize);

    let html = "";

    if (pageData.length === 0) {
        html = `<tr><td colspan="6" class="notice-empty">
                    ${currentNotifFilter === "unread" ? "안 읽은 알림이 없습니다." : "알림이 없습니다."}
                </td></tr>`;
    } else {
        pageData.forEach((notification, index) => {
            const star = notification.starYn === "Y" ? "★" : "☆";
            const readBadge = notification.readYn === "N"
                ? `<span class="chip notice-important">NEW</span>`
                : `<span class="chip notice-normal">읽음</span>`;
            const titleClass = notification.readYn === "Y"
                ? "notice-read-title"
                : "notice-unread-title";

            html += `
                <tr>
                    <td>${currentNotifData.length - start - index }</td>
                    <td>
                        <button type="button"
                                class="notice-list-star-btn"
                                onclick="toggleNotificationStar(event, ${notification.notificationId}, this)">
                            ${star}
                        </button>
                    </td>
                    <td>${readBadge}</td>
                    <td>
                        <a href="#"
                           class="notice-title-link ${titleClass}"
                           onclick="readNotification(event, ${notification.notificationId}, '${notification.linkUrl || ""}')">
                            ${notification.title}
                        </a>
                    </td>
                    <td>${notification.notificationType}</td>
                    <td>${notification.created_at}</td>
                </tr>`;
        });
    }

    tbody.innerHTML = html;
    drawNotifPagination();
}

function drawNotifPagination() {
    const pagination = document.getElementById("noticePagination");
    if (!pagination) return;

    const totalPage = Math.ceil(currentNotifData.length / notifPageSize);
    let html = "";
    for (let i = 1; i <= totalPage; i++) {
        html += `<button type="button"
                         class="notice-page-btn ${i === currentNotifPage ? "active" : ""}"
                         onclick="moveNotifPage(${i})">${i}</button>`;
    }
    pagination.innerHTML = html;
}

function moveNotifPage(page) {
    currentNotifPage = page;
    drawNotifPage();
}

    function loadImportantNoticeList() {
        fetch("/spendolive/notice/ajax/importantList.do", { credentials: 'same-origin' })
            .then(response => response.json())
            .then(data => {

                const tbody =
                    document.getElementById("noticeTableBody");

                let html = "";

                if (data.length === 0) {

                    html = `
                        <tr>
                            <td colspan="6" class="notice-empty">
                                중요 공지가 없습니다.
                            </td>
                        </tr>
                    `;

                } else {

                    data.forEach((notice, index) => {

                        const star = notice.starYn === "Y" ? "★" : "☆";

                        const pinnedBadge =
                            `<span class="chip notice-important">중요</span>`;
                    
                        const titleClass =
                            notice.readYn === "Y" || (!loginYn && localStorage.getItem("notice_read_" + notice.noticeId) === "Y")
                                ? "notice-read-title"
                                : "notice-unread-title";
                    
                        html += `
                            <tr>
                                <td>📌</td>
                                <td>
                                    ${
                                        loginYn
                                        ? `<button type="button"
                                                   class="notice-list-star-btn"
                                                   onclick="toggleNoticeStar(event, ${notice.noticeId}, this)">
                                                ${star}
                                           </button>`
                                        : ""
                                    }
                                </td>
                                <td>${pinnedBadge}</td>
                    
                                <td>
                                    <a class="notice-title-link ${titleClass}"
                                       href="/spendolive/notice/detail.do?noticeId=${notice.noticeId}"
                                       onclick="saveNoticeReadLocal(${notice.noticeId})">
                                        ${notice.title}
                                    </a>
                                </td>
                    
                                <td>${notice.adminId}</td>
                                <td>${notice.created_at}</td>
                            </tr>
                        `;
                    });
                }

                tbody.innerHTML = html;
            });
    }




function readNotification(event, notificationId, linkUrl) {
    event.preventDefault();

    fetch("/spendolive/notification/ajax/read.do", {
        method: "POST",
        credentials: 'same-origin',
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "notificationId=" + notificationId
    })
    .then(response => response.json())
    .then(data => {
        if (data.result === "OK") {
            // 로컬 데이터 즉시 읽음 처리
            const item = currentNotifData.find(n => n.notificationId === notificationId);
            if (item) item.readYn = "Y";

            // 헤더 배지 갱신
            if (typeof loadNotificationBadge === "function") loadNotificationBadge();

            if (linkUrl && linkUrl !== "null" && linkUrl !== "") {
                // linkUrl 있으면 해당 페이지로 이동 (공지 상세 등)
                location.href = linkUrl;
            } else {
                // linkUrl 없으면 알림 상세 페이지로 이동
                location.href = "/spendolive/notification/detail.do?notificationId=" + notificationId;
            }
        } else if (data.result === "LOGIN_REQUIRED") {
            alert("로그인이 필요합니다.");
        } else {
            alert("읽음 처리 중 오류가 발생했습니다.");
        }
    })
    .catch(() => alert("네트워크 오류가 발생했습니다."));
}

document.addEventListener("DOMContentLoaded", function () {
    setBoardTab("notice");
});


function saveNoticeReadLocal(noticeId) {
    localStorage.setItem("notice_read_" + noticeId, "Y");
}

function toggleNoticeStar(event, noticeId, button) {
    event.preventDefault();
    event.stopPropagation();

    if (!loginYn) {
        return;
    }

    fetch("/spendolive/notice/ajax/star.do", {
        method: "POST",
        credentials: 'same-origin',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "noticeId=" + noticeId
    })
    .then(response => response.json())
    .then(data => {
        if (data.result === "OK") {
            button.textContent =
                button.textContent.trim() === "★" ? "☆" : "★";
        }
    });
}

function toggleNotificationStar(event, notificationId, button) {
    event.preventDefault();
    event.stopPropagation();

    fetch("/spendolive/notification/ajax/star.do", {
        method: "POST",
        credentials: 'same-origin',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "notificationId=" + notificationId
    })
    .then(response => response.json())
    .then(data => {
        if (data.result === "OK") {
            button.textContent =
                button.textContent.trim() === "★" ? "☆" : "★";
        }
    });
}