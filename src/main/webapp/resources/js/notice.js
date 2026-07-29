let currentPage = 1;
const pageSize = 10;
let currentNoticeData = [];
// 지금 선택된 공지 필터(all/unread/important). 목록에서 상세로 이동할 때
// URL에 같이 실어 보내서, 상세에서 "목록으로" 눌렀을 때 같은 필터로 돌아오게 함
let currentNoticeFilter = "all";



function setBoardTab(mode, initialFilter) {


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
            <button type="button" class="notification-filter-btn"
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

        // 상세 페이지에서 "목록으로"를 눌러 filter=unread/important를 달고 돌아온 경우,
        // 그 필터 버튼을 활성화하고 그 필터 그대로 목록을 불러옴. 없으면 기본값(전체).
        const filterButtons = boardFilter.querySelectorAll(".notification-filter-btn");
        if (initialFilter === "unread") {
            filterButtons[1].classList.add("active");
            loadNoticeList("unread");
        } else if (initialFilter === "important") {
            filterButtons[2].classList.add("active");
            loadImportantNoticeList();
        } else {
            filterButtons[0].classList.add("active");
            loadNoticeList("all");
        }
    }
}

    function setFilterActive(button) {

        document.querySelectorAll(".notification-filter-btn")
            .forEach(btn => btn.classList.remove("active"));

        button.classList.add("active");
}


    function loadNoticeList(filter = "all") {
        currentNoticeFilter = filter;

        // 비로그인 + 안 읽은 공지 필터 → 전체를 받아서 클라이언트에서 걸러냄
        const url = (filter === "unread" && loginYn)
            ? "/spendolive/notice/ajax/unreadNoticeList.do"
            : "/spendolive/notice/ajax/noticeList.do";

        /* [AJAX] GET /notice/ajax/noticeList.do (또는 unreadNoticeList.do)
           - 서버에 credentials: 'same-origin' 옵션을 줘서 로그인 세션 쿠키를 같이 보냄
             (로그인 여부에 따라 read_yn 등 개인화된 값이 응답에 실려옴)
           - 응답은 JSON 배열(공지 목록) 그대로 받아서 currentNoticeData에 저장,
             실제 화면 그리기(<tr> 생성)는 여기서 안 하고 drawNoticePage()에 위임함
             → 페이지 이동(moveNoticePage) 시 서버 재요청 없이 저장해둔 배열만 잘라 씀 */


        fetch(url, { credentials: 'same-origin' })
            .then(response => response.json())
            .then(data => {

                // 비로그인 안 읽은 공지: localStorage 기준으로 필터링
                if (filter === "unread" && !loginYn) {
                    data = data.filter(notice =>
                        localStorage.getItem("notice_read_" + notice.notice_id) !== "Y"
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
            const star = notice.star_yn === "Y" ? "★" : "☆";

            const pinnedBadge = notice.pinned_yn === "Y"
                ? `<span class="chip notice-important">중요</span>`
                : `<span class="chip notice-normal">일반</span>`;

            const localRead =
                localStorage.getItem("notice_read_" + notice.notice_id) === "Y";

            const titleClass =
                notice.read_yn === "Y" || (!loginYn && localRead)
                    ? "notice-read-title"
                    : "notice-unread-title";

            html += `
                <tr>
                <td>${notice.pinned_yn === "Y" ? "📌" : currentNoticeData.length - start - index }</td>
                    <td>
                        ${
                            loginYn
                            ? `<button type="button"
                                       class="notice-list-star-btn"
                                       onclick="toggleNoticeStar(event, ${notice.notice_id}, this)">
                                    ${star}
                               </button>`
                            : ""
                        }
                    </td>
                    <td>${pinnedBadge}</td>
                    <td>
                        <a class="notice-title-link ${titleClass}"
                           href="/spendolive/notice/detail.do?notice_id=${notice.notice_id}&filter=${currentNoticeFilter}"
                           onclick="saveNoticeReadLocal(${notice.notice_id})">
                            ${notice.title}
                        </a>
                    </td>
                    <td>${notice.admin_id}</td>
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

    // 페이지가 1개(또는 0개, 데이터 없음)뿐이면 페이지 번호 자체를 안 보여줌
    if (totalPage <= 1) {
        pagination.innerHTML = "";
        return;
    }

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

     /* [AJAX] GET /notification/ajax/list.do
       - 로그인한 사용자의 알림 전체를 받아온 뒤, "안 읽은 알림" 필터는
         서버에 따로 안 물어보고 클라이언트에서 read_yn === "N"만 걸러냄
         (알림은 공지와 달리 비로그인 접근 시나리오가 없어서 이 요청 자체가
          로그인 필요 기능임 - setBoardTab에서 이미 loginYn 체크 후 호출됨) */
    fetch("/spendolive/notification/ajax/list.do", { credentials: 'same-origin' })
        .then(response => response.json())
        .then(data => {
            if (filter === "unread") {
                data = data.filter(n => n.read_yn === "N");
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
            const star = notification.star_yn === "Y" ? "★" : "☆";
            const readBadge = notification.read_yn === "N"
                ? `<span class="chip notice-important">NEW</span>`
                : `<span class="chip notice-normal">읽음</span>`;
            const titleClass = notification.read_yn === "Y"
                ? "notice-read-title"
                : "notice-unread-title";

            html += `
                <tr>
                    <td>${currentNotifData.length - start - index }</td>
                    <td>
                        <button type="button"
                                class="notice-list-star-btn"
                                onclick="toggleNotificationStar(event, ${notification.notification_id}, this)">
                            ${star}
                        </button>
                    </td>
                    <td>${readBadge}</td>
                    <td>
                        <a href="#"
                           class="notice-title-link ${titleClass}"
                           onclick="readNotification(event, ${notification.notification_id}, '${notification.link_url || ""}')">
                            ${notification.title}
                        </a>
                    </td>
                    <td>${notification.notification_type}</td>
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

    // 페이지가 1개(또는 0개, 데이터 없음)뿐이면 페이지 번호 자체를 안 보여줌
    if (totalPage <= 1) {
        pagination.innerHTML = "";
        return;
    }

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
        currentNoticeFilter = "important";
        /* [AJAX] GET /notice/ajax/importantList.do
           - pinned_yn = 'Y'(중요 고정) 공지만 서버에서 걸러서 내려줌
           - 목록 전체(loadNoticeList)와 달리 페이지네이션 없이 한 번에 다 그림
             (중요 공지는 개수가 적을 거라는 전제) */
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

                        const star = notice.star_yn === "Y" ? "★" : "☆";

                        const pinnedBadge =
                            `<span class="chip notice-important">중요</span>`;
                    
                        const titleClass =
                            notice.read_yn === "Y" || (!loginYn && localStorage.getItem("notice_read_" + notice.notice_id) === "Y")
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
                                                   onclick="toggleNoticeStar(event, ${notice.notice_id}, this)">
                                                ${star}
                                           </button>`
                                        : ""
                                    }
                                </td>
                                <td>${pinnedBadge}</td>
                    
                                <td>
                                    <a class="notice-title-link ${titleClass}"
                                       href="/spendolive/notice/detail.do?notice_id=${notice.notice_id}&filter=${currentNoticeFilter}"
                                       onclick="saveNoticeReadLocal(${notice.notice_id})">
                                        ${notice.title}
                                    </a>
                                </td>
                    
                                <td>${notice.admin_id}</td>
                                <td>${notice.created_at}</td>
                            </tr>
                        `;
                    });
                }

                tbody.innerHTML = html;
            });
    }




function readNotification(event, notification_id, link_url) {
    event.preventDefault();

        /* [AJAX] POST /notification/ajax/read.do
       - 알림 클릭 시 <a href="#">의 기본 이동을 막고(preventDefault) 먼저
         서버에 읽음 처리를 요청한 뒤, 성공하면 location.href로 직접 이동시킴
         (읽음 처리와 페이지 이동을 하나의 흐름으로 묶기 위해 GET 링크 대신 fetch 사용)
       - body는 application/x-www-form-urlencoded 형식의 단순 폼 데이터 문자열 */

    fetch("/spendolive/notification/ajax/read.do", {
        method: "POST",
        credentials: 'same-origin',
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "notification_id=" + notification_id
    })
    .then(response => response.json())
    .then(data => {
        if (data.result === "OK") {
            // 로컬 데이터 즉시 읽음 처리
            const item = currentNotifData.find(n => n.notification_id === notification_id);
            if (item) item.read_yn = "Y";

            // 헤더 배지 갱신
            if (typeof loadNotificationBadge === "function") loadNotificationBadge();

            if (link_url && link_url !== "null" && link_url !== "") {
                // link_url 있으면 해당 페이지로 이동 (공지 상세 등)
                location.href = link_url;
            } else {
                // link_url 없으면 알림 상세 페이지로 이동
                location.href = "/spendolive/notification/detail.do?notification_id=" + notification_id;
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
    // 이 페이지에 공지/알림 탭 UI 자체가 없으면(=noticeCenter.jsp가 아니면) 아무것도 안 함.
    // notice.js를 공지 상세 페이지(noticeDetail.jsp)에서도 찜하기 공용 함수 때문에
    // 같이 불러오게 되면서, 탭 관련 요소가 없는 페이지에서 setBoardTab이 에러 없이
    // 조용히 무시되도록 가드를 추가함.
    if (document.getElementById("noticeTabBtn")) {
        const params = new URLSearchParams(location.search);
        // ?tab=alert 이면 알림 탭으로 시작 (헤더 종모양의 '전체보기'가 이 링크로 연결됨).
        // 그 외에는 기존대로 공지 탭. (공지 상세에서 filter=unread/important 달고 돌아온 경우 반영)
        if (params.get("tab") === "alert") {
            setBoardTab("alert");
        } else {
            const urlFilter = params.get("filter");
            setBoardTab("notice", urlFilter);
        }
    }
});


function saveNoticeReadLocal(notice_id) {
    localStorage.setItem("notice_read_" + notice_id, "Y");
}

/* =========================================================
   공지 찜(star) 토글 - 목록(notice.js)과 상세(noticeDetail.js) 공용
   서버에 POST해서 토글 요청만 보내고 결과(JSON)를 그대로 반환함.
   버튼 텍스트 갱신이나 localStorage 처리 등 화면 갱신은 호출부에서 각자 처리
   (목록은 버튼 텍스트만 바꾸면 되지만, 상세는 localStorage까지 같이
    관리해야 해서 화면 갱신 로직 자체는 통일하지 않고 통신 부분만 공유함)
   ========================================================= */
/* [AJAX] POST /notice/ajax/star.do
- Promise를 return해서 호출부(toggleNoticeStar, noticeDetail.js)가
 각자 .then()으로 이어서 화면(버튼 텍스트/localStorage)을 갱신하도록 함
- 통신 로직만 여기 한 곳에 모아두고, 성공 후 UI 처리는 호출부 책임으로 분리 */
function postNoticeStarToggle(noticeId) {
    return fetch("/spendolive/notice/ajax/star.do", {
        method: "POST",
        credentials: 'same-origin',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "notice_id=" + noticeId
    }).then(response => response.json());
}

function toggleNoticeStar(event, notice_id, button) {
    event.preventDefault();
    event.stopPropagation();

    if (!loginYn) {
        return;
    }

    postNoticeStarToggle(notice_id).then(data => {
        if (data.result === "OK") {
            button.textContent =
                button.textContent.trim() === "★" ? "☆" : "★";
        } else if (data.result === "LOGIN_REQUIRED") {
            alert("로그인이 필요합니다.");
        } else {
            alert("처리 중 오류가 발생했습니다.");
        }
    })
    .catch(() => alert("네트워크 오류가 발생했습니다."));
}

function toggleNotificationStar(event, notification_id, button) {
    event.preventDefault();
    event.stopPropagation();


    /* [AJAX] POST /notification/ajax/star.do
       - 공지 찜(postNoticeStarToggle)과 구조는 동일하지만 알림은 항상 로그인
         상태에서만 목록이 뜨므로 별도의 loginYn 체크 없이 바로 요청함 */

    fetch("/spendolive/notification/ajax/star.do", {
        method: "POST",
        credentials: 'same-origin',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "notification_id=" + notification_id
    })
    .then(response => response.json())
    .then(data => {
        if (data.result === "OK") {
            button.textContent =
                button.textContent.trim() === "★" ? "☆" : "★";
        }
    });
}