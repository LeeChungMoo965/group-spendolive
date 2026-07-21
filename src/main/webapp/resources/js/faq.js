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
    // 카테고리를 누르면 검색어/검색 결과 숨김 상태를 초기화하고 카테고리 기준으로만 다시 보여준다
    if (document.querySelectorAll('.faq-list').length === 0) return;
    const searchInput = document.getElementById('faqSearchInput');
    if (searchInput) searchInput.value = '';
    document.querySelectorAll('.faq-item').forEach(item => { item.style.display = ''; });

    document.querySelectorAll('.cat-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    let totalVisible = 0;
    document.querySelectorAll('.faq-list').forEach(list => {
        const matches = (cat === 'all') || (list.dataset.cat === cat);
        list.style.display = matches ? '' : 'none';

        const label = list.previousElementSibling;
        if (label && label.classList.contains('section-label')) {
            label.style.display = matches ? '' : 'none';
        }
        if (matches) {
            totalVisible += list.querySelectorAll('.faq-item').length;
        }
    });

    // 해당 카테고리에 등록된 FAQ가 하나도 없으면 빈 공간 대신 안내 문구로 채운다
    // (내용이 사라지면서 화면 높이가 확 줄어드는 것 방지)
    const emptyBox = document.getElementById('faqSearchEmpty');
    const emptyIcon = document.getElementById('faqSearchEmptyIcon');
    const emptyText = document.getElementById('faqSearchEmptyText');
    if (emptyBox) {
        emptyBox.style.display = totalVisible === 0 ? '' : 'none';
        if (totalVisible === 0) {
            if (emptyIcon) emptyIcon.textContent = '📭';
            if (emptyText) emptyText.textContent = '아직 등록된 FAQ가 없습니다.';
        }
    }
}

/* ---- faqList.jsp : 질문/답변 텍스트 검색 (전체 카테고리 대상) ---- */
function searchFaq() {
    const input = document.getElementById('faqSearchInput');
    const query = input ? input.value.trim().toLowerCase() : '';
    const emptyBox = document.getElementById('faqSearchEmpty');
    const emptyIcon = document.getElementById('faqSearchEmptyIcon');
    const emptyText = document.getElementById('faqSearchEmptyText');

    // 검색은 카테고리 구분 없이 전체에서 찾는 게 자연스러우므로 카테고리 버튼을 '전체'로 리셋
    const catButtons = document.querySelectorAll('.cat-btn');
    catButtons.forEach(b => b.classList.remove('active'));
    if (catButtons.length > 0) catButtons[0].classList.add('active');

    let totalVisible = 0;

    document.querySelectorAll('.faq-list').forEach(list => {
        let visibleInGroup = 0;
        list.querySelectorAll('.faq-item').forEach(item => {
            const qText = item.querySelector('.faq-q-left') ? item.querySelector('.faq-q-left').textContent : '';
            const aText = item.querySelector('.faq-a-inner') ? item.querySelector('.faq-a-inner').textContent : '';
            const matches = query === '' || (qText + ' ' + aText).toLowerCase().includes(query);
            item.style.display = matches ? '' : 'none';
            if (matches) visibleInGroup++;
        });

        list.style.display = visibleInGroup > 0 ? '' : 'none';
        const label = list.previousElementSibling;
        if (label && label.classList.contains('section-label')) {
            label.style.display = visibleInGroup > 0 ? '' : 'none';
        }

        totalVisible += visibleInGroup;
    });

    if (emptyBox) {
        emptyBox.style.display = totalVisible === 0 ? '' : 'none';
        if (totalVisible === 0) {
            if (emptyIcon) emptyIcon.textContent = '🔍';
            if (emptyText) emptyText.textContent = '검색 결과가 없습니다.';
        }
    }
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
function goInquiryDetail(contextPath, inquiry_id) {
    window.location.href = contextPath + '/spendolive/inquiry/detail.do?inquiryNo=' + encodeURIComponent(inquiry_id);
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



    const submitBtn = formEl.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = '등록 중...';
    
    return true;
}