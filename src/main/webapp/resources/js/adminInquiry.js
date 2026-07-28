/* ============================================================
   adminInquiry.js  (관리자 문의 - 답변 AJAX)
   - 관리자 문의 목록의 필터/페이지/탭 전환과 상세 모달은 이미
     admin.js가 처리하고 있음. 이 파일은 "답변 등록/수정"만 AJAX로 바꾼다.
   - 답변 폼은 상세 모달 안에 있고(숨김 템플릿을 복사해 씀), 목록이 AJAX로
     교체될 때마다 새로 만들어지므로 document에 위임(delegation)해서 가로챈다.
   - 성공하면 모달을 닫고 목록 조각(#adminBoardArea)만 다시 불러와 배지를 갱신.
   - 공용 파일(admin.js 등)은 건드리지 않고, 알림 모달도 이 파일 안에 자체 포함.
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

    /* ── 답변 폼 제출 가로채기 (모달 안 폼이 동적 생성되므로 document 위임) ── */
    document.addEventListener("submit", function (e) {
        var form = e.target.closest("form.admin-reply-form");
        if (!form) return;
        e.preventDefault();
        submitReply(form);
    });

    function submitReply(form) {
        var contentEl = form.querySelector('[name="reply_content"]');
        var content = contentEl ? contentEl.value.trim() : "";
        if (!content) {
            soAlert("답변 내용을 입력해 주세요.", { type: "error" });
            return;
        }

        var submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) { submitBtn.disabled = true; }

        var params = new URLSearchParams(new FormData(form));

        fetch("/admin/inquiry/ajax/reply.do", {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params
        })
            .then(function (res) {
                if (res.status === 401) {
                    soAlert("관리자만 접근할 수 있습니다.", { type: "error" });
                    return null;
                }
                return res.json();
            })
            .then(function (data) {
                if (!data) return;
                if (data.result !== "OK") {
                    soAlert(data.message || "답변 등록에 실패했습니다.", { type: "error" });
                    if (submitBtn) submitBtn.disabled = false;
                    return;
                }
                // 상세 모달 닫기 (admin.js의 전역 함수 재사용)
                if (typeof closeAdminInquiryModal === "function") closeAdminInquiryModal();
                // 확인을 누르면 목록 조각만 다시 불러와 상태 배지 갱신
                soAlert(data.message).then(function () { reloadBoard(); });
            })
            .catch(function () {
                soAlert("답변 등록 중 네트워크 오류가 발생했습니다.", { type: "error" });
                if (submitBtn) submitBtn.disabled = false;
            });
    }

    /* 현재 목록 URL 그대로 다시 fetch → #adminBoardArea 내부만 교체.
       (admin.js의 loadBoard는 IIFE 내부라 못 부르므로 여기서 동일 동작 수행) */
    function reloadBoard() {
        var url = location.pathname + location.search; // /admin/inquiry/list.do?status=..&page=..
        fetch(url, { credentials: "same-origin" })
            .then(function (res) { return res.text(); })
            .then(function (html) {
                var doc = new DOMParser().parseFromString(html, "text/html");
                var na = doc.getElementById("adminBoardArea");
                var ca = document.getElementById("adminBoardArea");
                if (na && ca) ca.innerHTML = na.innerHTML;
                else window.location.reload();
            })
            .catch(function () { window.location.reload(); });
    }
})();