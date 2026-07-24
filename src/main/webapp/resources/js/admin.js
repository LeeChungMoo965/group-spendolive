(function () {
    const page = document.body.dataset.page;
    document.querySelectorAll('[data-nav]').forEach(link => {
        if (link.dataset.nav === page) {
            link.classList.add('active');
        }
    });

    const toast = document.querySelector('.toast');
    function showToast(message) {
        if (!toast) return;
        toast.textContent = message;
        toast.classList.add('show');
        window.clearTimeout(window.__toastTimer);
        window.__toastTimer = window.setTimeout(() => toast.classList.remove('show'), 1800);
    }

    document.querySelectorAll('[data-toast]').forEach(btn => {
        btn.addEventListener('click', () => showToast(btn.dataset.toast));
    });

    document.querySelectorAll('.filter-pills button').forEach(btn => {
        btn.addEventListener('click', () => {
            const group = btn.closest('.filter-pills');
            group.querySelectorAll('button').forEach(item => item.classList.remove('active'));
            btn.classList.add('active');
            showToast(btn.textContent.trim() + ' 기준으로 화면이 필터링되었습니다.');
        });
    });

    document.querySelectorAll('[data-search-table]').forEach(input => {
        const tableSelector = input.dataset.searchTable;
        const table = document.querySelector(tableSelector);
        if (!table) return;
        input.addEventListener('input', () => {
            const keyword = input.value.trim().toLowerCase();
            table.querySelectorAll('tbody tr').forEach(row => {
                row.style.display = row.textContent.toLowerCase().includes(keyword) ? '' : 'none';
            });
        });
    });
})();

function comment(report_id, reported_member_id,count,report_reason) {

    $('#form_report_id').val(report_id);
    $('#form_reported_member_id').val(reported_member_id);
    $('#form_count').val(count);
    $('#form_report_reason').val(report_reason);

    $('#commentArea').show();
 
    $('html, body').animate({
        scrollTop: $("#commentArea").offset().top
    }, 500);
}

function selectList(listData) {
    List = listData; 
    render(); 
}

/* =========================================================
   문의 관리 ↔ FAQ 관리 탭 전환
   ========================================================= */
   (function () {
    const BOARD_AREA_ID = 'adminBoardArea';
    // 이 두 URL로 가는 링크만 "AJAX로 바꿔치기 대상"으로 취급함
    // (탭 링크뿐 아니라 상태필터·페이지네이션도 결국 이 URL들로 다시 요청하므로 자동 포함됨)
    const BOARD_URL_PATTERNS = ['/admin/inquiry/list.do', '/admin/faq/list.do'];

    // 클릭된 링크의 href가 위 두 URL 패턴 중 하나를 포함하는지 검사
    function isBoardLink(href) {
        if (!href) return false;
        return BOARD_URL_PATTERNS.some(p => href.includes(p));
    }

    // 서버에서 새로 받아온 html(페이지 전체 응답) 중에서
    // #adminBoardArea 부분만 뽑아내 현재 화면의 #adminBoardArea와 교체
    // + 주소창 URL도 pushState로 바꿔서 새로고침/뒤로가기가 자연스럽게 동작하도록 함
    function swapBoardArea(html, pushUrl) {
        const doc = new DOMParser().parseFromString(html, 'text/html');
        const newArea = doc.getElementById(BOARD_AREA_ID);
        const curArea = document.getElementById(BOARD_AREA_ID);
        if (!newArea || !curArea) {
            // 응답 구조가 예상과 다르면(예: 에러 페이지) AJAX를 포기하고
            // 그냥 일반 페이지 이동으로 안전하게 폴백
            window.location.href = pushUrl;
            return;
        }
        curArea.innerHTML = newArea.innerHTML;
        if (pushUrl) {
            history.pushState({ boardUrl: pushUrl }, '', pushUrl);
        }
    }

    // 실제로 목록 페이지를 fetch로 받아와서 swapBoardArea에 넘겨주는 함수
    // push=true면 주소창 URL도 갱신(사용자가 직접 클릭한 경우),
    // push=false면 갱신 안 함(popstate로 브라우저 뒤로가기 했을 때 다시 push할 필요 없음)
    function loadBoard(url, push) {
        fetch(url, { credentials: 'same-origin' })
            .then(res => {
                if (!res.ok) throw new Error('board fetch failed: ' + res.status);
                return res.text();
            })
            .then(html => swapBoardArea(html, push ? url : null))
            .catch(() => { window.location.href = url; });
    }

    // #adminBoardArea 안에서 클릭이 일어났을 때, 그 클릭이
    // 탭/필터/페이지네이션 링크(a 태그)인지 확인하고 맞으면
    // 기본 이동(preventDefault)을 막고 대신 AJAX로 처리
    document.addEventListener('click', function (e) {
        const boardArea = document.getElementById(BOARD_AREA_ID);
        if (!boardArea) return;

        const link = e.target.closest('a');
        if (!link || !boardArea.contains(link)) return;
        if (!isBoardLink(link.getAttribute('href'))) return;

        e.preventDefault();
        loadBoard(link.href, true);
    });

    // 브라우저 뒤로가기/앞으로가기(popstate) 시에도 페이지 전체 새로고침 대신
    // 이전에 pushState로 저장해둔 URL 정보로 다시 AJAX 로드
    window.addEventListener('popstate', function (e) {
        if (e.state && e.state.boardUrl) {
            loadBoard(e.state.boardUrl, false);
        }
    });
})();
/* =========================================================
   adminFaqWrite.jsp / adminNoticeWrite.jsp
   체크박스 값을 hidden input으로 변환한 뒤 제출
   (체크박스는 체크 안 되면 name을 가진 값이 제출 자체가 안 되는 HTML 특성을 보완)
   ========================================================= */
function bindCheckboxAsHidden(formId, checkboxId, hiddenName) {
    const form = document.getElementById(formId);
    const cb = document.getElementById(checkboxId);
    if (!form || !cb) return;

    form.addEventListener('submit', function () {
        cb.name = '';
        const hidden = document.createElement('input');
        hidden.type = 'hidden';
        hidden.name = hiddenName;
        hidden.value = cb.checked ? 'Y' : 'N';
        form.appendChild(hidden);
    });
}

/* =========================================================
   관리자 문의 목록 
   ========================================================= */
   function openAdminInquiryModal(inquiryId) {
    const tpl = document.getElementById('adminInqDetailTpl' + inquiryId);
    const body = document.getElementById('adminInqDetailBody');
    if (!tpl || !body) return;
    body.innerHTML = tpl.innerHTML;
    document.getElementById('adminInqDetailModal').classList.add('show');
}
function closeAdminInquiryModal(e) {
    document.getElementById('adminInqDetailModal').classList.remove('show');
}
 