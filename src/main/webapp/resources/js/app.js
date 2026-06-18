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

function checkId(){
  const id=document.getElementById("userId")?.value.trim()||"";
  if(!id){setAuthMessage("idResult","아이디를 입력해주세요.","warn");return}
  if(id.length<4){setAuthMessage("idResult","아이디는 4자 이상 입력해주세요.","warn");return}
  setAuthMessage("idResult","사용할 수 있는 아이디입니다.","ok");
}

function sendEmailCode(){
  const email=document.getElementById("email")?.value.trim()||"";
  if(!email.includes("@")||!email.includes(".")){setAuthMessage("emailResult","올바른 이메일 형식으로 입력해주세요.","warn");return}
  setAuthMessage("emailResult","이메일 인증번호가 발송되었습니다.","ok");
}

function verifyEmailCode(){
  const code=document.getElementById("emailCode")?.value.trim()||"";
  if(!code){setAuthMessage("emailResult","이메일 인증번호를 입력해주세요.","warn");return}
  setAuthMessage("emailResult","이메일 인증이 완료되었습니다.","ok");
}

function sendPhoneCode(){
  const phone=document.getElementById("phone")?.value.trim()||"";
  if(!phone){setAuthMessage("phoneResult","전화번호를 입력해주세요.","warn");return}
  setAuthMessage("phoneResult","전화번호 인증번호가 발송되었습니다.","ok");
}

function verifyPhoneCode(){
  const code=document.getElementById("phoneCode")?.value.trim()||"";
  if(!code){setAuthMessage("phoneResult","전화번호 인증번호를 입력해주세요.","warn");return}
  setAuthMessage("phoneResult","전화번호 인증이 완료되었습니다.","ok");
}

document.addEventListener("DOMContentLoaded",()=>{
  document.querySelectorAll(".modal").forEach((modal)=>{
    modal.addEventListener("click",(event)=>{
      if(event.target===modal) modal.classList.remove("show");
    });
  });
});
