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
<<<<<<< HEAD
function comment(reportId, reportedmember_id,count,reportReason) {

    $('#form_report_id').val(reportId);
    $('#form_reported_member_id').val(reportedmember_id);
=======
function comment(report_id, reported_member_id,count,report_reason) {

    $('#form_report_id').val(report_id);
    $('#form_reported_member_id').val(reported_member_id);
>>>>>>> aitest
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