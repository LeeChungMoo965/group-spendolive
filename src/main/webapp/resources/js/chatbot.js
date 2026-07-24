(function () {
  const toggleBtn = document.getElementById('chatbotToggle');
  const panel = document.getElementById('chatbotPanel');
  const closeBtn = document.getElementById('chatbotClose');
  const body = document.getElementById('chatbotBody');
  const input = document.getElementById('chatbotInput');
  const sendBtn = document.getElementById('chatbotSend');

  function open() {
    panel.classList.add('show');
    input.focus();
  }
  function close() {
    panel.classList.remove('show');
  }

  function addMessage(text, sender, fallback) {
    const div = document.createElement('div');
    div.className = 'chatbot-msg ' + sender + (fallback ? ' fallback' : '');
    div.textContent = text;
    body.appendChild(div);
    body.scrollTop = body.scrollHeight;
    return div;
  }

  async function send() {
    const question = input.value.trim();
    if (!question) return;
    addMessage(question, 'user');
    input.value = '';

    const typing = addMessage('답변을 찾는 중...', 'bot typing');

    try {
      const res = await fetch(`${eContextPath}/chatbot/ask.do`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        skipGlobalLoading: true,  // 대화창 안에 자체 "답변을 찾는 중..." 버블이 이미 있어서 전역 스피너는 제외
        body: JSON.stringify({ question })
      });
      const data = await res.json();
      typing.remove();
      addMessage(data.answer, 'bot', !data.matched);
    } catch (e) {
      typing.remove();
      addMessage('일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요.', 'bot', true);
      console.error('[chatbot] 요청 실패', e);
    }
  }

  toggleBtn.addEventListener('click', open);
  closeBtn.addEventListener('click', close);
  sendBtn.addEventListener('click', send);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') send();
  });
})();