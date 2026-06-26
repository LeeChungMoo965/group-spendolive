function setBoardTab(mode) {
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

        const url =
        filter === "unread"
            ? "/spendolive/notice/ajax/unreadNoticeList.do"
            : "/spendolive/notice/ajax/noticeList.do";

        fetch(url)

            .then(response => response.json())
            .then(data => {
                const tbody = document.getElementById("noticeTableBody");
                let html = "";

                if (data.length === 0) {
                    html = `
                        <tr>
                            <td colspan="6" class="notice-empty">
                                공지사항이 없습니다.
                            </td>
                        </tr>
                    `;
                } else {
                    data.forEach((notice, index) => {

                        const star = notice.starYn === "Y" ? "★" : "☆";


                        const pinnedBadge = notice.pinnedYn === "Y"
                            ? `<span class="chip notice-important">중요</span>`
                            : `<span class="chip notice-normal">일반</span>`;

                        const titleClass = notice.readYn === "Y"
                            ? "notice-read-title"
                            : "notice-unread-title";
                                        

                            html += `
                            <tr>
                                <td>${index + 1}</td>
                            
                                <td>
                                    <button
                                        type="button"
                                        class="notice-list-star-btn"
                                        onclick="toggleNoticeStar(event, ${notice.noticeId}, this)">
                                        ${star}
                                    </button>
                                </td>
                            
                                <td>${pinnedBadge}</td>
                            
                                <td>
                                    <a class="notice-title-link ${titleClass}"
                                       href="/spendolive/notice/detail.do?noticeId=${notice.noticeId}">
                                        ${notice.title}
                                    </a>
                                </td>
                            
                                <td>${notice.adminId}</td>
                                <td>${notice.createdAt}</td>
                            </tr>
                            `;

                    });
                }

        tbody.innerHTML = html;
    });
}

    function loadNotificationList(filter = "all") {
        fetch("/spendolive/notification/ajax/list.do")
            .then(response => response.json())
            .then(data => {
                if (filter === "unread") {
                    data = data.filter(notification => notification.readYn === "N");
                }

                const tbody = document.getElementById("noticeTableBody");
                let html = "";

                if (data.length === 0) {
                    html = `
                        <tr>
                            <td colspan="6" class="notice-empty">
                                알림이 없습니다.
                            </td>
                        </tr>
                    `;
                } else {
                    data.forEach((notification, index) => {
                        const star = notification.starYn === "Y" ? "★" : "☆";
                        const readBadge = notification.readYn === "N"
                            ? `<span class="chip notice-important">NEW</span>`
                            : `<span class="chip notice-normal">읽음</span>`;

                        const titleClass = notification.readYn === "Y"
                            ? "notice-read-title"
                            : "notice-unread-title";    

                        html += `
                            
                            <tr>
                            <td>${index + 1}</td>
                        
                            <td>
                                <button
                                    type="button"
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
                                <td>${notification.createdAt}</td>
                            </tr>
                        `;
                    });
                }

                tbody.innerHTML = html;
            });
        
    }

    function loadImportantNoticeList() {
        fetch("/spendolive/notice/ajax/importantList.do")
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

                        const pinnedBadge =
                            `<span class="chip notice-important">중요</span>`;
                    
                        const titleClass =
                            notice.readYn === "Y"
                                ? "notice-read-title"
                                : "notice-unread-title";
                    
                        html += `
                            <tr>
                                <td>${index + 1}</td>
                                <td>${pinnedBadge}</td>
                    
                                <td>
                                    <a class="notice-title-link ${titleClass}"
                                       href="/spendolive/notice/detail.do?noticeId=${notice.noticeId}">
                                        ${notice.title}
                                    </a>
                                </td>
                    
                                <td>${notice.adminId}</td>
                                <td>${notice.createdAt}</td>
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
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "notificationId=" + notificationId
    })
    .then(response => response.json())
    .then(data => {
        if (data.result === "OK") {
            if (linkUrl && linkUrl !== "null") {
                location.href = linkUrl;
            } else {
                loadNotificationList("all");
            }
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    setBoardTab("notice");
});


function toggleNoticeStar(event, noticeId, button) {
    event.preventDefault();
    event.stopPropagation();

    button.textContent =
        button.textContent.trim() === "★" ? "☆" : "★";
}

function toggleNotificationStar(event, notificationId, button) {
    event.preventDefault();
    event.stopPropagation();

    fetch("/spendolive/notification/ajax/star.do", {
        method: "POST",
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