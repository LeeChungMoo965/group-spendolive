/* SpendOlive Complete Fixed JS */
let currentMonth = 6;

function openModal(id){const el=document.getElementById(id);if(el)el.classList.add("show")}
function closeModal(id){const el=document.getElementById(id);if(el)el.classList.remove("show")}

function addExpense(){
  const amount=Number(document.getElementById("expenseAmount")?.value||0);
  const tbody=document.getElementById("expenseRows");
  if(!tbody){alert("지출관리 페이지에서 사용할 수 있는 기능입니다.");return}
  const tr=document.createElement("tr");
  tr.innerHTML=`<td>오늘</td><td>새 지출</td><td>변동지출</td><td>식비</td><td>${amount.toLocaleString("ko-KR")}원</td>`;
  tbody.prepend(tr);
  alert("지출 내역이 등록되었습니다.");
}

function changeMonth(diff){
  currentMonth+=diff;
  if(currentMonth<1)currentMonth=12;
  if(currentMonth>12)currentMonth=1;
  const title=document.getElementById("calendarTitle");
  if(title) title.textContent=`2026년 ${currentMonth}월`;
}

function kakaoLogin(){alert("카카오톡 로그인 연동 예정입니다.")}
function authLogin(){alert("로그인 처리 예정입니다.")}
function authSignup(){alert("회원가입 처리 예정입니다.")}

function setAuthMessage(id,message,type){
  const el=document.getElementById(id);
  if(!el)return;
  el.textContent=message;
  el.className="auth-result-text "+type;
}
var isIdVerified = false; 

function checkId(){
  const id=document.getElementById("userId")?.value.trim()||"";
  if(!id){setAuthMessage("idResult","아이디를 입력해주세요.","warn");return}
  if(id.length<4){setAuthMessage("idResult","아이디는 4자 이상 입력해주세요.","warn").css('color', '#FF3B30');return}
  $.ajax({
    url: eContextPath + "/member/checkId", // 컨트롤러 매핑 주소
    type: 'POST',
    data: { id: id },
    success: function(isSuccess) {
      if (isSuccess) {
          $('#idResult').text('✓ 사용 가능한 아이디 입니다.').css('color', '#4CAF50');
          isIdVerified = true; 
      } else {
          $('#idResult').text('✗ 존재하는 아이디 입니다.').css('color', '#FF3B30');
          isIdVerified = false; 
      }
  },
  error: function() {
      alert('중복확인 중 오류가 발생했습니다.');
  }
});
 
 
  
}






document.addEventListener("DOMContentLoaded",()=>{
  document.querySelectorAll(".modal").forEach((modal)=>{
    modal.addEventListener("click",(event)=>{
      if(event.target===modal) modal.classList.remove("show");
    });
  });
});
var isEmailVerified = false; 

// 기존 스크립트와 주소 충돌을 피하기 위해 .do를 뺀 주소 추출 변수
const eContextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2)) === "/member" ? "" : window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));

// 1. 실제 구글 SMTP로 메일을 쏘는
function sendEmail() {
    var email = $("#email").val();
    if(!email || email.trim() === "") {
        alert("이메일을 입력해 주세요.");
        return;
    }

    alert("인증번호를 발송 중입니다. 잠시만 기다려 주세요...");
    $.ajax({
      url: eContextPath + "/member/checkEmail", // 컨트롤러 매핑 주소
      type: 'POST',
      data: { email: email },
      success: function(isSuccess) {
        if (isSuccess) {
          $.ajax({
              type: "POST",
              url: eContextPath + "/member/sendEmail",
              data: { "email": email },
              success: function(isSuccess) {
                  if(isSuccess) {
                      alert("입력하신 이메일로 인증번호가 전송되었습니다.");
                      $("#emailAuthArea").show(); 
    
                      if(typeof setAuthMessage === 'function') {
                          setAuthMessage("emailResult","이메일 인증번호가 발송되었습니다.","ok");
                      }
                  } else {
                      alert("메일 발송에 실패했습니다. 이메일 주소를 확인해 주세요.");
                  }
              },
              error: function() {
                  alert("서버 통신 오류가 발생했습니다.");
              }
          });
        } else {
          alert('✗ 존재하는 이메일 입니다.');
        }
    },
    error: function() {
              // 중복확인 메서들 따로 만들기는 하였으나 어차피 데이터 제약조건이 유니크라서 SQL에서 중복 차단
        alert('중복확인 중 오류가 발생했습니다.');
    }
       
  });
  }

// 2. 사용자가 입력한 6자리 인증번호를 확인
function verifyEmail() {
  const inputCode = $('#emailAuthCode').val();
  if (!inputCode) {
      alert('인증번호를 입력해 주세요.');
      return;
  }

  $.ajax({
      url: eContextPath + "/member/verifyEmail", // 컨트롤러 매핑 주소
      type: 'POST',
      data: { inputCode: inputCode },
      success: function(isSuccess) {
        if (isSuccess) {
            $('#emailAuthResult').text('✓ 이메일 인증이 완료되었습니다.').css('color', '#4CAF50');
            $('#email').attr('readonly', true);
            $('#emailAuthCode').attr('readonly', true);
            isEmailVerified = true; // 인증 성공 플래그 true
        } else {
            $('#emailAuthResult').text('✗ 인증번호가 일치하지 않습니다. 다시 확인해 주세요.').css('color', '#FF3B30');
            isEmailVerified = false;
        }
    },
    error: function() {

        alert('오류가 발생했습니다.');
    }
  });
}

// 글로벌 변수로 휴대폰 인증 여부 체크용 플래그 선언
let isPhoneVerified = false;

// 1. 전화번호로 인증번호 발송 요청 (가상 시뮬레이터 가동)
function sendSms() {
  const phone = $('#phone').val();
  if (!phone) {
      alert('전화번호를 입력해 주세요.');
      return;
  }
  $.ajax({
      url: eContextPath + "/member/checkPhone", // 컨트롤러 매핑 주소
      type: 'POST',
      data: { phone: phone },
      success: function(isSuccess) {
        if (isSuccess) {
          $.ajax({
              url: eContextPath + "/member/sendSms", // 컨트롤러 매핑 주소
              type: 'POST',
              data: { phone: phone },
              success: function(response) {
                  alert('인증번호가 가상 발송되었습니다. 서버 콘솔창을 확인하세요!');
                  $('#phoneAuthArea').show(); // 숨겨진 인증박스 오픈
                  $('#phoneAuthResult').text('콘솔창에 찍힌 6자리 숫자를 입력하세요.').css('color', '#666');
              },
              error: function() {
                  alert('문자 발송 요청 중 오류가 발생했습니다 핸드폰 번호를 확인해 주세요! ');
              }
          });
        } else {
          alert('✗ 존재하는 핸드폰 입니다.');
        }
    },
    error: function() {
        alert('중복확인 중 오류가 발생했습니다.');
    }
  });
  
}

// 2. 사용자가 입력한 가상 인증번호 검증
function verifySms() {
  const inputCode = $('#phoneAuthCode').val();
  if (!inputCode) {
      alert('인증번호를 입력해 주세요.');
      return;
  }

  $.ajax({
      url: eContextPath + "/member/verifySms", // 컨트롤러 매핑 주소
      type: 'POST',
      data: { inputCode: inputCode },
      success: function(isSuccess) {
          if (isSuccess) {
              $('#phoneAuthResult').text('✓ 휴대폰 인증이 완료되었습니다.').css('color', '#4CAF50');
              $('#phone').attr('readonly', true);
              $('#phoneAuthCode').attr('readonly', true);
              isPhoneVerified = true; // 인증 성공 플래그 true
          } else {
              $('#phoneAuthResult').text('✗ 인증번호가 일치하지 않습니다. 다시 확인해 주세요.').css('color', '#FF3B30');
              isPhoneVerified = false;
          }
      },
      error: function() {
          alert('인증 확인 중 오류가 발생했습니다.');
      }
  });
}
// 3. 회원가입 버튼 누를 때 최종 방어선 함수
function joinCheck() {
if(!isEmailVerified) {
    alert("이메일 인증을 완료해야 회원가입이 가능합니다.");
    return false;    
}
if (!isPhoneVerified) {
  alert('전화번호 인증을 완료해 주세요.');
  return false;
}
if (!isIdVerified) {
  alert('아이디 중복확인을 완료해 주세요.');
  return false;
}
if($("#password").val() !== $("#passwordCheck").val()) {
    alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    return false;
}
return true;
}
function joinCheckKakao() {
  if(!isEmailVerified) {
      alert("이메일 인증을 완료해야 회원가입이 가능합니다.");
      return false;    
  }
  if (!isPhoneVerified) {
    alert('전화번호 인증을 완료해 주세요.');
    return false;
  }
  return true;
}
// 3. 최종 회원가입 서브밋 전 벨리데이션 체크 (joinCheck 함수가 있다면 추가)

/* =========================================================
 OTT 고정 최고 멤버십 자동 계산
 ========================================================= */
(function () {
function toNumber(value) {
  const parsed = Number(value || 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatWon(value) {
  return toNumber(value).toLocaleString('ko-KR') + '원';
}

function updateFixedPlanForm(form) {
  const select = form.querySelector('.ott-service-select');
  const option = select && select.selectedOptions ? select.selectedOptions[0] : null;
  const preview = form.querySelector('.ott-fixed-plan-preview');

  if (!option || !option.value) {
    form.querySelector('.ott-plan-input')?.setAttribute('value', '');
    form.querySelector('.ott-total-price-input')?.setAttribute('value', '');
    form.querySelector('.ott-member-limit-input')?.setAttribute('value', '');
    if (preview) {
      preview.innerHTML = '<strong>OTT를 선택하면 최고 멤버십 기준 금액이 자동 적용됩니다.</strong><p>구독종류, 전체 구독료, 최대 인원은 직접 입력하지 않고 서비스 규칙으로 고정됩니다.</p>';
    }
    return;
  }

  const serviceName = option.dataset.serviceName || option.textContent.trim();
  const plan = option.dataset.plan || '프리미엄';
  const basePrice = toNumber(option.dataset.basePrice);
  const extraFee = toNumber(option.dataset.extraFee);
  const extraCount = toNumber(option.dataset.extraCount);
  const totalPrice = toNumber(option.dataset.totalPrice);
  const memberLimit = toNumber(option.dataset.memberLimit);
  const shareAmount = toNumber(option.dataset.shareAmount);
  const feeAmount = toNumber(option.dataset.feeAmount);
  const personAmount = toNumber(option.dataset.personAmount);

  const planInput = form.querySelector('.ott-plan-input');
  const totalInput = form.querySelector('.ott-total-price-input');
  const memberInput = form.querySelector('.ott-member-limit-input');

  if (planInput) planInput.value = plan;
  if (totalInput) totalInput.value = totalPrice;
  if (memberInput) memberInput.value = memberLimit;

  const extraText = extraFee > 0 && extraCount > 0
    ? `추가 계정 ${extraCount}명 × ${formatWon(extraFee)} 포함`
    : '추가 계정 비용 없음';

  if (preview) {
    preview.innerHTML = `
      <strong>${serviceName} · ${plan}</strong>
      <div class="ott-plan-preview-grid">
        <span><b>기본 구독료</b>${formatWon(basePrice)}</span>
        <span><b>추가 비용</b>${extraText}</span>
        <span><b>N분의 1 기준 금액</b>${formatWon(totalPrice)} / ${memberLimit}명</span>
        <span><b>1인 결제금액</b>${formatWon(personAmount)} <small>분담금 ${formatWon(shareAmount)} + 수수료 ${formatWon(feeAmount)}(3%)</small></span>
      </div>
    `;
  }
}

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('.ott-fixed-plan-form').forEach(function (form) {
    const select = form.querySelector('.ott-service-select');
    updateFixedPlanForm(form);
    if (select) {
      select.addEventListener('change', function () {
        updateFixedPlanForm(form);
      });
    }
  });
});
})();
// 알림 배지는 bellIcon.js 에서 처리