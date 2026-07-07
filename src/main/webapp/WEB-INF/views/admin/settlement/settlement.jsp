    <%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
    <c:set var="contextPath" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${contextPath}/resources/css/admin.css">


    <body data-page="report">
    <div class="admin-shell">

   
    <main class="admin-main">
    
    <section class="hero"><div><div class="hero-kicker">Report Management</div><h1>정산관리</h1>
    <p>금일 이용료 보관액 방장 정산</p></div></section>
    <section class="panel"><div class="panel-header"><div class="panel-title"><div class="section-kicker">Settlement List</div>
    <h2>금일 정산 리스트(총 ${settlementList.size()}건)</h2></div>
    <div class="filter-pills"><button class="active">전체</button><button>대기</button><button>처리중</button><button>완료</button></div>
    </div><div class="table-wrap">
      
    <c:choose>
                <c:when test="${empty settlementList}">
                <div class="panel-title" style="padding: 20px; text-align: center;">
                    <div class="section-kicker">정산 건이 없습니다</div>
                </div>
            </c:when>
                <c:otherwise>
                <table class="admin-table">
                <thead><tr><th>번호</th><th>방 ID</th><th>방장 ID</th><th>정산 금액</th><th>결제일</th><th>상태</th><th>처리</th></tr></thead><tbody>
                        <c:forEach var="room" items="${settlementList}" varStatus="s">
                      
    <tr><td>${s.count}</td><td>${room.roomId}</td><td>${room.hostMemberId}</td><td>${room.totalPrice}</td><td>${room.billingDay}</td><td><span class="badge yellow">${room.status}</span></td>
    <td><button class="mini-btn">상세</button><button class="mini-btn warning">정산금 보내기</button></td></tr>
                        </c:forEach>
                        </tbody></table>
                </c:otherwise>
        </c:choose>
        </tbody></table></div></section>
    <section class="panel"><div class="panel-title"><div class="section-kicker">Process Result</div>
    <h2>신고 처리결과 작성</h2>
    <form action="${contextPath}/admin/report/comment.do"  method="post">
    </div><div class="form-grid" style="margin-top:18px"><div class="form-field"><label>처리 상태</label><select class="form-input">
    <option>대기</option><option>완료</option><option>반려</option></select></div>
    <div class="form-field"><label>패널티</label><select class="form-input"><option>없음</option><option>경고 1회</option></select></div>
    <div class="form-field"><label>관리자</label><input class="form-input" value="admin"></div></div>
    <textarea name="admin_comment" class="form-textarea" placeholder="처리 결과를 입력하세요.">신고 내용을 확인했고 대상 회원에게 경고 1회를 적용했습니다.</textarea><div class="toolbar" style="margin-top:16px"><span></span>
    <button class="btn primary" type="submit" data-toast="신고 처리결과 저장 미리보기입니다.">처리결과 저장</button></div></section>
    </form>
    </main>

    <footer class="footer">
        <div class="footer-inner">
            <div>SpendOlive Admin UI Preview</div>
            <div>DB 연결 없이 화면 확인용으로 제작된 독립 HTML 프로젝트입니다.</div>
        </div>
    </footer>
    <div class="toast" aria-live="polite"></div>


    </div>
    </body>
    </html>
    <script>
        // 컨트롤러가 보낸 일회성 메시지(msg)가 있다면 alert을 띄운다
        var msg = "${msg}";
        if(msg && msg !== "") {
            alert(msg);
        }
    </script>
    <script src="${contextPath}/resources/js/admin.js"></script>
