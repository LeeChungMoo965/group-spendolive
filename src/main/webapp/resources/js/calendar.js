let calendar;


document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendar')

    calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'ko',
      height: 'auto',
      headerToolbar: false,
      datesSet: function(info) {
        const year = info.view.currentStart.getFullYear();
        const month = info.view.currentStart.getMonth() + 1;
        document.getElementById('calendarTitle').textContent = `${year}년 ${month}월`;
      },
      dateClick: function(info) {
        location.href = `${eContextPath}/expense.do?date=${info.dateStr}#expense-form`;
      }
    });
    calendar.render()
  

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