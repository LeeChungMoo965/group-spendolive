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
   - "문의사항" / "자주 묻는 질문" 탭이나 그 안의 상태 필터·페이지네이션을 눌러도
     큰 제목(hero)은 그대로 두고 #adminBoardArea(탭+게시판)만 AJAX로 바꿔치기 한다.
   - 페이지네이션/상태필터도 결국 같은 목록 페이지로 가는 링크라 똑같이 처리된다.
   ========================================================= */
(function () {
    const BOARD_AREA_ID = 'adminBoardArea';
    const BOARD_URL_PATTERNS = ['/admin/inquiry/list.do', '/admin/faq/list.do'];

    function isBoardLink(href) {
        if (!href) return false;
        return BOARD_URL_PATTERNS.some(p => href.includes(p));
    }

    function swapBoardArea(html, pushUrl) {
        const doc = new DOMParser().parseFromString(html, 'text/html');
        const newArea = doc.getElementById(BOARD_AREA_ID);
        const curArea = document.getElementById(BOARD_AREA_ID);
        if (!newArea || !curArea) {
            // 구조가 안 맞으면 안전하게 그냥 이동
            window.location.href = pushUrl;
            return;
        }
        curArea.innerHTML = newArea.innerHTML;
        if (pushUrl) {
            history.pushState({ boardUrl: pushUrl }, '', pushUrl);
        }
    }

    function loadBoard(url, push) {
        fetch(url, { credentials: 'same-origin' })
            .then(res => {
                if (!res.ok) throw new Error('board fetch failed: ' + res.status);
                return res.text();
            })
            .then(html => swapBoardArea(html, push ? url : null))
            .catch(() => { window.location.href = url; });
    }

    document.addEventListener('click', function (e) {
        const boardArea = document.getElementById(BOARD_AREA_ID);
        if (!boardArea) return;

        const link = e.target.closest('a');
        if (!link || !boardArea.contains(link)) return;
        if (!isBoardLink(link.getAttribute('href'))) return;

        e.preventDefault();
        loadBoard(link.href, true);
    });

    window.addEventListener('popstate', function (e) {
        if (e.state && e.state.boardUrl) {
            loadBoard(e.state.boardUrl, false);
        }
    });
})();