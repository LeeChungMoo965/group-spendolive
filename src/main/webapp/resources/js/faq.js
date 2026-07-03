/* ============================================================
   FAQ / 문의하기 공통 스크립트
   경로: resources/js/faq.js
   대상 페이지: faqList.jsp, inquiryWrite.jsp, inquiryList.jsp
   ============================================================ */

/* ---- faqList.jsp : 아코디언 토글 ---- */
function toggleFaq(el) {
    const isOpen = el.classList.contains('open');
    document.querySelectorAll('.faq-item.open').forEach(i => i.classList.remove('open'));
    if (!isOpen) el.classList.add('open');
}

/* ---- faqList.jsp : 카테고리 필터 ---- */
function filterFaqCat(btn, cat) {
    document.querySelectorAll('.cat-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    document.querySelectorAll('.faq-list,.section-label').forEach(el => {
        if (cat === 'all') { el.style.display = ''; return; }
        if (el.dataset.cat) {
            el.style.display = (el.dataset.cat === cat) ? '' : 'none';
        } else {
            const next = el.nextElementSibling;
            el.style.display = (next && next.dataset.cat === cat) ? '' : 'none';
        }
    });
}

/* ---- inquiryList.jsp : 상태 필터 ---- */
function filterInquiryStatus(btn, status) {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    document.querySelectorAll('.inq-card').forEach(card => {
        card.style.display = (status === 'all' || card.dataset.status === status) ? '' : 'none';
    });
}

/* ---- inquiryList.jsp : 카드 클릭 시 상세 이동 ---- */
function goInquiryDetail(contextPath, inquiryId) {
    window.location.href = contextPath + '/spendolive/inquiry/detail.do?inquiryNo=' + encodeURIComponent(inquiryId);
}

/* ---- inquiryWrite.jsp : 개인정보 수집·이용 상세 [자세히 보기] 토글 ---- */
function togglePrivacyDetail(linkEl) {
    const detail = document.getElementById('privacyDetail');
    if (!detail) return;
    const isOpen = detail.classList.toggle('open');
    linkEl.textContent = isOpen ? '[접기]' : '[자세히 보기]';
}

/* ---- inquiryWrite.jsp : 글자수 카운트 ---- */
function countChars(inputId, countId, max) {
    const val = document.getElementById(inputId).value.length;
    const el = document.getElementById(countId);
    el.textContent = val;
    el.style.color = val > max * 0.9 ? '#c0564a' : 'var(--so-olive-dark)';
}

/* ---- inquiryWrite.jsp : 첨부파일 선택 시 파일명 표시 ---- */
function handleFileSelect(input) {
    const list = document.getElementById('uploadFileNames');
    if (!list) return;
    if (!input.files || input.files.length === 0) {
        list.textContent = '';
        return;
    }
    const names = Array.from(input.files).map(f => f.name);
    list.textContent = '선택된 파일: ' + names.join(', ');
}

/* ---- inquiryWrite.jsp : 제출 전 유효성 검사 (실제 제출은 form action으로 처리) ---- */
function validateInquiryForm(formEl) {
    const privacyCheck = document.getElementById('privacyCheck');
    if (!privacyCheck.checked) {
        alert('개인정보 수집 및 이용에 동의해 주세요.');
        return false;
    }
    const title = document.getElementById('titleInput').value.trim();
    const body = document.getElementById('bodyInput').value.trim();
    if (!title || !body) {
        alert('제목과 상세 내용을 입력해 주세요.');
        return false;
    }
    return true;
}