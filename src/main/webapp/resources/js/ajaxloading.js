/* =========================================================
   담당 기능(공지/알림센터/캘린더/챗봇) 전용 AJAX 로딩 표시
   ========================================================= */

   let __loadingOverlayEl = null;
   let __loadingRequestCount = 0;
   
   function ensureLoadingOverlay() {
       if (__loadingOverlayEl) return __loadingOverlayEl;
       const el = document.createElement('div');
       el.id = 'globalLoadingOverlay';
       el.className = 'global-loading-overlay';
       el.innerHTML = '<div class="global-loading-spinner"></div>';
       document.body.appendChild(el);
       __loadingOverlayEl = el;
       return el;
   }
   
   function showLoading() {
       __loadingRequestCount++;
       ensureLoadingOverlay().classList.add('show');
   }
   
   function hideLoading() {
       __loadingRequestCount = Math.max(0, __loadingRequestCount - 1);
       if (__loadingRequestCount === 0 && __loadingOverlayEl) {
           __loadingOverlayEl.classList.remove('show');
       }
   }
   
   /* notice.js / bellIcon.js / calendar.js에서 fetch(...) 대신 이걸 씀.
      사용법은 fetch와 동일 - fetchWithLoading(url, options) */
   function fetchWithLoading(input, init) {
       showLoading();
       return fetch(input, init).finally(() => {
           hideLoading();
       });
   }