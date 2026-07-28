/* ============================================================
   inquiry.js  (사용자 문의사항 AJAX)
   - 목록 필터/페이지네이션: list.do를 fetch해서 #inqBoardArea 조각만 교체
     (전체 새로고침 없음. 관리자 admin.js의 swapBoardArea와 같은 방식)
   - 삭제/작성/수정: /spendolive/inquiry/ajax/* 로 JSON 주고받음
   - 상세 보기는 기존 faq.js의 openInqDetailModal(숨김 템플릿 복사)을 그대로 사용
   - 경로는 프로젝트 컨벤션대로 컨텍스트 없이 루트 기준(/spendolive/inquiry/...) 사용
   ============================================================ */

   (function () {
    "use strict";

    /* ─────────────────────────────────────────────────────────────
       이 화면 전용 알림/확인 모달 (지역 함수 — 공용 파일/soModal.js 없이 자체 포함)
       회원가입의 payment-status 모달과 같은 느낌(둥근 카드 + 아이콘 + 확인 버튼).
       CSS·DOM은 처음 호출될 때 만들고, id로 가드해서 중복 생성 방지.
         soAlert("메시지")                     // 성공(초록 ✓)
         soAlert("메시지", { type: "error" })  // 실패(빨강 !)
         soAlert(...).then(() => { ... })      // 확인 누른 뒤 실행
         soConfirm("정말?").then(ok => { if (ok) ... })
       ───────────────────────────────────────────────────────────── */
    function soEnsureModal() {
        if (document.getElementById("soLocalModalStyle") == null) {
            var css = ""
              + ".so-local-overlay{position:fixed;inset:0;z-index:100000;display:flex;align-items:center;justify-content:center;padding:1rem;background:rgba(20,29,18,.48);backdrop-filter:blur(2px);}"
              + ".so-local-overlay[hidden]{display:none;}"
              + ".so-local-box{width:min(100%,23rem);padding:2rem 1.5rem 1.5rem;border-radius:1.25rem;background:#fff;box-shadow:0 1.5rem 4rem rgba(21,40,18,.24);text-align:center;animation:soLocalPop .18s ease-out;}"
              + "@keyframes soLocalPop{from{transform:scale(.94);opacity:0;}to{transform:scale(1);opacity:1;}}"
              + ".so-local-icon{display:flex;align-items:center;justify-content:center;width:2.75rem;height:2.75rem;margin:0 auto 1.125rem;border-radius:50%;background:#eef5df;color:#5f7628;font-size:1.5rem;font-weight:900;}"
              + ".so-local-overlay[data-state='error'] .so-local-icon{background:#fff0eb;color:#c0392b;}"
              + ".so-local-title{margin:0 0 .5rem;color:#26351f;font-size:1.125rem;font-weight:800;white-space:pre-line;line-height:1.5;}"
              + ".so-local-msg{margin:0;color:#6f7b66;line-height:1.65;white-space:pre-line;}"
              + ".so-local-msg[hidden]{display:none;}"
              + ".so-local-actions{display:flex;gap:.5rem;justify-content:center;margin-top:1.5rem;}"
              + ".so-local-btn{min-width:5rem;padding:.65rem 1.2rem;border-radius:.7rem;font-weight:700;font-size:.95rem;cursor:pointer;border:1.5px solid transparent;}"
              + ".so-local-ok{background:#6d7f2e;color:#fff;}.so-local-ok:hover{background:#5f7628;}"
              + ".so-local-cancel{background:#f1f4df;color:#3f4a2c;border-color:#dfe6cb;}.so-local-cancel:hover{border-color:#b9c58f;}"
              + ".so-local-cancel[hidden]{display:none;}";
            var st = document.createElement("style");
            st.id = "soLocalModalStyle";
            st.textContent = css;
            document.head.appendChild(st);
        }
        var el = document.getElementById("soLocalModalOverlay");
        if (el == null) {
            el = document.createElement("div");
            el.id = "soLocalModalOverlay";
            el.className = "so-local-overlay";
            el.setAttribute("role", "dialog");
            el.setAttribute("aria-modal", "true");
            el.hidden = true;
            el.innerHTML =
                  '<div class="so-local-box">'
                +   '<div class="so-local-icon" aria-hidden="true"></div>'
                +   '<h3 class="so-local-title"></h3>'
                +   '<p class="so-local-msg"></p>'
                +   '<div class="so-local-actions">'
                +     '<button type="button" class="so-local-btn so-local-cancel" hidden>취소</button>'
                +     '<button type="button" class="so-local-btn so-local-ok">확인</button>'
                +   '</div>'
                + '</div>';
            document.body.appendChild(el);
        }
        return el;
    }
    function soOpenModal(message, opts, isConfirm) {
        opts = opts || {};
        var el = soEnsureModal();
        var icon = el.querySelector(".so-local-icon");
        var titleEl = el.querySelector(".so-local-title");
        var msgEl = el.querySelector(".so-local-msg");
        var okBtn = el.querySelector(".so-local-ok");
        var cancelBtn = el.querySelector(".so-local-cancel");
        var type = opts.type || (isConfirm ? "info" : "success");
        el.setAttribute("data-state", type === "error" ? "error" : "success");
        icon.textContent = (type === "error") ? "!" : (isConfirm ? "?" : "✓");
        if (opts.title) {
            titleEl.textContent = opts.title;
            msgEl.textContent = message || "";
            msgEl.hidden = !message;
        } else {
            titleEl.textContent = message || "";
            msgEl.hidden = true;
        }
        okBtn.textContent = opts.confirmText || "확인";
        cancelBtn.textContent = opts.cancelText || "취소";
        cancelBtn.hidden = !isConfirm;
        return new Promise(function (resolve) {
            function done(result) {
                el.hidden = true;
                okBtn.onclick = null; cancelBtn.onclick = null; el.onclick = null;
                document.removeEventListener("keydown", onKey);
                resolve(result);
            }
            function onKey(e) {
                if (e.key === "Escape") done(false);
                else if (e.key === "Enter") done(true);
            }
            okBtn.onclick = function () { done(true); };
            cancelBtn.onclick = function () { done(false); };
            el.onclick = function (e) { if (e.target === el) done(false); };
            document.addEventListener("keydown", onKey);
            el.hidden = false;
            okBtn.focus();
        });
    }
    function soAlert(message, opts) { return soOpenModal(message, opts, false); }
    function soConfirm(message, opts) { return soOpenModal(message, opts, true); }

    /* ── 공통: 401(미로그인) 응답이면 로그인 페이지로 ── */
    function handleAuth(res) {
        if (res.status === 401) {
            soAlert("로그인이 필요합니다. 로그인 후 다시 시도해 주세요.", { type: "error" });
            window.location.href = "/member/loginForm.do";
            return true;
        }
        return false;
    }

    /* ════════════════════════════════════════════════════════
       [목록 화면]  #inqBoardArea 가 있을 때만 동작
       ════════════════════════════════════════════════════════ */

    // 현재 보고 있는 필터/페이지 (삭제 후 같은 화면 다시 그릴 때 사용)
    let curStatus = "all";
    let curPage = 1;


    const boardArea = document.getElementById("inqBoardArea");
    if (boardArea) {
        initInquiryListPage();
    }



    function initInquiryListPage() {
        syncStateFromDom();

        // 이벤트 위임: #inqBoardArea 안의 필터·페이지 버튼 클릭을 한 곳에서 처리.
        // (조각이 교체돼도 #inqBoardArea 자체는 유지되므로 리스너 재등록 불필요)
        boardArea.addEventListener("click", function (e) {
            const filterBtn = e.target.closest(".filter-btn[data-status]");
            if (filterBtn) {
                loadBoard(filterBtn.dataset.status, 1, true);
                return;
            }
            const pgBtn = e.target.closest(".pg-btn[data-page]");
            if (pgBtn) {
                loadBoard(pgBtn.dataset.status || curStatus, Number(pgBtn.dataset.page), true);
                return;
            }
        });

        // 브라우저 뒤로/앞으로 가기 시에도 전체 새로고침 없이 조각만 다시 로드
        window.addEventListener("popstate", function (e) {
            const st = (e.state && e.state.inqStatus) || readParam("status", "all");
            const pg = (e.state && e.state.inqPage) || Number(readParam("page", "1"));
            loadBoard(st, pg, false);
        });
    }

    // 현재 화면(서버가 그려준 조각)에서 활성 필터/페이지를 읽어 상태 변수 초기화
    function syncStateFromDom() {
        const activeFilter = boardArea.querySelector(".filter-btn.active[data-status]");
        if (activeFilter) curStatus = activeFilter.dataset.status;
        const activePg = boardArea.querySelector(".pg-btn.active[data-page]");
        curPage = activePg ? Number(activePg.dataset.page) : 1;
    }

    function readParam(name, def) {
        return new URLSearchParams(location.search).get(name) || def;
    }

    /** list.do를 fetch해서 #inqBoardArea 내부만 교체.
     *  push=true면 주소창도 갱신(사용자 직접 클릭), false면 갱신 안 함(뒤로가기 복원). */
    function loadBoard(status, page, push) {
        const url = `/spendolive/inquiry/list.do?status=${encodeURIComponent(status)}&page=${page}`;

        fetch(url, { credentials: "same-origin" })
            .then(res => {
                if (!res.ok) throw new Error("list fetch failed: " + res.status);
                return res.text();
            })
            .then(html => {
                const doc = new DOMParser().parseFromString(html, "text/html");
                const newArea = doc.getElementById("inqBoardArea");
                if (!newArea) {
                    // 응답 구조가 예상과 다르면(에러 페이지 등) 일반 이동으로 폴백
                    window.location.href = url;
                    return;
                }
                boardArea.innerHTML = newArea.innerHTML;
                syncStateFromDom();

                if (push) {
                    history.pushState({ inqStatus: status, inqPage: page }, "", url);
                }
                window.scrollTo({ top: 0, behavior: "smooth" });
            })
            .catch(() => { window.location.href = url; });
    }

    /** 현재 필터/페이지 그대로 목록만 다시 로드 (삭제 직후 갱신용) */
    function reloadBoard() {
        loadBoard(curStatus, curPage, false);
    }

    /* ── 삭제: faq.js의 deleteInquiry를 AJAX 버전으로 덮어씀 ──
       (inquiry.js가 faq.js보다 뒤에 로드되므로 이 정의가 우선함) */
    window.deleteInquiry = function (inquiryId) {
        soConfirm("이 문의를 삭제하시겠어요? 삭제하면 되돌릴 수 없어요.").then(function (ok) {
            if (ok) doDeleteInquiry(inquiryId);
        });
    };

    function doDeleteInquiry(inquiryId) {
        const body = new URLSearchParams({ inquiryNo: inquiryId });
        fetch("/spendolive/inquiry/ajax/delete.do", {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body
        })
            .then(res => {
                if (handleAuth(res)) return null;
                return res.json();
            })
            .then(data => {
                if (!data) return;
                if (data.result !== "OK") {
                    soAlert(data.message || "삭제에 실패했습니다.", { type: "error" });
                    return;
                }
                // 상세 모달이 열려 있으면 닫고, 목록을 새로고침 없이 갱신
                if (typeof closeInqDetailModal === "function") closeInqDetailModal();
                reloadBoard();
            })
            .catch(() => soAlert("삭제 중 네트워크 오류가 발생했습니다.", { type: "error" }));
    }

    /* ════════════════════════════════════════════════════════
       [작성 화면]  #inquiryWriteForm 이 있을 때
       ════════════════════════════════════════════════════════ */
    const writeForm = document.getElementById("inquiryWriteForm");
    if (writeForm) {
        const btn = document.getElementById("inquirySubmitBtn");
        if (btn) btn.addEventListener("click", submitWrite);
    }

    function submitWrite() {
        // 개인정보 동의 + 필수값 검증 (작성 폼에만 privacyCheck 존재)
        const privacy = document.getElementById("privacyCheck");
        if (privacy && !privacy.checked) {
            soAlert("개인정보 수집 및 이용에 동의해 주세요.", { type: "error" });
            return;
        }
        const title = writeForm.querySelector('[name="title"]').value.trim();
        const content = writeForm.querySelector('[name="content"]').value.trim();
        const category = writeForm.querySelector('[name="category"]').value;
        const inquiryType = writeForm.querySelector('[name="inquiry_type"]').value;
        if (!category || !inquiryType) { soAlert("카테고리와 문의 유형을 선택해 주세요.", { type: "error" }); return; }
        if (!title || !content) { soAlert("제목과 상세 내용을 입력해 주세요.", { type: "error" }); return; }

        const submitBtn = document.getElementById("inquirySubmitBtn");
        submitBtn.disabled = true;
        submitBtn.textContent = "등록 중...";

        // FormData면 파일 첨부(multipart)가 자동 처리됨. Content-Type은 브라우저가 설정하게 둠.
        const formData = new FormData(writeForm);

        fetch("/spendolive/inquiry/ajax/write.do", {
            method: "POST",
            credentials: "same-origin",
            body: formData
        })
            .then(res => {
                if (handleAuth(res)) return null;
                return res.json();
            })
            .then(data => {
                if (!data) return;
                if (data.result !== "OK") {
                    soAlert(data.message || "문의 접수에 실패했습니다.", { type: "error" });
                    submitBtn.disabled = false;
                    submitBtn.textContent = "문의 제출하기";
                    return;
                }
                soAlert(data.message).then(function () {
                    window.location.href = "/spendolive/inquiry/list.do";
                });
            })
            .catch(() => {
                soAlert("문의 접수 중 네트워크 오류가 발생했습니다.", { type: "error" });
                submitBtn.disabled = false;
                submitBtn.textContent = "문의 제출하기";
            });
    }

    /* ════════════════════════════════════════════════════════
       [수정 화면]  #inquiryEditForm 이 있을 때
       ════════════════════════════════════════════════════════ */
    const editForm = document.getElementById("inquiryEditForm");
    if (editForm) {
        const btn = document.getElementById("inquiryEditSubmitBtn");
        if (btn) btn.addEventListener("click", submitEdit);
    }

    function submitEdit() {
        const title = editForm.querySelector('[name="title"]').value.trim();
        const content = editForm.querySelector('[name="content"]').value.trim();
        const category = editForm.querySelector('[name="category"]').value;
        const inquiryType = editForm.querySelector('[name="inquiry_type"]').value;
        if (!category || !inquiryType) { soAlert("카테고리와 문의 유형을 선택해 주세요.", { type: "error" }); return; }
        if (!title || !content) { soAlert("제목과 상세 내용을 입력해 주세요.", { type: "error" }); return; }

        const submitBtn = document.getElementById("inquiryEditSubmitBtn");
        submitBtn.disabled = true;
        submitBtn.textContent = "수정 중...";

        const params = new URLSearchParams(new FormData(editForm));

        fetch("/spendolive/inquiry/ajax/edit.do", {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params
        })
            .then(res => {
                if (handleAuth(res)) return null;
                return res.json();
            })
            .then(data => {
                if (!data) return;
                if (data.result !== "OK") {
                    soAlert(data.message || "수정에 실패했습니다.", { type: "error" });
                    submitBtn.disabled = false;
                    submitBtn.textContent = "수정 완료";
                    return;
                }
                soAlert(data.message).then(function () {
                    window.location.href = "/spendolive/inquiry/list.do";
                });
            })
            .catch(() => {
                soAlert("수정 중 네트워크 오류가 발생했습니다.", { type: "error" });
                submitBtn.disabled = false;
                submitBtn.textContent = "수정 완료";
            });
    }
})();