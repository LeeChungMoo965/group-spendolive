let calendar;
let monthlyExpenses = [];
let sidePanelPage = 1;
const SIDE_PAGE_SIZE = 3;

document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendar')

    calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'ko',
      height: 'auto',
      
      headerToolbar: false,
      fixedWeekCount: 1,
      dayMaxEvents: 2, // 날짜 칸당 최대 2개까지만 표시, 넘으면 "+N개" 링크로 숨김 (칸 높이 고정)
      datesSet: function(info) {
        const year = info.view.currentStart.getFullYear();
        const month = info.view.currentStart.getMonth() + 1;
        document.getElementById('calendarTitle').textContent = `${year}년 ${month}월`;

        // 월/뷰가 바뀔 때마다(이전달, 다음달, 초기로드 전부 포함) 그 달 지출 다시 불러옴
        sidePanelPage = 1;
        loadMonthlyExpenses(year, month);
      },
      dateClick: function(info) {

        // 클릭한 지점이 날짜 숫자(예: "7일")가 아니면 그냥 무시
        const isDayNumberClick = info.jsEvent.target.closest('.fc-daygrid-day-number');
        if (!isDayNumberClick) {
            return;
        }
        location.href = `${eContextPath}/expense.do?date=${info.dateStr}#expense-list`;
        },
      
      eventContent: function(arg) {
        const amount = arg.event.extendedProps.amount;
        const category_name = arg.event.extendedProps.category_name;
        const expense_type = arg.event.extendedProps.expense_type; // FIXED / VARIABLE / OTT

        const wrapper = document.createElement('div');
        wrapper.className = `calendar-expense-chip chip-type-${(expense_type || 'variable').toLowerCase()}`;
        wrapper.innerHTML = `
          <span class="chip-amount">${Number(amount).toLocaleString()}원</span>
          <span class="chip-category">${category_name}</span>
        `;
        return { domNodes: [wrapper] };
      }
    });
    calendar.render()


    loadTodayTodo();  
  

  function changeMonth(direction) {
    if (direction === -1) calendar.prev();
    if (direction === 1) calendar.next();
  }
  window.changeMonth = changeMonth; // onclick="changeMonth(-1)" 에서 접근 가능하게

  const detailBtn = document.getElementById('detailBtn');
  if (detailBtn) {
    detailBtn.addEventListener('click', function() {
        if (calendar.view.type === 'dayGridMonth') {
        calendar.changeView('listMonth');
        this.textContent = '달력으로 보기';
        } else {
        calendar.changeView('dayGridMonth');
        this.textContent = '자세히보기';
        }
    });
} else {
    console.warn('detailBtn 요소를 못 찾았어요');
}
});

/* =========================================================
   지출 데이터 로드 & 렌더링
   ========================================================= */

function loadMonthlyExpenses(year, month) {
    fetch(`${eContextPath}/calendar/expenses.do?year=${year}&month=${month}`, {
        credentials: 'same-origin'
    })
        .then(res => {
            if (!res.ok) {
                throw new Error('지출 목록을 불러오지 못했습니다.');
            }
            return res.json();
        })
        .then(data => {
            monthlyExpenses = data;
            renderCalendarEvents();
            renderSidePanel();
        })
        .catch(err => {
            console.error(err);
        });
}

function renderCalendarEvents() {
    const events = monthlyExpenses.map(exp => ({
        id: String(exp.expense_id),
        title: exp.expense_title,
        date: exp.expense_date,
        extendedProps: {
            amount: exp.amount,
            category_name: exp.category_name,
            expense_type: exp.expense_type
        }
    }));

    calendar.removeAllEvents();
    calendar.addEventSource(events);
}

function renderSidePanel() {
    // 날짜 최신순 정렬
    const sorted = [...monthlyExpenses].sort((a, b) =>
        a.expense_date < b.expense_date ? 1 : -1
    );

    const totalPages = Math.max(1, Math.ceil(sorted.length / SIDE_PAGE_SIZE));
    if (sidePanelPage > totalPages) {
        sidePanelPage = totalPages;
    }

    const startIdx = (sidePanelPage - 1) * SIDE_PAGE_SIZE;
    const pageItems = sorted.slice(startIdx, startIdx + SIDE_PAGE_SIZE);

    const listEl = document.getElementById('sideEventList');
    if (!listEl) {
        console.warn('sideEventList 요소를 못 찾았어요 (calendar.jsp 확인 필요)');
        return;
    }

    listEl.innerHTML = '';

    if (pageItems.length === 0) {
        listEl.innerHTML = '<p class="empty-text">이번 달 지출 내역이 없습니다.</p>';
    } else {
        pageItems.forEach(exp => {
            const dateLabel = exp.expense_date.slice(5).replace('-', '.'); // "2026-07-05" -> "07.05"
            const typeClass = `type-${(exp.expense_type || 'variable').toLowerCase()}`;
            const item = document.createElement('div');
            item.className = `side-event ${typeClass}`;
            item.innerHTML = `
                <strong>${dateLabel} ${exp.expense_title}</strong>
                <span>${Number(exp.amount).toLocaleString()}원 · ${exp.category_name}</span>
            `;
            listEl.appendChild(item);
        });
    }

    renderSidePanelPager(totalPages);
}

function renderSidePanelPager(totalPages) {
    let pagerEl = document.getElementById('sidePanelPager');

    if (!pagerEl) {
        pagerEl = document.createElement('div');
        pagerEl.id = 'sidePanelPager';
        pagerEl.className = 'side-pager';
        document.getElementById('sideEventList').insertAdjacentElement('afterend', pagerEl);
    }

    if (totalPages <= 1) {
        pagerEl.innerHTML = '';
        return;
    }

    // 1, 2, 3, 4, 5 ... 숫자 버튼 생성
    let buttonsHtml = '';
    for (let page = 1; page <= totalPages; page++) {
        const isActive = page === sidePanelPage ? 'active' : '';
        buttonsHtml += `<button type="button" class="pager-num-btn ${isActive}" data-page="${page}">${page}</button>`;
    }
    pagerEl.innerHTML = buttonsHtml;

    pagerEl.querySelectorAll('.pager-num-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            sidePanelPage = Number(btn.dataset.page);
            renderSidePanel();
        });
    });
}
/* =========================================================
   오늘 할 일 - "오늘 날짜에 잡혀있는 지출"만 따로 보여줌
   (달력을 이전달/다음달로 넘겨도 이건 안 바뀜)
   ========================================================= */

   function loadTodayTodo() {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth() + 1;
    const todayStr = `${year}-${String(month).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

    fetch(`${eContextPath}/calendar/expenses.do?year=${year}&month=${month}`, {
        credentials: 'same-origin'
    })
        .then(res => {
            if (!res.ok) {
                throw new Error('오늘 할 일을 불러오지 못했습니다.');
            }
            return res.json();
        })
        .then(data => {
            const todayItems = data.filter(exp => exp.expense_date === todayStr);
            renderTodayTodo(todayItems);
        })
        .catch(err => {
            console.error(err);
        });
}

function renderTodayTodo(todayItems) {
    const listEl = document.getElementById('todayTodoList');
    if (!listEl) {
        console.warn('todayTodoList 요소를 못 찾았어요 (calendar.jsp 확인 필요)');
        return;
    }

    listEl.innerHTML = '';

    if (todayItems.length === 0) {
        listEl.innerHTML = '<p class="empty-text">오늘 예정된 지출이 없습니다.</p>';
        return;
    }

    todayItems.forEach(exp => {
        const typeClass = `type-${(exp.expense_type || 'variable').toLowerCase()}`;

        const item = document.createElement('div');
        item.className = `side-event ${typeClass}`;
        item.innerHTML = `
            <strong>${exp.expense_title}</strong>
            <span>${Number(exp.amount).toLocaleString()}원 · ${exp.category_name}</span>
        `;
        listEl.appendChild(item);
    });
}
